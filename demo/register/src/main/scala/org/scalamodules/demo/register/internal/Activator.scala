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
package org.scalamodules.demo.register.internal

import scala.collection.Map
import scala.collection.immutable
import org.osgi.framework.{BundleActivator, BundleContext}
import org.osgi.service.cm.ManagedService
import org.scalamodules.services.cm.BaseManagedService
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import org.scalamodules.demo._

class Activator extends BundleActivator {

  override def start(context: BundleContext) {

    // Register Greeting
    context registerAs classOf[Greeting] theService new Greeting {
      override def welcome = "Hello!"
      override def goodbye = "See you!";
    }

    // Register Greeting with properties
    context registerAs classOf[Greeting] withProperties 
      immutable.Map("name" -> "welcome") theService new Greeting {
        override def welcome = "Welcome!"
        override def goodbye = "Goodbye!"
      }

    // Register Greeting + ManagedService
    val managedGreeting = new Greeting with BaseManagedService {
      override def welcome = w
      override def goodbye = g
      override def handleUpdate(properties: Option[Map[String, Any]]) {
        properties match {
          case None        => w = Howdy; g = Bye
          case Some(props) => {
            props get "welcome" match {
              case None    => w = Howdy
              case Some(s) => w = s.toString
            }
            props get "goodbye" match {
              case None    => g = Bye
              case Some(s) => g = s.toString
            }
          }
        }
      }
      private var w: String = "UNCONFIGURED"
      private var g: String = "UNCONFIGURED"
      private val Howdy = "Howdy!"
      private val Bye = "Bye!"
    }
    context registerAs classOf[Greeting] andAs classOf[ManagedService] withProperties 
      immutable.Map("service.pid" -> "managedGreeting") theService managedGreeting 
  }

  override def stop(context: BundleContext) { // Nothing!
  }
}
