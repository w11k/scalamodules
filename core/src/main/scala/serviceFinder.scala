/*
 * Copyright 2009-2011 Weigle Wilczek GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.BundleContext

private[scalamodules] class ServiceFinder[I <: AnyRef](
    interface: Class[I],
    context: BundleContext) {

  assert(interface != null, "The service interface must not be null!")
  assert(context != null, "The BundleContext must not be null!")

  /**
   * Applies the given function to a service. The service is found by its service interface.
   * @param f The function to be applied to a service; must not be null!
   * @return The optional result of applying the given function; None if no service available
   */
  def andApply[T](f: I => T): Option[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context getServiceReference interface.getName match {
      case null => {
        logger info "Could not find a ServiceReference for interface %s.".format(interface.getName)
        None
      }
      case ref => {
        logger info "Found a ServiceReference for interface %s.".format(interface.getName)
        invokeService(ref, f, context)
      }
    }
  }

  /**
   * Applies the given function to a service and its properties. The service is found by its service interface.
   * @param f The function to be applied to a service and its properties; must not be null!
   * @return The optional result of applying the given function; None if no service available
   */
  def andApply[T](f: (I, Props) => T): Option[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context getServiceReference interface.getName match {
      case null => {
        logger info "Could not find a ServiceReference for interface %s.".format(interface.getName)
        None
      }
      case ref => {
        logger info "Found a ServiceReference for interface %s.".format(interface.getName)
        invokeService(ref, f(_: I, ref.properties), context)
      }
    }
  }
}

private[scalamodules] class ServicesFinder[I <: AnyRef](
    interface: Class[I],
    context: BundleContext,
    filter: Option[Filter] = None) {

  assert(interface != null, "The service interface must not be null!")
  assert(context != null, "The BundleContext must not be null!")
  assert(filter != null, "The filter must not be null!")

  /**
   * Additionally use the given Filter for finding services.
   * @param filter The Filter to be added to this ServiceFinders; must not be null!
   * @return A ServiceFinders for a service interface and the given Filter
   */
  def withFilter(filter: Filter) = {
    require(filter != null, "The filter must not be null!")
    new ServicesFinder(interface, context, Some(filter))
  }

  /**
   * Applies the given function to all services. The services are found by service interface and an optional filter.
   * @param f The function to be applied to all services; must not be null!
   * @return A Seq with the results of applying the given function to all services
   */
  def andApply[T](f: I => T): Seq[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context.getServiceReferences(interface.getName, filter map { _.toString } orNull) match {
      case null => {
        logger info "Could not find any ServiceReferences for interface %s and optional filter %s.".format(interface.getName, filter)
        Nil
      }
      case refs => {
        logger info "Found %s ServiceReferences for interface %s and optional filter %s.".format(refs.size, interface.getName, filter)
        refs.toList flatMap { invokeService(_, f, context) }
      }
    }
  }

  /**
   * Applies the given function to all services and their properties. The services are found by service interface and an optional filter.
   * @param f The function to be applied to all services and their properties; must not be null!
   * @return A Seq with the results of applying the given function to all services
   */
  def andApply[T](f: (I, Props) => T): Seq[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context.getServiceReferences(interface.getName, filter map { _.toString } orNull) match {
      case null => {
        logger info "Could not find any ServiceReferences for interface %s and optional filter %s.".format(interface.getName, filter)
        Nil
      }
      case refs => {
        logger info "Found %s ServiceReferences for interface %s and optional filter %s.".format(refs.size, interface.getName, filter)
        refs.toList flatMap { ref => invokeService(ref, f(_: I, ref.properties), context) }
      }
    }
  }
}
