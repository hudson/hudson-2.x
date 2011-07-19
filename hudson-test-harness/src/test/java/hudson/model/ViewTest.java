/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import org.jvnet.hudson.test.Bug;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import org.jvnet.hudson.test.Email;
import org.jvnet.hudson.test.HudsonTestCase;
import org.w3c.dom.Text;

import static hudson.model.Messages.Hudson_ViewName;

/**
 * @author Kohsuke Kawaguchi
 */
public class ViewTest extends HudsonTestCase {

    @Bug(7100)
    public void testXHudsonHeader() throws Exception {
        assertNotNull(new WebClient().goTo("/").getWebResponse().getResponseHeaderValue("X-Hudson"));
    }

	/**
     * Creating two views with the same name.
     */
    @Email("http://d.hatena.ne.jp/ssogabe/20090101/1230744150")
    public void testConflictingName() throws Exception {
        assertNull(hudson.getView("foo"));

        HtmlForm form = new WebClient().goTo("newView").getFormByName("createView");
        form.getInputByName("name").setValueAttribute("foo");
        form.getRadioButtonsByName("mode").get(0).setChecked(true);
        submit(form);
        assertNotNull(hudson.getView("foo"));

        // do it again and verify an error
        try {
            submit(form);
            fail("shouldn't be allowed to create two views of the same name.");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(400,e.getStatusCode());
        }
    }

    public void testPrivateView() throws Exception {
        createFreeStyleProject("project1");
        User user = User.get("me", true); // create user

        WebClient wc = new WebClient();
        HtmlPage userPage = wc.goTo("/user/me");
        HtmlAnchor privateViewsLink = userPage.getFirstAnchorByText("My Views");
        assertNotNull("My Views link not available", privateViewsLink);

        HtmlPage privateViewsPage = (HtmlPage) privateViewsLink.click();

        Text viewLabel = (Text) privateViewsPage.getFirstByXPath("//table[@id='viewList']//td[@class='active']/text()");
        assertTrue("'All' view should be selected", viewLabel.getTextContent().contains(Hudson_ViewName()));

        View listView = new ListView("listView", hudson);
        hudson.addView(listView);

        HtmlPage newViewPage = wc.goTo("/user/me/my-views/newView");
        HtmlForm form = newViewPage.getFormByName("createView");
        form.getInputByName("name").setValueAttribute("proxy-view");
        ((HtmlRadioButtonInput) form.getInputByValue("hudson.model.ProxyView")).setChecked(true);
        HtmlPage proxyViewConfigurePage = submit(form);
        View proxyView = user.getProperty(MyViewsProperty.class).getView("proxy-view");
        assertNotNull(proxyView);
        form = proxyViewConfigurePage.getFormByName("viewConfig");
        form.getSelectByName("proxiedViewName").setSelectedAttribute("listView", true);
        submit(form);

        assertTrue(proxyView instanceof ProxyView);
        assertEquals(((ProxyView) proxyView).getProxiedViewName(), "listView");
        assertEquals(((ProxyView) proxyView).getProxiedView(), listView);
    }
    
    public void testDeleteView() throws Exception {
    	WebClient wc = new WebClient();

    	ListView v = new ListView("list", hudson);
		hudson.addView(v);
    	HtmlPage delete = wc.getPage(v, "delete");
    	submit(delete.getFormByName("delete"));
    	assertNull(hudson.getView("list"));
    	
    	User user = User.get("user", true);
    	MyViewsProperty p = user.getProperty(MyViewsProperty.class);
    	v = new ListView("list", p);
		p.addView(v);
    	delete = wc.getPage(v, "delete");
    	submit(delete.getFormByName("delete"));
    	assertNull(p.getView("list"));
    	
    }
}
