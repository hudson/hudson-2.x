package hudson.ui;

import com.thoughtworks.selenium.Selenium;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Test cases for free-style jobs.
 * <p/>
 * Copyright (C) 2011 Hudson-CI.org
 * <p/>
 * Date: 4/29/11
 *
 * @author Anton Kozak
 */
public class FreestyleJobUITest extends BaseUITest {

    private static final String BUILD_SUCCESS_TEXT = "Finished: SUCCESS";

    @Test
    public void testSubversionScm() {
        Selenium selenium = getSelenium();
        waitQuietly(4000L);
        selenium.open("/");
        waitQuietly(10000L);
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", "subversion-plugin");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click("radio-block-25");
        selenium.type("svn.remote.loc", "https://svn.java.net/svn/hudson~svn/trunk/hudson/plugins/subversion");
        selenium.click("_.useUpdate");
        selenium.click("//span[@id='yui-gen2']/span/button");
        selenium.click("link=Invoke top-level Maven targets");
        selenium.type("textarea._.targets", "clean install -DskipTests");
        selenium.click("//span[@id='yui-gen11']/span/button");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/subversion-plugin/1/console");
        waitQuietly(30000L);
        assertTrue(selenium.isTextPresent(BUILD_SUCCESS_TEXT));
    }

    @Test
	public void testGitScm() {
        Selenium selenium = getSelenium();
        waitQuietly(4000L);
		selenium.open("/");
        waitQuietly(10000L);
		selenium.click("link=New Job");
     	selenium.waitForPageToLoad("30000");
		selenium.type("name", "git-plugin");
		selenium.click("mode");
		selenium.click("//button[@type='button']");
		selenium.waitForPageToLoad("30000");
		selenium.click("radio-block-26");
		selenium.type("git.repo.url", "https://github.com/hudson-plugins/git-plugin.git");
		selenium.click("//span[@id='yui-gen2']/span/button");
		selenium.click("link=Invoke top-level Maven targets");
		selenium.type("textarea._.targets", "clean install -DskipTests");
		selenium.click("//span[@id='yui-gen19']/span/button");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/git-plugin/1/console");
        waitQuietly(30000L);
        assertTrue(selenium.isTextPresent(BUILD_SUCCESS_TEXT));
	}

    @Test
    public void testCvsScm() {
        Selenium selenium = getSelenium();
        waitQuietly(4000L);
		selenium.open("/");
        waitQuietly(10000L);
		selenium.click("link=New Job");
		selenium.waitForPageToLoad("30000");
		selenium.type("name", "cvs-plugin");
		selenium.click("mode");
		selenium.click("//button[@type='button']");
		selenium.waitForPageToLoad("30000");
		selenium.click("radio-block-27");
		selenium.type("cvs_root", ":pserver:anonymous@proftp.cvs.sourceforge.net:2401/cvsroot/proftp");
		selenium.click("//span[@id='yui-gen19']/span/button");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Build Now");
        selenium.waitForPageToLoad("30000");
        selenium.open("/job/cvs-plugin/1/console");
        waitQuietly(20000L);
		assertTrue(selenium.isTextPresent("Finished: SUCCESS"));
    }
}
