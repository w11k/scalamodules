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
package org.scalamodules.core

import scala.collection.Map
import org.osgi.framework.{BundleContext, Filter, ServiceReference}
import org.osgi.util.tracker.ServiceTracker
import org.scalamodules.core.RichServiceReference.fromServiceReference

/**
 * Provides service tracking. 
 */
class Track[T](context: BundleContext, 
               serviceInterface: Class[T],
               filter: String) {

  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")
  
  def this(context: BundleContext, 
           serviceInterface: Class[T]) {
    this(context, serviceInterface, null)
  }

  /**
   * Sets the given filter for service look-ups.
   */
  def withFilter(filter: String) =
    new Track(context, serviceInterface, filter)

  /**
   * Handles a TrackEvent.
   */
  def on(f: TrackEvent[T] => Unit): Track[T] = {
    require(f != null, "TrackEvent handler function must not be null!")
    tracker = new ServiceTracker(context, createFilter, null) {
      override def addingService(ref: ServiceReference) = {
        val service = context.getService(ref)  // Cannot be null (-> spec.)
        f(Adding(service.asInstanceOf[T], ref.properties))
        service
      }
      override def modifiedService(ref: ServiceReference, service: AnyRef) = {
        f(Modified(service.asInstanceOf[T], ref.properties))
        context.ungetService(ref)
      }
      override def removedService(ref: ServiceReference, service: AnyRef) = {
        f(Removed(service.asInstanceOf[T], ref.properties))
        context.ungetService(ref)
      }
    }
    tracker.open()
    this
  }

  /**
   * Stops tracking.
   */
  def stop() {
    if (tracker != null) tracker.close()
  }
  
  private var tracker: ServiceTracker = _

  private def createFilter: Filter =  {
    val filterString = filter match {
      case null => String.format("(objectClass=%s)", serviceInterface.getName)
      case s    => String.format("(&(objectClass=%s)%s)", serviceInterface.getName, s)
    }
    context.createFilter(filterString)
  } 
}

/**
 * Super class for service tracking events.
 */
sealed abstract class TrackEvent[T](service: T, 
                                    properties: Map[String, AnyRef])

/**
 * A service is being added to the tracked services.
 */
case class Adding[T](service: T, 
                          properties: Map[String, AnyRef]) 
  extends TrackEvent[T](service, properties)

/**
 * A tracked service was modified.
 */
case class Modified[T](service: T, 
                            properties: Map[String, AnyRef]) 
  extends TrackEvent[T](service, properties)

/**
 * A service was removed from  the tracked services.
 */
case class Removed[T](service: T, 
                          properties: Map[String, AnyRef]) 
  extends TrackEvent[T](service, properties)
