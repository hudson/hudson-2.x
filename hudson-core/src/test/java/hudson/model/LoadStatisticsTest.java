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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.util.graph.Graph;
import hudson.util.graph.MultiStageTimeSeries.TimeScale;
import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class LoadStatisticsTest extends TestCase {
    // This test is kipped for now. Can't run since it needs to load the
    // GraphSupport Plugin
	public void testGraph() throws IOException {
//		LoadStatistics ls = new LoadStatistics(0, 0) {
//			public int computeIdleExecutors() {
//				throw new UnsupportedOperationException();
//			}
//
//			public int computeTotalExecutors() {
//				throw new UnsupportedOperationException();
//			}
//
//			public int computeQueueLength() {
//				throw new UnsupportedOperationException();
//			}
//		};
//
//		for (int i = 0; i < 50; i++) {
//			ls.totalExecutors.update(4);
//			ls.busyExecutors.update(3);
//			ls.queueLength.update(3);
//		}
//
//		for (int i = 0; i < 50; i++) {
//			ls.totalExecutors.update(0);
//			ls.busyExecutors.update(0);
//			ls.queueLength.update(1);
//		}
//
//		Graph graph = ls.createTrendChart(TimeScale.SEC10).createGraph();
//		BufferedImage image = graph.createImage(400, 200);
//		
//		File tempFile = File.createTempFile("chart-", "png");
//		FileOutputStream os = new FileOutputStream(tempFile);
//		try {
//			ImageIO.write(image, "PNG", os);
//		} finally {
//			IOUtils.closeQuietly(os);
//			tempFile.delete();
//		}
	}
}
