package org.pinterest.analyzer.service;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.pinterest.analyzer.PinterestProperties;
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

    private final PinterestProperties properties;

    @Autowired
    public CrawlerService(PinterestProperties properties) {
        this.properties = properties;
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
        HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_38) {
            @Override
            protected WebClient getWebClient() {
                WebClient webClient = super.getWebClient();
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getOptions().setJavaScriptEnabled(true);
                return webClient;
            }
        };
        driver.setJavascriptEnabled(true);

        return new FirefoxDriver();
    }

    private Map<Pin, Integer> crawlPins(AnalysisRequest request, WebDriver driver) throws IOException {
        login(driver);

        driver.get(request.getUrl());

        // wait until page is loaded fully
        while (!driver.findElement(By.className("gridFooterSpinner")).getCssValue("display").equals("none")) {

        }
        Set<PinWrapper> pinWrappers = scrollAndCrawl(driver, request.getCount());

        return countByPin(pinWrappers);
    }

    private void login(WebDriver driver) {
        driver.get(properties.getLoginUrl());
        WebElement emailInput = driver.findElement(By.className("email"));
        emailInput.sendKeys(properties.getEmail());
        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(properties.getPassword());

        driver.findElement(By.className("primary")).click();

        while (driver.getCurrentUrl().equals("https://www.pinterest.com/login")) {

        }
    }

    private Set<PinWrapper> scrollAndCrawl(WebDriver driver, Integer count) {
        Set<PinWrapper> pins = parsePins(driver);

        int latestHeight = driver.findElement(By.className("GridItems")).getSize().getHeight();
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        while(pins.size() < count) {
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

    private Map<Pin, Integer> countByPin(Set<PinWrapper> pinWrappers) {
        Map<Pin, Integer> map = new HashMap<>();
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
