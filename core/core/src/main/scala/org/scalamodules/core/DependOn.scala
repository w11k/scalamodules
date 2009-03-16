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
import org.osgi.framework.{BundleContext, ServiceReference, ServiceRegistration}
import org.osgi.util.tracker.ServiceTracker
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import org.scalamodules.core.RichServiceReference.toRichServiceReference
import org.scalamodules.util.jcl.Conversions.mapToJavaDictionary

/**
 * Declares a dependency for a service to be registered 
 * with one service interface. 
 */
class DependOn[T, S](context: BundleContext,
                     dependee: Class[S],
                     t: Class[T],
                     properties: Map[String, Any]) {
  
  require(context != null, "Bundle context must not be null!")
  require(dependee != null, "Dependee must not be null!")
  require(t != null, "Service interface must not be null!")

  /**
   * Registers the service created by the given factory function which is given
   * the dependent service.
   */
  def theService(f: S => T) {
    require(f != null, "Factory function must not be null!")
    new DependOnTracker(context, dependee, Array(t), properties) {
      override protected def createService(ref: ServiceReference) =
        f(context.getService(ref).asInstanceOf[S])
    }.open()
  }

  /**
   * Registers the service created by the given factory function which is given
   * the dependent service and its properties.
   */
  def theService(f: (S, Map[String, Any]) => T) {
    require(f != null, "Factory function must not be null!")
    new DependOnTracker(context, dependee, Array(t), properties) {
      override protected def createService(ref: ServiceReference) =
        f(context.getService(ref).asInstanceOf[S], ref.properties)
    }.open()
  }
}

/**
 * Declares a dependency for a service to be registered 
 * with two service interfaces. 
 */
class DependOn2[T1, T2, S](context: BundleContext,
                           dependee: Class[S],
                           t1: Class[T1],
                           t2: Class[T2],
                           properties: Map[String, Any]) {
  
  require(context != null, "Bundle context must not be null!")
  require(dependee != null, "Dependee must not be null!")
  require(t1 != null, "First service interface must not be null!")
  require(t2 != null, "Second service interface must not be null!")

  /**
   * Registers the service created by the given factory function which is given
   * the dependent service.
   */
  def theService(f: S => T1 with T2) {
    require(f != null, "Factory function must not be null!")
    new DependOnTracker(context, dependee, Array(t1, t2), properties) {
      override protected def createService(ref: ServiceReference) =
        f(context.getService(ref).asInstanceOf[S])
    }.open()
  }

  /**
   * Registers the service created by the given factory function which is given
   * the dependent service and its properties.
   */
  def theService(f: (S, Map[String, Any]) => T1 with T2) {
    require(f != null, "Factory function must not be null!")
    new DependOnTracker(context, dependee, Array(t1, t2), properties) {
      override protected def createService(ref: ServiceReference) =
        f(context.getService(ref).asInstanceOf[S], ref.properties)
    }.open()
  }
}

/**
 * Declares a dependency for a service to be registered 
 * with three service interfaces. 
 */
class DependOn3[T1, T2, T3, S](context: BundleContext,
                               dependee: Class[S],
                               t1: Class[T1],
                               t2: Class[T2],
                               t3: Class[T3],
                               properties: Map[String, Any]) {
  
  require(context != null, "Bundle context must not be null!")
  require(dependee != null, "Dependee must not be null!")
  require(t1 != null, "First service interface must not be null!")
  require(t2 != null, "Second service interface must not be null!")
  require(t3 != null, "Third service interface must not be null!")

  /**
   * Registers the service created by the given factory function which is given
   * the dependent service.
   */
  def theService(f: S => T1 with T2 with T3) {
    require(f != null, "Factory function must not be null!")
    new DependOnTracker(context, dependee, Array(t1, t2, t3), properties) {
      override protected def createService(ref: ServiceReference) =
        f(context.getService(ref).asInstanceOf[S])
    }.open()
  }

  /**
   * Registers the service created by the given factory function which is given
   * the dependent service and its properties.
   */
  def theService(f: (S, Map[String, Any]) => T1 with T2 with T3) {
    require(f != null, "Factory function must not be null!")
    new DependOnTracker(context, dependee, Array(t1, t2, t3), properties) {
      override protected def createService(ref: ServiceReference) =
        f(context.getService(ref).asInstanceOf[S], ref.properties)
    }.open()
  }
}

private abstract class DependOnTracker[S](context: BundleContext,
                                          dependee: Class[S],
                                          serviceInterfaces: Array[Class[_]],
                                          properties: Map[String, Any])
    extends ServiceTracker(context, dependee.getName, null) {

  override def addingService(ref: ServiceReference): ServiceRegistration = 
    synchronized {
      satisfied match {
        case true  => null
        case false => {
        satisfied = true
        context.registerService(serviceInterfaces map { _.getName }, 
                                createService(ref), 
                                properties)
        }
      }
    }

  override def removedService(ref: ServiceReference, registration: AnyRef) = 
    synchronized {
      registration.asInstanceOf[ServiceRegistration].unregister()
      satisfied = false
      context.ungetService(ref)
    }

  protected def createService(ref: ServiceReference): Any

  private var satisfied = false
}
