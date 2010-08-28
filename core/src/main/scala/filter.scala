/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

private[scalamodules] object Filter {

  /**
   * Implicitly converts a FilterComponent into a Filter.
   * @param filterComponent The FilterComponent to be converted
   * @return The Filter initialized with the given FilterComponent
   */
  implicit def filterComponentToFilter(filterComponent: FilterComponent) =
    Filter(filterComponent)
}

private[scalamodules] case class Filter(filterComponent: FilterComponent) {

  assert(filterComponent != null, "The FilterComponent must not be null!")

  /**
   * String representation for this Filter.
   * @return (x) where x is the String representation of the FilterComponent
   */
  override def toString = "(%s)" format filterComponent
}

// FilterComponents

private[scalamodules] object FilterComponent {

  /**
   * Implicitly converts a FilterComponent into an AndBuilder.
   * @param filterComponent The FilterComponent to be converted; must not be null!
   * @return The AndBuilder initialized with the given FilterComponent
   */
  implicit def filterComponentToAndBuilder(filterComponent: FilterComponent) = {
    require(filterComponent != null, "The FilterComponent must not be null!")
    new AndBuilder(filterComponent)
  }

  /**
   * Implicitly converts a FilterComponent into an OrBuilder.
   * @param filterComponent The FilterComponent to be converted; must not be null!
   * @return The OrBuilder initialized with the given FilterComponent
   */
  implicit def filterComponentToOrBuilder(filterComponent: FilterComponent) = {
    require(filterComponent != null, "The FilterComponent must not be null!")
    new OrBuilder(filterComponent)
  }

  /**
   * Implicitly converts a FilterComponent into a NotBuilder.
   * @param filterComponent The FilterComponent to be converted; must not be null!
   * @return The NotBuilder initialized with the given FilterComponent
   */
  implicit def filterComponentToNotBuilder(filterComponent: FilterComponent) = {
    require(filterComponent != null, "The FilterComponent must not be null!")
    new NotBuilder(filterComponent)
  }
}

private[scalamodules] sealed abstract class FilterComponent

private[scalamodules] case class And(filters: List[Filter]) extends FilterComponent {

  assert(filters != null, "The filters must not be null!")

  /**
   * String representation for this FilterComponent.
   * @return &x where x is the String representation of filters
   */
  override def toString = "&" + filters.mkString
}

private[scalamodules] case class Or(filters: List[Filter]) extends FilterComponent {

  assert(filters != null, "The filters must not be null!")

  /**
   * String representation for this FilterComponent.
   * @return |x where x is the String representation of filters
   */
  override def toString = "|" + filters.mkString
}

private[scalamodules] case class Not(filter: Filter) extends FilterComponent {

  assert(filter != null, "The Filter must not be null!")

  /**
   * String representation for this FilterComponent.
   * @return !x where x is the String representation of filter
   */
  override def toString = "!" + filter
}

private[scalamodules] case class SimpleOp(attr: String, filterType: FilterType, value: String)
  extends FilterComponent {

  assert(attr != null, "The attr must not be null!")
  assert(filterType != null, "The FilterType must not be null!")
  assert(value != null, "The value must not be null!")

  /**
   * String representation for this FilterComponent.
   * @return Concatenation of attr, filterType's String representation and value
   */
  override def toString = attr + filterType + value
}

private[scalamodules] case class Present(attr: String) extends FilterComponent {

  assert(attr != null, "The attr must not be null!")

  /**
   * String representation for this FilterComponent.
   * @return x=* where x is attr
   */
  override def toString = attr + "=*"
}

// FilterTypes

private[scalamodules] sealed abstract class FilterType

private[scalamodules] case object Equal extends FilterType {

  /**
   * String representation for this FilterType.
   * @return =
   */
  override def toString = "="
}

private[scalamodules] case object Approx extends FilterType {

  /**
   * String representation for this FilterType.
   * @return ~=
   */
  override def toString = "~="
}

private[scalamodules] case object GreaterEqual extends FilterType {

  /**
   * String representation for this FilterType.
   * @return >=
   */
  override def toString = ">="
}

private[scalamodules] case object LessEqual extends FilterType {

  /**
   * String representation for this FilterType.
   * @return <=
   */
  override def toString = "<="
}

