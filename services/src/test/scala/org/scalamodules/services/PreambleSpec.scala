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

import Preamble.{toServicesBundleContext, toRichConfiguration}

import org.easymock.EasyMock
import org.osgi.framework.BundleContext
import org.osgi.service.cm.Configuration
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

object PreambleSpec extends Spec with ShouldMatchers {

  describe("The function Preamble.toServicesBundleContext") {

    it("should implicitly convert a BundleContext to ServicesBundleContext") {
      val rbc: ServicesBundleContext = EasyMock createNiceMock classOf[BundleContext]
      rbc should not be null
    }
  }

  describe("The function Preamble.toRichConfiguration") {

    it("should implicitly convert a Configuration to RichConfiguration") {
      val rc: RichConfiguration = EasyMock createNiceMock classOf[Configuration]
      rc should not be null
    }
  }
}
