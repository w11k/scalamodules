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
package org.scalamodules.demo.depend.internal

import java.io.PrintStream
import org.apache.felix.shell.Command
import org.osgi.framework.{BundleActivator, BundleContext}
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import org.scalamodules.services.ServicesRichBundleContext.toServicesRichBundleContext
import org.scalamodules.demo.Greeting

class Activator extends BundleActivator {

  override def start(context: BundleContext) {

    // For Felix
    try {
      context registerAs classOf[Command] dependOn classOf[Greeting] theService {
        greeting => new Command {
          override def getName = "greet"
          override def getShortDescription = "Invoke Greeting"
          override def getUsage = "greet [welcome | goodbye | configure <WELCOME> <GOODBYE>]"
          override def execute(cmdLine: String, out: PrintStream, err: PrintStream) {
            cmdLine split " " match {
              case Array("greet", "welcome") => out println greeting.welcome
              case Array("greet", "goodbye") => out println greeting.goodbye
              case Array("greet", "configure", welcome, goodbye) => 
                context configure "managedGreeting" updateWith 
                  Map("welcome" -> welcome, "goodbye" -> goodbye)
              case _ => err.printf("Illegal usage! Try \"%s\"%n", getUsage)
            }
          }
        }
      }
    } catch {
      case _ => // Optional import "org.apache.felix.shell" not satisfied: Ignore!
    }

    // For Equinox
    // TODO
  }

  override def stop(context: BundleContext) { // Nothing!
  }
}
