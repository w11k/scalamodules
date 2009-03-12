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
 * Provides service registration for one service interface.
 */
class RegisterAs[T](val context: BundleContext,
                    val t: Class[T])
    extends Registerer[T] {

  require(context != null, "Bundle context must not be null!")
  require(t != null, "Service interface must not be null!")

  /**
   * Adds the given service interface.
   */
  def andAs[T2](t2: Class[T2]): RegisterAs2[T, T2] = 
    new RegisterAs2[T, T2](context, t, t2)

  /**
   * Adds the given service properties.
   */
  def withProperties(props: Map[String, Any]): Registerer[T] =
    new {
      override protected val context = this.context
      override protected val t = this.t
      override protected val properties = props
    } with Registerer[T]

  override protected val properties = null
}

/**
 * Provides service registration for two service interfaces.
 */
class RegisterAs2[T1, T2](protected val context: BundleContext,
                          protected val t1: Class[T1],
                          protected val t2: Class[T2])
    extends Registerer2[T1, T2] {

  require(context != null, "Bundle context must not be null!")
  require(t1 != null, "First service interface must not be null!")
  require(t2 != null, "Second service interface must not be null!")

  /**
   * Adds the given service interface.
   */
  def andAs[T3](t3: Class[T3]): RegisterAs3[T1, T2, T3] = 
    new RegisterAs3[T1, T2, T3](context, t1, t2, t3)

  /**
   * Adds the given service properties.
   */
  def withProperties(props: Map[String, Any]): Registerer2[T1, T2] =
    new {
      override protected val context = this.context
      override protected val t1 = this.t1
      override protected val t2 = this.t2
      override protected val properties = props
    } with Registerer2[T1, T2]

  override protected val properties = null
}

/**
 * Provides service registration for three service interfaces.
 */
class RegisterAs3[T1, T2, T3](protected val context: BundleContext,
                              protected val t1: Class[T1],
                              protected val t2: Class[T2],
                              protected val t3: Class[T3])
    extends Registerer3[T1, T2, T3] {

  require(context != null, "Bundle context must not be null!")
  require(t1 != null, "First service interface must not be null!")
  require(t2 != null, "Second service interface must not be null!")
  require(t3 != null, "Third service interface must not be null!")

  /**
   * Adds the given service properties.
   */
  def withProperties(props: Map[String, Any]): Registerer3[T1, T2, T3] =
    new {
      override protected val context = this.context
      override protected val t1 = this.t1
      override protected val t2 = this.t2
      override protected val t3 = this.t3
      override protected val properties = props 
    } with Registerer3[T1, T2, T3]

  override protected val properties = null
}
