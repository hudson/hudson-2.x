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
import org.junit.Test;

/**
 * Test cases to verify security configuration.
 * <p/>
 * Copyright (C) 2011 Hudson-CI.org
 * <p/>
 * Date: 4/29/11
 *
 * @author Anton Kozak
 */
public class SecurityHudsonRealmTest extends BaseUITest {

    private static final String BUILD_SUCCESS_TEXT = "Finished: SUCCESS";
    private static final String BUILD_FAILURE_TEXT = "Finished: FAILURE";

    private static final String HUDSUN_USER_DB_LBL_SELECT_EXP = "//label[contains(text(),'own user database')]";
    private static final String ANYONE_CAN_DO_ANUTHING_LBL_SELECT_EXP = "//label[contains(text(),'Anyone can do anything')]";
    private static final String CONFIG_SAVE_SELECT_EXP = "//button[contains(text(),'Save')]";
    private static final String SUBVERSION_LBL_SELECT_EXP = "//label[contains(text(),'Subversion')]";


    @Test
    public void testAnyoneCanDoAnything() {
        Selenium selenium = getSelenium();
        selenium.open("/configure");
        waitForTextPresent("New Job");
        selenium.click("cb0");

        selenium.click(HUDSUN_USER_DB_LBL_SELECT_EXP);
        selenium.click("privateRealm.enableCaptcha");
        selenium.click(ANYONE_CAN_DO_ANUTHING_LBL_SELECT_EXP);
        selenium.click(CONFIG_SAVE_SELECT_EXP);
        selenium.waitForPageToLoad("30000");
        selenium.open("/signup");
        selenium.waitForPageToLoad("30000");
        selenium.type("username", "admin");
        selenium.type("password1", "admin");
        selenium.type("password2", "admin");
        selenium.type("fullname", "admin");
        selenium.type("email", "admin@gmail.com");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        //run test job
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "testHudsonRealmAnyoneCanDoAnything");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click(SUBVERSION_LBL_SELECT_EXP);
        selenium.type("svn.remote.loc", "https://svn.java.net/svn/hudson~svn/trunk/hudson/plugins/subversion");
//        selenium.click("//span[@id='yui-gen2']/span/button");
//        selenium.click("link=Invoke top-level Maven targets");
//        selenium.type("textarea._.targets", "clean");
        selenium.click("//span[@id='yui-gen19']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/testHudsonRealmAnyoneCanDoAnything/1/console");
        waitForTextPresent(BUILD_SUCCESS_TEXT, BUILD_FAILURE_TEXT);
    }
}
