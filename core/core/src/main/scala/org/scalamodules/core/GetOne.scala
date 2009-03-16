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

/**
 * Provides service consumption for a single service.
 */
class GetOne[T](context: BundleContext, 
                serviceInterface: Class[T]) {

  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")

  /**
   * Applies the given function to the service.
   */
  def andApply[S](f: T => S): Option[S] = {
    work(context.applyWithRef(_, f))
  }

  /**
   * Applies the given function to the service and its properties.
   */
  def andApply[S](f: (T, Map[String, AnyRef]) => S): Option[S] = {
    work(context.applyWithRef(_, f))
  }

  private def work[S](f: ServiceReference => Option[S]): Option[S] = {
    assert(f != null, "Function to be applied must not be null!")
    context.getServiceReference(serviceInterface.getName) match {
      case null => None
      case ref  => f(ref)
    }
  }
}
