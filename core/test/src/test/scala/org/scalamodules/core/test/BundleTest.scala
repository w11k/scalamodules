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
import org.scalamodules.core._
import org.scalamodules.core.RichBundleContext.toRichBundleContext
import org.scalamodules.exam.ExamTest

@RunWith(classOf[JUnit4TestRunner])
class BundleTest extends ExamTest {

  addWrappedBundle("org.ops4j.pax.exam", "pax-exam-junit", "0.3.0")
  addBundle("org.scalamodules", "scalamodules.util", "1.0.0")
  addBundle("org.scalamodules", "scalamodules.core", "1.0.0")
  addBundle("org.apache.felix", "org.apache.felix.configadmin", "1.0.10")
  
//  @Configuration
//  override def configuration = options(equinox, provision(bundles.toArray: _*))

  @Inject
  private var context: BundleContext = _
  
  @Test
  def test() {

    // Start tracking
    var greetingStatus = "NONE" 
    val track = context track classOf[Greeting] on {
      case Adding(service, properties)   => greetingStatus = "ADDING" 
      case Modified(service, properties) => greetingStatus = "MODIFIED"
      case Removed(service, properties)  => greetingStatus = "REMOVED"
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
    val properties = immutable.Map("service.ranking" -> 1, 
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

    context registerAs classOf[Greeting] andAs classOf[Introduction] andAs classOf[Interested] theService
      new Greeting with Introduction with Interested {
        override def greet = "Howdy!"
        override def myNameIs = "Multi-interface Service."
        override def andYours = "And what's your name?"
      }
    
    // Get one for Introduction should result in a successful look-up
    val introductionResult = context getOne classOf[Introduction] andApply { _ => true }
    assert(Some(true) == introductionResult, "But was: " + introductionResult)

    // Get one for Interested should result in a successful look-up
    val interestedResult = context getOne classOf[Interested] andApply { _ => true }
    assert(Some(true) == interestedResult, "But was: " + interestedResult)

    // Get many services should result in Some(List("Hello!", "Welcome!"))
    val greetingsResult = context getMany classOf[Greeting] andApply { _.greet }
    assert(greetingsResult != None)
    val sorter = (s1: String, s2: String) => s1 < s2
    assert(List("Hello!", "Welcome!", "Howdy!").sort(sorter) == greetingsResult.get.sort(sorter), 
           "But was: " + greetingsResult)

    // Get many services with filter (!(name=*)) should result in Some(List("Hello!", "Howdy!"))
    val filteredGreetingsResult = 
      context getMany classOf[Greeting] withFilter "(!(name=*))" andApply { _.greet }
    assert(List("Hello!", "Howdy!").sort(sorter) == filteredGreetingsResult.get.sort(sorter), 
           "But was: " + filteredGreetingsResult)
    
    // Unregistering a service should result in greetingStatus == "REMOVED" 
    welcomeRegistration.unregister()
    assert(greetingStatus == "REMOVED")

    // Stopping the tracking should result in greetingStatus == "REMOVED" 
    greetingStatus = "WRONG"
    track.stop()
    assert(greetingStatus == "REMOVED")
    
    context registerAs classOf[Greeting] andAs classOf[Introduction] andAs classOf[Interested] withProperties 
      immutable.Map("feature" -> "dependOn") dependOn classOf[BigInt] theService { _ => 
        new Greeting with Introduction with Interested {
          override def greet = "Howdy!"
          override def myNameIs = "Multi-interface Service."
          override def andYours = "And what's your name?"
        }
    }
    var result = context getMany classOf[Greeting] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result == None) 
    
    val dependeeRegistration1 = context registerAs classOf[BigInt] theService { BigInt(1) }
    result = context getMany classOf[Greeting] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result != None) 
    assert(result.get.size == 1)
    
    val dependeeRegistration2 = context registerAs classOf[BigInt] theService { BigInt(2) }
    result = context getMany classOf[Introduction] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result != None) 
    assert(result.get.size == 1)
    
    dependeeRegistration2.unregister()
    result = context getMany classOf[Interested] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result != None) 
    assert(result.get.size == 1)

    dependeeRegistration1.unregister()
    result = context getMany classOf[Greeting] withFilter "(feature=dependOn)" andApply { _ => }
    assert(result == None) 

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
}
