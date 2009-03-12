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
import org.scalamodules.util.jcl.Conversions.mapToJavaDictionary

/**
 * Mixin for performing the service registration and dependency declaration for
 * one service interface.
 */
trait Registerer[T] {

  /**
   * Registers the given service.
   */
  def theService(service: T): ServiceRegistration = {
    require(service != null, "Service to be registered must not be null!")
    context.registerService(t.getName, service, properties)
  }

  /**
   * Declares a dependency for a service to be registered 
   * with one service interface. 
   */
  def dependOn[S](dependee: Class[S]) = 
    new DependOn(context, dependee, t, properties)

  protected val context: BundleContext
  protected val t: Class[T]
  protected val properties: Map[String, Any] 
}

/**
 * Mixin for performing the service registration and dependency declaration for
 * two service interfaces.
 */
trait Registerer2[T1, T2] {

  /**
   * Registers the given service.
   */
  def theService(service: T1 with T2): ServiceRegistration = {
    require(service != null, "Service to be registered must not be null!")
    context.registerService(Array(t1.getName, t2.getName), 
                            service, 
                            properties)
  }

  /**
   * Declares a service dependency.
   */
  def dependOn[S](dependee: Class[S]) = 
    new DependOn2(context, dependee, t1, t2, properties)

  protected val context: BundleContext
  protected val t1: Class[T1]
  protected val t2: Class[T2]
  protected val properties: Map[String, Any] 
}


/**
 * Mixin for performing the service registration and dependency declaration for
 * three service interfaces.
 */
trait Registerer3[T1, T2, T3] {

  /**
   * Registers the given service.
   */
  def theService(service: T): ServiceRegistration = {
    require(service != null, "Service to be registered must not be null!")
    context.registerService(Array(t1.getName, t2.getName, t3.getName), 
                            service, 
                            properties)
  }

  /**
   * Declares a service dependency.
   */
  def dependOn[S](dependee: Class[S]) = 
    new DependOn3(context,dependee, t1, t2, t3, properties)

  protected val context: BundleContext
  protected val t1: Class[T1]
  protected val t2: Class[T2]
  protected val t3: Class[T3]
  protected val properties: Map[String, Any] 
}
