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

import Preamble.{Prop, Props}
import Util.toOption

/**
 * Registration information for an independent service.
 */
private[core] class RegIndepInfo[I <: AnyRef, S <: I](val srv: S,
                                                      val srvIntf: Option[Class[I]],
                                                      val props: Option[Props]) {

  require(srv != null, "Service to be registered must not be null!")
  require(srvIntf != null, "Option for service interface used for registration must not be null!")
  require(props != null, "Option for service properties must not be null!")

  def this(srv: S) = this(srv, None, None)

  def this(srv: S, srvIntf: Option[Class[I]]) = this(srv, srvIntf, None)

  /**
   * Register a service under the given service interface.
   */
  def /(srvIntf: Class[I]) = as(srvIntf)

  /**
   * Register a service under the given service interface.
   */
  def as(srvIntf: Class[I]) = new RegIndepInfo(srv, srvIntf, props)

  /**
   * Register a service with the given properties.
   */
  def %(props: Props) = withProps(props)

  /**
   * Register a service with the given properties.
   */
  def withProps(props: Props) = new RegIndepInfo(srv, srvIntf, props)

  /**
   * Register a service with the given properties.
   */
  def %(props: Prop*) = withProps(Map(props: _*))

  /**
   * Register a service with the given properties.
   */
  def withProps(props: Prop*) = new RegIndepInfo(srv, srvIntf, Map(props: _*))
}

/**
 * Registration information for a service depending on another service.
 */
private[core] class RegDepInfo[I <: AnyRef, S <: I, D <: AnyRef](val srvFactory: D => S,
                                                                 val srvIntf: Option[Class[I]],
                                                                 val props: Option[Props]) {

  require(srvFactory != null, "Factory function for service to be registered must not be null!")
  require(srvIntf != null, "Option for service interface used for registration must not be null!")
  require(props != null, "Option for service properties must not be null!")

  def this(srvFactory: D => S) = this(srvFactory, None, None)

  /**
   * Register a service under the given service interface.
   */
  def /(srvIntf: Class[I]) = as(srvIntf)

  /**
   * Register a service under the given service interface.
   */
  def as(srvIntf: Class[I]) = new RegDepInfo(srvFactory, srvIntf, props)

  /**
   * Register a service with the given properties.
   */
  def %(props: Props) = withProps(props)

  /**
   * Register a service with the given properties.
   */
  def withProps(props: Props) = new RegDepInfo(srvFactory, srvIntf, props)

  /**
   * Register a service with the given properties.
   */
  def %(props: Prop*) = withProps(Map(props: _*))

  /**
   * Register a service with the given properties.
   */
  def withProps(props: Prop*) = new RegDepInfo(srvFactory, srvIntf, Map(props: _*))
}
