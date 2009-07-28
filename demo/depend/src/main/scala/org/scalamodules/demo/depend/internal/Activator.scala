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

import core.Preamble._
import core.{Adding, Removed}

import org.osgi.framework.{BundleActivator, BundleContext}

class Activator extends BundleActivator {

  override def start(ctx: BundleContext) {
    // Register a Reverser service depending on a Greeting service using operator notation
    ctx < { (grt: Greeting) => new GreetingReverser(grt) }

    // Track the Reverser service using operator notation
    ctx >> classOf[Reverser] & {
      case Adding(rev, _) => println("Adding Reverser: " + rev.reverse)
      case Removed(rev, _) => println("Removed Reverser: " + rev.reverse)
    }
  }

  override def stop(ctx: BundleContext) {}
}

private[internal] class GreetingReverser(grt: Greeting)
  extends Reverser {

  val reverse = new StringBuilder(grt.welcome).reverse.toString
}
