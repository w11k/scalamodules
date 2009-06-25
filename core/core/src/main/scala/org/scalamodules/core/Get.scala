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
import org.osgi.framework.{BundleContext, ServiceReference}
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import org.scalamodules.core.RichServiceReference.toRichServiceReference

/**
 * Consume a single service.
 */
class GetOne[I](ctx: BundleContext, srvIntf: Class[I])
    extends Get(ctx, srvIntf) {

  override protected type Result[T] = T

  override protected def work[T](f: ServiceReference => Option[T]): Option[Result[T]] = {
    assert(f != null, "Function to be applied must not be null!")
    ctx.getServiceReference(srvIntf.getName) match {
      case null => None
      case ref  => f(ref)
    }
  }
}

/**
 * Consume multiple services.
 */
class GetMany[I](ctx: BundleContext, srvIntf: Class[I], filter: String)
  extends Get(ctx, srvIntf) {

  def this(ctx: BundleContext, srvIntf: Class[I]) = this(ctx, srvIntf, null)

  /**
   * Sets the given filter for service look-ups.
   */
  def withFilter(filter: String) = new GetMany(ctx, srvIntf, filter)

  override protected type Result[T] = List[T]

  override protected def work[T](f: ServiceReference => Option[T]): Option[Result[T]] = {
    assert(f != null, "Function to be applied must not be null!")
    var result: List[T] = Nil
    ctx.getServiceReferences(srvIntf.getName, filter) match {
      case null => result
      case refs => refs.foreach { 
        f(_) match {
          case None    => result
          case Some(s) => result = s :: result
        }
      }  
    }
    if (result.isEmpty) None else Some(result)
  }
}

/**
 * Abstract superclass for consuming services.
 */
private[core] abstract class Get[I](ctx: BundleContext, srvIntf: Class[I]) {

  require(ctx != null, "BundleContext must not be null!")
  require(srvIntf != null, "Service interface must not be null!")

  /**
   * Applies the given function to the service.
   */
  def andApply[T](f: I => T) = work(applyWithRef(_, f))

  /**
   * Applies the given function to the service and its properties.
   */
  def andApply[T](f: (I, Map[String, Any]) => T) = work(applyWithRef(_, f))

  protected type Result[T]

  protected def work[T](f: ServiceReference => Option[T]): Option[Result[T]]

  private def applyWithRef[T](ref: ServiceReference, f: I => T): Option[T] = {
    assert(ref != null, "ServiceReference must not be null!")
    assert(f != null, "Function to be applied must not be null!")
    try {
      ctx.getService(ref) match {  // Might be null even if ref is not null
        case null            => None
        case service: AnyRef => Some(f(service.asInstanceOf[I]))
      }
    } finally ctx.ungetService(ref)  // Must be called
  }

  private def applyWithRef[T](ref: ServiceReference,
                              f: (I, Map[String, Any]) => T): Option[T] = {
    assert(ref != null, "ServiceReference must not be null!")
    assert(f != null, "Function to be applied must not be null!")
    try {
      ctx.getService(ref) match {  // Might be null even if ref is not null
        case null            => None
        case service: AnyRef => Some(f(service.asInstanceOf[I], ref.properties))
      }
    } finally ctx.ungetService(ref)  // Must be called
  }
}
