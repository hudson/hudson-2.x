package hudson.util;

import java.util.Calendar;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see hudson.util.graph.Graph
 */
public class Graph extends hudson.util.graph.Graph {

    public Graph(long timestamp, int defaultW, int defaultH) {
        super(timestamp, defaultW, defaultH);
    }

    public Graph(Calendar timestamp, int defaultW, int defaultH) {
        super(timestamp, defaultW, defaultH);
    }
}
