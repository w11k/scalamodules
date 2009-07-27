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
package org.scalamodules.demo.track.internal

import core.{Adding, Removed}
import core.Preamble._

import org.osgi.framework.{BundleActivator, BundleContext}

class Activator extends BundleActivator {

  override def start(ctx: BundleContext) {
    // Track Greeting services, but only polite ones by applying a filter
    ctx track classOf[Greeting] withFilter "(polite=true)" on {
      case Adding(grt, _)  => println("Adding polite Greeting: " + grt.welcome)
      case Removed(grt, _) => println("Removed polite Greeting: " + grt.goodbye)
    }

    // Track Greeting services once more using operator notation
    ctx >> classOf[Greeting] & {
      case Adding(grt, _)  => println("Adding Greeting: " + grt.welcome)
      case Removed(grt, _) => println("Removed Greeting: " + grt.goodbye)
    }
  }

  override def stop(ctx: BundleContext) {}
}
