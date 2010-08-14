/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

private[scalamodules] object Filter {
  implicit def filterComponentToFilter(component: FilterComponent) = Filter(component)
}

private[scalamodules] case class Filter(component: FilterComponent) {
  assert(component != null, "The FilterComponent must not be null!")
  override def toString = "(%s)" format component
}

// Components

private[scalamodules] object FilterComponent {
  implicit def filterComponentToAndBuilder(component: FilterComponent) = new AndBuilder(component)
  implicit def filterComponentToOrBuilder(component: FilterComponent) = new OrBuilder(component)
  implicit def filterComponentToNotBuilder(component: FilterComponent) = new NotBuilder(component)
}

private[scalamodules] sealed abstract class FilterComponent

private[scalamodules] case class And(filters: List[Filter]) extends FilterComponent {
  assert(filters != null, "The filters must not be null!")
  override def toString = "&" + filters.mkString
}

private[scalamodules] case class Or(filters: List[Filter]) extends FilterComponent {
  assert(filters != null, "The filters must not be null!")
  override def toString = "|" + filters.mkString
}

private[scalamodules] case class Not(filter: Filter) extends FilterComponent {
  assert(filter != null, "The Filter must not be null!")
  override def toString = "!" + filter
}

private[scalamodules] case class SimpleOp(attr: String, filterType: FilterType, value: String)
  extends FilterComponent {

  assert(attr != null, "The attr must not be null!")
  assert(filterType != null, "The FilterType must not be null!")
  assert(value != null, "The value must not be null!")

  override def toString = attr + filterType + value
}

private[scalamodules] case class Present(attr: String) extends FilterComponent {
  assert(attr != null, "The attr must not be null!")
  override def toString = attr + "=*"
}

// Types

private[scalamodules] sealed abstract class FilterType

private[scalamodules] case object Equal extends FilterType {
  override def toString = "="
}

private[scalamodules] case object Approx extends FilterType {
  override def toString = "~="
}

private[scalamodules] case object GreaterEqual extends FilterType {
  override def toString = ">="
}

private[scalamodules] case object LessEqual extends FilterType {
  override def toString = "<="
}

// Builders

private[scalamodules] class AndBuilder(component: FilterComponent) {

  assert(component != null, "The FilterComponent must not be null!")

  /**
   *
   */
  def &&(nextComponent: FilterComponent) = and(nextComponent)

  /**
   *
   */
  def and(nextComponent: FilterComponent) = {
    require(nextComponent != null, "The FilterComponent must not be null!")
    component match {
      case And(filters) => And(filters :+ Filter(nextComponent))
      case _ => And(Filter(component) :: Filter(nextComponent) :: Nil)
    }
  }
}

private[scalamodules] class OrBuilder(component: FilterComponent) {

  assert(component != null, "The FilterComponent must not be null!")

  /**
   *
   */
  def ||(nextComponent: FilterComponent) = or(nextComponent)

  /**
   *
   */
  def or(nextComponent: FilterComponent) = {
    require(nextComponent != null, "The FilterComponent must not be null!")
    component match {
      case Or(filters) => Or(filters :+ Filter(nextComponent))
      case _ => Or(Filter(component) :: Filter(nextComponent) :: Nil)
    }
  }
}

private[scalamodules] class NotBuilder(component: FilterComponent) {

  assert(component != null, "The FilterComponent must not be null!")

  /**
   *
   */
  def unary_! = not

  /**
   *
   */
  def not = Not(Filter(component))
}

private[scalamodules] class SimpleOpBuilder(attr: String) {

  assert(attr != null, "The attr must not be null!")

  /**
   *
   */
  def ===(value: String) = equal(value)

  /**
   *
   */
  def equal(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, Equal, value.toString)
  }

  /**
   *
   */
  def ~==(value: String) = approx(value)

  /**
   *
   */
  def approx(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, Approx, value.toString)
  }

  /**
   *
   */
  def >==(value: String) = greaterEqual(value)

  /**
   *
   */
  def ge(value: String) = greaterEqual(value)

  /**
   *
   */
  def greaterEqual(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, GreaterEqual, value.toString)
  }

  /**
   *
   */
  def <==(value: String) = lessEqual(value)

  /**
   *
   */
  def le(value: String) = lessEqual(value)

  /**
   *
   */
  def lessEqual(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, LessEqual, value.toString)
  }
}

private[scalamodules] class PresentBuilder(attr: String) {

  assert(attr != null, "The attr must not be null!")

  /**
   *
   */
  def unary_~ = present

  /**
   *
   */
  def present = Present(attr)
}
