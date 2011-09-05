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

package hudson.widgets;

import hudson.Functions;
import hudson.model.ModelObject;
import hudson.model.Run;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Displays the history of records (normally {@link Run}s) on the side panel.
 *
 * @param <O>
 *      Owner of the widget.
 * @param <T>
 *      Type individual record.
 * @author Kohsuke Kawaguchi
 */
public class HistoryWidget<O extends ModelObject,T> extends Widget {
    /**
     * The given data model of records. Newer ones first.
     */
    //TODO: review and check whether we can do it private
    public Iterable<T> baseList;

    /**
     * Indicates the next build number that client ajax should fetch.
     */
    private String nextBuildNumberToFetch;

    /**
     * URL of the {@link #owner}.
     */
    //TODO: review and check whether we can do it private
    public final String baseUrl;

    //TODO: review and check whether we can do it private
    public final O owner;

    private boolean trimmed;

    //TODO: review and check whether we can do it private
    public final Adapter<? super T> adapter;

    /**
     * First transient build record. Everything >= this will be discarded when AJAX call is made.
     */
    private String firstTransientBuildKey;

    /**
     * @param owner
     *      The parent model object that owns this widget.
     */
    public HistoryWidget(O owner, Iterable<T> baseList, Adapter<? super T> adapter) {
        this.adapter = adapter;
        this.baseList = baseList;
        this.baseUrl = Functions.getNearestAncestorUrl(Stapler.getCurrentRequest(),owner);
        this.owner = owner;
    }

    /**
     * Title of the widget.
     */
    public String getDisplayName() {
        return Messages.BuildHistoryWidget_DisplayName();
    }

    @Override
    public String getUrlName() {
        return "buildHistory";
    }

    public String getFirstTransientBuildKey() {
        return firstTransientBuildKey;
    }

    public Iterable<T> getBaseList() {
        return baseList;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public O getOwner() {
        return owner;
    }

    public Adapter<? super T> getAdapter() {
        return adapter;
    }

    private Iterable<T> updateFirstTransientBuildKey(Iterable<T> source) {
        String key=null;
        for (T t : source)
            if(adapter.isBuilding(t))
                key = adapter.getKey(t);
        firstTransientBuildKey = key;
        return source;
    }

    /**
     * The records to be rendered this time.
     */
    public Iterable<T> getRenderList() {
        if(trimmed) {
            List<T> lst;
            if (baseList instanceof List) {
                lst = (List<T>) baseList;
                if(lst.size()>THRESHOLD)
                    return updateFirstTransientBuildKey(lst.subList(0,THRESHOLD));
                trimmed=false;
                return updateFirstTransientBuildKey(lst);
            } else {
                lst = new ArrayList<T>(THRESHOLD);
                Iterator<T> itr = baseList.iterator();
                while(lst.size()<=THRESHOLD && itr.hasNext())
                    lst.add(itr.next());
                trimmed = itr.hasNext(); // if we don't have enough items in the base list, setting this to false will optimize the next getRenderList() invocation.
                return updateFirstTransientBuildKey(lst);
            }
        } else
            return updateFirstTransientBuildKey(baseList);
    }

    public boolean isTrimmed() {
        return trimmed;
    }

    public void setTrimmed(boolean trimmed) {
        this.trimmed = trimmed;
    }

    /**
     * Handles AJAX requests from browsers to update build history.
     *
     * @param n
     *      The build 'number' to fetch. This is string because various variants
     *      uses non-numbers as the build key.
     */
    public void doAjax( StaplerRequest req, StaplerResponse rsp,
                  @Header("n") String n ) throws IOException, ServletException {

        rsp.setContentType("text/html;charset=UTF-8");

        // pick up builds to send back
        List<T> items = new ArrayList<T>();

        String nn=null; // we'll compute next n here

        // list up all builds >=n.
        for (T t : baseList) {
            if(adapter.compare(t,n)>=0) {
                items.add(t);
                if(adapter.isBuilding(t))
                    nn = adapter.getKey(t); // the next fetch should start from youngest build in progress
            } else
                break;
        }

        if (nn==null) {
            if (items.isEmpty()) {
                // nothing to report back. next fetch should retry the same 'n'
                nn=n;
            } else {
                // every record fetched this time is frozen. next fetch should start from the next build
                nn=adapter.getNextKey(adapter.getKey(items.get(0)));
            }
        }

        baseList = items;

        rsp.setHeader("n",nn);
        firstTransientBuildKey = nn; // all builds >= nn should be marked transient

        req.getView(this,"ajaxBuildHistory.jelly").forward(req,rsp);
    }

    private static final int THRESHOLD = 30;

    public String getNextBuildNumberToFetch() {
        return nextBuildNumberToFetch;
    }

    public void setNextBuildNumberToFetch(String nextBuildNumberToFetch) {
        this.nextBuildNumberToFetch = nextBuildNumberToFetch;
    }

    public interface Adapter<T> {
        /**
         * If record is newer than the key, return a positive number.
         */
        int compare(T record, String key);
        String getKey(T record);
        boolean isBuilding(T record);
        String getNextKey(String key);
    }
}
