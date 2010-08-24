/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek

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
   * Entry point to the ScalaModules DSL.
   */
  implicit def toRichBundleContext(context: BundleContext) = {
    require(context != null, "The BundleContext must not be null!")
    new RichBundleContext(context)
  }

  /**
   * Implicitly converts a ServiceReference into a RichServiceReference.
   */
  implicit def toRichServiceReference(serviceReference: ServiceReference) = {
    require(serviceReference != null, "The ServiceReference must not be null!")
    new RichServiceReference(serviceReference)
  }

  /**
   * Implicitly converts a Pair into a Map in order to allow for easy definition of
   * single entry service properties.
   */
  implicit def pairToMap[A, B](pair: (A, B)) = if (pair == null) null else Map(pair)

  /**
   * Implicitly converts the given string into a builder for a "simple operation" filter component.
   */
  implicit def stringToSimpleOpBuilder(attr: String) = {
    require(attr != null, "The attr must not be null!")
    new SimpleOpBuilder(attr)
  }

  /**
   * Returns converts the given string into a builder for a "present" filter component.
   */
  implicit def stringToPresentBuilder(attr: String) = {
    require(attr != null, "The attr must not be null!")
    new PresentBuilder(attr)
  }

  /**
   * Returns the given or inferred type wrapped into a Some.
   */
  def interface[I](implicit manifest: Manifest[I]) = Some(manifest.erasure.asInstanceOf[Class[I]])

  /**
   * Returns the given or inferred type.
   */
  def withInterface[I](implicit manifest: Manifest[I]) = manifest.erasure.asInstanceOf[Class[I]]

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

  private[scalamodules] def optionalFilterToString(filter: Option[Filter]) = {
    assert(filter != null, "The optional Filter must not be null!")
    filter map { _.toString } orNull
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
        case null => None
        case service => Some(f(service.asInstanceOf[I]))
      }
    } finally context ungetService serviceReference
  }
}
