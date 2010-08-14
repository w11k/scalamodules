/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.BundleContext
import org.specs.Specification
import org.specs.mock.Mockito

class ServicesWatcherSpec extends Specification with Mockito {

  "Calling ServicesWatcher.withFilter" should {
    "throw an IllegalArgumentException given a null Filter" in {
      new ServicesWatcher(classOf[String], mock[BundleContext]) withFilter null must throwA[IllegalArgumentException]
    }
  }

  "Calling ServicesWatcher.andHandle" should {
    "throw an IllegalArgumentException given a null partial function to handle ServiceEvents" in {
      new ServicesWatcher(classOf[String], mock[BundleContext]).andHandle(null) must throwA[IllegalArgumentException]
    }
  }
}

class ServiceEventSpec extends Specification with Mockito {

  "Creating a ServiceEvent (subclass)" should {
    "throw an IllegalArgumentException given a null service" in {
      new AddingService(null, Map[String, Any]()) must throwA [IllegalArgumentException]
    }
    "throw an IllegalArgumentException given null service properties" in {
      new AddingService("", null) must throwA [IllegalArgumentException]
    }
  }
}
