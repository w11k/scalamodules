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

import Preamble._
import Util.mapToJavaDictionary

import java.util.Dictionary
import org.easymock.{EasyMock, IArgumentMatcher}
import EasyMock.{aryEq, eq, isNull, same}
import org.osgi.framework.{BundleContext, ServiceReference}
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

object TrackSpec extends Spec with ShouldMatchers {

  val mockCtx = EasyMock createNiceMock classOf[BundleContext]

  describe("The class Track") {

    it("should throw an IAE when constructed with a null BundleContext") {
      intercept[IllegalArgumentException] {
        new Track(null, classOf[String], None)
      }
    }

    it("should throw an IAE when constructed with a null service interface") {
      intercept[IllegalArgumentException] {
        new Track(mockCtx, null, None)
      }
    }

    it("should throw an IAE when constructed with a null filter option") {
      intercept[IllegalArgumentException] {
        new Track(mockCtx, classOf[String], null)
      }
    }
  }

  describe("Track.withFilter") {

    it("should return a new Track when called with a not-null filter") {
      val track = new Track(mockCtx, classOf[String])
      val newTrack = track withFilter "(Scala=[Modules,FooBar])"
      newTrack should not be null
    }

    it("should return a new Track when called with a null filter") {
      val track = new Track(mockCtx, classOf[String])
      val newTrack = track % null
      newTrack should not be null
    }
  }

  describe("Track.on") {

    it("should throw an IAE when called with a null handler") {
      intercept[IllegalArgumentException] {
        val track = new Track(mockCtx, classOf[String])
        track & null
      }
    }
  }
}
