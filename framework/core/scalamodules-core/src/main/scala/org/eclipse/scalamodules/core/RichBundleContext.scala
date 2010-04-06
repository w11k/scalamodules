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
package core

import org.osgi.framework.{ BundleContext, ServiceRegistration }

private[scalamodules] class RichBundleContext(context: BundleContext) {
  require(context != null, "The BundleContext must not be null!")

  def createService[S <: AnyRef,
                    I1 >: S <: AnyRef,
                    I2 >: S <: AnyRef,
                    I3 >: S <: AnyRef]
                   (service: S,
                    properties: Properties = Map.empty,
                    interface1: Option[Class[I1]] = None,
                    interface2: Option[Class[I2]] = None,
                    interface3: Option[Class[I3]] = None): ServiceRegistration = {
    require(service != null, "The service object must not be null!")
    require(properties != null, "The service properties must not be null!")
    require(interface1 != null, "The first service interface must not be null!")
    require(interface2 != null, "The second service interface must not be null!")
    require(interface3 != null, "The third service interface must not be null!")

    def interfaces: Array[String] = {
      def allInterfaces[S](clazz: Class[S]): Array[Class[_]] = {
        val interfaces = clazz.getInterfaces filter { _ != classOf[ScalaObject] }
        interfaces ++ (interfaces flatMap { allInterfaces(_) } filter { i => !(interfaces contains i) })
      }
      def allInterfacesOrClass = {
        val interfaces = allInterfaces(service.getClass)
        if (interfaces.isEmpty) Array(service.getClass.getName) else interfaces map { i => i.getName }
      }
      val interfaces = Traversable(interface1, interface2, interface3) flatMap { i => i } map { _.getName }
      if (!interfaces.isEmpty) interfaces.toArray else allInterfacesOrClass
    }

    if (properties.isEmpty) context.registerService(interfaces, service, null)
    else context.registerService(interfaces, service, properties)
  }

  def findService[I <: AnyRef](interface: Class[I]): ServiceFinder[I] = {
    require(interface != null, "The service interface must not be null!")
    new ServiceFinder(interface)(context)
  }

  def findServices[I <: AnyRef](interface: Class[I]): ServicesFinder[I] = {
    require(interface != null, "The service interface must not be null!")
    new ServicesFinder(interface)(context)
  }

  def watchServices[I <: AnyRef](interface: Class[I]): ServicesWatcher[I] = {
    require(interface != null, "The service interface must not be null!")
    new ServicesWatcher(interface)(context)
  }
}
