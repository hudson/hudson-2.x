/*
  * The MIT License
  *
  * Copyright (c) 2011, Oracle Corporation, Anton Kozak
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
package org.hudsonci.test.ui;

import com.thoughtworks.selenium.Selenium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Base class for UI testing.
 * <p/>
 * Copyright (C) 2011 Hudson-CI.org
 * <p/>
 * Date: 4/29/11
 *
 * @author Anton Kozak
 */
public abstract class BaseUITest{

    /**
     * Base application URL.
     */
    public static final String BASE_URL = "http://localhost:6002/hudson";

    /**
     * Default wait period.
     */
    private static final long VERIFICATION_ATTEMPT_PERIOD = 5000L;

    /**
     * Count of attempts.
     */
    private static final int VERIFICATION_ATTEMPTS_COUNT = 100;

    /**
     * WebDriver.
     */
    private static WebDriver driver;

    /**
     * Selenium
     */
    private static Selenium selenium;

    /**
     * Starts WebDriver and selenium.
     *
     * @throws Exception Exception.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        driver = new FirefoxDriver();
        selenium = new WebDriverBackedSelenium(driver, BASE_URL);
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
     * Returns web driver.
     *
     * @return web driver.
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Waits specified time.
     *
     * @param delay delay.
     */
    protected void waitQuietly(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Wait for text present on UI.
     *
     * @param successText text to search.
     */
    protected void waitForTextPresent(String successText) {
        waitForTextPresent(successText, null);
    }

    /**
     * Wait for text present on UI.
     *
     * @param successText text to search.
     * @param failureText text shows that test fails.
     */
    protected void waitForTextPresent(String successText, String failureText) {
        boolean isSuccessTextPresent = false;
        for (int i = 0; i < VERIFICATION_ATTEMPTS_COUNT; i++) {
            if (failureText != null) {
                assertFalse("Failure text is present:" + failureText, getSelenium().isTextPresent(failureText));
            }
            try {
                if (getSelenium().isTextPresent(successText)) {
                    isSuccessTextPresent = true;
                    break;
                }
            } catch (Exception ignored) {
            }
            waitQuietly(VERIFICATION_ATTEMPT_PERIOD);
        }
        assertTrue("Cannot find success text:" + successText, isSuccessTextPresent);
    }

    /**
     * Wait for element presence. If element is absent, exception is thrown.
     *
     * @param element element search.
     */
    protected void waitForElementPresence(String element) {
        for (int i = 0; i < VERIFICATION_ATTEMPTS_COUNT; i++) {
            if (!getSelenium().isElementPresent(element)) {
                waitQuietly(VERIFICATION_ATTEMPT_PERIOD);
            } else {
                break;
            }
        }
        if (!getSelenium().isElementPresent(element)) {
            throw new NoSuchElementException("Expected element: " + element + " is absent");
        }
    }

    /**
     * Quits webdriver, closing every associated window.
     */
    @AfterClass
    public static void tearDown() {
        driver.quit();
    }
}
