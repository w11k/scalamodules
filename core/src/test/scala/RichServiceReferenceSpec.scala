/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.ServiceReference
import org.specs.Specification
import org.specs.mock.Mockito

class RichServiceReferenceSpec extends Specification with Mockito {

  "Calling RichServiceReference.properties" should {
    val serviceReference = mock[ServiceReference]
    "return the empty Map given there are no service properties" in {
      serviceReference.getPropertyKeys returns Array[String]()
      new RichServiceReference(serviceReference).properties mustEqual Map.empty
    }
    "return a Map containing (a -> a) and (b -> b) given there are service properties a=1 and b=b" in {
      serviceReference.getPropertyKeys returns Array("a", "b")
      serviceReference getProperty "a" returns "a"
      serviceReference getProperty "b" returns "b"
      new RichServiceReference(serviceReference).properties mustEqual Map("a" -> "a", "b" -> "b")
    }
  }
}
