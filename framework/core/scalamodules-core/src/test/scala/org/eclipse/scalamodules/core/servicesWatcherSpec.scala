/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Heiko Seeberger   - initial API and implementation
 *   Roman Roelofsen   - initial API and implementation
 *   Kjetil Valstadsve - initial API and implementation
 */
package org.eclipse.scalamodules.core

import org.osgi.framework.BundleContext
import org.specs.SpecificationWithJUnit
import org.specs.mock.Mockito

class ServicesWatcherSpec extends SpecificationWithJUnit with Mockito {

  "Creating a ServicesWatcher" should {
    val interface = classOf[TestInterface1]
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null service interface" in {
      new ServicesWatcher(null)(context) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null Filter" in {
      new ServicesWatcher(interface, null)(context) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null BundleContext" in {
      new ServicesWatcher(interface)(null) must throwA[IllegalArgumentException]
    }
  }

  "Calling ServicesWatcher.withFilter" should {
    val interface = classOf[TestInterface1]
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null Filter" in {
      new ServicesWatcher(interface)(context) withFilter null must throwA[IllegalArgumentException]
    }
  }

  "Calling ServicesWatcher.andHandle" should {
    "throw an IllegalArgumentException given a null partial function to handle ServiceEvents" in {
      new ServicesWatcher(classOf[TestInterface1])(mock[BundleContext]).andHandle(null) must throwA[IllegalArgumentException]
    }
  }
}
