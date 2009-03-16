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
package org.scalamodules.services.cm

import scala.collection.Map
import scala.collection.immutable
import org.osgi.framework.BundleContext
import org.osgi.service.cm.{Configuration, ConfigurationAdmin}
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import org.scalamodules.services.cm.RichConfiguration.toRichConfiguration
import org.scalamodules.util.jcl.Conversions.mapToJavaDictionary

/**
 * Provides configuration via Configuration Admin service. 
 */
class Configure(context: BundleContext,
                pid: String) {

  require(context != null, "Bundle context must not be null!")
  require(pid != null, "PID must not be null!")

  /**
   * Updates the configuration with the given properties. 
   */
  def updateWith(properties: Map[String, Any]) {
    require(properties != null, "Properties must not be null!")
    context getOne classOf[ConfigurationAdmin] andApply {
      (configAdmin: ConfigurationAdmin) => {
        val config = configAdmin.getConfiguration(pid, null)
        val current = config.properties match {
          case None        => immutable.Map[String, Any]()
          case Some(props) => immutable.Map[String, Any]() ++ props
        }
        config.update(current ++ properties)
      }
    }
  }

  /**
   * Replaces the configuration with the given properties. 
   */
  def replaceWith(properties: Map[String, Any]) {
    require(properties != null, "Properties must not be null!")
    context getOne classOf[ConfigurationAdmin] andApply {
      (configAdmin: ConfigurationAdmin) => {
        val config = configAdmin.getConfiguration(pid, null)
        config.update(properties)
      }
    }
  }
}
