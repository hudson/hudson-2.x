/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        
 *
 *******************************************************************************/ 

package net.java.sezpoz;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.sezpoz.impl.SerAnnotatedElement;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

/**
 * Represents an index of a single annotation.
 * Indices are <em>not</em> automatically cached
 * (but reading them should be pretty cheap anyway).
 * @param T the type of annotation to load
 * @param I the type of instance which will be created
 */
public final class SpaceIndex<T extends Annotation, I> implements Iterable<SpaceIndexItem<T,I>> {

    private static final Logger LOGGER = Logger.getLogger(SpaceIndex.class.getName());

    /**
     * Load an index for a given annotation type.
     * @param annotation the type of annotation to find
     * @param instanceType the type of instance to be created (use {@link Void} if all instances will be null)
     * @param space a class space in which to find the index and any annotated classes
     * @param globalIndex search the entire classloader hierarchy?
     * @return an index of all elements known to be annotated with it
     * @throws IllegalArgumentException if the annotation type is not marked with {@link Indexable}
     *                                  or the instance type is not equal to or a supertype of the annotation's actual {@link Indexable#type}
     */
    public static <T extends Annotation,I> SpaceIndex<T,I> load(Class<T> annotation, Class<I> instanceType, ClassSpace space, boolean globalIndex) throws IllegalArgumentException {
        return new SpaceIndex<T,I>(annotation, instanceType, space, globalIndex);
    }

    private final boolean globalIndex;
    private final Class<T> annotation;
    private final Class<I> instanceType;
    private final ClassSpace space;

    private SpaceIndex(Class<T> annotation, Class<I> instance, ClassSpace space, boolean globalIndex) {
        this.globalIndex = globalIndex;
        this.annotation = annotation;
        this.instanceType = instance;
        this.space = space;
    }

    /**
     * Find all items in the index.
     * Calls to iterator methods may fail with {@link IndexError}
     * as the index is parsed lazily.
     * @return an iterator over items in the index
     */
    public Iterator<SpaceIndexItem<T,I>> iterator() {
        return new LazyIndexIterator();
    }

    /**
     * Lazy iterator. Opens and parses annotation streams only on demand.
     */
    private final class LazyIndexIterator implements Iterator<SpaceIndexItem<T,I>> {

        private Enumeration<URL> resources;
        private ObjectInputStream ois;
        private URL resource;
        private SpaceIndexItem<T,I> next;
        private boolean end;
        private final Set<String> loadedMembers = new HashSet<String>();

        public LazyIndexIterator() {
            if (LOGGER.isLoggable(Level.FINE)) {
                String urls;
                if (space instanceof URLClassSpace) {
                    urls = " " + Arrays.toString(((URLClassSpace) space).getURLs());
                } else {
                    urls = "";
                }
                LOGGER.log(Level.FINE, "Searching for indices of {0} in {1}{2}", new Object[] {annotation, space, urls});
            }
        }

        private void peek() throws IndexError {
            try {
                for (int iteration = 0; true; iteration++) {
                    if (iteration == 9999) {
                        LOGGER.log(Level.WARNING, "possible endless loop getting index for {0} from {1}", new Object[] {annotation, space});
                    }
                    if (next != null || end) {
                        return;
                    }
                    if (ois == null) {
                        if (resources == null) {
                            if (globalIndex) {
                                resources = space.getResources("META-INF/annotations/" + annotation.getName());
                            } else {
                                resources = space.findEntries("META-INF/annotations/", annotation.getName(), false);
                            }
                        }
                        if (!resources.hasMoreElements()) {
                            // Exhausted all streams.
                            end = true;
                            return;
                        }
                        resource = resources.nextElement();
                        LOGGER.log(Level.FINE, "Loading index from {0}", resource);
                        ois = new ObjectInputStream(resource.openStream());
                    }
                    SerAnnotatedElement el = (SerAnnotatedElement) ois.readObject();
                    if (el == null) {
                        // Skip to next stream.
                        ois.close();
                        ois = null;
                        continue;
                    }
                    String memberName = el.isMethod ? el.className + '#' + el.memberName + "()" :
                        el.memberName != null ? el.className + '#' + el.memberName :
                            el.className;
                    if (!loadedMembers.add(memberName)) {
                        // Already encountered this element, so skip it.
                        LOGGER.log(Level.FINE, "Already loaded index item {0}", el);
                        continue;
                    }
                    // XXX JRE #6865375 would make loader param accurate for duplicated modules
                    next = new SpaceIndexItem<T,I>(el, annotation, instanceType, space, resource);
                    break;
                }
            } catch (Exception x) {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException x2) {
                        LOGGER.log(Level.WARNING, null, x2);
                    }
                }
                throw new IndexError(x);
            }
        }

        public boolean hasNext() {
            peek();
            return !end;
        }

        public SpaceIndexItem<T,I> next() {
            peek();
            if (!end) {
                assert next != null;
                SpaceIndexItem<T,I> _next = next;
                next = null;
                return _next;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
