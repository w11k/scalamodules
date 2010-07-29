/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.{ BundleContext, ServiceReference }
import org.specs.Specification
import org.specs.mock.Mockito

class ServiceFinderSpec extends Specification with Mockito {

  "Creating a ServiceFinder" should {
    val interface = classOf[TestInterface1]
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null service interface" in {
      new ServicesFinder(null)(context) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null BundleContext" in {
      new ServicesFinder(interface)(null) must throwA[IllegalArgumentException]
    }
  }

  "Calling ServiceFinder.andApply" should {
    val context = mock[BundleContext]
    val serviceReference = mock[ServiceReference]
    val interface = classOf[TestInterface1]
    val service = mock[TestInterface1]
    "throw an IllegalArgumentException given a null function go be applied to the service" in {
      new ServiceFinder(interface)(context) andApply (null: (TestInterface1 => Any)) must throwA[IllegalArgumentException]
    }
    "return None when there is no requested service reference available" in {
      context.getServiceReference(interface.getName) returns null
      val serviceFinder = new ServiceFinder(interface)(context)
      serviceFinder andApply { _.name } mustBe None
    }
    "return None when there is a requested service reference available but no service" in {
      context.getServiceReference(interface.getName) returns serviceReference
      context.getService(serviceReference) returns null
      val serviceFinder = new ServiceFinder(interface)(context)
      serviceFinder andApply { _.name } mustBe None
      there was one(context).ungetService(serviceReference)
    }
    "return Some when there is a requested service reference with service available" in {
      context.getServiceReference(interface.getName) returns serviceReference
      context.getService(serviceReference) returns service
      service.name returns "YES"
      val serviceFinder = new ServiceFinder(interface)(context)
      val names = serviceFinder andApply { _.name }
      names mustEqual Some("YES")
      there was one(context).ungetService(serviceReference)
    }
  }
}
