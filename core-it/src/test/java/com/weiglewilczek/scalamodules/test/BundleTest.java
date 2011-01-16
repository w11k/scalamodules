/*
 * Copyright 2009-2011 Weigle Wilczek GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
