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

import Preamble.toServicesBundleContext

import org.easymock.EasyMock
import org.osgi.framework.BundleContext
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.Map

object BaseManagedServiceSpec extends Spec with ShouldMatchers {

  describe("BaseManagedService.updated()") {

    it("should return None when called with null properties") {
      assert(false, "Not yet implemented!")
    }

    it("should return Some when called with not-null properties") {
      assert(false, "Not yet implemented!")
    }
  }
}
