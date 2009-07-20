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
import internal.Util.toOption

import scala.collection.Map
import org.osgi.framework.{BundleContext, ServiceReference}

/**
 * Consume a single service.
 */
private class GetOne[I](ctx: BundleContext, srvIntf: Class[I])
    extends Get(ctx, srvIntf) {

  override private[core] type Result[T] = Option[T]

  override private[core] def work[T](f: ServiceReference => Option[T]): Result[T] = {
    assert(f != null, "Function to be applied must not be null!")
    ctx getServiceReference srvIntf.getName match {
      case null => None
      case ref  => f(ref)
    }
  }
}

/**
 * Consume multiple services.
 */
private class GetMany[I](ctx: BundleContext, srvIntf: Class[I], filter: Option[String])
  extends Get(ctx, srvIntf) {

  require(filter != null, "Option for filter must not be null!")

  def this(ctx: BundleContext, srvIntf: Class[I]) = this(ctx, srvIntf, None)

  /**
   * Sets the given filter for service look-ups.
   */
  def %(filter: String) = withFilter(filter)

  /**
   * Sets the given filter for service look-ups.
   */
  def withFilter(filter: String) = new GetMany(ctx, srvIntf, filter)

  override private[core] type Result[T] = List[T]

  override private[core] def work[T](f: ServiceReference => Option[T]): Result[T] = {
    assert(f != null, "Function to be applied must not be null!")
    var result: List[Option[T]] = Nil
    ctx.getServiceReferences(srvIntf.getName, filter getOrElse null) match {
      case null =>
      case refs => refs foreach { ref => result = f(ref) :: result } 
    }
    result flatMap { o => o }
  }
}

private abstract class Get[I](ctx: BundleContext, srvIntf: Class[I]) {

  require(ctx != null, "BundleContext must not be null!")
  require(srvIntf != null, "Service interface must not be null!")

  /**
   * Applies the given function to the service.
   */
  def &[T](f: I => T) = andApply(f)

  /**
   * Applies the given function to the service.
   */
  def andApply[T](f: I => T) = {
    require(f != null, "Function to be applied must not be null!")
    work(applyWithRef(_, f))
  }

  /**
   * Applies the given function to the service and its properties.
   */
  def &[T](f: (I, Props) => T) = andApply(f)

  /**
   * Applies the given function to the service and its properties.
   */
  def andApply[T](f: (I, Props) => T) = {
    require(f != null, "Function to be applied must not be null!")
    work(applyWithRef(_, f))
  }

  private[core] type Result[T]

  private[core] def work[T](f: ServiceReference => Option[T]): Result[T]

  private def applyWithRef[T](ref: ServiceReference, f: I => T): Option[T] = {
    assert(ref != null, "ServiceReference must not be null!")
    assert(f != null, "Function to be applied must not be null!")
    try {
      ctx getService ref match {  // Might be null even if ref is not null
        case null            => None
        case service: AnyRef => Some(f(service.asInstanceOf[I]))
      }
    } finally ctx ungetService ref  // Must be called
  }

  private def applyWithRef[T](ref: ServiceReference,
                              f: (I, Props) => T): Option[T] = {
    assert(ref != null, "ServiceReference must not be null!")
    assert(f != null, "Function to be applied must not be null!")
    try {
      ctx getService ref match {  // Might be null even if ref is not null
        case null            => None
        case service: AnyRef => Some(f(service.asInstanceOf[I], ref.properties))
      }
    } finally ctx ungetService ref  // Must be called
  }
}
