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
package org.eclipse.scalamodules.test

import org.eclipse.scalamodules._

import java.lang.String
import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner
import org.osgi.framework.BundleContext
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Map

@org.junit.runner.RunWith(classOf[MavenConfiguredJUnit4TestRunner])
class BundleTest extends ShouldMatchers {

  @org.junit.Test
  def test() {
    val Service1 = "service1"
    val Service2 = "service2"
    val services = Map[String, String]()

    context watchServices withInterface[ServiceInterface] withFilter ForFilter.present andHandle {
      case AddingService(service, properties)   => services += (service.name -> nameProperty(properties))
      case ServiceModified(service, properties) => services += (service.name -> nameProperty(properties))
      case ServiceRemoved(service, properties)  => services -= service.name
    }

    context findService withInterface[ServiceInterface] andApply { s => s } should be (None)

    val service1 = ServiceImplementation(Service1)
    val service1Registration = context.createService(service1, Map(Name -> Service1, ForFilter -> "true"))
    context findService withInterface[ServiceInterface] andApply { _.name } should be (Some(Service1))
    services should have size (1)
    services should contain key (Service1)
    services should contain value (Service1)

    val service2 = ServiceImplementation(Service2)
    val service2Registration = context.createService(service2, Name -> Service2)
    val names = context findServices withInterface[ServiceInterface] andApply {
      (service, properties) => service.name + nameProperty(properties)
    }
    names should have size (2)
    names should contain (Service1 + Service1)
    names should contain (Service2 + Service2)
    services should have size (1)

    val dummies = context findServices withInterface[ServiceInterface] withFilter "name" === "service1" andApply {
      (_, _) => "dummy"
    }
    dummies should have size (1)

    service2Registration.unregister()
    services should have size (1)

    service1Registration setProperties Map(Name -> "CHANGED", ForFilter -> "true")
    services should have size (1)
    services should contain key (Service1)
    services should contain value ("CHANGED")
  }

  private val Name = "name"
  private val ForFilter = "forfilter"

  @Inject
  private var context: BundleContext = _

  private def nameProperty(properties: Properties) = properties get Name getOrElse "" toString

  private trait ServiceInterface {
    def name: String
  }

  private case class ServiceImplementation(name: String) extends ServiceInterface
}
