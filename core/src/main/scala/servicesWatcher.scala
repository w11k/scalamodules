/*
 * Copyright 2009-2011 Weigle Wilczek GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.{ BundleContext, ServiceReference }
import org.osgi.framework.Constants._
import org.osgi.util.tracker.ServiceTracker

/**
 * Base class for service events.
 * @param service The service for this event; must not be null!
 * @param properties The service properties for this event; must not be null!
 */
sealed abstract class ServiceEvent[I](service: I, properties: Props) {
  require(service != null, "The service must not be null!")
  require(properties != null, "The service properties must not be null!")
}

/**
 * Service event for a service being added to the watched (tracked) services.
 * @param service The service for this event
 * @param properties The service properties for this event; must not be null!
 */
case class AddingService[I](service: I, properties: Props)
  extends ServiceEvent[I](service, properties)

/**
 * Service event for a watched (tracked) service having been modified.
 * @param service The service for this event
 * @param properties The service properties for this event; must not be null!
 */
case class ServiceModified[I](service: I, properties: Props)
  extends ServiceEvent[I](service, properties)

/**
 * Service event for a watched (tracked) service having been removed from the watched ones.
 * @param service The service for this event
 * @param properties The service properties for this event; must not be null!
 */
case class ServiceRemoved[I](service: I, properties: Props)
  extends ServiceEvent[I](service, properties)

private[scalamodules] class ServicesWatcher[I <: AnyRef](
    interface: Class[I],
    context: BundleContext,
    filter: Option[Filter] = None) {

  assert(interface != null, "The service interface must not be null!")
  assert(context != null, "The BundleContext must not be null!")
  assert(filter != null, "The filter must not be null!")

  /**
   * Additionally use the given Filter for finding services.
   * @param filter The Filter to be added to this ServiceWatcher; must not be null!
   * @return A ServiceWatcher for a service interface and the given Filter
   */
  def withFilter(filter: Filter) = {
    require(filter != null, "The filter must not be null!")
    new ServicesWatcher(interface, context, Some(filter))
  }

  /**
   * Handles ServiceEvents by applying the given handler function.
   * @param handler The handler to be used for ServiceEvents; must not be null!
   */
  def andHandle(handler: PartialFunction[ServiceEvent[I], Unit]) {

    require(handler != null, "The handler for ServiceEvents must not be null!")

    val fullFilter = filter match {
      case None => Filter(OBJECTCLASS === interface.getName)
      case Some(f) => Filter(OBJECTCLASS === interface.getName && f.filterComponent)
    }

    val tracker = new ServiceTracker(context, context createFilter fullFilter.toString, null) {

      override def addingService(serviceReference: ServiceReference) = {
        val service = context getService serviceReference
        val serviceEvent = AddingService(service.asInstanceOf[I], serviceReference.properties)
        if (handler.isDefinedAt(serviceEvent)) {
          handler(serviceEvent)
          logger info "Handled AddingService event."
        }
        service
      }

      override def modifiedService(serviceReference: ServiceReference, service: AnyRef) {
        val serviceEvent = ServiceModified(service.asInstanceOf[I], serviceReference.properties)
        if (handler.isDefinedAt(serviceEvent)) {
          handler(serviceEvent)
          logger info "Handled ServiceModified event."
        }
      }

      override def removedService(serviceReference: ServiceReference, service: AnyRef) {
        val serviceEvent = ServiceRemoved(service.asInstanceOf[I], serviceReference.properties)
        if (handler.isDefinedAt(serviceEvent)) {
          handler(serviceEvent)
          logger info "Handled ServiceRemoved event."
        }
        context ungetService serviceReference
      }
    }

    tracker.open()
  }
}
