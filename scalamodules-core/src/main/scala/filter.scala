/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scalamodules

private[scalamodules] object Filter {
  implicit def filterComponentToFilter(component: FilterComponent) = Filter(component)
}
// TODO Remove comment after testing!
/*private[scalamodules]*/case class Filter(component: FilterComponent) {
  override def toString = "(%s)" format component
}

private[scalamodules] object FilterComponent {
  implicit def filterComponentToAndBuilder(component: FilterComponent) = new AndBuilder(component)
  implicit def filterComponentToOrBuilder(component: FilterComponent) = new OrBuilder(component)
  implicit def filterComponentToNotBuilder(component: FilterComponent) = new NotBuilder(component)
}
private[scalamodules] sealed abstract class FilterComponent
private[scalamodules] case class And(filters: List[Filter]) extends FilterComponent {
  override def toString = "&" + filters.mkString
}
private[scalamodules] case class Or(filters: List[Filter]) extends FilterComponent {
  override def toString = "|" + filters.mkString
}
private[scalamodules] case class Not(filter: Filter) extends FilterComponent {
  override def toString = "!" + filter
}
private[scalamodules] case class SimpleOp(attr: String, filterType: FilterType, value: String) extends FilterComponent {
  override def toString = attr + filterType + value
}
private[scalamodules] case class Present(attr: String) extends FilterComponent {
  override def toString = attr + "=*"
}

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

private[scalamodules] class AndBuilder(component: FilterComponent) {
  def &&(nextComponent: FilterComponent) = and(nextComponent)
  def and(nextComponent: FilterComponent) = component match {
    case And(filters) => And(filters :+ Filter(nextComponent))
    case _            => And(Filter(component) :: Filter(nextComponent) :: Nil)
  }
}
private[scalamodules] class OrBuilder(component: FilterComponent) {
  def ||(nextComponent: FilterComponent) = or(nextComponent)
  def or(nextComponent: FilterComponent) = component match {
    case Or(filters) => Or(filters :+ Filter(nextComponent))
    case _           => Or(Filter(component) :: Filter(nextComponent) :: Nil)
  }
}
private[scalamodules] class NotBuilder(component: FilterComponent) {
  def unary_! = not
  def not = Not(Filter(component))
}
private[scalamodules] class SimpleOpBuilder(attr: String) {
  def ===(value: String) = equal(value)
  def equal(value: String) = SimpleOp(attr, Equal, value.toString)
  def ~==(value: String) = approx(value)
  def approx(value: String) = SimpleOp(attr, Approx, value.toString)
  def >==(value: String) = greaterEqual(value)
  def ge(value: String) = greaterEqual(value)
  def greaterEqual(value: String) = SimpleOp(attr, GreaterEqual, value.toString)
  def <==(value: String) = lessEqual(value)
  def le(value: String) = lessEqual(value)
  def lessEqual(value: String) = SimpleOp(attr, LessEqual, value.toString)
}
private[scalamodules] class PresentBuilder(attr: String) {
  def unary_~ = present
  def present = Present(attr)
}
