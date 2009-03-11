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
import scala.collection.immutable
import org.osgi.framework.BundleContext
import org.osgi.service.cm.{Configuration, ConfigurationAdmin}
import org.scalamodules.core.RichBundleContext.fromBundleContext
import org.scalamodules.util.jcl.Conversions.mapToJavaDictionary

class Configure(context: BundleContext,
                pid: String) {

  require(context != null, "Bundle context must not be null!")
  require(pid != null, "PID must not be null!")

  def updateWith(properties: Map[String, Any]) {
    require(properties != null, "Properties must not be null!")
    context getOne classOf[ConfigurationAdmin] andApply {
      (configAdmin: ConfigurationAdmin) => {
        val config = configAdmin.getConfiguration(pid, null)
        if (config != null) {
          val current = config.getProperties match {
            case null    => immutable.Map[String, Any]()
            case current => immutable.Map[String, Any]() ++ current.asInstanceOf[Map[String, Any]]
          }
          config.update(current ++ properties)
        }
      }
    }
  }
}
