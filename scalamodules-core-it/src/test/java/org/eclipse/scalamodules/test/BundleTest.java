/**
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scalamodules.test;

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
        return options(
            provision(
                bundle("file:scalamodules-core/target/scala_2.8.0/scalamodules-core_2.8.0-2.0.jar"),
                wrappedBundle(bundle("file:scalamodules-core-it/lib_managed/scala_2.8.0/test/specs_2.8.0-1.6.5.jar")),
                wrappedBundle(bundle("file:project/boot/scala-2.8.0/lib/scala-library.jar"))
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
