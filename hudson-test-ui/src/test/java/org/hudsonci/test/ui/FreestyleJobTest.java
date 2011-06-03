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
 * Test cases for free-style jobs.
 * <p/>
 * Copyright (C) 2011 Hudson-CI.org
 * <p/>
 * Date: 4/29/11
 *
 * @author Anton Kozak
 */
public class FreestyleJobTest extends BaseUITest {

    private static final String BUILD_SUCCESS_TEXT = "Finished: SUCCESS";
    private static final String BUILD_FAILURE_TEXT = "Finished: FAILURE";

    private static final String SUBVERSION_LBL_SELECT_EXP = "//label[contains(text(),'Subversion')]";
    private static final String GIT_LBL_SELECT_EXP = "//label[contains(text(),'Git')]";
    private static final String CVS_LBL_SELECT_EXP = "//label[contains(text(),'CVS')]";

    @Test
    public void testSubversionScm() {
        Selenium selenium = getSelenium();
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "subversion-plugin");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click(SUBVERSION_LBL_SELECT_EXP);
        selenium.type("svn.remote.loc", "https://svn.java.net/svn/hudson~svn/trunk/hudson/plugins/subversion");
//        selenium.click("//span[@id='yui-gen2']/span/button");
//        selenium.click("link=Invoke top-level Maven targets");
//        selenium.type("textarea._.targets", "clean install -DskipTests");
        selenium.click("//span[@id='yui-gen19']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/subversion-plugin/1/console");
        waitForTextPresent(BUILD_SUCCESS_TEXT, BUILD_FAILURE_TEXT);
    }

    @Test
    public void testGitScm() {
        Selenium selenium = getSelenium();
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "git-plugin");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click(GIT_LBL_SELECT_EXP);
        selenium.type("git.repo.url", "git://github.com/hudson-plugins/git-plugin.git");
//        selenium.click("//span[@id='yui-gen2']/span/button");
//        selenium.click("link=Invoke top-level Maven targets");
//        selenium.type("textarea._.targets", "clean install -DskipTests");
        selenium.click("//span[@id='yui-gen19']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/git-plugin/1/console");
        waitForTextPresent(BUILD_SUCCESS_TEXT, BUILD_FAILURE_TEXT);
    }

    @Test
    public void testCvsScm() {
        Selenium selenium = getSelenium();
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "cvs-plugin");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click(CVS_LBL_SELECT_EXP);
        selenium.type("cvs_root", ":pserver:anonymous@proftp.cvs.sourceforge.net:2401/cvsroot/proftp");
        selenium.click("//span[@id='yui-gen19']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/cvs-plugin/1/console");
        waitForTextPresent(BUILD_SUCCESS_TEXT, BUILD_FAILURE_TEXT);
    }
}
