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
package org.scalamodules.services.test;

import Preamble._
import core.Preamble._

import java.util.Dictionary
import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.CoreOptions._
import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit._
import org.osgi.framework.BundleContext
import org.osgi.service.cm.ManagedService
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

@RunWith(classOf[MavenConfiguredJUnit4TestRunner])
class BundleTest {

  @Test
  def test() {

    // Register a managed service
    val greeting = new Greeting with BaseManagedService {
      override def handleUpdate(properties: Option[Map[String, Any]]) {
        properties match {
          case None        => salutation = "SALUTATION"; message = "MESSAGE"
          case Some(props) => {
            props.get("salutation") match {
              case None        => salutation = "SALUTATION"
              case Some(value) => salutation = value.toString;
            }
            props.get("message") match {
              case None        => message = "MESSAGE"
              case Some(value) => message = value.toString
            }
          }
        }
      }
      override def greet = salutation + " " + message
      private var salutation = "SALUTATION"
      private var message = "MESSAGE"
    }
    ctx register (greeting withProps ("name" -> "CM", "service.pid" -> "CM"))

    // Replace configuration for greeting service
    ctx configure "CM" replaceWith (IMap("salutation" -> "REPLACED"))
    Thread sleep 1000
    // Get many services with filter (name=CM)) should result in Some(List("REPLACED MESSAGE"))
    var result = ctx getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
    assert(List("REPLACED MESSAGE") == result, "Was " + result)

    // Update configuration for greeting service
    ctx configure "CM" updateWith (("message" -> "REPLACED"))
    Thread sleep 1000
    // Get many services with filter (name=CM)) should result in Some(List("test"))
    result = ctx getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
    assert(List("REPLACED REPLACED") == result, "Was " + result)

    // Replace configuration for greeting service once more
    ctx configure "CM" replaceWith (("salutation" -> "REPLACED"))
    Thread sleep 1000
    // Get many services with filter (name=CM)) should result in Some(List("REPLACED MESSAGE"))
    result = ctx getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
    assert(List("REPLACED MESSAGE") == result, "Was " + result)
  }

  @Inject
  private var ctx: BundleContext = _
}
