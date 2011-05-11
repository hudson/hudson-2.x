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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Test cases to verify security configuration.
 * <p/>
 * Copyright (C) 2011 Hudson-CI.org
 * <p/>
 * Date: 4/29/11
 *
 * @author Anton Kozak
 */
public class SecurityHudsonRealmIT extends BaseUITest {

    private static final String BUILD_SUCCESS_TEXT = "Finished: SUCCESS";
    private static final String BUILD_FAILURE_TEXT = "Finished: FAILURE";

    private static final String HUDSUN_USER_DB_LBL_SELECT_EXP = ".//label[contains(text(),'own user database')]";
    private static final String ANYONE_CAN_DO_ANUTHING_LBL_SELECT_EXP = ".//label[contains(text(),'Anyone can do anything')]";
    private static final String MATRIX_BASED_SECURITY_LBL_SELECT_EXP = ".//label[contains(text(),'Matrix-based security')]";
    private static final String CONFIG_SAVE_SELECT_EXP = ".//button[contains(text(),'Save')]";
    private static final String SUBVERSION_LBL_SELECT_EXP = ".//label[contains(text(),'Subversion')]";


    @Test
    public void testAnyoneCanDoAnything() {
        Selenium selenium = getSelenium();
        selenium.open("/configure");
        waitForTextPresent("New Job");
        selenium.click("cb0");

        getDriver().findElement(By.xpath(HUDSUN_USER_DB_LBL_SELECT_EXP)).click();
        selenium.click("privateRealm.enableCaptcha");
        getDriver().findElement(By.xpath(ANYONE_CAN_DO_ANUTHING_LBL_SELECT_EXP)).click();
        getDriver().findElement(By.xpath(CONFIG_SAVE_SELECT_EXP)).click();
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
        getDriver().findElement(By.xpath(SUBVERSION_LBL_SELECT_EXP)).click();
        selenium.type("svn.remote.loc", "https://svn.java.net/svn/hudson~svn/trunk/hudson/plugins/subversion");
        selenium.click("//span[@id='yui-gen2']/span/button");
        selenium.click("link=Invoke top-level Maven targets");
        selenium.type("textarea._.targets", "clean");
        selenium.click("//span[@id='yui-gen19']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/testHudsonRealmAnyoneCanDoAnything/1/console");
        waitForTextPresent(BUILD_SUCCESS_TEXT, BUILD_FAILURE_TEXT);

    }

//    @Test
    //TODO fix me
    public void testMatrixBasedSecurity() {
        Selenium selenium = getSelenium();
        selenium.open("/configure");
        waitForTextPresent("New Job");
        selenium.click("cb0");

        getDriver().findElement(By.xpath(HUDSUN_USER_DB_LBL_SELECT_EXP)).click();
        selenium.click("privateRealm.enableCaptcha");
        getDriver().findElement(By.xpath(MATRIX_BASED_SECURITY_LBL_SELECT_EXP)).click();
        waitQuietly(3000L);
        WebElement userAddTable = getDriver().findElement(By.id("add-user-tbl"));
        List<WebElement> userInputs = userAddTable.findElements(By.xpath(".//input[@type='text']"));
        for (WebElement userInput : userInputs) {
            try {
                userInput.clear();
                userInput.sendKeys("admin");
            } catch (Exception ignore) {
            }
        }
        waitQuietly(3000000L);
        userAddTable.findElement(By.xpath(".//button[@type='button']")).click();
//        selenium.click("//input[@name='add-user-input']");
//        waitQuietly(3000L);
//        selenium.typeKeys("//input[@name='add-user-input']", "admin");
//        selenium.typeKeys("xpath=//td[contains(text(),'User/group to add:')]/input", "admin");
        waitQuietly(300000000L);
//        selenium.click("//td[contains(text(),'User/group to add:')]/span/span/button");
//       selenium.click("//*[@name='add-user-btn']");
//        getDriver().findElement(By.xpath("//button[@name='add-user-btn']")).click();

//        selenium.type("id1text", "admin");  id11001text
//        selenium.click("//span[@id='id1button']/span/button");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[2]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[3]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[4]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[5]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[6]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[7]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[8]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[9]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[10]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[11]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[12]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[13]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[14]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[15]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[4]/td[16]/input");
        selenium.type("id1text", "user");
        selenium.click("//span[@id='id1button']/span/button");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[5]/td[3]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[5]/td[9]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[5]/td[10]/input");
        selenium.click("//table[@id='hudson-security-GlobalMatrixAuthorizationStrategy']/tbody/tr[5]/td[11]/input");
        selenium.click("//span[@id='yui-gen8']/span/button");
        selenium.waitForPageToLoad("30000");
/*
        selenium.click("//td[@id='main-panel']/div/div/a");
        selenium.waitForPageToLoad("30000");
        selenium.type("username", "admin");
        selenium.type("password1", "admin");
        selenium.type("password2", "admin");
        selenium.type("fullname", "admin");
        selenium.type("email", "admin@gmail.com");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "test");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click("radio-block-24");
        selenium.type("svn.remote.loc", "https://svn.java.net/svn/hudson~svn/trunk/hudson/plugins/subversion");
        selenium.click("//span[@id='yui-gen11']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.click("link=May 10, 2011 3:38:24 PM");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Console Output");
        selenium.waitForPageToLoad("30000");
        selenium.click("//td[@id='login-field']/span/span/a[2]/b");
        selenium.waitForPageToLoad("30000");
        selenium.click("//td[@id='main-panel']/div/div/a");
        selenium.waitForPageToLoad("30000");
        selenium.type("username", "user");
        selenium.type("password1", "user");
        selenium.type("password2", "user");
        selenium.type("fullname", "user");
        selenium.type("email", "user@gmail.com");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=the top page");
        selenium.waitForPageToLoad("30000");
        selenium.click("//img[@alt='Schedule a build']");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=test");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=May 10, 2011 3:39:23 PM");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Console Output");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Back to Project");
        selenium.waitForPageToLoad("30000");
*/
    }
}
