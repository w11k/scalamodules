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
package org.scalamodules.core

import org.scalamodules.core.RegDepInfo.toRegDepInfo
import org.scalamodules.core.RegIndepInfo.toRegIndepInfo
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

object RegIndepInfoSpec extends Spec with ShouldMatchers {

  val info = new RegIndepInfo("ScalaModules")

  describe("The object RegIndepInfo") {

    it("should implicitly convert an AnyRef to RegIndepInfo") {
      val info: RegIndepInfo[String, String] = ""
      info should not be null
    }
  }

  describe("The class RegIndepInfo") {

    it("should throw an IAE when constructed with a null service") {
      intercept[IllegalArgumentException] { 
        new RegIndepInfo(null)
      }
    }

    it("should throw an IAE when constructed again with a null service") {
      intercept[IllegalArgumentException] { 
        new RegIndepInfo(null, None, None)
      }
    }

    it("should throw an IAE when constructed with a null service interface option") {
      intercept[IllegalArgumentException] { 
        new RegIndepInfo("", null, None)
      }
    }

    it("should throw an IAE when constructed with a null service properties option") {
      intercept[IllegalArgumentException] { 
        new RegIndepInfo("", None, null)
      }
    }
  }

  describe("RegIndepInfo.as(Class)") {

    it("should return a new RegIndepInfo with srvIntf == Some(Class) when called with a not-null Class") {
      val clazz = classOf[String]
      val newInfo = info as clazz
      newInfo should not be null
      newInfo.srvIntf should equal (Some(clazz))
    }

    it("should return a new RegIndepInfo with srvIntf == None when called with a null Class") {
      val newInfo = info as null
      newInfo should not be null
      newInfo.srvIntf should equal (None)
    }
  }

  describe("RegIndepInfo.withProps(Map)") {

    it("should return a new RegIndepInfo with props == Some(Map) when called with a not-null Map") {
      val props = IMap("Scala" -> "Modules")
      val newInfo = info withProps props
      newInfo should not be null
      (newInfo.props getOrElse IMap.empty).getOrElse("Scala", "") should equal ("Modules")
    }

    it("should return a new RegIndepInfo with srvIntf == None when called with a null Map") {
      val newInfo = info withProps null.asInstanceOf[Map[String, Any]]
      newInfo should not be null
      newInfo.props should equal (None)
    }
  }

  describe("RegIndepInfo.withProps((String, Any)*)") {
    
    it("should return a new RegIndepInfo with props == Some(Map) when called with a not-null pair") {
      val props = "Scala" -> "Modules"
      val newInfo = info withProps props
      newInfo should not be null
      (newInfo.props getOrElse IMap.empty).getOrElse("Scala", "") should equal ("Modules")
    }
  }
}

object RegDepInfoSpec extends Spec with ShouldMatchers {

  import java.util.Date

  val info = new RegDepInfo((d: Date) => "ScalaModules-" + d)

  describe("The object RegDepInfo") {

    it("should implicitly convert an AnyRef to RegDepInfo") {
      val info: RegDepInfo[String, String, Int] = (i: Int) => ""
      info should not be null
    }
  }

  describe("The class RegDepInfo") {

    it("should throw an IAE when constructed with a null service") {
      intercept[IllegalArgumentException] { 
        new RegDepInfo(null)
      }
    }

    it("should throw an IAE when constructed again with a null service") {
      intercept[IllegalArgumentException] { 
        new RegDepInfo(null, None, None, None)
      }
    }

    it("should throw an IAE when constructed with a null service interface option") {
      intercept[IllegalArgumentException] { 
        new RegDepInfo((d: Date) => "ScalaModules", null, None, Some(classOf[Date]))
      }
    }

    it("should throw an IAE when constructed with a null service properties option") {
      intercept[IllegalArgumentException] { 
        new RegDepInfo((d: Date) => "ScalaModules", Some(classOf[String]), null, Some(classOf[Date]))
      }
    }

    it("should throw an IAE when constructed with a null dependency option") {
      intercept[IllegalArgumentException] { 
        new RegDepInfo[String, String, Date]((d: Date) => "ScalaModules", Some(classOf[String]), None, null)
      }
    }
  }

  describe("RegDepInfo.as(Class)") {
    
    it("should return a new RegDepInfo with srvIntf == Some(Class) when called with a not-null Class") {
      val clazz = classOf[String]
      val newInfo = info as clazz
      newInfo should not be null
      newInfo.srvIntf should equal (Some(clazz))
    }

    it("should return a new RegDepInfo with srvIntf == None when called with a null Class") {
      val newInfo = info as null
      newInfo should not be null
      newInfo.srvIntf should equal (None)
    }
  }

  describe("RegDepInfo.withProps(Map)") {

    it("should return a new RegDepInfo with props == Some(Map) when called with a not-null Map") {
      val props = IMap("Scala" -> "Modules")
      val newInfo = info withProps props
      newInfo should not be null
      (newInfo.props getOrElse IMap.empty).getOrElse("Scala", "") should equal ("Modules")
    }

    it("should return a new RegDepInfo with srvIntf == None when called with a null Map") {
      val newInfo = info withProps null.asInstanceOf[Map[String, Any]]
      newInfo should not be null
      newInfo.props should equal (None)
    }
  }

  describe("RegDepInfo.withProps((String, Any)*)") {
    
    it("should return a new RegDepInfo with props == Some(Map) when called with a not-null pair") {
      val props = "Scala" -> "Modules"
      val newInfo = info withProps props
      newInfo should not be null
      (newInfo.props getOrElse IMap.empty).getOrElse("Scala", "") should equal ("Modules")
    }
  }

  describe("RegDepInfo.dependOn(Class)") {
    
    it("should return a new RegDepInfo with depIntf == Some(Class) when called with a not-null Class") {
      val clazz = classOf[Date]
      val newInfo = info dependOn clazz
      newInfo should not be null
      newInfo.depIntf should equal (Some(clazz))
    }

    it("should return a new RegDepInfo with srvIntf == None when called with a null Class") {
      val newInfo = info dependOn null
      newInfo should not be null
      newInfo.depIntf should equal (None)
    }
  }
}