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
