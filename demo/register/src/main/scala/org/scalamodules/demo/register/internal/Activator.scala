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

import core.Preamble._

import scala.collection.Map
import scala.collection.immutable.{Map => IMap}
import org.osgi.framework.{BundleActivator, BundleContext}

class Activator extends BundleActivator {

  override def start(ctx: BundleContext) {

    // Register a Greeting service
    val hello = new Greeting {
      override val welcome = "Hello!"
      override val goodbye = "See you!"
    }
    ctx register hello

    // Register a Greeting service with properties
    // Using operator notation here!
    val welcome = new Greeting {
      override val welcome = "Welcome!"
      override val goodbye = "Goodbye!"
    }
    ctx < welcome % ("polite" -> "true")

    // The following would also work:
    // ctx register welcome ## ("polite" -> "true")

    // But the following would not, because of precedence
    // ctx register welcome withProps ("polite" -> "true")

    // You have to use parenthesis if you do not want to use operators
    // ctx register (welcome withProps ("polite" -> "true"))
  }

  override def stop(ctx: BundleContext) { // Nothing!
  }

  private def greeting(_welcome: String, _goodbye: String) =
    new {
      val welcome = _welcome
      val goodbye = _goodbye
    } with Greeting
}