// Builders

private[scalamodules] class AndBuilder(component: FilterComponent) {

  assert(component != null, "The FilterComponent must not be null!")

  /**
   * Creates an And FilterComponent.
   * @param The next FilterComponent to be "anded" with the FilterComponent of this AndBuilder; must not be null!
   * @return And FilterComponent "anding" the FilterComponent of this AndBuilder and the given one
   */
  def &&(nextComponent: FilterComponent) = and(nextComponent)

  /**
   * Creates an And FilterComponent.
   * @param The next FilterComponent to be "anded" with the FilterComponent of this AndBuilder; must not be null!
   * @return And FilterComponent "anding" the FilterComponent of this AndBuilder and the given one
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
   * Creates an Or FilterComponent.
   * @param The next FilterComponent to be "ored" with the FilterComponent of this OrBuilder; must not be null!
   * @return Or FilterComponent "oring" the FilterComponent of this OrBuilder and the given one
   */
  def ||(nextComponent: FilterComponent) = or(nextComponent)

  /**
   * Creates an Or FilterComponent.
   * @param The next FilterComponent to be "ored" with the FilterComponent of this OrBuilder; must not be null!
   * @return Or FilterComponent "oring" the FilterComponent of this OrBuilder and the given one
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
   * Creates a Not FilterComponent.
   * @return Not FilterComponent "negating" the FilterComponent of this NotBuilder
   */
  def unary_! = not

  /**
   * Creates a Not FilterComponent.
   * @return Not FilterComponent "negating" the FilterComponent of this NotBuilder
   */
  def not = Not(Filter(component))
}

private[scalamodules] class SimpleOpBuilder(attr: String) {

  assert(attr != null, "The attr must not be null!")

  /**
   * Creates a SimpleOp FilterComponent for FilterType Equal.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of Equal and
   * the given value
   */
  def ===(value: String) = equal(value)

  /**
   * Creates a SimpleOp FilterComponent for FilterType Equal.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of Equal and
   * the given value
   */
  def equal(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, Equal, value.toString)
  }

  /**
   * Creates a SimpleOp FilterComponent for FilterType Approx.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of Approx and
   * the given value
   */
  def ~==(value: String) = approx(value)

  /**
   * Creates a SimpleOp FilterComponent for FilterType Approx.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of Approx and
   * the given value
   */
  def approx(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, Approx, value.toString)
  }

  /**
   * Creates a SimpleOp FilterComponent for FilterType GreaterEqual.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of GreaterEqual and
   * the given value
   */
  def >==(value: String) = greaterEqual(value)

  /**
   * Creates a SimpleOp FilterComponent for FilterType GreaterEqual.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of GreaterEqual and
   * the given value
   */
  def ge(value: String) = greaterEqual(value)

  /**
   * Creates a SimpleOp FilterComponent for FilterType GreaterEqual.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of GreaterEqual and
   * the given value
   */
  def greaterEqual(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, GreaterEqual, value.toString)
  }

  /**
   * Creates a SimpleOp FilterComponent for FilterType LessEqual.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of LessEqual and
   * the given value
   */
  def <==(value: String) = lessEqual(value)

  /**
   * Creates a SimpleOp FilterComponent for FilterType LessEqual.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of LessEqual and
   * the given value
   */
  def le(value: String) = lessEqual(value)

  /**
   * Creates a SimpleOp FilterComponent for FilterType LessEqual.
   * @param The value for the SimpleOp; must not be null!
   * @return SimpleOp inialized with the attr of this SimpleOpBuilder, a FilterType of LessEqual and
   * the given value
   */
  def lessEqual(value: String) = {
    require(value != null, "The value must not be null!")
    SimpleOp(attr, LessEqual, value.toString)
  }
}

private[scalamodules] class PresentBuilder(attr: String) {

  assert(attr != null, "The attr must not be null!")

  /**
   * Creates a Present FilterComponent.
   * @return Present FilterComponent initialized with the attr of this PresentBuilder.
   */
  def unary_~ = present

  /**
   * Creates a Present FilterComponent.
   * @return Present FilterComponent initialized with the attr of this PresentBuilder.
   */
  def present = Present(attr)
}
