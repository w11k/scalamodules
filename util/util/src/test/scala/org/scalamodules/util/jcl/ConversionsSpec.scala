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
package org.scalamodules.util.jcl

import org.easymock.EasyMock
import org.scalatest.Spec

class ConversionsSpec extends Spec {

  "The function toJavaDictionary" -- {

    "should result in null for a null Scala Map" - {
      var result = Conversions.mapToJavaDictionary(null)
      assert(null == result)
    }

    "should result in (2 == size) for a Scala Map(1 -> 11, 2 -> 12)" - {
      var result = Conversions.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
      assert(null != result)
      assert(2 == result.size)
    }

    "should result in (!isEmpty) for a Scala Map(1 -> 11, 2 -> 12)" - {
      var result = Conversions.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
      assert(null != result)
      assert(!result.isEmpty)
    }

    "should result in (isEmpty) for an empty Scala Map" - {
      var result = Conversions.mapToJavaDictionary(Map())
      assert(null != result)
      assert(result.isEmpty)
    }

    "should result in (keys = Enumeration(1, 2)) for a Scala Map(1 -> 11, 2 -> 12)" - {
      var result = Conversions.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
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

    "should result in (elements = Enumeration(11, 12)) for a Scala Map(1 -> 11, 2 -> 12)" - {
      var result = Conversions.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
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
    
    "should throw an UOE for calling put" - {
      intercept(classOf[UnsupportedOperationException]) {
        var result = Conversions.mapToJavaDictionary(Map(1 -> 11))
        result.put(2, 12)
      }
    }
    
    "should throw an UOE for calling remove" - {
      intercept(classOf[UnsupportedOperationException]) {
        var result = Conversions.mapToJavaDictionary(Map(1 -> 11, 2 -> 12))
        result.remove("")
      }
    }
  }  

  "The function toJavaEnumeration" -- {

    "should result in null for a null Scala Iterator" - {
      var result = Conversions.iteratorToJavaEnumeration(null)
      assert(null == result)
    }

    "should result in an empty Java Enumeration for an empty Scala Iterator"  - {
      var iterator = createIterator()
      var result = Conversions.iteratorToJavaEnumeration(iterator)
      assert(null != result)
      assert(!result.hasMoreElements)
      EasyMock.verify(iterator)
    }

    "should result in a Java Enumeration ('a', 'b') for a Scala Iterator ('a', 'b')"  - {
      var iterator = createIterator("a", "b")
      var result = Conversions.iteratorToJavaEnumeration(iterator)
      assert(null != result)
      assert(result.hasMoreElements)
      assert("a" == result.nextElement)
      assert(result.hasMoreElements)
      assert("b" == result.nextElement)
      assert(!result.hasMoreElements)
      EasyMock.verify(iterator)
    }
  }

  "The function toJavaIterator" -- {

    "should result in null for a null Scala Iterator" - {
      var result = Conversions.iteratorToJavaIterator(null)
      assert(null == result)
    }

    "should result in an empty Java Iterator for an empty Scala Iterator"  - {
      var iterator = createIterator()
      var result = Conversions.iteratorToJavaIterator(iterator)
      assert(null != result)
      assert(!result.hasNext)
      EasyMock.verify(iterator)
    }

    "should result in a Java Iterator ('a', 'b') for a Scala Iterator ('a', 'b')"  - {
      var iterator = createIterator("a", "b")
      var result = Conversions.iteratorToJavaIterator(iterator)
      assert(null != result)
      assert(result.hasNext)
      assert("a" == result.next)
      assert(result.hasNext)
      assert("b" == result.next)
      assert(!result.hasNext)
      EasyMock.verify(iterator)
    }

    "should throw an UOE for calling remove" - {
      intercept(classOf[UnsupportedOperationException]) {
        var result = Conversions.iteratorToJavaIterator(createIterator("a", "b"))
        result.remove
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
