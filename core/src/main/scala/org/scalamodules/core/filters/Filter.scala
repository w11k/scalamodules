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
package org.scalamodules.core.filters

import scala.{StringBuilder => Bldr}
import collection.mutable.ListBuffer

/**
 * Importing Filter._ will enable the factory methods
 */
object Filter {

  def and(filters: Filter*) = compose("&", filters, false)

  def or(filters: Filter*) = compose("|", filters, false)

  def not(filter: Filter) = compose("!", filter :: Nil, true)

  def set(attr: String):Filter = set(attr, null)

  def set(attr: String, value: Any) = atom(attr, "=", value, true)

  def notSet(attr: String):Filter = notSet(attr, null)

  def notSet(attr: String, value: Any) = not(set(attr, value))

  def isTrue(attr: String) = atom(attr, "=", true)

  def isFalse(attr: String) = atom(attr, "=", false)

  def lt(attr: String, value: Any) = atom(attr, "<=", value)

  def bt(attr: String, value: Any) = atom(attr, ">=", value)

  def approx(attr: String, value: Any) = atom(attr, "~=", value)

  private[Filter] case class PropertyFilterBuilder(attr: String) {

    def set = Filter.set(attr)

    def notSet = Filter.notSet(attr)

    def isTrue = Filter.isTrue(attr)

    def isFalse = Filter.isFalse(attr)

    def ===(value: Any) = Filter.set(attr, value)

    def !==(value: Any) = Filter.notSet(attr, value)

    def <==(value: Any) = Filter.lt(attr, value)

    def >==(value: Any) = Filter.bt(attr, value)

    def ~==(value: Any) = Filter.approx(attr, value)
  }

  object NilFilter extends Filter {

    override def not = this
  }

  implicit def stringToUnaryPropertyFilterBuilder(attr: String) = PropertyFilterBuilder(attr)

  implicit def tupleToIs(tuple: Tuple2[String,Any]) = set(tuple._1, tuple._2)

  implicit def stringToIsSet(string: String): Filter = set(string)

  implicit def filterToString(filter: Filter): String = filter toString

  private def compose(op: String, filters: Seq[Filter], unary: Boolean): Filter =
    prune(op, filters.filter(_ != NilFilter), unary)

  private def prune(op: String, seq: Seq[Filter], unary: Boolean) = seq match {
    case Nil => NilFilter
    case Seq(filter) => if (unary) new CompositeFilter(op, filter :: Nil) else filter
    case _ => new CompositeFilter(op, possiblyCollapsed(op, seq))
  }

  private def possiblyCollapsed(op: String, seq: Seq[Filter]) = {
    val lb = new ListBuffer[Filter]
    seq.foreach(_.append(op, lb))
    lb.toSeq
  }

  private def ifNull[T](obj: T, fallback: String):String = if (obj == null) fallback else String.valueOf(obj)

  private def atom(attr: String, op: String, value: Any): Filter = atom(attr, op, value, false);

  private def atom(attr: String, op: String, value: Any, allowNull: Boolean): Filter =
    new PropertyFilter(notNull(attr, "attr"),
      op,
      if (allowNull) ifNull(value, "*") else String.valueOf(notNull(value, "value")))

  private def notNull[T](obj: T, msg: String): T =
    if (obj == null) throw new NullPointerException("Expected non-null: " + msg) else obj
}

abstract class Filter {

  final def && (filter: Filter) = and(filter)

  final def || (filter: Filter) = or(filter)

  def and(filters :Filter*) = Filter.and(conc(filters):_*)

  def or (filters :Filter*) = Filter.or(conc(filters):_*)

  def not = Filter.not(this)

  override final def toString = writeTo(new Bldr).toString

  protected def pars(b :Bldr, writeBody :Bldr => Bldr) = writeBody(b.append("(")).append(")")

  protected def writeTo(b: Bldr): Bldr = b

  protected def append(compositeOp :String, lb: ListBuffer[Filter]):Unit = { }

  protected def appendFilters(b: Bldr, filters: Seq[Filter]): Bldr =
    { filters.foreach(_.writeTo(b)); b }

  private def conc(seq: Seq[Filter]): Seq[Filter] = Array.concat(this :: List(seq:_*))
}

final class CompositeFilter(op: String, filters: Seq[Filter]) extends Filter {

  protected override def append(compositeOp :String, lb: ListBuffer[Filter]) =
    if (compositeOp == op) lb.appendAll(filters) else lb.append(this)

  private def appendSubfilters(b: Bldr) = appendFilters(b.append(op), filters)

  protected override def writeTo(b: Bldr) = pars(b, appendSubfilters(_))
}

final class PropertyFilter(attr: String, op: String, value: String) extends Filter {

  protected override def append(compositeOp :String, lb: ListBuffer[Filter]) = lb.append(this)

  protected override def writeTo(b: Bldr) = pars(b, _.append(attr).append(op).append(value))
}
