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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;
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
public abstract class BaseUITest{

    /**
     * Base application URL.
     */
    public static final String BASE_URL = "http://localhost:6002/hudson";

    /**
     * WebDriver.
     */
    private WebDriver driver;

    /**
     * Selenium
     */
    private Selenium selenium;

    /**
     * Starts WebDriver and selenium.
     *
     * @throws Exception Exception.
     */
    @BeforeClass
    public void setUp() throws Exception {
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
     * Quits webdriver, closing every associated window.
     */
    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
