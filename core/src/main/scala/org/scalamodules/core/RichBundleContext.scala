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

import internal.Util.mapToJavaDictionary

import org.osgi.framework.{BundleContext, ServiceReference, ServiceRegistration}
import org.osgi.util.tracker.ServiceTracker
import org.scalamodules.core.RichServiceReference.toRichServiceReference

/**
 * Companion object for RichBundleContext providing implicit conversions.
 */
object RichBundleContext {

  /**
   * Implicitly converts the given BundleContext to RichBundleContext.
   */
  implicit def toRichBundleContext(ctx: BundleContext) = new RichBundleContext(ctx) 
}

/**
 * Rich wrapper for BundleContext: 
 * Makes service handling more convenient and enables the ScalaModules DSL.
 */
class RichBundleContext(ctx: BundleContext) {
  
  require(ctx != null, "BundleContext must not be null!")

  /**
   * Register an independent service.
   */
  def register[I <: AnyRef, S <: I](info: RegIndepInfo[I, S]) = {
    val srvIntfs = info.srvIntf match {
      case Some(srvIntf) => Array(srvIntf.getName)
      case None          => info.srv.getClass.getInterfaces map { _.getName }
    }
    val props = info.props match {
      case Some(props) => mapToJavaDictionary(props)
      case None        => null
    }
    ctx.registerService(srvIntfs, info.srv, props)
  }

  /**
   * Register a service depending on another service. 
   */
  def register[I <: AnyRef, S <: I, D](info: RegDepInfo[I, S, D]) = {
    val srvIntfs = info.srvIntf match {
      case Some(srvIntf) => Array(srvIntf.getName)
      case None          => info.srv.getClass.getInterfaces map { _.getName }
    }
    val props = info.props match {
      case Some(props) => mapToJavaDictionary(props)
      case None        => null
    }
    new ServiceTracker(ctx, "", null)
    // TODO: Finalize depending services!
  }

  /**
   * Consume a single service.
   */
  def getOne[I](srvIntf: Class[I]) = new GetOne[I](ctx, srvIntf)

  /**
   * Consume multiple services.
   */
  def getMany[I](srvIntf: Class[I]) = new GetMany[I](ctx, srvIntf)

  /**
   * Track a service. 
   */
  def track[I](srvIntf: Class[I]) = new Track[I](ctx, srvIntf)
}
