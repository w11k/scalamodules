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
        String version = System.getProperty("scalamodules.version");
        return options(
            provision(
                bundle("file:core/target/scala_2.8.0/scalamodules-core_2.8.0-" + version + ".jar"),
                bundle("mvn:com.weiglewilczek.scala-lang-osgi/scala-library/2.8.0"),
                bundle("file:core/lib_managed/scala_2.8.0/compile/slf4s_2.8.0-1.0.0.jar"),
                bundle("file:core/lib_managed/scala_2.8.0/compile/slf4j-api-1.6.1.jar"),
                bundle("file:core/lib_managed/scala_2.8.0/test/slf4j-simple-1.6.1.jar"),
                wrappedBundle(bundle("file:core-it/lib_managed/scala_2.8.0/test/specs_2.8.0-1.6.5.jar"))
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
