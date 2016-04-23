package org.pinterest.analyzer.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.pinterest.analyzer.PinterestProperties;
import org.pinterest.analyzer.ProxyProperties;
import org.pinterest.analyzer.controller.AnalysisRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jayway.awaitility.Awaitility.await;

@Service
public class CrawlerService {

    private final PinterestProperties pinterestProperties;
    private final ProxyProperties proxyProperties;

    @Autowired
    public CrawlerService(PinterestProperties pinterestProperties, ProxyProperties proxyProperties) {
        this.pinterestProperties = pinterestProperties;
        this.proxyProperties = proxyProperties;
    }

    public Map<Pin, Integer> crawl(AnalysisRequest request) {
        WebDriver driver = getDriver();

        try {
            return crawlPins(request, driver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            driver.close();
        }
    }

    private WebDriver getDriver() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        if (proxyProperties.getSocksProxy() != null && !proxyProperties.getSocksProxy().equals("")) {
            Proxy proxy = new Proxy();
            proxy.setSocksProxy(proxyProperties.getSocksProxy());
            if (proxyProperties.getProxyUsername() != null && !proxyProperties.getProxyUsername().equals("")) {
                proxy.setSocksUsername(proxyProperties.getProxyUsername());
            }
            if (proxyProperties.getProxyPassword() != null && !proxyProperties.getProxyPassword().equals("")) {
                proxy.setSocksPassword(proxyProperties.getProxyPassword());
            }
            capabilities.setCapability(CapabilityType.PROXY, proxy);
        }
        return new FirefoxDriver(capabilities);
    }

    private Map<Pin, Integer> crawlPins(AnalysisRequest request, WebDriver driver) throws IOException {
        login(driver);

        driver.get(request.getUrl());

        // wait until page is loaded fully
        while (!driver.findElement(By.className("gridFooterSpinner")).getCssValue("display").equals("none")) {

        }
        Set<PinWrapper> pinWrappers = scrollAndCrawl(driver, request.getCount());

        return countByPin(pinWrappers, request.getFilter());
    }

    private void login(WebDriver driver) {
        driver.get(pinterestProperties.getLoginUrl());
        WebElement emailInput = driver.findElement(By.className("email"));
        emailInput.sendKeys(pinterestProperties.getEmail());
        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(pinterestProperties.getPassword());

        driver.findElement(By.className("primary")).click();

        while (driver.getCurrentUrl().equals("https://www.pinterest.com/login")) {

        }
    }

    private Set<PinWrapper> scrollAndCrawl(WebDriver driver, Integer count) {
        Set<PinWrapper> pins = parsePins(driver);

        int latestHeight = driver.findElement(By.className("GridItems")).getSize().getHeight();
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        while (pins.size() < count) {
            long startedAt = System.currentTimeMillis();
            while (!driver.findElement(By.className("gridFooterSpinner")).getCssValue("display").equals("inline-block")
                    && latestHeight == driver.findElement(By.className("GridItems")).getSize().getHeight()) {
                if (System.currentTimeMillis() > startedAt + 20 * 1000) {
                    // reahed the end
                    return pins;
                }
                jse.executeScript("window.scrollBy(0, 250)", "");
            }
            waitUntilLoadingDone(driver);
            latestHeight = driver.findElement(By.className("GridItems")).getSize().getHeight();
            pins.addAll(parsePins(driver));
        }

        return pins;
    }

    private Set<PinWrapper> parsePins(WebDriver driver) {
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);

        Set<PinWrapper> pins = doc.getElementsByClass("item").stream().map(this::toPinWrapper).filter(p -> p != null).collect(Collectors.toSet());

        return pins;
    }

    private void waitUntilLoadingDone(WebDriver driver) {
        await().atMost(20, TimeUnit.SECONDS)
                .until(() -> {
                            String displayClass = driver.findElement(By.className("gridFooterSpinner")).getCssValue("display");
                            return displayClass.equals("block") || displayClass.equals("none");
                        }
                );
    }

    private PinWrapper toPinWrapper(Element itemElement) {
        try {
            String top = itemElement.attr("style").split(";")[0].replace("top: ", "");
            String left = itemElement.attr("style").split(";")[1].replace(" left: ", "");
            String pinUrl = "https://www.pinterest.com" + itemElement.getElementsByClass("pinImageWrapper").get(0).attr("href");
            String imgUrl = itemElement.getElementsByClass("pinImg").get(0).attr("src");
            String description = itemElement.getElementsByClass("pinDescription").get(0).text();
            PinWrapper pinWrapper = new PinWrapper(imgUrl, pinUrl, description, top, left);
            return pinWrapper;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<Pin, Integer> countByPin(Set<PinWrapper> pinWrappers, String filter) {
        Map<Pin, Integer> map = new HashMap<>();

        if (filter != null && !filter.trim().equals("")) {
            pinWrappers = pinWrappers.stream().filter(p -> p.getDescription().contains(filter.trim())).collect(Collectors.toSet());
        }

        pinWrappers.forEach(wrapper -> {
            Pin pin = new Pin(wrapper.getImgUrl(), wrapper.getPinUrl(), wrapper.getDescription());
            Integer count = map.get(pin);
            if (count == null) {
                map.put(pin, 1);
            } else {
                map.put(pin, count + 1);
            }
        });

        return map;
    }

}
