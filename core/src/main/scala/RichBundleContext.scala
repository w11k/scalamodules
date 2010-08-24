/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.osgi.framework.{ BundleContext, ServiceRegistration }
import scala.annotation.tailrec

private[scalamodules] class RichBundleContext(context: BundleContext) {

  assert(context != null, "The BundleContext must not be null!")

  def createService[S <: AnyRef, I1 >: S <: AnyRef, I2 >: S <: AnyRef, I3 >: S <: AnyRef](
      service: S,
      properties: Props = Map.empty,
      interface1: Option[Class[I1]] = None,
      interface2: Option[Class[I2]] = None,
      interface3: Option[Class[I3]] = None): ServiceRegistration = {

    require(service != null, "The service object must not be null!")
    require(properties != null, "The service properties must not be null!")
    require(interface1 != null, "The first service interface must not be null!")
    require(interface2 != null, "The second service interface must not be null!")
    require(interface3 != null, "The third service interface must not be null!")

    val interfaces = {
      lazy val allInterfacesOrClass = {
        def allInterfaces(clazz: Class[_]) = {
          def interfacesWithoutScalaObject(clazz: Class[_]) =
            clazz.getInterfaces.toList filter { _ != classOf[ScalaObject] }
          @tailrec
          def allInterfacesTR(interfaces: List[Class[_]], result: List[Class[_]]): List[Class[_]] =
            interfaces match {
              case Nil => result
              case _ => {
                val nextInterfaces = interfaces flatMap interfacesWithoutScalaObject
                allInterfacesTR(nextInterfaces, interfaces ::: result)
              }
            }
          allInterfacesTR(interfacesWithoutScalaObject(clazz), Nil).distinct
        }
        val interfaces = allInterfaces(service.getClass).toArray
        if (!interfaces.isEmpty) interfaces map { _.getName } else Array(service.getClass.getName)
      }
      val interfaces = List(interface1, interface2, interface3).flatten map { _.getName }
      if (!interfaces.isEmpty) interfaces.toArray else allInterfacesOrClass
    }

    context.registerService(interfaces, service, if (properties.isEmpty) null else properties)
  }

  def findService[I <: AnyRef](interface: Class[I]): ServiceFinder[I] = {
    require(interface != null, "The service interface must not be null!")
    new ServiceFinder(interface, context)
  }

  def findServices[I <: AnyRef](interface: Class[I]): ServicesFinder[I] = {
    require(interface != null, "The service interface must not be null!")
    new ServicesFinder(interface, context)
  }

  def watchServices[I <: AnyRef](interface: Class[I]): ServicesWatcher[I] = {
    require(interface != null, "The service interface must not be null!")
    new ServicesWatcher(interface, context)
  }
}
