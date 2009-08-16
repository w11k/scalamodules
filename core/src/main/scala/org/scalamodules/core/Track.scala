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

import core.Filter.NilFilter
import Preamble.toRichServiceReference
import Util.toOption

import org.osgi.framework.{BundleContext, Filter => OSGiFilter, ServiceReference}
import org.osgi.util.tracker.ServiceTracker
import scala.collection.Map

/**
 * Provides service tracking.
 */
private class Track[I](ctx: BundleContext,
                       srvIntf: Class[I],
                       filter: Option[Filter]) {

  require(ctx != null, "BundleContext must not be null!")
  require(srvIntf != null, "Service interface must not be null!")
  require(filter != null, "Option for filter must not be null!")

  def this(ctx: BundleContext, srvIntf: Class[I]) =
    this(ctx, srvIntf, None)

  /**
   * Sets the given filter for service look-ups.
   */
  def %(filter: String) =
    withFilter(filter)

  /**
   * Sets the given filter for service look-ups.
   */
  def withFilter(filter: Filter) =
    new Track(ctx, srvIntf, filter)

  /**
   * Handles a TrackEvent.
   */
  def &(f: PartialFunction[TrackEvent[I], Unit]) =
    on(f)

  /**
   * Handles a TrackEvent.
   */
  def on(f: PartialFunction[TrackEvent[I], Unit]) = {
    require(f != null, "TrackEvent handler function must not be null!")
    tracker = new ServiceTracker(ctx, createFilter, null) {
      override def addingService(ref: ServiceReference) = {
        val service = ctx.getService(ref)  // Cannot be null (-> spec.)
        val trackEvent = Adding(service.asInstanceOf[I], ref.properties)
        if (f.isDefinedAt(trackEvent)) f(trackEvent)
        service
      }
      override def modifiedService(ref: ServiceReference, service: AnyRef) = {
        val trackEvent = Modified(service.asInstanceOf[I], ref.properties)
        if (f.isDefinedAt(trackEvent)) f(trackEvent)
        ctx.ungetService(ref)
      }
      override def removedService(ref: ServiceReference, service: AnyRef) = {
        val trackEvent = Removed(service.asInstanceOf[I], ref.properties)
        if (f.isDefinedAt(trackEvent)) f(trackEvent)
        ctx.ungetService(ref)
      }
    }
    tracker.open()
    tracker
  }

  private var tracker: ServiceTracker = _

  private def createFilter: OSGiFilter = ctx.createFilter(fullFilter asString)
  
  private def fullFilter = Filter.objectClass(srvIntf) && (filter getOrElse NilFilter)
}

/**
 * Super class for service tracking events.
 */
sealed abstract class TrackEvent[I](srv: I,
                                    props: Map[String, Any])

/**
 * A service is being added to the tracked services.
 */
case class Adding[I](srv: I,
                     props: Map[String, Any])
  extends TrackEvent[I](srv, props)

/**
 * A tracked service was modified.
 */
case class Modified[I](srv: I,
                       props: Map[String, Any])
  extends TrackEvent[I](srv, props)

/**
 * A service was removed from  the tracked services.
 */
case class Removed[I](srv: I,
                      props: Map[String, Any])
  extends TrackEvent[I](srv, props)
