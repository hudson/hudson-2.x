package org.jvnet.hudson.maven.plugins.hpi;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Collection;
import java.util.Set;
import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {

    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }

    public Collection<String> supportedAnnotationTypes() {
        return Collections.singletonList("*");
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set, final AnnotationProcessorEnvironment annotationProcessorEnvironment) {
        return AnnotationProcessors.getCompositeAnnotationProcessor(
            new AnnotationProcessor() {
                public void process() {
                    System.out.println("Yay!");
                }
            },
            new org.kohsuke.stapler.AnnotationProcessorFactoryImpl().getProcessorFor(set,annotationProcessorEnvironment)
        );
    }
}
