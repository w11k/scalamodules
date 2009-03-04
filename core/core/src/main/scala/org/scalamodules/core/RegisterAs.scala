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

import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.scalamodules.util.jcl.Conversions.mapToJavaDictionary

/**
 * Provides service registration.
 */
class RegisterAs[T](context: BundleContext,
                    serviceInterface: Class[T],
                    properties: Map[String, Any]) {

  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")

  def this(context: BundleContext, 
           serviceInterface: Class[T]) {
    this(context, serviceInterface, null)
  }

  /**
   * Adds the given service properties.
   */
  def withProperties(properties: Map[String, Any]) =
    new RegisterAs(context, serviceInterface, properties)


  /**
   * Provides declaring a dependency.
   */
  def dependOn[S](dependee: Class[S]) = { 
    require(dependee != null, "Dependee on must not be null!")
    new DependOn(context, serviceInterface, properties, dependee)
  }

  /**
   * Registers the given service.
   */
  def theService(service: T) = {
    require(service != null, "Service to be registered must not be null!")
    context.registerService(serviceInterface.getName, service, properties)
  }
}
