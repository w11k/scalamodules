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

import org.easymock.EasyMock
import org.osgi.framework.BundleContext
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

object ServicesBundleContextSpec extends Spec with ShouldMatchers {

  val mockCtx = EasyMock createNiceMock classOf[BundleContext]

  describe("The class ServicesBundleContext") {

    it("should throw an IAE when constructed with a null BundleContext") {
      intercept[IllegalArgumentException] { 
        new ServicesBundleContext(null)
      }
    }
  }

  describe("ServicesBundleContext.configure(String)") {

    it("should return a not-null Configure when called with not-null PID") {

      val rbc = new ServicesBundleContext(mockCtx)
      val result = rbc configure "PID"
      result should not be null
    }
  }
}
