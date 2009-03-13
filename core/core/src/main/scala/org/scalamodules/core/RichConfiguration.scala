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

import java.util.Dictionary
import scala.collection.Map
import org.osgi.service.cm.Configuration
import org.scalamodules.util.jcl.Conversions.dictionaryToMap

/**
 * Companion object for RichConfiguration.
 */
object RichConfiguration {

  /**
   * Implicitly converts the given Configuration to RichConfiguration.
   */
  implicit def fromConfiguration(config: Configuration) = 
    new RichConfiguration(config) 
}

/**
 * Rich wrapper for Configuration: 
 * Makes handling of configurations more convenient.
 */
class RichConfiguration(config: Configuration) {

  require(config != null, "Configuration must not be null!")

  /**
   * Get properties as optional Scala Map.
   */
  def properties: Option[Map[String, AnyRef]] = {
    config.getProperties match {
      case null  => None
      case props => Some(props.asInstanceOf[Dictionary[String, AnyRef]])
    }
  }
}
