package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import pages.SearchPage;
import pages.SearchResultsPage;

import static com.codeborne.selenide.Selenide.closeWebDriver;

public class TestBase {

    protected final SearchPage searchPage = new SearchPage();
    protected final SearchResultsPage searchResultsPage = new SearchResultsPage();

    @BeforeAll
    static void setUpAllure() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @BeforeEach
    void setUp() {
        Configuration.baseUrl = System.getProperty("baseUrl", "https://github.com");
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.browserSize = System.getProperty("browserSize", "1920x1080");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }
}