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
package org.scalamodules.core.internal

import java.util.Hashtable
import org.scalatest.Spec
import org.easymock.EasyMock

object UtilSpec extends Spec {

  describe("The function dictionaryToMap") {

    it("should result in null for a null Dictionary") {
      var result = Util.dictionaryToMap(null)
      assert(null == result)
    }

    it("should result in Set((1, 11), (2, 12)) as elements for a Dictionary(1 -> 11, 2 -> 12)") {
      val dictionary = new Hashtable[Int, Int]
      dictionary.put(1, 11)
      dictionary.put(2, 12)
      var result = Util.dictionaryToMap(dictionary)
      assert(null != result)
      val elements = result.elements
      assert(null != elements)
      assert(elements.hasNext)
      val next1 = elements.next
      assert(elements.hasNext)
      val next2 = elements.next
      assert(!elements.hasNext)
      assert(Set((1, 11), (2, 12)) == Set(next1, next2))
    }

    it("should result in 1->11, 2->12 for a Dictionary(1 -> 11, 2 -> 12)") {
      val dictionary = new Hashtable[Int, Int]
      dictionary.put(1, 11)
      dictionary.put(2, 12)
      var result = Util.dictionaryToMap(dictionary)
      assert(null != result)
      assert(None == result.get(0))
      assert(Some(11) == result.get(1))
      assert(Some(12) == result.get(2))
    }

    it("should result in (2 == size) for a Dictionary(1 -> 11, 2 -> 12)") {
      val dictionary = new Hashtable[Int, Int]
      dictionary.put(1, 11)
      dictionary.put(2, 12)
      var result = Util.dictionaryToMap(dictionary)
      assert(null != result)
      assert(2 == result.size)
    }
  }

  describe("The function mapToJavaDictionary") {

    it("should result in null for a null Scala Map") {
      var result = Util.mapToJavaDictionary(null)
      assert(null == result)
    }

    it("should result in (2 == size) for a Scala Map(1 -> 11, 2 -> 12)") {
      var result = Util.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
      assert(null != result)
      assert(2 == result.size)
    }

    it("should result in (!isEmpty) for a Scala Map(1 -> 11, 2 -> 12)") {
      var result = Util.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
      assert(null != result)
      assert(!result.isEmpty)
    }

    it("should result in (isEmpty) for an empty Scala Map") {
      var result = Util.mapToJavaDictionary(Map())
      assert(null != result)
      assert(result.isEmpty)
    }

    it("should result in (keys = Enumeration(1, 2)) for a Scala Map(1 -> 11, 2 -> 12)") {
      var result = Util.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
      assert(null != result)
      var keys = result.elements
      assert(keys.hasMoreElements)
      var key = keys.nextElement
      assert(11 == key || 12 == key)
      assert(keys.hasMoreElements)
      key = keys.nextElement
      assert(11 == key || 12 == key)
      assert(!keys.hasMoreElements)
    }

    it("should result in (elements = Enumeration(11, 12)) for a Scala Map(1 -> 11, 2 -> 12)") {
      var result = Util.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
      assert(null != result)
      var elements = result.elements
      assert(elements.hasMoreElements)
      var element = elements.nextElement
      assert(11 == element || 12 == element)
      assert(elements.hasMoreElements)
      element = elements.nextElement
      assert(11 == element || 12 == element)
      assert(!elements.hasMoreElements)
    }
    
    it("should throw an UOE for calling put") {
      intercept[UnsupportedOperationException] {
        var result = Util.mapToJavaDictionary(Map(1 -> 11))
        result.put(2, 12)
        ()
      }
    }
    
    it("should throw an UOE for calling remove") {
      intercept[UnsupportedOperationException] { 
        var result = Util.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
        result.remove("")
        ()
      }
    }
  }  

  describe("The function toJavaEnumeration") {

    it("should result in null for a null Scala Iterator") {
      var result = Util.iteratorToJavaEnumeration(null)
      assert(null == result)
    }

    it("should result in an empty Java Enumeration for an empty Scala Iterator") {
      var iterator = createIterator()
      var result = Util.iteratorToJavaEnumeration(iterator)
      assert(null != result)
      assert(!result.hasMoreElements)
      EasyMock.verify(iterator)
    }

    it("should result in a Java Enumeration ('a', 'b') for a Scala Iterator ('a', 'b')") {
      var iterator = createIterator("a", "b")
      var result = Util.iteratorToJavaEnumeration(iterator)
      assert(null != result)
      assert(result.hasMoreElements)
      assert("a" == result.nextElement)
      assert(result.hasMoreElements)
      assert("b" == result.nextElement)
      assert(!result.hasMoreElements)
      EasyMock.verify(iterator)
    }
  }

  describe("The function toJavaIterator") {

    it("should result in null for a null Scala Iterator") {
      var result = Util.iteratorToJavaIterator(null)
      assert(null == result)
    }

    it("should result in an empty Java Iterator for an empty Scala Iterator") {
      var iterator = createIterator()
      var result = Util.iteratorToJavaIterator(iterator)
      assert(null != result)
      assert(!result.hasNext)
      EasyMock.verify(iterator)
    }

    it("should result in a Java Iterator ('a', 'b') for a Scala Iterator ('a', 'b')") {
      var iterator = createIterator("a", "b")
      var result = Util.iteratorToJavaIterator(iterator)
      assert(null != result)
      assert(result.hasNext)
      assert("a" == result.next)
      assert(result.hasNext)
      assert("b" == result.next)
      assert(!result.hasNext)
      EasyMock.verify(iterator)
    }

    it("should throw an UOE for calling remove") {
      intercept[UnsupportedOperationException] {
        var result = Util.iteratorToJavaIterator(createIterator("a", "b"))
        result.remove
        ()
      }
    }
  }
  
  private def createIterator[T](ts: T*) = {
    var iterator = EasyMock.createMock(classOf[Iterator[T]])
    for(t <- ts) {
      EasyMock.expect(iterator.hasNext).andReturn(true)
      EasyMock.expect(iterator.next).andReturn(t)
    }
    EasyMock.expect(iterator.hasNext).andReturn(false)
    EasyMock.replay(iterator)
    iterator
  }
}
