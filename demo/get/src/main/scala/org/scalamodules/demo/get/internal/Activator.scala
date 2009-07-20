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

import core.Preamble._

import java.lang.Boolean.parseBoolean
import org.osgi.framework.{BundleActivator, BundleContext}

class Activator extends BundleActivator {

  override def start(context: BundleContext) {

    // Get one Greeting service
    context getOne classOf[Greeting] andApply { 
      _.welcome
    } match {
      case None          => noGreetingService() 
      case Some(welcome) => println(welcome)
    }

    // Get many Greeting services with a fiter applied
    context getMany classOf[Greeting] withFilter "(polite=*)" andApply { 
      _.welcome
    } match {
      case Nil      => noGreetingService()
      case welcomes => welcomes.foreach { println }
    }

    // Get many Greeting services and their properties
    context getMany classOf[Greeting] andApply { 
      (greeting, properties) => {
        val polite = if (parseBoolean(properties.getOrElse("polite", "").toString)) "polite"
                     else "not polite"
        greeting.welcome + " is " + polite
      }
    } match {
      case Nil      => noGreetingService()
      case welcomes => welcomes.foreach { println }
    }
  }

  override def stop(context: BundleContext) { // Nothing!
  }

  private def noGreetingService() {
    println("No Greeting service available!")
  }
}
