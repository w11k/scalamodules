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
class ServiceEventSpec extends WordSpec with ShouldMatchers with MockitoSugar {

  "Creating a ServiceEvent (subclass)" when {

    "the given service is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new AddingService(null, Map[String, Any]()) } should produce [IllegalArgumentException]
      }
    }

    "the given service properteis are null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new AddingService(new TestClass1, null) } should produce [IllegalArgumentException]
      }
    }
  }
}

@org.junit.runner.RunWith(classOf[JUnitRunner])
class ServicesWatcherSpec extends WordSpec with ShouldMatchers with MockitoSugar {

  "Creating a ServicesWatcher" when {

    "the given service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new ServicesWatcher(null)(mock[BundleContext]) } should produce [IllegalArgumentException]
      }
    }

    "the given BundleContext is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new ServicesWatcher(classOf[TestInterface1])(null) } should produce [IllegalArgumentException]
      }
    }
  }

  "Calling ServicesWatcher.andHandle" when {

    "the given partial function to handle ServiceEvents is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new ServicesWatcher(classOf[TestInterface1])(mock[BundleContext]).andHandle(null) } should produce [IllegalArgumentException]
      }
    }
  }
}
