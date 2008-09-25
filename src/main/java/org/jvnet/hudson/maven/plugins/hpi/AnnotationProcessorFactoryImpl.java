package org.jvnet.hudson.maven.plugins.hpi;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.apt.Filer.Location;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set, final AnnotationProcessorEnvironment env) {
        return AnnotationProcessors.getCompositeAnnotationProcessor(
            /**
             * Marks the class that extends Plugin
             */
            new AnnotationProcessor() {
                public void process() {
                    try {
                        for( TypeDeclaration d : env.getTypeDeclarations() ) {
                            if(!(d instanceof ClassDeclaration))    continue;

                            ClassDeclaration cd = (ClassDeclaration) d;
                            //if(cd.getModifiers().contains(Modifier.ABSTRACT))
                            //    continue;   // ignore abstract classes from indices

                            ClassType sc = cd.getSuperclass();
                            if(sc==null)    continue;   // be robust against compile errors
                            ClassDeclaration sd = sc.getDeclaration();
                            if(sd==null)    continue;
                            if(sd.getQualifiedName().equals("hudson.Plugin")) {
                                write(cd);
                            }
                        }
                    } catch (IOException e) {
                        env.getMessager().printError(e.getMessage());
                    }
                }
                private void write(ClassDeclaration c) throws IOException {
                    File f = new File("META-INF/services/hudson.Plugin");
                    OutputStream os = env.getFiler().createBinaryFile(Location.CLASS_TREE,"", f);

                    Writer w = new OutputStreamWriter(os,"UTF-8");
                    w.write(c.getQualifiedName());
                    w.close();
                }
            },
            new org.kohsuke.stapler.AnnotationProcessorFactoryImpl().getProcessorFor(set,env)
        );
    }
}
