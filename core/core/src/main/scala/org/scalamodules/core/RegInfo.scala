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
import scala.collection.immutable.{Map => IMap}

/**
 * Companion object for RegInfo providing implicit conversions.
 */
object RegInfo {

  /**
   * Implicitly converts the given object to ImdRegInfo.
   */
  implicit def toImdRegInfo[S <: AnyRef](srv: S) = new ImdRegInfo(srv) 

  /**
   * Implicitly converts the given function to ImdRegInfo.
   */
  implicit def toDepRegInfo[S <: AnyRef, D](srv: D => S) = new DepRegInfo(srv) 
}

/**
 * Holds registration information and provides methods for detailing this.
 */
class ImdRegInfo[I <: AnyRef, S <: I](val srv: S,
                                      val srvIntf: Option[Class[I]],
                                      val props: Option[Map[String, Any]]) {

  require(srv != null, "Service to be registered must not be null!")

  def this(srv: S) = this(srv, None, None)

  /**
   * Register a service under the given service interface.
   */
  def as(srvIntf: Class[I]) = new ImdRegInfo(srv, toOption(srvIntf), props) 

  /**
   * Register a service with the given properties.
   */
  def withProps(props: Map[String, Any]) = 
    new ImdRegInfo(srv, srvIntf, toOption(props)) 

  /**
   * Register a service with the given properties.
   */
  def withProps(props: (String, Any)*) = 
    new ImdRegInfo(srv, srvIntf, toOption(IMap[String, Any](props: _*))) 

  private def toOption[T](any: T) = any match {
    case null => None
    case _    => Some(any) 
  }
}

/**
 * Holds registration information and provides methods for detailing this.
 */
class DepRegInfo[I <: AnyRef, S <: I, D](val srv: D => S,
                                         val srvIntf: Option[Class[I]],
                                         val props: Option[Map[String, Any]],
                                         val depIntf: Option[Class[D]]) {

  require(srv != null, "Service to be registered must not be null!")

  def this(srv: D => S) = this(srv, None, None, None)

  /**
   * Register a service under the given service interface.
   */
  def as(srvIntf: Class[I]) = 
    new DepRegInfo(srv, toOption(srvIntf), props, depIntf) 

  /**
   * Register a service with the given properties.
   */
  def withProps(props: Map[String, Any]) = 
    new DepRegInfo(srv, srvIntf, toOption(props), depIntf) 

  /**
   * Register a service with the given properties.
   */
  def withProps(props: (String, Any)*) = 
    new DepRegInfo(srv, srvIntf, toOption(IMap[String, Any](props: _*)), depIntf) 

  /**
   * Register a service depending on a service with the given service interface.
   */
  def dependOn(depIntf: Class[D]) =
    new DepRegInfo(srv, srvIntf, props, toOption(depIntf))

  private def toOption[T](any: T) = any match {
    case null => None
    case _    => Some(any) 
  }
}
