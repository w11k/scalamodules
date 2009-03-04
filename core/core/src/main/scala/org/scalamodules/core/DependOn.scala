/**
 * Copyright 2009 Heiko Seeberger and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalamodules.core

import scala.collection.Map
import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.scalamodules.core._
import org.scalamodules.core.RichBundleContext.fromBundleContext
import org.scalamodules.util.jcl.Conversions.mapToJavaDictionary

/**
 * Provides declaring a dependency.
 */
class DependOn[T, S](context: BundleContext,
                     serviceInterface: Class[T],
                     properties: Map[String, Any],
                     dependeeInterface: Class[S]) {
  
  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")
  require(dependeeInterface != null, "Dependee interface must not be null!")

  /**
   * Registers the service created by the given factory function.
   */
  def theService(f: S => T) = {
    require(f != null, "Factory function must not be null!")
    context track dependeeInterface onEvent {
      case AddingEvent(dependee, dependeeProperties)   => registration = 
        context.registerService(serviceInterface.getName, f(dependee), properties)
      case ModifiedEvent(dependee, dependeeProperties) => // Nothing!
      case RemovedEvent(dependee, dependeeProperties)  =>  registration.unregister()
    }
  }

  private var registration: ServiceRegistration = _
}
