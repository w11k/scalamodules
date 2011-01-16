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

import org.osgi.framework.{ BundleContext, ServiceReference }
import org.specs.Specification
import org.specs.mock.Mockito

class ServiceFinderSpec extends Specification with Mockito {

  "Calling ServiceFinder.andApply" should {
    val context = mock[BundleContext]
    val serviceReference = mock[ServiceReference]
    val interface = classOf[TestInterface1]
    val service = mock[TestInterface1]
    "throw an IllegalArgumentException given a null function go be applied to the service" in {
      new ServiceFinder(interface, context) andApply (null: (TestInterface1 => Any)) must throwA[IllegalArgumentException]
    }
    "return None when there is no requested service reference available" in {
      context.getServiceReference(interface.getName) returns null
      val serviceFinder = new ServiceFinder(interface, context)
      serviceFinder andApply { _.name } mustBe None
    }
    "return None when there is a requested service reference available but no service" in {
      context.getServiceReference(interface.getName) returns serviceReference
      context.getService(serviceReference) returns null
      val serviceFinder = new ServiceFinder(interface, context)
      serviceFinder andApply { _.name } mustBe None
      there was one(context).ungetService(serviceReference)
    }
    "return Some when there is a requested service reference with service available" in {
      context.getServiceReference(interface.getName) returns serviceReference
      context.getService(serviceReference) returns service
      service.name returns "YES"
      val serviceFinder = new ServiceFinder(interface, context)
      val names = serviceFinder andApply { _.name }
      names mustEqual Some("YES")
      there was one(context).ungetService(serviceReference)
    }
  }
}
