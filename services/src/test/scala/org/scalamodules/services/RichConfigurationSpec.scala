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
package org.scalamodules.services

import Preamble._

import org.easymock.EasyMock
import org.osgi.service.cm.Configuration
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

object RichConfigurationSpec extends Spec with ShouldMatchers {

  val mockConfig = EasyMock createNiceMock classOf[Configuration]

  describe("The class RichConfiguration") {

    it("should throw an IAE when constructed with a null Confguration") {
      intercept[IllegalArgumentException] { 
        new RichConfiguration(null)
      }
    }
  }

  describe("RichConfiguration.properties()") {

    it("should return a None for no properties") {
      EasyMock reset mockConfig
      EasyMock expect (mockConfig.getProperties) andReturn null
      EasyMock replay mockConfig

      val rc = new RichConfiguration(mockConfig)
      val result = rc.properties
      result should be (None)
    }

    it("should return a None for no properties") {
      assert(false, "Not yet finalized!")
      EasyMock reset mockConfig
      // TODO How can we mock a return of type Dictionary? Mockito?
      //EasyMock expect (mockConfig.getProperties) andReturn
      EasyMock replay mockConfig

      val rc = new RichConfiguration(mockConfig)
      val result = rc.properties
      result should be (Some)
    }
  }
}
