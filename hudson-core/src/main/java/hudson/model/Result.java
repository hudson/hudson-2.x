/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
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

import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import hudson.Extension;
import hudson.cli.declarative.OptionHandlerExtension;
import hudson.util.EditDistance;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.*;
import org.kohsuke.stapler.export.CustomExportedBean;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The build outcome.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Result implements Serializable, CustomExportedBean {
    /**
     * The build had no errors.
     */
    public static final Result SUCCESS = new Result("SUCCESS",BallColor.BLUE,0);
    /**
     * The build had some errors but they were not fatal.
     * For example, some tests failed.
     */
    public static final Result UNSTABLE = new Result("UNSTABLE",BallColor.YELLOW,1);
    /**
     * The build had a fatal error.
     */
    public static final Result FAILURE = new Result("FAILURE",BallColor.RED,2);
    /**
     * The module was not built.
     * <p>
     * This status code is used in a multi-stage build (like maven2)
     * where a problem in earlier stage prevented later stages from building.
     */
    public static final Result NOT_BUILT = new Result("NOT_BUILT",BallColor.GREY,3);
    /**
     * The build was manually aborted.
     */
    public static final Result ABORTED = new Result("ABORTED",BallColor.ABORTED,4);

    private final String name;

    /**
     * Bigger numbers are worse.
     */
    //TODO: review and check whether we can do it private
    public final int ordinal;

    /**
     * Default ball color for this status.
     */
    //TODO: review and check whether we can do it private
    public final BallColor color;

    private Result(String name, BallColor color, int ordinal) {
        this.name = name;
        this.color = color;
        this.ordinal = ordinal;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public BallColor getColor() {
        return color;
    }


    /**
     * Combines two {@link Result}s and returns the worse one.
     */
    public Result combine(Result that) {
        if(this.ordinal < that.ordinal)
            return that;
        else
            return this;
    }

    public boolean isWorseThan(Result that) {
        return this.ordinal > that.ordinal;
    }

    public boolean isWorseOrEqualTo(Result that) {
        return this.ordinal >= that.ordinal;
    }

    public boolean isBetterThan(Result that) {
        return this.ordinal < that.ordinal;
    }

    public boolean isBetterOrEqualTo(Result that) {
        return this.ordinal <= that.ordinal;
    }


    @Override
    public String toString() {
        return name;
    }

    public String toExportedObject() {
        return name;
    }
    
    public static Result fromString(String s) {
        for (Result r : all)
            if (s.equalsIgnoreCase(r.name))
                return r;
        return FAILURE;
    }

    private static List<String> getNames() {
        List<String> l = new ArrayList<String>();
        for (Result r : all)
            l.add(r.name);
        return l;
    }

    // Maintain each Result as a singleton deserialized (like build result from a slave node)
    private Object readResolve() {
        for (Result r : all)
            if (ordinal==r.ordinal)
                return r;
        return FAILURE;
    }

    private static final long serialVersionUID = 1L;

    private static final Result[] all = new Result[] {SUCCESS,UNSTABLE,FAILURE,NOT_BUILT,ABORTED};

    public static final SingleValueConverter conv = new AbstractSingleValueConverter () {
        public boolean canConvert(Class clazz) {
            return clazz==Result.class;
        }

        public Object fromString(String s) {
            return Result.fromString(s);
        }
    };

    @OptionHandlerExtension
    public static final class OptionHandlerImpl extends OptionHandler<Result> {
        public OptionHandlerImpl(CmdLineParser parser, OptionDef option, Setter<? super Result> setter) {
            super(parser, option, setter);
        }

        @Override
        public int parseArguments(Parameters params) throws CmdLineException {
            String param = params.getParameter(0);
            Result v = fromString(param.replace('-', '_'));
            if (v==null)
                throw new CmdLineException(owner,"No such status '"+param+"'. Did you mean "+
                        EditDistance.findNearest(param.replace('-', '_').toUpperCase(), getNames()));
            setter.addValue(v);
            return 1;
        }

        @Override
        public String getDefaultMetaVariable() {
            return "STATUS";
        }
    }
}
