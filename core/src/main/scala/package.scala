/*
 * Copyright 2009-2011 Weigle Wilczek GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weiglewilczek

import com.weiglewilczek.slf4s.Logger
import java.util.Dictionary
import org.osgi.framework.{ BundleContext, ServiceReference }

/**
 * Some implicit conversions and other stuff essential for the ScalaModules DSL.
 */
package object scalamodules {

  /**
   * Type alias for service properties.
   */
  type Props = Map[String, Any]

  /**
   * Implicitly converts a BundleContext into a RichBundleContext.
   * @param context The BundleContext to be converted; must not be null!
   * @return The RichBundleContext initialized with the given BundleContext
   */
  implicit def toRichBundleContext(context: BundleContext): RichBundleContext = {
    require(context != null, "The BundleContext must not be null!")
    new RichBundleContext(context)
  }

  /**
   * Implicitly converts a ServiceReference into a RichServiceReference.
   * @param serviceReference The ServiceReference to be converted; must not be null!
   * @return The RichServiceReference initialized with the given ServiceReference
   */
  implicit def toRichServiceReference(serviceReference: ServiceReference): RichServiceReference = {
    require(serviceReference != null, "The ServiceReference must not be null!")
    new RichServiceReference(serviceReference)
  }

  /**
   * Implicitly converts a Pair into a Map in order to easily define single entry service properties.
   * @param pair The pair to be converted
   * @return A Map initialized with the given pair or null, if the given pair is null
   */
  implicit def pairToMap[A, B](pair: (A, B)): Map[A, B] =
    if (pair == null) null else Map(pair)

  /**
   * Implicitly converts a String attribute into a SimpleOpBuilder FilterComponent.
   * @param attr The attribute to be converted; must not be null!
   * @return A SimpleOpBuilder initialized with the given String attribute
   */
  implicit def toSimpleOpBuilder(attr: String): SimpleOpBuilder = {
    require(attr != null, "The attr must not be null!")
    new SimpleOpBuilder(attr)
  }

  /**
   * Implicitly converts a String attribute into a PresentBuilder FilterComponent.
   * @param attr The attribute to be converted; must not be null!
   * @return A PresentBuilder initialized with the given String attribute
   */
  implicit def toPresentBuilder(attr: String): PresentBuilder = {
    require(attr != null, "The attr must not be null!")
    new PresentBuilder(attr)
  }

  /**
   * Returns the given or inferred type wrapped into a Some.
   */
  def interface[I](implicit manifest: Manifest[I]): Option[Class[I]] =
    Some(manifest.erasure.asInstanceOf[Class[I]])

  /**
   * Returns the given or inferred type.
   */
  def withInterface[I](implicit manifest: Manifest[I]): Class[I] =
    manifest.erasure.asInstanceOf[Class[I]]

  private[scalamodules] val logger = Logger("com.weiglewilczek.scalamodules")

  private[scalamodules] implicit def scalaMapToJavaDictionary[K, V](map: Map[K, V]) = {
    import scala.collection.JavaConversions._
    if (map == null) null: Dictionary[K, V]
    else new Dictionary[K, V] {
      override def size = map.size
      override def isEmpty = map.isEmpty
      override def keys = map.keysIterator
      override def elements = map.valuesIterator
      override def get(o: Object) = map.get(o.asInstanceOf[K]) match {
        case None => null.asInstanceOf[V]
        case Some(value) => value.asInstanceOf[V]
      }
      override def put(key: K, value: V) =
        throw new UnsupportedOperationException("This Dictionary is read-only!")
      override def remove(o: Object) =
        throw new UnsupportedOperationException("This Dictionary is read-only!")
    }
  }

  private[scalamodules] def invokeService[I, T](
      serviceReference: ServiceReference,
      f: I => T,
      context: BundleContext): Option[T] = {

    assert(serviceReference != null, "The ServiceReference must not be null!")
    assert(f != null, "The function to be applied to the service must not be null!")
    assert(context != null, "The BundleContext must not be null!")

    try {
      context getService serviceReference match {
        case null => {
          logger warn "Could not get service for ServiceReference %s!".format(serviceReference)
          None
        }
        case service => {
          val result = Some(f(service.asInstanceOf[I]))
          logger info "Invoked service for  ServiceReference %s!".format(serviceReference)
          result
        }
      }
    } finally context ungetService serviceReference
  }
}
