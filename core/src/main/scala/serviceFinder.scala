/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.BundleContext
import scala.collection.Seq

private[scalamodules] class ServiceFinder[I <: AnyRef](
    interface: Class[I],
    context: BundleContext) {

  require(interface != null, "The service interface must not be null!")
  require(context != null, "The BundleContext must not be null!")

  def andApply[T](f: I => T): Option[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context getServiceReference interface.getName match {
      case null => None
      case ref => invokeService(ref, f, context)
    }
  }

  def andApply[T](f: (I, Props) => T): Option[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context getServiceReference interface.getName match {
      case null => None
      case ref => invokeService(ref, f(_: I, ref.properties), context)
    }
  }
}

private[scalamodules] class ServicesFinder[I <: AnyRef](
    interface: Class[I],
    context: BundleContext,
    filter: Option[Filter] = None) {

  require(interface != null, "The service interface must not be null!")
  require(context != null, "The BundleContext must not be null!")
  require(filter != null, "The filter must not be null!")

  def withFilter(filter: Filter) = {
    require(filter != null, "The filter must not be null!")
    new ServicesFinder(interface, context, Some(filter))
  }

  def andApply[T](f: I => T): Seq[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context.getServiceReferences(interface.getName, filter) match {
      case null => Nil
      case refs => refs.toList flatMap { invokeService(_, f, context) }
    }
  }

  def andApply[T](f: (I, Props) => T): Seq[T] = {
    require(f != null, "The function to be applied to the service must not be null!")
    context.getServiceReferences(interface.getName, filter) match {
      case null => Nil
      case refs => refs.toList flatMap { ref => invokeService(ref, f(_: I, ref.properties), context) }
    }
  }
}
