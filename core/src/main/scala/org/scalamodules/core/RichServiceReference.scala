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
import scala.collection.immutable.{Map => IMap}
import org.osgi.framework.ServiceReference

/**
 * Companion object for RichServiceReference providing implicit conversions.
 */
object RichServiceReference {
  
  /**
   * Implicitly converts the given ServiceReference to RichServiceReference.
   */
  implicit def toRichServiceReference(ref: ServiceReference) = 
    new RichServiceReference(ref)
}

/**
 * Rich wrapper for ServiceReference: 
 * Makes handling of service properties more convenient.
 */
class RichServiceReference(ref: ServiceReference) {

  require(ref != null, "ServiceReference must not be null!")

  val properties = IMap(fromRef(ref): _*)

  private def fromRef(ref: ServiceReference): Array[(String, Any)] = {
    ref.getPropertyKeys match {
      case null => Array[(String, Any)]()
      case keys => keys map { key => (key, ref getProperty key) }
    }
  }
}
