/**
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(JUnit4TestRunner.class)
public class BundleTest {

    @Inject
    public BundleContext context;

    @Configuration
    public static Option[] configuration() {

        String scalaModulesVersion = System.getProperty("scalaModules.version");
        String scalaVersion = System.getProperty("scala.version");
        String slf4jVersion = System.getProperty("slf4j.version");
        String slf4sVersion = System.getProperty("slf4s.version");
        String specsVersion = System.getProperty("specs.version");

        return options(
            provision(
                bundle(String.format("file:core/target/scala_%s/scalamodules-core_%s-%s.jar", scalaVersion, scalaVersion, scalaModulesVersion)),
                bundle(String.format("mvn:http://scala-tools.org/repo-releases!com.weiglewilczek.scala-lang-osgi/scala-library/%s", scalaVersion)),
                bundle(String.format("file:core/lib_managed/scala_%s/compile/slf4j-api-%s.jar", scalaVersion, slf4jVersion)),
                bundle(String.format("file:core/lib_managed/scala_%s/test/slf4j-simple-%s.jar", scalaVersion, slf4jVersion)),
                bundle(String.format("file:core/lib_managed/scala_%s/compile/slf4s_%s-%s.jar", scalaVersion, scalaVersion, slf4sVersion)),
                wrappedBundle(bundle(String.format("file:core-it/lib_managed/scala_%s/test/specs_%s-%s.jar", scalaVersion, scalaVersion, specsVersion)))
            )
        );
    }

    @Test
    public void test() {
        Assert.assertNotNull("The bundle context was NOT injected!", context);
        final BundleSpec spec = new BundleSpec(context);
        spec.test();
    }
}
