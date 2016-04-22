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
import java.util.List;
import java.util.Map;
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
        scrollEnough(driver, request.getCount());

        List<Pin> pins = parsePins(driver);

        return countByPin(pins);
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

    private void scrollEnough(WebDriver driver, Integer count) throws IOException {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        while (!driver.findElement(By.className("gridFooterSpinner")).getCssValue("display").equals("none")) {

        }
        int latestHeight = driver.findElement(By.className("GridItems")).getSize().getHeight();
        while (getPinsCount(driver) < count) {

            long startedAt = System.currentTimeMillis();
            while (!driver.findElement(By.className("gridFooterSpinner")).getCssValue("display").equals("inline-block")
                    && latestHeight == driver.findElement(By.className("GridItems")).getSize().getHeight()) {
                if (System.currentTimeMillis() > startedAt + 20 * 1000) {
                    // reahed the end
                    return;
                }
                jse.executeScript("window.scrollBy(0, 250)", "");
            }
            waitUntilLoadingDone(driver);
            latestHeight = driver.findElement(By.className("GridItems")).getSize().getHeight();
        }
    }

    private void waitUntilLoadingStarted(WebDriver driver) {
        await().atMost(20, TimeUnit.SECONDS)
                .until(() ->
                        driver.findElement(By.className("gridFooterSpinner")).getCssValue("display").equals("inline-block")
                );
    }

    private void waitUntilLoadingDone(WebDriver driver) {
        await().atMost(20, TimeUnit.SECONDS)
                .until(() ->
                        driver.findElement(By.className("gridFooterSpinner")).getCssValue("display").equals("block")
                );
    }

    private int getPinsCount(WebDriver driver) throws IOException {
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);

        return doc.getElementsByClass("pinWrapper").size();
    }

    private List<Pin> parsePins(WebDriver driver) throws IOException {
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);

        List<Pin> pins = doc.getElementsByClass("pinWrapper").stream().map(this::toPin).filter(p -> p != null).collect(Collectors.toList());

        return pins;
    }

    private Pin toPin(Element element) {
        try {
            String pinUrl = "https://www.pinterest.com" + element.getElementsByClass("pinImageWrapper").get(0).attr("href");
            String imgUrl = element.getElementsByClass("pinImg").get(0).attr("src");
            String description = element.getElementsByClass("pinDescription").get(0).text();
            Pin pin = new Pin(imgUrl, pinUrl, description);
            return pin;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<Pin, Integer> countByPin(List<Pin> pins) {
        Map<Pin, Integer> map = new HashMap<>();
        pins.forEach(pin -> {
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
