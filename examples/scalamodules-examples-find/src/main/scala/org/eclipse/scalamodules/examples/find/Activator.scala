/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scalamodules
package examples
package find

import org.eclipse.scalamodules.core._
import org.osgi.framework.{ BundleActivator, BundleContext }

class Activator extends BundleActivator {

  override def start(context: BundleContext) {
    // Find a service and call it
    context findService withInterface[Greeting] andApply { _.welcome } match {
      case None          => println("No Greeting service available!")
      case Some(welcome) => println(welcome)
    }
    // Find all services and call these also making use of its properties
    context findServices withInterface[Greeting] andApply {
      (greeting, properties) => "%s: %s".format(properties get "style" getOrElse "UNKNOWN", greeting.welcome) }
    match {
      case Nil      => println("No Greeting service available!")
      case welcomes => welcomes foreach println
    }
  }

  override def stop(context: BundleContext) {}
}
