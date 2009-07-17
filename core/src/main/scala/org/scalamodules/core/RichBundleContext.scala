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

import Preamble._
import internal.Util.mapToJavaDictionary

import org.osgi.framework.{BundleContext, ServiceReference, ServiceRegistration}
import org.osgi.util.tracker.ServiceTracker
import scala.collection.Map
import scala.reflect.Manifest

/**
 * Rich wrapper for BundleContext: 
 * Makes service handling more convenient and enables the ScalaModules DSL.
 */
private[core] class RichBundleContext(ctx: BundleContext) {

  require(ctx != null, "BundleContext must not be null!")

  /**
   * Register an independent service.
   */
  def >>[I <: AnyRef, S <: I](info: RegIndepInfo[I, S]) = register(info)

  /**
   * Register an independent service.
   */
  def register[I <: AnyRef, S <: I](info: RegIndepInfo[I, S]) = {

    require(info != null, "RegIndepInfo must not be null!")

    ctx.registerService(srvIntfs(info.srv, info.srvIntf), 
                        info.srv, 
                        props(info.props))
  }

  /**
   * Register a service depending on another service. 
   */
  def >>[I <: AnyRef, S <: I, D <: AnyRef](info: RegDepInfo[I, S, D])
                                         (implicit mf: Manifest[D]) =
    register(info)(mf)

  /**
   * Register a service depending on another service. 
   */
  def register[I <: AnyRef, S <: I, D <: AnyRef](info: RegDepInfo[I, S, D])
                                                (implicit mf: Manifest[D]) = {

    require(info != null, "RegIndepInfo must not be null!")

    val tracker = new ServiceTracker(ctx, mf.erasure.getName, null) {

      override def addingService(ref: ServiceReference) = 
        synchronized {
          satisfied match {
            case true  => null
            case false =>
              satisfied = true
              val dep = (ctx getService ref).asInstanceOf[D]
              val srv = info.srvFactory(dep)
              ctx.registerService(srvIntfs(srv, info.srvIntf), srv, props(info.props))
          }
        }

      override def removedService(ref: ServiceReference, reg: AnyRef) = {
        synchronized {
          reg.asInstanceOf[ServiceRegistration].unregister()
          satisfied = false
        }
        context ungetService ref
      }

      private var satisfied = false
    }

    tracker.open()

    tracker
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

  private def interfacesOrClass[S <: AnyRef](srv: S): Array[String] = {
    val intfs = srv.getClass.getInterfaces filter { _ != classOf[ScalaObject] }
    intfs.isEmpty match {
      case true  => Array(srv.getClass.getName)
      case false => intfs map { clazz => clazz.getName }
    }
  }

  private def srvIntfs[I <: AnyRef, S <: I](srv: S, srvIntf: Option[Class[I]]) = 
    srvIntf match {
      case Some(srvIntf) => Array(srvIntf.getName)
      case None          => interfacesOrClass(srv)
    }

  private def props(p: Option[Props]) = 
    p map { mapToJavaDictionary(_) } getOrElse null
}
