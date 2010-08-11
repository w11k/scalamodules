/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.ServiceReference

/**
 * Pimped ServiceReference offering easier handling of service properties.
 */
class RichServiceReference(serviceReference: ServiceReference) {

  require(serviceReference != null, "The ServiceReference must not be null!")

  /**
   * Gives access to service properties as Props (Scala Map).
   */
  lazy val properties: Props = Map(propsFrom(serviceReference): _*)

  private def propsFrom(serviceReference: ServiceReference): Array[(String, Any)] = {
    serviceReference.getPropertyKeys match {
      case null => Array[(String, Any)]()
      case keys => keys map { key => (key, serviceReference getProperty key) }
    }
  }
}
