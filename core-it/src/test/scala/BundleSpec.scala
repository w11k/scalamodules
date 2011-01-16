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
package test

import org.junit.runners.Suite.SuiteClasses
import org.osgi.framework.BundleContext
import org.specs.SpecsMatchers
import scala.collection.mutable

@SuiteClasses(Array(classOf[BundleSpec]))
class BundleSpec(context: BundleContext) extends SpecsMatchers {

  def test() {
    val Service1 = "1"
    val Service2 = "service2"
    val services = mutable.Map[String, String]()

    context watchServices withInterface[ServiceInterface] withFilter ForFilter.present andHandle {
      case AddingService(service, properties)   => services += (service.name -> nameProperty(properties))
      case ServiceModified(service, properties) => services += (service.name -> nameProperty(properties))
      case ServiceRemoved(service, properties)  => services -= service.name
    }

    context findService withInterface[ServiceInterface] andApply { s => s } mustBe None

    val service1 = ServiceImplementation(Service1)
    val service1Registration = context.createService(service1, Map(Name -> 1, ForFilter -> "true"))
    context findService withInterface[ServiceInterface] andApply { _.name } mustEqual Some(Service1)
    services must haveSize(1)
    services must havePair(Service1 -> Service1)

    val service2 = ServiceImplementation(Service2)
    val service2Registration = context.createService(service2, Name -> Service2)
    val names = context findServices withInterface[ServiceInterface] andApply {
      (service, properties) => service.name + nameProperty(properties)
    }
    names must haveSize(2)
    names mustContain Service1 + Service1
    names mustContain Service2 + Service2
    services must haveSize(1)

    val dummies = context findServices withInterface[ServiceInterface] withFilter "name" === Service1 andApply {
      (_, _) => "dummy"
    }
    dummies must haveSize(1)

    service2Registration.unregister()
    services must haveSize(1)

    service1Registration setProperties Map(Name -> "CHANGED", ForFilter -> "true")
    services must haveSize(1)
    services must havePair (Service1 -> "CHANGED")
  }

  private val Name = "name"
  private val ForFilter = "forfilter"

  private def nameProperty(properties: Props) = properties get Name getOrElse "" toString

  private trait ServiceInterface {
    def name: String
  }

  private case class ServiceImplementation(name: String) extends ServiceInterface
}
