/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

package hudson.console;

import hudson.MarkupText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Annotates one line of console output.
 *
 * <p>
 * In Hudson, console output annotation is done line by line, and
 * we model this as a state machine &mdash;
 * the code encapsulates some state, and it uses that to annotate one line (and possibly update the state.)
 *
 * <p>
 * A {@link ConsoleAnnotator} instance encapsulates this state, and the {@link #annotate(Object, MarkupText)}
 * method is used to annotate the next line based on the current state. The method returns another
 * {@link ConsoleAnnotator} instance that represents the altered state for annotating the next line.
 *
 * <p>
 * {@link ConsoleAnnotator}s are run when a browser requests console output, and the above-mentioned chain
 * invocation is done for each client request separately. Therefore, logically you can think of this process as:
 *
 * <pre>
 * ConsoleAnnotator ca = ...;
 * ca.annotate(context,line1).annotate(context,line2)...
 * </pre>
 *
 * <p>
 * Because of a browser can request console output incrementally, in addition to above a console annotator
 * can be serialized at any point and deserialized back later to continue annotation where it left off.
 *
 * <p>
 * {@link ConsoleAnnotator} instances can be created in a few different ways. See {@link ConsoleNote}
 * and {@link ConsoleAnnotatorFactory}.
 *
 * @author Kohsuke Kawaguchi
 * @see ConsoleAnnotatorFactory
 * @see ConsoleNote
 * @since 1.349
 */
public abstract class ConsoleAnnotator<T> implements Serializable {
    /**
     * Annotates one line.
     *
     * @param context
     *      The object that owns the console output. Never null.
     * @param text
     *      Contains a single line of console output, and defines convenient methods to add markup.
     *      The callee should put markup into this object. Never null.
     * @return
     *      The {@link ConsoleAnnotator} object that will annotate the next line of the console output.
     *      To indicate that you are not interested in the following lines, return null.
     */
    public abstract ConsoleAnnotator annotate(T context, MarkupText text );

    /**
     * Cast operation that restricts T.
     */
    public static <T> ConsoleAnnotator<T> cast(ConsoleAnnotator<? super T> a) {
        return (ConsoleAnnotator)a;
    }

    /**
     * Bundles all the given {@link ConsoleAnnotator} into a single annotator.
     */
    public static <T> ConsoleAnnotator<T> combine(Collection<? extends ConsoleAnnotator<? super T>> all) {
        switch (all.size()) {
        case 0:     return null;    // none
        case 1:     return  cast(all.iterator().next()); // just one
        }

        class Aggregator extends ConsoleAnnotator<T> {
            List<ConsoleAnnotator<T>> list;

            Aggregator(Collection list) {
                this.list = new ArrayList<ConsoleAnnotator<T>>(list);
            }

            public ConsoleAnnotator annotate(T context, MarkupText text) {
                ListIterator<ConsoleAnnotator<T>> itr = list.listIterator();
                while (itr.hasNext()) {
                    ConsoleAnnotator a =  itr.next();
                    ConsoleAnnotator b = a.annotate(context,text);
                    if (a!=b) {
                        if (b==null)    itr.remove();
                        else            itr.set(b);
                    }
                }

                switch (list.size()) {
                case 0:     return null;    // no more annotator left
                case 1:     return list.get(0); // no point in aggregating
                default:    return this;
                }
            }
        }
        return new Aggregator(all);
    }

    /**
     * Returns the all {@link ConsoleAnnotator}s for the given context type aggregated into a single
     * annotator.
     */
    public static <T> ConsoleAnnotator<T> initial(T context) {
        return combine(_for(context));
    }

    /**
     * List all the console annotators that can work for the specified context type.
     */
    public static <T> List<ConsoleAnnotator<T>> _for(T context) {
        List<ConsoleAnnotator<T>> r  = new ArrayList<ConsoleAnnotator<T>>();
        for (ConsoleAnnotatorFactory f : ConsoleAnnotatorFactory.all()) {
            if (f.type().isInstance(context)) {
                ConsoleAnnotator ca = f.newInstance(context);
                if (ca!=null)
                    r.add(ca);
            }
        }
        return r;
    }

    private static final long serialVersionUID = 1L;
}
