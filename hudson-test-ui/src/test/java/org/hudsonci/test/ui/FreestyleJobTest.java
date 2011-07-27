/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Anton Kozak
 *      
 *
 *******************************************************************************/ 

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
//        selenium.click("link=Invoke Maven 2 (Legacy)");
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
//        selenium.click("link=Invoke Maven 2 (Legacy)");
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
        selenium.type("cvs_root", ":pserver:anonymous@ayam.cvs.sourceforge.net:/cvsroot/ayam");
        selenium.type("textarea._.allModules", "ayam");
        selenium.click("//span[@id='yui-gen19']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/cvs-plugin/1/console");
        waitForTextPresent(BUILD_SUCCESS_TEXT, BUILD_FAILURE_TEXT);
    }
}
