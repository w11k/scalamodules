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
package org.eclipse.scalamodules
package core

import org.mockito.Mockito._
import org.osgi.framework.{ BundleContext, ServiceReference }
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

@org.junit.runner.RunWith(classOf[JUnitRunner])
class ServiceFinderSpec extends WordSpec with ShouldMatchers with MockitoSugar {

  // ServiceFinder

  "Creating a ServiceFinder" when {

    "the given service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new ServiceFinder(null)(mock[BundleContext]) } should produce [IllegalArgumentException]
      }
    }

    "the given BundleContext is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new ServiceFinder(classOf[TestInterface1])(null) } should produce [IllegalArgumentException]
      }
    }
  }

  "Calling ServiceFinder.andApply" when {

    "the given function to be applied to the service is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new ServiceFinder(classOf[TestInterface1])(mock[BundleContext]) andApply (null: (TestInterface1 => Any))
        } should produce [IllegalArgumentException]
      }
    }

    "the given function to be applied to the service is not-null and there is no TestInterface1 service reference available" should {
      "result in the proper methods called on the BundleContext and return None" in {
        val context = mock[BundleContext]
        when(context.getServiceReference(classOf[TestInterface1].getName)).thenReturn(null)
        val serviceFinder = new ServiceFinder(classOf[TestInterface1])(context)
        serviceFinder andApply { _.name } should be (None)
      }
    }

    "the given function to be applied to the service is not-null and there is a TestInterface1 service reference available but no service" should {
      "result in the proper methods called on the BundleContext and return None" in {
        val context = mock[BundleContext]
        val serviceReference = mock[ServiceReference]
        when(context.getServiceReference(classOf[TestInterface1].getName)).thenReturn(serviceReference)
        val service: TestInterface1 = null
        when(context.getService(serviceReference)).thenReturn(service, service)  // TODO Can we get rid of this double arg?
        val serviceFinder = new ServiceFinder(classOf[TestInterface1])(context)
        serviceFinder andApply { _.name } should be (None)
        verify(context).ungetService(serviceReference)
      }
    }

    "the given function to be applied to the service is not-null and there is a TestInterface1 service reference and service available" should {
      "result in the proper methods called on the BundleContext and return Some()" in {
        val yes = "YES"
        val context = mock[BundleContext]
        val serviceReference = mock[ServiceReference]
        when(context.getServiceReference(classOf[TestInterface1].getName)).thenReturn(serviceReference)
        val service = mock[TestInterface1]
        when(context.getService(serviceReference)).thenReturn(service, service)  // TODO Can we get rid of this double arg?
        when(service.name).thenReturn(yes)
        val serviceFinder = new ServiceFinder(classOf[TestInterface1])(context)
        serviceFinder andApply { _.name } should be (Some(yes))
        serviceFinder andApply { (service, _) => service.name } should be (Some(yes))
        verify(context, times(2)).ungetService(serviceReference)
      }
    }
  }
}
