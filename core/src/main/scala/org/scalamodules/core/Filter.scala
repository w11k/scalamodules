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

/**
 * Importing Filter._ will enable the factory methods
 */
object Filter {

  val nil = NilFilter

  def and(filters: Filter*) = compose("&", toList(filters:_*), false)

  def or(filters: Filter*) = compose("|", toList(filters:_*), false)

  def not(filter: Filter) = compose("!", List(filter), true)

  def objectClass(value: Class[_]*) = atom(OBJECT_CLASS, "=", toList(value:_*) map(_ getName))

  def exists(attr: Any) = set(attr)

  def set(attr: Any, value: Any*) = nullableAtom(attr, "=", toList(value:_*))

  def notSet(attr: Any):Filter = notSet(attr, null)

  def notSet(attr: Any, value: Any) = not(set(attr, value))

  def isTrue(attr: Any) = atom(attr, "=", List(true))

  def isFalse(attr: Any) = atom(attr, "=", List(false))

  def lt(attr: Any, value: Any) = atom(attr, "<=", List(value))

  def bt(attr: Any, value: Any) = atom(attr, ">=", List(value))

  def approx(attr: Any, value: Any) = atom(attr, "~=", List(value))

  def literal(filter: AnyRef) = toStr(filter) match {
    case null => NilFilter
    case str => LiteralFilter(str)
  }

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

  implicit def attributeToPropertyFilterBuilder(attr: String) = PropertyFilterBuilder(attr)

  implicit def classToObjectClassFilter(objectClass: Class[_]) = Filter objectClass(objectClass)

  implicit def attributeToIsSetFilter(attr: String) = attr match {
    case null => NilFilter
    case _ => Filter set(attr)
  }

  implicit def twoTupleToSetFilter(tuple: Tuple2[String,Any]) = tuple match {
    case null => NilFilter
    case _ => Filter set(tuple _1, tuple _2)
  }

  private def toStr(any: Any) = any match {
    case null => null
    case _ => String valueOf(any) trim
  }

  private def compose(op: String, filters: List[Filter], unary: Boolean) =
    prune(op, filters filter(nonNull _), unary)

  private def nonNull(filter: Filter) = filter != null && filter != NilFilter

  private def prune(op: String, list: List[Filter], unary: Boolean) = list match {
    case Nil => NilFilter
    case Seq(filter) => if (unary) CompositeFilter(op, List(filter)) else filter
    case _ => CompositeFilter(op, collapsed(op, list))
  }

  private def collapsed(op: String, filters: List[Filter]) = filters filter(nonNull _) flatMap(_ sublist(op))

  private def atom(attr: Any, op: String, value: List[_]) = newAtom(attr, op, value, false)

  private def nullableAtom(attr: Any, op: String, value: List[_]) = newAtom(attr, op, value, true)

  private def newAtom(attr: Any, op: String, value: List[_], allowNull: Boolean): Filter =
    PropertyFilter(validAttr(validString(attr, "attribute")), op, arguments(value))

  private def arguments(value: List[_]) = valueString(sequence(value))

  private def subseq(s:Any): List[Any] = s match {
    case null => Nil
    case string: String if string.trim.isEmpty => Nil
    case string: String if string.trim == PRESENT => List(PRESENT)
    case nestedArray: Array[_] => sequence(toList(nestedArray:_*))
    case nestedSeq: Seq[_] => sequence(nestedSeq toList)
    case None => Nil
    case Some(x) => List(x)
    case _ => List(s)
  }

  private def sequence(list: List[Any]): List[Any] = list flatMap (subseq _)

  private[core] lazy val PRESENT = "*"

  private[core] lazy val OBJECT_CLASS = "objectClass"

  private lazy val invalidAttributeChars = List("=", ">", "<", "~", "(", ")")

  private def validAttr(attr: String) = {
    invalidAttributeChars foreach((invalid: String) =>
            require(!(attr contains invalid),
              "Illegal character '" + invalid + "' found in attribute name '" + attr + "'"))
    attr
  }

  private def requireNonNull[T](t: T, msg: Any): T =
    if (t == null) throw new NullPointerException(msg.toString) else t

  private def validString(obj: Any, item: Any) = {
    val str = toStr(requireNonNull(obj, "Expected non-null " + item))
    require(!(str isEmpty), "Expected non-empty " + item)
    str
  }

  private def valueString(value: List[Any]) = {
    val isPresent = (value: Any) => value != null && value.toString.trim == PRESENT
    if (value == null || value == Nil || value.exists(isPresent))
      PRESENT
    else value match {
      case head::Nil => validStringOrFallback(head)
      case seq => "[" + (seq mkString ",") + "]"
    }
  }

  private def validStringOrFallback(obj: Any) = toStr(obj) match {
    case string if (string isEmpty) => Filter.PRESENT
    case string => string
  }

  /**
   * Trying to distil all the ugliness out on the bottom here.
   */
  private def toList[A](args: A*):List[A] = {
    if (args.isInstanceOf[Array[A]]) {
      List.fromArray(args.asInstanceOf[Array[A]])
    } else if (args.isInstanceOf[List[A]]) {
      args.asInstanceOf[List[A]]
    } else {
      error("Unexpected varargs input: " + args); // http://lampsvn.epfl.ch/trac/scala/ticket/1360
    }
  }
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

  protected def sublist(compositeOp: String): List[Filter] = Nil

  private def concat(seq: Seq[Filter]): Seq[Filter] = this :: List(seq:_*)

  protected def appendFilters(b: Bldr, filters: List[Filter]): Bldr = {
    filters foreach(_ writeTo(b))
    b
  }
}

object NilFilter extends Filter {

  // Negated null filter is still a null filter
  override def not = this
}

final case class CompositeFilter(composite: String, filters: List[Filter]) extends Filter {

  override protected def sublist(compositeOp: String) =
    if (compositeOp == composite) filters else List(this)

  protected override def writeTo(b: Bldr) = pars(b, appendSubfilters _)

  private def appendSubfilters(b: Bldr): Bldr = appendFilters(b append(composite), filters)
}

trait AtomicFilter extends Filter {

  override protected def sublist(compositeOp: String):List[Filter] = List(this)
}

final case class PropertyFilter(attr: String, op: String, value: String) extends AtomicFilter {

  override protected def writeTo(b: Bldr) = pars(b, _ append(attr) append(op) append(value))
}

final case class LiteralFilter(literal: String) extends AtomicFilter {

  override protected def writeTo(b: Bldr) = b append(literal)
}
