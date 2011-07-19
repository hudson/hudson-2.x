/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.service.internal;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.security.Permission;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hudson.service.NodeNotFoundException;
import org.eclipse.hudson.service.NodeService;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.internal.NodeServiceImpl;
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
