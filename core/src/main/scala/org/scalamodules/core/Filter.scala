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

import scala.{StringBuilder => Bldr}
import scala.collection.mutable.ListBuffer

/**
 * Importing Filter._ will enable the factory methods
 */
object Filter {

  def and(filters: Filter*) = compose("&", filters, false)

  def or(filters: Filter*) = compose("|", filters, false)

  def not(filter: Filter) = compose("!", List(filter), true)

  def objectClass(value: Class[_]*) = atom(OBJECT_CLASS, "=", value map(_ getName), true)

  def exists(attr: Any) = set(attr)

  def set(attr: Any, value: Any*) = atom(attr, "=", toSeq(value:_*), true)

  def notSet(attr: Any):Filter = notSet(attr, null)

  def notSet(attr: Any, value: Any) = not(set(attr, value))

  def isTrue(attr: Any) = atom(attr, "=", List(true))

  def isFalse(attr: Any) = atom(attr, "=", List(false))

  def lt(attr: Any, value: Any) = atom(attr, "<=", List(value))

  def bt(attr: Any, value: Any) = atom(attr, ">=", List(value))

  def approx(attr: Any, value: Any) = atom(attr, "~=", List(value))

  private[Filter] case class PropertyFilterBuilder(attr: Any) {
    validString(attr, "attribute")

    def set = Filter set(attr)

    def notSet = Filter notSet(attr)

    def isTrue = Filter isTrue(attr)

    def isFalse = Filter isFalse(attr)

    def ===(value: Any) = Filter set(attr, value)

    def !==(value: Any) = Filter notSet(attr, value)

    def <==(value: Any) = Filter lt(attr, value)

    def >==(value: Any) = Filter bt(attr, value)

    def ~==(value: Any) = Filter approx(attr, value)
  }

  object NilFilter extends Filter {

    override def not = this
  }

  implicit def toFilterBuilder(attr: String): PropertyFilterBuilder = PropertyFilterBuilder(attr)

  implicit def toFilter(objectClass: Class[_]) = set(OBJECT_CLASS, objectClass getName)

  implicit def toIsSet(attr: String): Filter = attr match {
    case null => NilFilter
    case _ => set(attr)
  }

  implicit def tupleToSet(tuple: Tuple2[String,Any]) = tuple match {
    case null => NilFilter
    case _ => set(tuple _1, tuple _2)
  }

  private def compose(op: String, filters: Seq[Filter], unary: Boolean): Filter =
    prune(op, filters filter(nonNull _), unary)

  private def nonNull(filter: Filter) = filter != null && filter != NilFilter

  private def prune(op: String, seq: Seq[Filter], unary: Boolean) = seq match {
    case Nil => NilFilter
    case Seq(filter) => if (unary) CompositeFilter(op, filter :: Nil) else filter
    case _ => CompositeFilter(op, possiblyCollapsed(op, seq))
  }

  private def possiblyCollapsed(op: String, seq: Seq[Filter]) = {
    val lb = new ListBuffer[Filter]
    seq foreach(filter => if (filter != null) filter append(op, lb)) // Laugh all you want, o functional guru, then show me how.
    lb toSeq
  }

  private def atom(attr: Any, op: String, value: Seq[_]): Filter = atom(attr, op, value, false)

  private def atom(attr: Any, op: String, value: Seq[_], allowNull: Boolean): Filter =
    PropertyFilter(validAttr(validString(attr, "attribute")), op, arguments(value))

  private def arguments(value: Seq[_]) = valueString(sequence(value, new ListBuffer[Any]))

  private def sequence(seq: Seq[_], lb: ListBuffer[Any]): List[Any] = {
    for (s <- seq) {
      s match {
        case nestedArray: Array[_] => sequence(toSeq(nestedArray:_*), lb)
        case nestedSeq: Seq[_] => sequence(nestedSeq, lb)
        case string: String if string.trim.isEmpty =>
        case string: String if string.trim == "*" => return Nil
        case None =>
        case null =>
        case Some(x) => lb += x
        case _ => lb += s
      }
    }
    lb toList
  }

  private[core] lazy val PRESENT = "*"

  private[core] lazy val OBJECT_CLASS = "objectClass"

  private lazy val invalidAttributeChars = List("=", ">", "<", "~", "(", ")")

  private def validAttr(attr: String): String = {
    invalidAttributeChars foreach ((s: String) => if (attr contains s)
      throw new IllegalArgumentException("Illegal character " + s + " in " + attr))
    attr
  }

  private def validString(obj: Any, item: Any): String = obj match {
    case null => throw new NullPointerException("Expected non-null " + item)
    case _ => validNonNullString(obj, item)
  }

  private def validNonNullString(obj: Any, item: Any): String = String valueOf obj trim match {
    case string if (string isEmpty) => throw new IllegalArgumentException("Expected non-empty " + item)
    case string => string
  }

  private def valueString(value: List[_]): String = value match {
    case Nil => PRESENT
    case head::Nil => validStringOrFallback(head)
    case seq => "[" + (seq mkString ",") + "]"
  }

  private def validStringOrFallback(obj: Any): String = String valueOf obj trim match {
    case string if (string isEmpty) => Filter.PRESENT
    case string => string
  }

  private def toSeq[A](args: A*):Seq[A] = List.fromArray(args.asInstanceOf[Array[A]])
}

abstract class Filter {

  final def && (filter: Filter) = and(filter)

  final def || (filter: Filter) = or(filter)

  def and(filters: Filter*) = Filter and(concat(filters):_*)

  def or(filters: Filter*) = Filter or(concat(filters):_*)

  def not = Filter not(this)

  final def asString = writeTo(new Bldr) toString

  override final def toString = asString

  protected def pars(b: Bldr, writeBody: Bldr => Bldr) = writeBody(b append("(")) append(")")

  protected def writeTo(b: Bldr): Bldr = b

  protected def append(compositeOp: String, lb: ListBuffer[Filter]):Unit = { }

  protected def appendFilters(b: Bldr, filters: Seq[Filter]): Bldr = { filters foreach(_ writeTo(b)); b }

  private def concat(seq: Seq[Filter]): Seq[Filter] = this :: List(seq:_*)
}

case class CompositeFilter(composite: String, filters: Seq[Filter]) extends Filter {

  protected override def writeTo(b: Bldr) = pars(b, appendSubfilters(_))

  protected override def append(superComposite: String, lb: ListBuffer[Filter]) =
    if (superComposite == composite) lb appendAll(filters) else lb append(this)

  private def appendSubfilters(b: Bldr): Bldr = appendFilters(b append(composite), filters)
}

case class PropertyFilter(attr: String, op: String, value: String) extends Filter {

  protected override def writeTo(b: Bldr) = pars(b, _ append(attr) append(op) append(value))

  protected override def append(compositeOp: String, lb: ListBuffer[Filter]) = lb append(this)
}