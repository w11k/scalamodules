/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Heiko Seeberger   - initial API and implementation
 *   Roman Roelofsen   - initial API and implementation
 *   Kjetil Valstadsve - initial API and implementation
 */
package org.eclipse.scalamodules
package examples
package find

import org.eclipse.scalamodules.core._
import org.osgi.framework.{ BundleActivator, BundleContext }

class Activator extends BundleActivator {

  override def start(context: BundleContext) {

    println("Find a Greeting service and print the result of calling welcome:")
    context findService withInterface[Greeting] andApply { _.welcome } match {
      case None          => println("No Greeting service available!")
      case Some(welcome) => println(welcome)
    }

    println("""Find all Greeting services and print their "style" property plus the result of calling welcome:""")
    context findServices withInterface[Greeting] andApply {
      (greeting, properties) => "%s: %s".format(properties get "style" getOrElse "UNKNOWN", greeting.welcome)
    } match {
      case Nil      => println("No Greeting service available!")
      case welcomes => welcomes foreach println
    }

    println("""Find all Greeting services matching the filter "style".present and print their "style" property plus the result of calling welcome:""")
    context findServices withInterface[Greeting] withFilter "style".present andApply {
      (greeting, properties) => "%s: %s".format(properties("style"), greeting.welcome)
    } match {
      case Nil      => println("No Greeting service available!")
      case welcomes => welcomes foreach println
    }
  }

  override def stop(context: BundleContext) {}
}
