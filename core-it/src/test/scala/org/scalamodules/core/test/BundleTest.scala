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

import Preamble._

import java.util.{Date, Dictionary}
import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.CoreOptions._
import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit._
import org.osgi.framework._
import org.osgi.service.cm.ManagedService
import org.scalamodules.core.Filter._
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
    val tracker = ctx >> classOf[Greeting] & {
      case Adding(_, _)  => addingIndex += 1; greetingStatus = "ADDING-" + addingIndex
      case Removed(_, _) => removedIndex += 1; greetingStatus = "REMOVED-" + removedIndex
    }

    // Get one service should result in None
    val noGreeting = ctx ?> classOf[Greeting] & { _.greet }
    assert(noGreeting == None, "But was: " + noGreeting)

    // Registering a service should result in greetingStatus == ADDING-1
    val hello = new Greeting {
      override val greet = "Hello!"
    }
    val helloRegistration = ctx < hello
    assert(greetingStatus == "ADDING-1", "But was: " + greetingStatus)

    // Get one service should result in Some("Hello!")
    val helloResult = ctx ?> classOf[Greeting] & { _.greet }
    assert(helloResult == Some("Hello!"), "But was: " + helloResult)

    // Register another service with properties should result in greetingStatus == ADDING-2
    val welcome = new Greeting {
      override val greet = "Welcome!"
    }
    val welcomeRegistration =
      ctx < welcome / classOf[Greeting] % ("service.ranking" -> 1, "name" -> "ScalaModules")
    assert(greetingStatus == "ADDING-2", "But was: " + greetingStatus)

    // Get one service should result in Some("Welcome...")
    val welcomeResult = ctx ?> classOf[Greeting] & {
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
    val multiRegistration = ctx <
      new Greeting with Introduction with Interested {
        override val greet = "Howdy!"
        override val myNameIs = "Multi-interface Service."
        override val andYours = "And what's your name?"
      }

    // Get one for Introduction should result in a successful look-up
    val introductionResult = ctx ?> classOf[Introduction] & { _ => true }
    assert(Some(true) == introductionResult, "But was: " + introductionResult)

    // Get one for Interested should result in a successful look-up
    val interestedResult = ctx ?> classOf[Interested] & { _ => true }
    assert(Some(true) == interestedResult, "But was: " + interestedResult)

    // Get many services should result in Some(List("Hello!", "Welcome!", "Howdy!))
    val greetingsResult = ctx *> classOf[Greeting] & { _.greet }
    assert(greetingsResult != None)
    assert(List("Hello!", "Welcome!", "Howdy!").sort(Sorter) == greetingsResult.sort(Sorter),
           "But was: " + greetingsResult)

    // Get many services with filter (!(name=*)) should result in Some(List("Hello!", "Howdy!"))
    val filteredGreetingsResult =
      ctx *> classOf[Greeting] % notSet("name") & { _.greet }
    assert(List("Hello!", "Howdy!").sort(Sorter) == filteredGreetingsResult.sort(Sorter),
           "But was: " + filteredGreetingsResult)

    // Because of the partial function support this must not throw an error!
    welcomeRegistration setProperties new java.util.Hashtable[String, String]()

    // Unregistering a service should result in greetingStatus == "REMOVED-1"
    welcomeRegistration.unregister()
    assert(greetingStatus == "REMOVED-1", "But was: " + greetingStatus)

    // Stopping the tracking should result in greetingStatus == "REMOVED-3" (three Greeting services untracked)
    tracker.close()
    assert(greetingStatus == "REMOVED-3", "But was: " + greetingStatus)

    helloRegistration.unregister()
    multiRegistration.unregister()

    class GreetingReverser(grt: Greeting) extends Reverser {
      val reverse = new StringBuilder(grt.greet).reverse.toString
    }

    ctx < { grt: Greeting => new GreetingReverser(grt) } % IMap("feature" -> "dependOn")
    var result = ctx *> classOf[Reverser] % ("feature" === "dependOn") & { _ => }
    assert(result.isEmpty, "But size was: " + result.size)

    val dependeeRegistration1 = ctx < hello
    result = ctx *> classOf[Reverser] % ("feature" === "dependOn") & { _ => }
    assert(result != None)
    assert(result.size == 1, "But size was: " + result.size)

    val dependeeRegistration2 = ctx < welcome
    result = ctx *> classOf[Reverser] % ("feature" === "dependOn") & { _ => }
    assert(result != None)
    assert(result.size == 1, "But size was: " + result.size)

    dependeeRegistration2.unregister()
    result = ctx *> classOf[Reverser] % ("feature" === "dependOn") & { _ => }
    assert(result != None)
    assert(result.size == 1, "But size was: " + result.size)

    dependeeRegistration1.unregister()
    result = ctx *> classOf[Reverser] % ("feature" === "dependOn") & { _ => }
    assert(result.isEmpty, "But size was: " + result.size)
  }

  private val Sorter = (s1: String, s2: String) => s1 < s2

  @Inject
  private var ctx: BundleContext = _
}
