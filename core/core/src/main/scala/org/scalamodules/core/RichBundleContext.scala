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
import org.osgi.framework.{BundleContext, ServiceReference}
import org.scalamodules.core.RichServiceReference.fromServiceReference
  
/**
 * Companion object for RichBundleContext.
 */
object RichBundleContext {

  /**
   * Implicitly converts the given BundleContext to RichBundleContext.
   */
  implicit def fromBundleContext(context: BundleContext) = 
    new RichBundleContext(context) 
}

/**
 * Rich wrapper for BundleContext: 
 * Makes service handling more convenient and enables the ScalaModules DSL.
 */
class RichBundleContext(context: BundleContext) {
  
  require(context != null, "Bundle context must not be null!")

  /**
   * Provides service registration.
   */
  def registerAs[T](serviceInterface: Class[T]) =
    new RegisterAs[T](context, serviceInterface)

  /**
   * Provides service consumption for a single service.
   */
  def getOne[T](serviceInterface: Class[T]) =
    new GetOne[T](context, serviceInterface)

  /**
   * Provides service consumption for multiple service.
   */
  def getMany[T](serviceInterface: Class[T]) =
    new GetMany[T](context, serviceInterface)

  /**
   * Provides service tracking. 
   */
  def track[T](serviceInterface: Class[T]) =
    new Track[T](context, serviceInterface)

  /**
   * Gets the service for the given service reference 
   * and applies the given function if the service exists.
   */
  def applyWithRef[T, S](ref: ServiceReference, 
                         f: T => S): Option[S] = {
    assert(ref != null, "ServiceReference must not be null!")
    assert(f != null, "Function to be applied must not be null!")
    try {
      context.getService(ref) match {  // Might be null even if ref is not null
        case null       => None
        case service: T => Some(f(service))
      }
    } finally context.ungetService(ref)  // Must be called
  }

  /**
   * Gets the service and its properties for the given service reference 
   * and applies the give function if the service exists.
   */
  def applyWithRef[T, S](ref: ServiceReference,
                         f: (T, Map[String, AnyRef]) => S): Option[S] = {
    assert(ref != null, "ServiceReference must not be null!")
    assert(f != null, "Function to be applied must not be null!")
    try {
      context.getService(ref) match {  // Might be null even if ref is not null
        case null       => None
        case service: T => Some(f(service, ref.properties))
      }
    } finally context.ungetService(ref)  // Must be called
  }
}
