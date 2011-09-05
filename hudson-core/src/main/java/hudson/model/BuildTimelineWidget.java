/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.model;

import hudson.Functions;
import hudson.util.RunList;
import java.util.TimeZone;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.kohsuke.stapler.QueryParameter;

/**
 * UI widget for showing the SMILE timeline control.
 *
 * <p>
 * Return this from your "getTimeline" method.
 *
 * @author Kohsuke Kawaguchi, Winston Prakash
 * @since 1.372
 */
public class BuildTimelineWidget {

     
    protected final RunList<?> builds;

    public BuildTimelineWidget(RunList<?> builds) {
        this.builds = builds;
    }

    public Run<?, ?> getFirstBuild() {
        return builds.getFirstBuild();
    }

    public Run<?, ?> getLastBuild() {
        return builds.getLastBuild();
    }

    /**
     * Get timezone offset with Daylight time saving support
     *
     * @return int value
     */
    public int getTimeZoneOffset() {
        return TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000;
    }

    public TimelineEventList doData(StaplerRequest req, @QueryParameter long min, @QueryParameter long max) throws IOException {
        TimelineEventList result = new TimelineEventList();
        for (Run r : builds.byTimestamp(min, max)) {
            Event e = new Event();
            e.start = r.getTime();
            e.end = new Date(r.timestamp + r.getDuration());
            e.title = r.getFullDisplayName();
            // what to put in the description?
            // e.description = "Longish description of event "+r.getFullDisplayName();
            // e.durationEvent = true;
            e.link = req.getContextPath() + '/' + r.getUrl();
            BallColor c = r.getIconColor();
            e.color = String.format("#%06X", c.getBaseColor().darker().getRGB() & 0xFFFFFF);
            e.classname = "event-" + c.noAnime().toString() + " " + (c.isAnimated() ? "animated" : "");
            result.add(e);
        }
        return result;
    }
    
    /**
     * List of {@link Event} that the timeline component will display.
     */
    private static class TimelineEventList extends ArrayList<Event> implements HttpResponse {

        /**
         * Renders HTTP response.
         */
        public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
            // Date needs to be converted into iso-8601 date format.
            JsonConfig config = new JsonConfig();
            config.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {

                public synchronized Object processArrayValue(Object value, JsonConfig jsonConfig) {
                    if (value != null){
                       DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss 'GMT'Z", Functions.getClientLocale());
                       return dateFormat.format(value);
                    }
                    return null;
                }

                public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) {
                    return processArrayValue(value, jsonConfig);
                }
            });

            JSONObject o = new JSONObject();
            o.put("events", JSONArray.fromObject(this, config));
            rsp.setContentType("application/javascript;charset=UTF-8");
            o.write(rsp.getWriter());
        }
    }

    /**
     * Event data to be rendered on timeline.
     * See http://code.google.com/p/simile-widgets/wiki/Timeline_EventSources
    
     * <p>
     * This is bound to JSON and sent to the client-side JavaScript.
     */
    public static class Event {
        //TODO: review and check whether we can do it private
        public Date start;

        public Date end;
        public String title, description;
        /**
         * If true, the event occurs over a time duration. No icon. The event will be
         * drawn as a dark blue tape. The tape color is set with the color attribute.
         * Default color is #58A0DC
         *
         * If false (default), the event is focused on a specific "instant" (shown with the icon).
         * The event will be drawn as a blue dot icon (default) with a pale blue tape.
         * The tape is the default color (or color attribute color), with opacity
         * set to 20. To change the opacity, change the theme's instant: {impreciseOpacity: 20}
         * value. Maximum 100.
         */
        public Boolean durationEvent;
        /**
         * Url. The bubble's title text be a hyper-link to this address.
         */
        public String link;
        /**
         * Color of the text and tape (duration events) to display in the timeline.
         * If the event has durationEvent = false, then the bar's opacity will
         * be applied (default 20%). See durationEvent, above.
         */
        public String color;
        /**
         * CSS class name.
         */
        public String classname;

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Boolean getDurationEvent() {
            return durationEvent;
        }

        public String getLink() {
            return link;
        }

        public String getColor() {
            return color;
        }

        public String getClassname() {
            return classname;
        }
    }
}
