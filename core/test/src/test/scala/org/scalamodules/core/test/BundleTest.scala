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
package org.scalamodules.core.test

import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit.JUnit4TestRunner
import org.osgi.framework.BundleContext
import org.scalamodules.core._
import org.scalamodules.core.RichBundleContext.fromBundleContext
import org.scalamodules.exam.ExamTest

@RunWith(classOf[JUnit4TestRunner])
class BundleTest extends ExamTest {

  addWrappedBundle("org.ops4j.pax.exam", "pax-exam-junit", "0.3.0-SNAPSHOT")
  addBundle("org.scalamodules", "scalamodules.util", "1.0.0")
  addBundle("org.scalamodules", "scalamodules.core", "1.0.0")

  @Inject
  private var context: BundleContext = _
  
  @Test
  def test() {

    // Start tracking
    var greetingStatus = "NONE" 
    val track = context track classOf[Greeting] onEvent {
      case AddingEvent(service, properties)   => greetingStatus = "ADDING" 
      case ModifiedEvent(service, properties) => greetingStatus = "MODIFIED"
      case RemovedEvent(service, properties)  => greetingStatus = "REMOVED"
    }
    
    // Get one service should result in None
    val noGreeting = context getOne classOf[Greeting] andApply { _ => }
    assert(noGreeting == None)
    
    // Register a service
    val hello = new Greeting {
      override def greet = "Hello!"
    }
    val helloRegistration = context registerAs classOf[Greeting] theService hello
    assert(greetingStatus == "ADDING")
    
    // Get one service should result in Some("Hello!")
    val helloResult = context getOne classOf[Greeting] andApply { _.greet }
    assert(helloResult == Some("Hello!"))
    
    // Register another service with properties
    val welcome = new Greeting {
      override def greet = "Welcome!"
    }
    val properties = Map("service.ranking" -> 1, 
                         "name" -> "Welcome-Greeting")
    val welcomeRegistration = 
      context registerAs classOf[Greeting] withProperties properties theService welcome
    assert(greetingStatus == "ADDING")

    // Get one service should result in Some("Welcome...")
    val welcomeResult = context getOne classOf[Greeting] andApply { 
      (greeting, properties) => {
        val name = properties.get("name") match {
          case None    => "UNKNOWN"
          case Some(s) => s
        }
        name + " sais: " + greeting.greet
      }
    }
    assert(welcomeResult == Some("Welcome-Greeting sais: Welcome!"), "Was: " + welcomeResult)

    // Get many services should result in Some(List("Hello!", "Welcome!"))
    val greetingsResult = context getMany classOf[Greeting] andApply { _.greet }
    assert(greetingsResult != None)
    val sorter = (s1: String, s2: String) => s1 < s2
    assert(List("Hello!", "Welcome!").sort(sorter) ==  greetingsResult.get.sort(sorter))

    // Get many services with filter (!(name=*)) should result in Some(List("Hello!"))
    val filteredGreetingsResult = 
      context getMany classOf[Greeting] withFilter "(!(name=*))" andApply { _.greet }
    assert(filteredGreetingsResult == Some(List("Hello!")))
    
    // Unregistering a service should result in greetingStatus == "REMOVED" 
    welcomeRegistration.unregister()
    assert(greetingStatus == "REMOVED")

    greetingStatus = "WRONG"
    track.stop()
    assert(greetingStatus == "REMOVED")
  }
}
