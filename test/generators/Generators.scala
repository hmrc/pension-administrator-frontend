/*
 * Copyright 2018 HM Revenue & Customs
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

package generators

import java.text.NumberFormat

import org.scalacheck.{Arbitrary, Gen, Shrink}
import Gen._
import Arbitrary._

import scala.util.Random

trait Generators {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max)
    val formatter = NumberFormat.getIntegerInstance
    val random = Random

    numberGen.map(n => {
      random.nextInt(10) match {
        case 1 => formatter.format(n)
        case _  => n.toString
      }
    })
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat(x => x > Int.MaxValue)

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat(x => x < Int.MinValue)

  def nonNumerics: Gen[String] =
    alphaStr suchThat(_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map(_.formatted("%f"))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat(_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat(_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat(x => x < min || x > max)

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat (_.nonEmpty)
      .suchThat (_ != "true")
      .suchThat (_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def numbersWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      num <- listOfN(length, numChar)
    } yield num.mkString

  def stringsLongerThan(minLength: Int): Gen[String] =
    arbitrary[String] suchThat (_.length > minLength)

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))
}
