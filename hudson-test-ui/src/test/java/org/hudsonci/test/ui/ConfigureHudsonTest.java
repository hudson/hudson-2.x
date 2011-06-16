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
 *    Nikita Levyankov.
 *      
 *
 *******************************************************************************/ 

package org.hudsonci.test.ui;

import com.thoughtworks.selenium.Selenium;
import org.hudsonci.test.ui.util.SystemUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for configure system page.
 * <p/>
 * Date: 5/6/11
 *
 * @author Nikita Levyankov
 */
public class ConfigureHudsonTest extends BaseUITest {

    private static final String GLOBAL_PROPS_LBL_SELECT_EXP = "//label[contains(text(),'Environment variables')]";
    private static final String SUBVERSION_LBL_SELECT_EXP = "//label[contains(text(),'Subversion')]";
    private static final String BUILD_SUCCESS_TEXT = "Finished: SUCCESS";
    private static final String BUILD_FAILURE_TEXT = "Finished: FAILURE";

    @Test
    public void testAddJDK() throws Exception {
        String addJDKButtonXpath = "//button[contains(text(), 'Add JDK')]";
        String jdkName = "jdk_6_24";
//        String jdkVersion = "6 Update 22";
        Selenium selenium = getSelenium();
        selenium.open("/");
        //Open Manage Hudson page
        waitForTextPresent("Manage Hudson", null);
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        //Open Configure System Page
        selenium.click("link=Configure System");
        waitForElementPresence(addJDKButtonXpath);
        //Validate Add JDK button presence
        assertTrue(selenium.isElementPresent(addJDKButtonXpath));
        selenium.click(addJDKButtonXpath);
        assertTrue(selenium.isElementPresent("//input[@name='_.name']"));
        //name is pre-validated. Non-Empty value is required. Check that error is displayed
        assertTrue(selenium.isTextPresent("Required"));
        //Validate for accept licence checkbox presence
        selenium.isElementPresent("//input[@name='_.acceptLicense']");
        assertTrue(selenium.isTextPresent("You must agree to the license to download the JDK."));
        //Enter required jdk name
        selenium.type("_.name", jdkName);
//        selenium.select("_.id", jdkVersion);
        //Need to accept oracle licence
        selenium.click("_.acceptLicense");
        //Click save button.
        selenium.click("//button[contains(text(), 'Save')]");
        waitForTextPresent("Manage Hudson", null);
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        selenium.click("link=Configure System");

        //Re-validate changes
        assertEquals(selenium.getValue("_.name"), jdkName);
//        assertEquals(selenium.getSelectedLabel("_.id"), jdkVersion);
        //Click delete installer and save button.
        selenium.click("//button[contains(text(), 'Delete JDK')]");
        selenium.click("//button[contains(text(), 'Save')]");
    }

    @Test
    public void testChangeSystemMessage() throws Exception {
        Selenium selenium = getSelenium();
        selenium.open("/");
        //Navigate to Configure System page
        waitForTextPresent("Manage Hudson", null);
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        selenium.click("link=Configure System");
        waitForTextPresent("System Message", null);
        //Enter a simple message and save
        selenium.type("system_message", "A simple test message\n\n<p>With some html tags</p>");
        selenium.click("//button[contains(text(), 'Save')]");
        selenium.waitForPageToLoad("30000");
        //Verify the message appears
        waitForTextPresent("A simple test message With some html tags", null);
    }

    @Test
    public void testChangeExecutors() throws Exception {
        Selenium selenium = getSelenium();
        selenium.open("/");
        //Check that we have two executors to start with
        waitForElementPresence("//table[@id='executors']/tbody[2]/tr[1]/th[1]");
        assertEquals("Status 0/2", selenium.getText("//table[@id='executors']/tbody[2]/tr[1]/th[1]"));
        //Navigate to Configure System page
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        selenium.click("link=Configure System");
        waitForTextPresent("# of executors", null);
        //Check and update the number of executors
        assertEquals(selenium.getValue("_.numExecutors"), "2");
        selenium.type("_.numExecutors", "1");
        //Save
        selenium.click("//button[contains(text(), 'Save')]");
        waitForTextPresent("Manage Hudson", null);
        //Verify the number of excutors now
        waitForElementPresence("//table[@id='executors']/tbody[2]/tr[1]/th[1]");
        assertEquals("Status 0/1", selenium.getText("//table[@id='executors']/tbody[2]/tr[1]/th[1]"));
    }

