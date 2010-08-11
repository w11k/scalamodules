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

class ServicesFinderSpec extends Specification with Mockito {

  "Creating a ServicesFinder" should {
    val interface = classOf[TestInterface1]
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null service interface" in {
      new ServicesFinder(null, context) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null BundleContext" in {
      new ServicesFinder(interface, null) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null Filter" in {
      new ServicesFinder(interface, context, null) must throwA[IllegalArgumentException]
    }
  }

  "Calling ServicesFinder.withFilter" should {
    val interface = classOf[TestInterface1]
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null Filter" in {
      new ServicesFinder(interface, context) withFilter null must throwA[IllegalArgumentException]
    }
  }

  "Calling ServicesFinder.andApply" should {
    val context = mock[BundleContext]
    val serviceReference = mock[ServiceReference]
    val serviceReference2 = mock[ServiceReference]
    val interface = classOf[TestInterface1]
    val service = mock[TestInterface1]
    val service2 = mock[TestInterface1]
    "throw an IllegalArgumentException given a null function go be applied to the service" in {
      new ServicesFinder(interface, context) andApply (null: (TestInterface1 => Any)) must throwA[IllegalArgumentException]
    }
    "return Nil when there are no requested service references available" in {
      context.getServiceReferences(interface.getName, null) returns null
      val servicesFinder = new ServicesFinder(interface, context)
      servicesFinder andApply { _.name } mustBe Nil
    }
    "return Nil when there is a requested service references available but no service" in {
      context.getServiceReferences(interface.getName, null) returns Array(serviceReference)
      context.getService(serviceReference) returns null
      val servicesFinder = new ServicesFinder(interface, context)
      servicesFinder andApply { _.name } mustBe Nil
      there was one(context).ungetService(serviceReference)
    }
    "return a List with one element when there is one requested service reference with service available" in {
      context.getServiceReferences(interface.getName, "(&(a=1)(b=*))") returns Array(serviceReference)
      context.getService(serviceReference) returns service
      service.name returns "YES"
      val servicesFinder = new ServicesFinder(interface, context, Some(("a" === "1") && "b".present))
      val names = servicesFinder andApply { _.name }
      names mustEqual List("YES")
      there was one(context).ungetService(serviceReference)
    }
    "return a List with two elements when there are two requested service references with services available" in {
      context.getServiceReferences(interface.getName, null) returns Array(serviceReference, serviceReference2)
      context.getService(serviceReference) returns service
      context.getService(serviceReference2) returns service2
      service.name returns "YES"
      service2.name returns "NO"
      val servicesFinder = new ServicesFinder(interface, context)
      val names = servicesFinder andApply { (service, _) => service.name }
      names mustEqual List("YES", "NO")
      there was one(context).ungetService(serviceReference)
      there was one(context).ungetService(serviceReference2)
    }
  }
}
