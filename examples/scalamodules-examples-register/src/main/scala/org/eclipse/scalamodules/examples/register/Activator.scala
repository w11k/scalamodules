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
package register

import java.io.Serializable
import org.eclipse.scalamodules._
import org.osgi.framework.{ BundleActivator, BundleContext }

class Activator extends BundleActivator {

  override def start(context: BundleContext) {
    val greeting = new Greeting {
      override def welcome = "Hello!"
      override def goodbye = "Bye!"
    }
    context createService greeting

    val coolGreeting = new Greeting {
      override def welcome = "Hey!"
      override def goodbye = "See you!"
    }
    context createService (coolGreeting, Style -> "cool", interface[Greeting])

    val politeGreeting = new Greeting with Serializable {
      override def welcome = "Welcome!"
      override def goodbye = "Good-bye!"
    }
    context createService (politeGreeting,
                           interface1 = interface[Greeting],
                           interface2 = interface[Serializable],
                           properties = Style -> "polite")
  }

  override def stop(context: BundleContext) {}

  private val Style = "style"
}
