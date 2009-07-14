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

import java.util.Dictionary
import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.CoreOptions._
import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit._
import org.osgi.framework.BundleContext
import org.osgi.service.cm.ManagedService
import org.scalamodules.core.RegIndepInfo.toRegIndepInfo
import org.scalamodules.core.RegDepInfo.toRegDepInfo
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

@RunWith(classOf[MavenConfiguredJUnit4TestRunner])
class BundleTest {

  @Test
  def test() {

    // Start tracking
    var addingIndex = 0
    var removedIndex = 0
    var greetingStatus = "NONE"
    val track = context track classOf[Greeting] on {
      case Adding(_, _)  => addingIndex += 1; greetingStatus = "ADDING-" + addingIndex
      case Removed(_, _) => removedIndex += 1; greetingStatus = "REMOVED-" + removedIndex
    }

    // Get one service should result in None
    val noGreeting = context getOne classOf[Greeting] andApply { _.greet }
    assert(noGreeting == None, "But was: " + noGreeting)

    // Registering a service should result in greetingStatus == ADDING-1
    val hello = new Greeting {
      override val greet = "Hello!"
    }
    val helloRegistration = context register hello
    assert(greetingStatus == "ADDING-1", "But was: " + greetingStatus)

    // Get one service should result in Some("Hello!")
    val helloResult = context getOne classOf[Greeting] andApply { _.greet }
    assert(helloResult == Some("Hello!"), "But was: " + helloResult)

    // Register another service with properties should result in greetingStatus == ADDING-2
    val welcome = new Greeting {
      override val greet = "Welcome!"
    }
    val welcomeRegistration = 
      context register (welcome as classOf[Greeting] withProps ("service.ranking" -> 1, "name" -> "ScalaModules"))
    assert(greetingStatus == "ADDING-2", "But was: " + greetingStatus)

    // Get one service should result in Some("Welcome...")
    val welcomeResult = context getOne classOf[Greeting] andApply { 
      (greeting, properties) => {
        val name = properties.get("name") match {
          case None    => "UNKNOWN"
          case Some(s) => s
        }
        name + " says: " + greeting.greet
      }
    }
    assert(welcomeResult == Some("ScalaModules says: Welcome!"), "But was: " + welcomeResult)

    // Register another service with multiple service interfaces
    context register
      new Greeting with Introduction with Interested {
        override val greet = "Howdy!"
        override val myNameIs = "Multi-interface Service."
        override val andYours = "And what's your name?"
      }

    // Get one for Introduction should result in a successful look-up
    val introductionResult = context getOne classOf[Introduction] andApply { _ => true }
    assert(Some(true) == introductionResult, "But was: " + introductionResult)

    // Get one for Interested should result in a successful look-up
    val interestedResult = context getOne classOf[Interested] andApply { _ => true }
    assert(Some(true) == interestedResult, "But was: " + interestedResult)

    // Get many services should result in Some(List("Hello!", "Welcome!", "Howdy!))
    val greetingsResult = context getMany classOf[Greeting] andApply { _.greet }
    assert(greetingsResult != None)
    assert(List("Hello!", "Welcome!", "Howdy!").sort(Sorter) == greetingsResult.sort(Sorter), 
           "But was: " + greetingsResult)

    // Get many services with filter (!(name=*)) should result in Some(List("Hello!", "Howdy!"))
    val filteredGreetingsResult = 
      context getMany classOf[Greeting] withFilter "(!(name=*))" andApply { _.greet }
    assert(List("Hello!", "Howdy!").sort(Sorter) == filteredGreetingsResult.sort(Sorter), 
           "But was: " + filteredGreetingsResult)

    // Because of the partial function support this must not throw an error!
    welcomeRegistration setProperties new java.util.Hashtable[String, String]()

    // Unregistering a service should result in greetingStatus == "REMOVED-1"
    welcomeRegistration.unregister()
    assert(greetingStatus == "REMOVED-1", "But was: " + greetingStatus)

    // Stopping the tracking should result in greetingStatus == "REMOVED-3" (three Greeting services untracked) 
    track.stop()
    assert(greetingStatus == "REMOVED-3", "But was: " + greetingStatus)

    context register ({
      big: BigInt => 
        new Greeting with Introduction with Interested {
          override def greet = "Howdy!"
          override def myNameIs = "Multi-interface Service."
          override def andYours = "And what's your name?"
        }
      } withProps IMap("feature" -> "dependOn") dependOn classOf[BigInt])
    var result = context getMany classOf[Greeting] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result.isEmpty, "But size was: " + result.size) 

    val dependeeRegistration1 = context register BigInt(1)
    result = context getMany classOf[Greeting] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result != None) 
    assert(result.size == 1, "But size was: " + result.size)

    val dependeeRegistration2 = context register BigInt(2)
    result = context getMany classOf[Introduction] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result != None) 
    assert(result.size == 1, "But size was: " + result.size)

    dependeeRegistration2.unregister()
    result = context getMany classOf[Interested] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result != None) 
    assert(result.size == 1, "But size was: " + result.size)

    dependeeRegistration1.unregister()
    result = context getMany classOf[Greeting] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result.isEmpty, "But size was: " + result.size) 

//    // Register a managed service
//    val greeting = new Greeting with BaseManagedService {
//      override def handleUpdate(properties: Option[Map[String, Any]]) {
//        properties match {
//          case None        => salutation = "SALUTATION"; message = "MESSAGE"
//          case Some(props) => {
//            props.get("salutation") match {
//              case None        => salutation = "SALUTATION"
//              case Some(value) => salutation = value.toString;
//            }
//            props.get("message") match {
//              case None        => message = "MESSAGE"
//              case Some(value) => message = value.toString
//            }
//          }
//        }
//      }
//      override def greet = salutation + " " + message
//      private var salutation = "SALUTATION"
//      private var message = "MESSAGE"
//    }
//    context registerAs classOf[Greeting] andAs classOf[ManagedService] withProperties 
//      immutable.Map("name" -> "CM", "service.pid" -> "CM") theService greeting
//
//    // Replace configuration for greeting service
//    context configure "CM" replaceWith (immutable.Map("salutation" -> "REPLACED"))
//    Thread sleep 1000
//    // Get many services with filter (name=CM)) should result in Some(List("REPLACED MESSAGE"))
//    var cmResult = 
//      context getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
//    assert(Some(List("REPLACED MESSAGE")) == cmResult, "Was " + cmResult)
//
//    // Update configuration for greeting service
//    context configure "CM" updateWith (immutable.Map("message" -> "REPLACED"))
//    Thread sleep 1000
//    // Get many services with filter (name=CM)) should result in Some(List("test"))
//    cmResult = 
//      context getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
//    assert(Some(List("REPLACED REPLACED")) == cmResult, "Was " + cmResult)
//
//    // Replace configuration for greeting service once more
//    context configure "CM" replaceWith (immutable.Map("salutation" -> "REPLACED"))
//    Thread sleep 1000
//    // Get many services with filter (name=CM)) should result in Some(List("REPLACED MESSAGE"))
//    cmResult = 
//      context getMany classOf[Greeting] withFilter "(name=CM)" andApply { _.greet }
//    assert(Some(List("REPLACED MESSAGE")) == cmResult, "Was " + cmResult)
  }

  private val Sorter = (s1: String, s2: String) => s1 < s2

  @Inject
  private var context: BundleContext = _
}
