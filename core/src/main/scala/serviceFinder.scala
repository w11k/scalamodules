/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scalamodules

import org.osgi.framework.BundleContext

private[scalamodules] class ServiceFinder[I <: AnyRef](interface: Class[I])(context: BundleContext) {
  require(interface != null, "The service interface must not be null!")
  require(context != null, "The BundleContext must not be null!")

  def andApply[T](f: I => T): Option[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context getServiceReference interface.getName match {
      case null             => None
      case serviceReference => invokeService(serviceReference, f)(context)
    }
  }

  def andApply[T](f: (I, Properties) => T): Option[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context getServiceReference interface.getName match {
      case null             => None
      case serviceReference => invokeService(serviceReference, f(_: I, serviceReference.properties))(context)
    }
  }
}

private[scalamodules] class ServicesFinder[I <: AnyRef]
                                          (interface: Class[I], filter: Option[Filter] = None)
                                          (context: BundleContext) {
  require(interface != null, "The service interface must not be null!")
  require(filter != null, "The filter must not be null!")
  require(context != null, "The BundleContext must not be null!")

  def withFilter(filter: Filter) = {
    require(filter != null, "The filter must not be null!")
    new ServicesFinder(interface, Some(filter))(context)
  }

  def andApply[T](f: I => T): List[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context.getServiceReferences(interface.getName, filter map { _.toString } orNull) match {
      case null              => Nil
      case serviceReferences => (serviceReferences flatMap { r => invokeService(r, f)(context) }).toList
    }
  }

  def andApply[T](f: (I, Properties) => T): List[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context.getServiceReferences(interface.getName, filter map { _.toString} orNull) match {
      case null              => Nil
      case serviceReferences => (serviceReferences flatMap { r => invokeService(r, f(_: I, r.properties))(context) }).toList
    }
  }
}
