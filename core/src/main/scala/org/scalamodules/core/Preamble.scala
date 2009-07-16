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

import org.osgi.framework.{BundleContext, ServiceReference}
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

object Preamble {

  /**
   * Service properties.
   */
  type Props = Map[String, Any]

  /**
   * Implementation of service properties.
   */
  type PropsImpl = IMap[String, Any]

  /**
   * Implicitly converts the given BundleContext to RichBundleContext.
   */
  implicit def toRichBundleContext(ctx: BundleContext) = 
    new RichBundleContext(ctx) 

  /**
   * Implicitly converts the given ServiceReference to RichServiceReference.
   */
  implicit def toRichServiceReference(ref: ServiceReference) = 
    new RichServiceReference(ref)

  /**
   * Implicitly converts the given object to RegIndepInfo.
   */
  implicit def toRegIndepInfo[S <: AnyRef](srv: S) = new RegIndepInfo(srv) 

  /**
   * Implicitly converts the given function to RegDepInfo.
   */
  implicit def toRegDepInfo[S <: AnyRef, D <: AnyRef](srvFactory: D => S) = 
    new RegDepInfo(srvFactory) 
}
