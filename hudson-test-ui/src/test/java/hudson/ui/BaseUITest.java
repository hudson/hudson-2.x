package hudson.ui;

import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Base class for UI testing.
 * <p/>
 * Copyright (C) 2011 Hudson-CI.org
 * <p/>
 * Date: 4/29/11
 *
 * @author Anton Kozak
 */
@ContextConfiguration(locations = {"/test-context.xml"})
public abstract class BaseUITest extends AbstractTestNGSpringContextTests {

    /**
     * Base application URL.
     */
    private String baseUrl;

    /**
     * WebDriver.
     */
    private WebDriver driver;

    /**
     * Selenium
     */
    private Selenium selenium;

    /**
     * Returns base URL.
     *
     * @return base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets base URL based on maven configuration.
     *
     * @param baseUrl base url.
     */
    @Autowired
    public void setBaseUrl(@Qualifier("baseUrl") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Starts WebDriver and selenium.
     *
     * @throws Exception Exception.
     */
    @BeforeClass
    public void setUp() throws Exception {
        driver = new FirefoxDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
    }

    /**
     * Returns selenium configured by base URL.
     *
     * @return selenium.
     */
    protected Selenium getSelenium() {
        return selenium;
    }

    /**
     * Waits specified time.
     *
     * @param delay delay.
     */
    protected void waitQuietly(long delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Quits webdriver, closing every associated window.
     */
    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
