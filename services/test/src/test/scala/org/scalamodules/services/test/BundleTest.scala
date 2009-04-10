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

import java.util.Dictionary
import scala.collection.Map
import scala.collection.immutable
import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.CoreOptions._
import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit.Configuration
import org.ops4j.pax.exam.junit.JUnit4TestRunner
import org.osgi.framework.BundleContext
import org.osgi.service.cm.ManagedService
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import org.scalamodules.services.ServicesRichBundleContext.toServicesRichBundleContext
import org.scalamodules.services.cm._
import org.scalamodules.exam.ExamTest

@RunWith(classOf[JUnit4TestRunner])
class BundleTest extends ExamTest {

  addBundle("org.scalamodules", "scalamodules.util", "1.0.0")
  addBundle("org.scalamodules", "scalamodules.core", "1.0.0")
  addBundle("org.scalamodules", "scalamodules.services", "1.0.0")
  addBundle("org.apache.felix", "org.apache.felix.configadmin", "1.0.10")
  
//  @Configuration
//  override def configuration = options(equinox, provision(bundles.toArray: _*))
  
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
    context registerAs classOf[Greeting] andAs classOf[ManagedService] withProperties 
      immutable.Map("name" -> "CM", "service.pid" -> "CM") theService greeting

    // Replace configuration for greeting service
    context configure "CM" replaceWith (immutable.Map("salutation" -> "REPLACED"))
    Thread sleep 1000
    // Get many services with filter (name=CM)) should result in Some(List("REPLACED MESSAGE"))
    var cmResult = 
      context getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
    assert(Some(List("REPLACED MESSAGE")) == cmResult, "Was " + cmResult)

    // Update configuration for greeting service
    context configure "CM" updateWith (immutable.Map("message" -> "REPLACED"))
    Thread sleep 1000
    // Get many services with filter (name=CM)) should result in Some(List("test"))
    cmResult = 
      context getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
    assert(Some(List("REPLACED REPLACED")) == cmResult, "Was " + cmResult)

    // Replace configuration for greeting service once more
    context configure "CM" replaceWith (immutable.Map("salutation" -> "REPLACED"))
    Thread sleep 1000
    // Get many services with filter (name=CM)) should result in Some(List("REPLACED MESSAGE"))
    cmResult = 
      context getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
    assert(Some(List("REPLACED MESSAGE")) == cmResult, "Was " + cmResult)
  }

  @Inject
  private var context: BundleContext = _
}