    @Test
    public void testGlobalProperties() throws Exception {
        Selenium selenium = getSelenium();
        selenium.open("/");
        //Navigate to Configure System page
        waitForTextPresent("Manage Hudson", null);
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        selenium.click("link=Configure System");
        waitForTextPresent("System Message", null);
        selenium.click(GLOBAL_PROPS_LBL_SELECT_EXP);
        selenium.click("//button[contains(text(), 'Save')]");
        //Navigate to Configure System page
        waitForTextPresent("Manage Hudson", null);
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        selenium.click("link=Configure System");
        waitForTextPresent("System Message", null);
        selenium.click("//tr[38]/td[3]/div/span/span/button");
        waitForTextPresent("List of key-value pairs", null);
        selenium.type("env.key", "TEST");
        selenium.type("env.value", "Hello");
        selenium.click("//button[contains(text(), 'Save')]");

        //Create Job that uses TEST property
        waitForTextPresent("Manage Hudson", null);
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "global-prop-test");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click("//span[@id='yui-gen2']/span/button");
        if (SystemUtils.isWindows()) {
            //On windows use batch command
            selenium.click("link=Execute Windows batch command");
            waitForTextPresent("Execute Windows batch command", null);
            selenium.type("command", "echo %TEST%");
            selenium.click("//button[contains(text(), 'Save')]");
            selenium.waitForPageToLoad("30000");
        } else {
            //On non-windows use shell
            selenium.click("link=Execute shell");
            waitForTextPresent("Execute shell", null);
            selenium.type("command", "echo $TEST");
            selenium.click("//button[contains(text(), 'Save')]");
            selenium.waitForPageToLoad("30000");
        }
        //Run and verify
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/global-prop-test/1/console");
        waitForTextPresent(BUILD_SUCCESS_TEXT, BUILD_FAILURE_TEXT);
        waitForTextPresent("Hello", null);
    }

    @Test
    public void testReduceQuietPeriod() throws Exception {
        Selenium selenium = getSelenium();
        selenium.open("/");
        //Navigate to Configure System page
        waitForTextPresent("Manage Hudson", null);
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        selenium.click("link=Configure System");
        waitForTextPresent("System Message", null);
        selenium.type("quiet_period", "0");
        selenium.click("//button[contains(text(), 'Save')]");

        //Create a Job that should run immediately
        waitForTextPresent("Manage Hudson", null);
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "time-test");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click("//span[@id='yui-gen2']/span/button");
        if (SystemUtils.isWindows()) {
            //On windows use batch command
            selenium.click("link=Execute Windows batch command");
            waitForTextPresent("Execute Windows batch command", null);
            selenium.type("command", "PING 1.1.1.1 -n 1 -w 3000  1>NUL");
            selenium.click("//button[contains(text(), 'Save')]");
            selenium.waitForPageToLoad("30000");
        } else {
            //On non-windows use shell
            selenium.click("link=Execute shell");
            waitForTextPresent("Execute shell", null);
            selenium.type("command", "sleep 3");
            selenium.click("//button[contains(text(), 'Save')]");
            selenium.waitForPageToLoad("30000");
        }
        //Run and verify
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.open("/job/time-test/1/console");
    }

    @Test
    public void testIncreaseSCMRetryCount() {
        Selenium selenium = getSelenium();
        selenium.open("/");
        //Navigate to Configure System page
        waitForTextPresent("Manage Hudson", null);
        selenium.click("link=Manage Hudson");
        waitForTextPresent("Configure System", null);
        selenium.click("link=Configure System");
        waitForTextPresent("System Message", null);
        selenium.type("retry_count", "1");
        selenium.click("//button[contains(text(), 'Save')]");

        //Create a Job that should run immediately
        waitForTextPresent("Manage Hudson", null);
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "scm-test");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.click(SUBVERSION_LBL_SELECT_EXP);
        selenium.type("svn.remote.loc", "http://not.there.com/");
        selenium.click("//button[contains(text(), 'Save')]");
        
        //Run and verify
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/scm-test/1/console");
        waitForTextPresent("Retrying after 10 seconds", null);
    }
}
