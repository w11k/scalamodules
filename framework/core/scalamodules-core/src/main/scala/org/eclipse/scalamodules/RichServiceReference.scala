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

import org.osgi.framework.ServiceReference
import scala.collection.immutable.{Map => IMap}

/**
 * Pimped ServiceReference that eases using service properties.
 */
class RichServiceReference(serviceReference: ServiceReference) {
  require(serviceReference != null, "The ServiceReference must not be null!")

  /** Return the service properties as a Scala Map. */
  lazy val properties: Properties = IMap(fromServiceReference(serviceReference): _*)

  private def fromServiceReference(serviceReference: ServiceReference): Array[(String, Any)] = {
    serviceReference.getPropertyKeys match {
      case null => Array[(String, Any)]()
      case keys => keys map { key => (key, serviceReference getProperty key) }
    }
  }
}
