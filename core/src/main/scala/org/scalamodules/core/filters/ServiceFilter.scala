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


import collection.mutable.ListBuffer

object FilterHelpers {

  private[FilterHelpers] case class PropertyFilterBuilder(attr: String) {

    def set = Set(attr)

    def notSet = NotSet(attr)

    def isTrue = IsTrue(attr)

    def isFalse = IsFalse(attr)

    def ===(value: Any) = Set(attr, value)

    def !==(value: Any) = Not(Set(attr, value))

    def <==(value: Any) = Lt(attr, value)

    def >==(value: Any) = Bt(attr, value)

    def ~==(value: Any) = Approx(attr, value)
  }

  implicit def stringToUnaryPropertyFilterBuilder(attr: String) = PropertyFilterBuilder(attr)

  implicit def tupleToIs(tuple: Tuple2[String,Any]) = Set(tuple._1, tuple._2)

  implicit def stringToIsSet(string: String): ServiceFilter = Set(string)
}

abstract class ServiceFilter {

  protected def pars(sb :StringBuilder, fun :StringBuilder=>StringBuilder) = fun(sb.append("(")).append(")")

  protected def writeTo(sb: StringBuilder)

  private def conc(seq: Seq[ServiceFilter]): Seq[ServiceFilter] = Array.concat(this :: List(seq:_*))

  def && (filter: ServiceFilter) = and(filter)

  def and(filters :ServiceFilter*) = And(conc(filters):_*)

  def || (filter: ServiceFilter) = or(filter)

  def or (filters :ServiceFilter*) = Or(conc(filters):_*)

  def not() = Not(this)

  override final def toString = {
    val sb = new StringBuilder
    writeTo(sb)
    sb.toString
  }

  private[filters] def append(compositeOp :String, lb: ListBuffer[ServiceFilter]):Unit = { }
}

object NilFilter extends ServiceFilter {

  override def and(filters: ServiceFilter*) = And(filters:_*)

  override def or(filters: ServiceFilter*) = Or(filters:_*)

  override def not = this

  override protected def writeTo(sb: StringBuilder) = sb
}

private [filters] object CompositeFilter {

  def apply(op: String, filters: Seq[ServiceFilter]): ServiceFilter =
    apply(op, filters, false);

  def apply(op: String, filters: Seq[ServiceFilter], unary: Boolean): ServiceFilter =
    prune(op, filters.filter(_ != NilFilter), unary)

  private def prune(op: String, seq: Seq[ServiceFilter], unary: Boolean) = seq match {
    case Nil => NilFilter
    case Seq(filter) => if (unary) new CompositeFilter(op, filter :: Nil) else filter
    case _ => new CompositeFilter(op, possiblyCollapsed(op, seq))
  }

  private def possiblyCollapsed(op: String, seq: Seq[ServiceFilter]) = {
    val lb = new ListBuffer[ServiceFilter]
    seq.foreach(_.append(op, lb))
    lb.toSeq
  }
}

class CompositeFilter(op: String, filters: Seq[ServiceFilter]) extends ServiceFilter {

  private[filters] override def append(compositeOp :String, lb: ListBuffer[ServiceFilter]) =
    if (compositeOp == op) lb.appendAll(filters) else lb.append(this)

  def writeTo(sb: StringBuilder) = pars(sb, _.append(op).append(filters.mkString("")))
}

private [filters] object PropertyFilter {

  def apply(attr: String, op: String, value: Any): ServiceFilter = apply(attr, op, value, false);

  def apply(attr: String, op: String, value: Any, allowNull: Boolean): ServiceFilter =
    new PropertyFilter(NotNull(attr, "attr"),
      op,
      if (allowNull) IfNull(value, "*") else String.valueOf(NotNull(value, "value")))
}

class PropertyFilter(attr: String, op: String, value: String) extends ServiceFilter {

  private[filters] override def append(compositeOp :String, lb: ListBuffer[ServiceFilter]) = lb.append(this)

  def writeTo(sb: StringBuilder) = pars(sb, _.append(attr).append(op).append(value))
}

object And {

  def apply(filters: ServiceFilter*) = CompositeFilter("&", filters)
}

object Or {

  def apply(filters: ServiceFilter*) = CompositeFilter("|", filters)
}

object Not {
  def apply(filter: ServiceFilter) = CompositeFilter("!", filter :: Nil, true)
}

object Set {

  def apply(attr: String): ServiceFilter = apply(attr, null)

  def apply(attr: String, value: Any) = PropertyFilter(attr, "=", value, true)
}

object NotSet {

  def apply(attr: String): ServiceFilter = apply(attr, null)

  def apply(attr: String, value: Any) = Not(Set(attr, value))
}

object IsTrue {

  def apply(attr: String) = PropertyFilter(attr, "=", true)
}

object IsFalse {

  def apply(attr: String) = PropertyFilter(attr, "=", false)
}

object Lt {

  def apply(attr: String, value: Any) = PropertyFilter(attr, "<=", value)
}

object Bt {

  def apply(attr: String, value: Any) = PropertyFilter(attr, ">=", value)
}

object Approx {

  def apply(attr: String, value: Any) = PropertyFilter(attr, "~=", value)
}

private[filters] object IfNull {

  def apply[T](obj: T, fallback: String):String = if (obj == null) fallback else String.valueOf(obj)
}

private[filters] object NotNull {

  def apply[T](obj: T, msg: String): T =
    if (obj == null)
      throw new NullPointerException("Expected non-null: " + msg)
    else
      obj
}
