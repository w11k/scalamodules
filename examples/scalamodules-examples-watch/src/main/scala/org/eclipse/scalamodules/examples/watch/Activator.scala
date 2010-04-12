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
package watch

import org.eclipse.scalamodules.core._
import org.osgi.framework.{ BundleActivator, BundleContext }

class Activator extends BundleActivator {

  override def start(context: BundleContext) {
    def message(property: Option[Any], s: String) = "ServiceEvent - %s: %s".format(property getOrElse "UNKNOWN", s)
    def styleProperty(properties: Properties) = properties get "style"
    context watchServices withInterface[Greeting] andHandle {
      case AddingService(greeting, properties)  => println(message(styleProperty(properties), greeting.welcome))
      case ServiceRemoved(greeting, properties) => println(message(styleProperty(properties), greeting.goodbye))
    }
  }

  override def stop(context: BundleContext) {}
}
