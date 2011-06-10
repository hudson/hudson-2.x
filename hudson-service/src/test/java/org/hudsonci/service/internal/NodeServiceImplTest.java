/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.service.internal;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.security.Permission;

import java.util.ArrayList;
import java.util.List;

import org.hudsonci.service.NodeNotFoundException;
import org.hudsonci.service.NodeService;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.internal.NodeServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Hudson.class)
public class NodeServiceImplTest {

    // setup
    private Hudson hudson;

    @Mock
    private SecurityService security;

    @Mock
    private NodeServiceImpl nodeService;

    @Mock
    private Node node;

    @Before
    public void setUp() throws Exception {
        mockStatic(Hudson.class); // static methods
        hudson = mock(Hudson.class); // final and native

        MockitoAnnotations.initMocks(this);
        nodeService = new NodeServiceImpl(security);
        nodeService.setHudson(hudson);
    }

    private NodeServiceImpl getInst() {
        return nodeService;
    }

    @Test
    public void setupProperly() {
        assertThat(getInst(),notNullValue());
        assertThat(hudson,notNullValue());
    }

    @Test
    public void getAllNodesSecurity() {

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);
        Node masterNode = hudson;

        NodeService inst = spy(getInst());

        doReturn(nodes).when(hudson).getNodes();
        doReturn(hudson).when(inst).getMasterNode();
        doReturn(true).when(security).hasPermission(masterNode, Permission.READ);

        List<Node> result = inst.getAllNodes();

        assertThat(result, not(contains(node)));
        assertThat(result, contains(masterNode));
        Mockito.verify(security).hasPermission(node, Permission.READ);
        Mockito.verify(security).hasPermission(masterNode, Permission.READ);

    }

    @Test
    public void getNodesSecurity() {

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);

        NodeService inst = spy(getInst());

        doReturn(nodes).when(hudson).getNodes();
        doReturn(true).when(security).hasPermission(node, Permission.READ);

        List<Node> result = inst.getNodes();

        assertThat(result, contains(node));

        Mockito.verify(security).hasPermission(node, Permission.READ);

    }


    @Test
    public void getNodesDoesNotIncludeMaster() {

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);
        Node masterNode = hudson;

        NodeService inst = spy(getInst());

        doReturn(nodes).when(hudson).getNodes();
        doReturn(true).when(security).hasPermission(node, Permission.READ);
        // try to fake it out
        doReturn(hudson).when(inst).getMasterNode();
        doReturn(true).when(security).hasPermission(masterNode, Permission.READ);

        List<Node> result = inst.getNodes();

        assertThat(result, contains(node));
        assertThat(result, not(contains(masterNode)));
    }



    @Test(expected=NullPointerException.class)
    public void getNodeNullArg() {
        getInst().getNode(null);
    }

    @Test(expected=NodeNotFoundException.class)
    public void getNodeNotFound() {
        getInst().getNode("nodeName");
    }

    @Test
    public void getNodeSecurity() {
        when(hudson.getNode("nodeName")).thenReturn(node);
        assertThat(getInst().getNode("nodeName"), equalTo(node));
        Mockito.verify(security).checkPermission(node, Permission.READ);
    }

    @Test(expected=NullPointerException.class)
    public void findNodeNullArg() {
        getInst().findNode(null);
    }

    @Test
    public void findNodeNotFound() {
        assertThat(getInst().findNode("nodeName"), nullValue());
    }

    @Test
    public void findNodeReturnMaster() {
        Node masterNode = hudson;
        assertThat(getInst().findNode(""), equalTo(masterNode));
        Mockito.verify(hudson, Mockito.never()).getNode("");
    }

    @Test
    public void findNodeReturnNotMaster() {
        when(hudson.getNode("nodeName")).thenReturn(node);
        assertThat(getInst().findNode("nodeName"), equalTo(node));
    }

    @Test
    public void findNodeSecurity() {
        when(hudson.getNode("nodeName")).thenReturn(node);
        assertThat(getInst().findNode("nodeName"), equalTo(node));
        Mockito.verify(security).checkPermission(node, Permission.READ);
    }

    @Test
    public void getMasterNodeIsHudson() {
        Node masterNode = hudson;
        assertThat(getInst().getMasterNode(), equalTo(masterNode));
    }



}
