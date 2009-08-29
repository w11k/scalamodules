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
package org.scalamodules.demo.get.internal

import core.Filter.isTrue
import core.Preamble._

import java.lang.Boolean.parseBoolean
import org.osgi.framework.{BundleActivator, BundleContext}

class Activator extends BundleActivator {

  override def start(ctx: BundleContext) {
    // Get one Greeting service
    ctx getOne classOf[Greeting] andApply { _.welcome } match {
      case None          => println(NoGreeting)
      case Some(welcome) => println(welcome)
    }

    // Get one Greeting service once more using operator notation
    ctx ?> classOf[Greeting] & { _.welcome } match {
      case None          => println(NoGreeting)
      case Some(welcome) => println(welcome)
    }

    // Get many Greeting services, but only polite ones by applying a fiter
    ctx getMany classOf[Greeting] withFilter isTrue("polite") andApply { _.welcome } match {
      case Nil      => println(NoGreeting)
      case welcomes => welcomes foreach { println }
    }

    // Get many filtered Greeting services once more using operator notation
    ctx *> classOf[Greeting] % ("polite" -> "true") & { _.welcome } match {
      case Nil      => println(NoGreeting)
      case welcomes => welcomes foreach { println }
    }

    // Get many Greeting services and their properties
    ctx getMany classOf[Greeting] andApply {
      (grt, props) => {
        val polite = if (parseBoolean(props.getOrElse("polite", "false").toString)) "polite"
                     else "not polite"
        grt.welcome + " is " + polite
      }
    } match {
      case Nil      => println(NoGreeting)
      case welcomes => welcomes.foreach { println }
    }
  }

  override def stop(ctx: BundleContext) {}

  private val NoGreeting = "No Greeting service available!"
}
