/*******************************************************************************
 *
 * Copyright (c) 2004-2011, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Winston Prakash
 *        
 *
 *******************************************************************************/ 

package hudson.util.graph;

import hudson.model.Api;
import hudson.model.Messages;
import hudson.model.TimeSeries;
import hudson.util.TimeUnit2;
import java.awt.Color;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletException;

import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Maintains several {@link TimeSeries} with different update frequencies to satisfy three goals;
 * (1) retain data over long timespan, (2) save memory, and (3) retain accurate data for the recent past.
 *
 * All in all, one instance uses about 8KB space.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class MultiStageTimeSeries {

    /**
     * Name of this data series.
     */
    public final Localizable title;
    /**
     * Used to render a line in the trend chart.
     */
    public final Color color;
    /**
     * Updated every 10 seconds. Keep data up to 1 hour.
     */
    @Exported
    public final TimeSeries sec10;
    /**
     * Updated every 1 min. Keep data up to 1 day.
     */
    @Exported
    public final TimeSeries min;
    /**
     * Updated every 1 hour. Keep data up to 4 weeks.
     */
    @Exported
    public final TimeSeries hour;
    private int counter;

    public MultiStageTimeSeries(Localizable title, Color color, float initialValue, float decay) {
        this.title = title;
        this.color = color;
        this.sec10 = new TimeSeries(initialValue, decay, 6 * 60);
        this.min = new TimeSeries(initialValue, decay, 60 * 24);
        this.hour = new TimeSeries(initialValue, decay, 28 * 24);
    }

    /**
     * @deprecated since 2009-04-05.
     *      Use {@link #MultiStageTimeSeries(Localizable, Color, float, float)}
     */
    public MultiStageTimeSeries(float initialValue, float decay) {
        this(Messages._MultiStageTimeSeries_EMPTY_STRING(), Color.WHITE, initialValue, decay);
    }

    /**
     * Call this method every 10 sec and supply a new data point.
     */
    public void update(float f) {
        counter = (counter + 1) % 360;   // 1hour/10sec = 60mins/10sec=3600secs/10sec = 360
        sec10.update(f);
        if (counter % 6 == 0) {
            min.update(f);
        }
        if (counter == 0) {
            hour.update(f);
        }
    }

    /**
     * Selects a {@link TimeSeries}.
     */
    public TimeSeries pick(TimeScale timeScale) {
        switch (timeScale) {
            case HOUR:
                return hour;
            case MIN:
                return min;
            case SEC10:
                return sec10;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Gets the most up-to-date data point value.
     */
    public float getLatest(TimeScale timeScale) {
        return pick(timeScale).getLatest();
    }

    public Api getApi() {
        return new Api(this);
    }

    /**
     * Choose which datapoint to use.
     */
    public enum TimeScale {

        SEC10(TimeUnit2.SECONDS.toMillis(10)),
        MIN(TimeUnit2.MINUTES.toMillis(1)),
        HOUR(TimeUnit2.HOURS.toMillis(1));
        /**
         * Number of milliseconds (10 secs, 1 min, and 1 hour)
         * that this constant represents.
         */
        public final long tick;

        TimeScale(long tick) {
            this.tick = tick;
        }

        /**
         * Creates a new {@link DateFormat} suitable for processing
         * this {@link TimeScale}.
         */
        public DateFormat createDateFormat() {
            switch (this) {
                case HOUR:
                    return new SimpleDateFormat("MMM/dd HH");
                case MIN:
                    return new SimpleDateFormat("HH:mm");
                case SEC10:
                    return new SimpleDateFormat("HH:mm:ss");
                default:
                    throw new AssertionError();
            }
        }

        /**
         * Parses the {@link TimeScale} from the query parameter.
         */
        public static TimeScale parse(String type) {
            if (type == null) {
                return TimeScale.MIN;
            }
            return Enum.valueOf(TimeScale.class, type.toUpperCase(Locale.ENGLISH));
        }
    }

    /**
     * Represents the trend chart that consists of several {@link MultiStageTimeSeries}.
     *
     * <p>
     * This object is renderable as HTTP response.
     */
    public static final class TrendChart implements HttpResponse {

        public final TimeScale timeScale;
        public final List<MultiStageTimeSeries> series;
        public final DataSet dataset;

        public TrendChart(TimeScale timeScale, MultiStageTimeSeries... series) {
            this.timeScale = timeScale;
            this.series = new ArrayList<MultiStageTimeSeries>(Arrays.asList(series));
            this.dataset = createDataset();
        }

        /**
         * Creates a {@link DefaultCategoryDataset} for rendering a graph from a set of {@link MultiStageTimeSeries}.
         */
        private DataSet createDataset() {
            float[][] dataPoints = new float[series.size()][];
            for (int i = 0; i < series.size(); i++) {
                dataPoints[i] = series.get(i).pick(timeScale).getHistory();
            }

            int dataLength = dataPoints[0].length;
            for (float[] dataPoint : dataPoints) {
                assert dataLength == dataPoint.length;
            }

            DataSet<String, String> ds = new DataSet<String, String>();

            DateFormat format = timeScale.createDateFormat();

            Date date = new Date(System.currentTimeMillis() - timeScale.tick * dataLength);
            for (int i = dataLength - 1; i >= 0; i--) {
                date = new Date(date.getTime() + timeScale.tick);
                String timeStr = format.format(date);
                for (int j = 0; j < dataPoints.length; j++) {
                   ds.add(dataPoints[j][i], series.get(j).title.toString(), timeStr);
                }
            }
            return ds;
        }
        
        public Graph createGraph(){
            Graph graph = new Graph(-1, 500, 400);
            graph.setXAxisLabel("");
            graph.setData(createDataset());
            graph.setChartType(Graph.TYPE_LINE); 
            graph.setMultiStageTimeSeries(series);
            return graph;
        }

        /**
         * Renders this object as an image.
         */
        public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
            createGraph().doPng(req, rsp); 
        }
    }

    public static TrendChart createTrendChart(TimeScale scale, MultiStageTimeSeries... data) {
        return new TrendChart(scale,data);
    }
}
