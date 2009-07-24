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
package org.scalamodules.services

import core.Util.dictionaryToMap

import java.util.Dictionary
import org.osgi.service.cm.Configuration
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

/**
 * Rich wrapper for Configuration: 
 * Makes handling of configurations more convenient.
 */
class RichConfiguration(config: Configuration) {

  require(config != null, "Configuration must not be null!")

  /**
   * Get properties as Map[String, Any].
   */
  def properties: Map[String, Any] = {
    config.getProperties match {
      case null  => IMap[String, Any]()
      case props => props.asInstanceOf[Dictionary[String, Any]]
    }
  }
}
