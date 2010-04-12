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
package org.eclipse.scalamodules.core

import org.osgi.framework.{ BundleContext, ServiceReference }
import org.osgi.framework.Constants._
import org.osgi.util.tracker.ServiceTracker

/** Super class for service events. */
sealed abstract class ServiceEvent[I](service: I, properties: Properties) {
  require(service != null, "The service must not be null!")
  require(properties != null, "The service properties must not be null!")
}

/** A service is being added to the watched (tracked) services. */
case class AddingService[I](service: I, properties: Properties) extends ServiceEvent[I](service, properties)

/** A watched (tracked) service was modified. */
case class ServiceModified[I](service: I, properties: Properties) extends ServiceEvent[I](service, properties)

/** A service was removed from the watched (tracked) services. */
case class ServiceRemoved[I](service: I, properties: Properties) extends ServiceEvent[I](service, properties)

private[scalamodules] class ServicesWatcher[I <: AnyRef]
                                           (interface: Class[I], filter: Option[Filter] = None)
                                           (context: BundleContext) {
  require(interface != null, "The service interface must not be null!")
  require(filter != null, "The filter must not be null!")
  require(context != null, "The BundleContext must not be null!")

  def withFilter(filter: Filter) = {
    require(filter != null, "The filter must not be null!")
    new ServicesWatcher(interface, Some(filter))(context)
  }

  def andHandle(f: PartialFunction[ServiceEvent[I], Unit]) {
    require(f != null, "The partial function to handle ServiceEvents must not be null!")

    val filterString = filter match {
      case None    => Filter(OBJECTCLASS === interface.getName).toString
      case Some(f) => Filter((OBJECTCLASS === interface.getName) && f.component).toString
    }
    val tracker = new ServiceTracker(context, context createFilter filterString, null) {
      override def addingService(serviceReference: ServiceReference) = {
        val service = context getService serviceReference
        val serviceEvent = AddingService(service.asInstanceOf[I], serviceReference.properties)
        if (f.isDefinedAt(serviceEvent)) f(serviceEvent)
        service
      }
      override def modifiedService(serviceReference: ServiceReference, service: AnyRef) {
        val serviceEvent = ServiceModified(service.asInstanceOf[I], serviceReference.properties)
        if (f.isDefinedAt(serviceEvent)) f(serviceEvent)
      }
      override def removedService(serviceReference: ServiceReference, service: AnyRef) {
        val serviceEvent = ServiceRemoved(service.asInstanceOf[I], serviceReference.properties)
        if (f.isDefinedAt(serviceEvent)) f(serviceEvent)
        context ungetService serviceReference
      }
    }
    tracker.open()
  }
}
