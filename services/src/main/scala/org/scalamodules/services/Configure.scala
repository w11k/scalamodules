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

import Preamble.toRichConfiguration
import core.Preamble.{Prop, Props, PropsImpl, toRichBundleContext}
import core.Util.mapToJavaDictionary

import org.osgi.framework.BundleContext
import org.osgi.service.cm.{Configuration, ConfigurationAdmin}

/**
 * Provides configuration via Configuration Admin service. 
 */
private[services] class Configure(ctx: BundleContext,
                                  pid: String) {

  require(ctx != null, "BundleContext must not be null!")
  require(pid != null, "PID must not be null!")

  /**
   * Updates the configuration with the given properties. 
   */
  def updateWith(props: Props) {

    require(props != null, "Properties must not be null!")

    ctx getOne classOf[ConfigurationAdmin] andApply {
      (configAdmin: ConfigurationAdmin) => {
        val config = configAdmin.getConfiguration(pid, null)
        config update (config.properties ++ props)
      }
    }
  }

  /**
   * Updates the configuration with the given properties. 
   */
  def updateWith(props: Prop*) {
    require(props != null, "Properties must not be null!")
    updateWith(Map(props: _*))
  }

  /**
   * Replaces the configuration with the given properties. 
   */
  def replaceWith(props: Props) {

    require(props != null, "Properties must not be null!")

    ctx getOne classOf[ConfigurationAdmin] andApply {
      (configAdmin: ConfigurationAdmin) => {
        val config = configAdmin.getConfiguration(pid, null)
        config update props
      }
    }
  }

  /**
   * Replaces the configuration with the given properties. 
   */
  def replaceWith(props: Prop*) {
    require(props != null, "Properties must not be null!")
    replaceWith(Map(props: _*))
  }
}
