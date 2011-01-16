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
