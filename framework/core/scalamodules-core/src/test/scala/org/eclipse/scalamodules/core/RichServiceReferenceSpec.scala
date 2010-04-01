/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scalamodules
package core

import org.mockito.{ ArgumentCaptor, Matchers }
import org.mockito.Mockito._
import org.osgi.framework.ServiceReference
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

@org.junit.runner.RunWith(classOf[JUnitRunner])
class RichServiceReferenceSpec extends WordSpec with ShouldMatchers with MockitoSugar {

  "Creating a RichServiceReference" when {
    "the given ServiceReference is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new RichServiceReference(null) } should produce [IllegalArgumentException]
      }
    }
  }

  "Calling RichServiceReference.properties" when {
    "there are no service properties" should {
      "return the empty Map" in {
        val serviceReference = mock[ServiceReference]
        when(serviceReference.getPropertyKeys) thenReturn Array[String]()
        new RichServiceReference(serviceReference).properties should be (Map.empty)
      }
    }
    "there are service properties a=1 and b=b" should {
      "return a Map containing (a -> a) and (b -> b)" in {
        val serviceReference = mock[ServiceReference]
        when(serviceReference.getPropertyKeys).thenReturn(Array("a", "b"))
        when(serviceReference getProperty "a").thenReturn("a", "a")
        when(serviceReference getProperty "b").thenReturn("b", "b")
        new RichServiceReference(serviceReference).properties should be (Map("a" -> "a", "b" -> "b"))
      }
    }
  }
}
