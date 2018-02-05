package models.$routeFile$

import utils.{Enumerable, InputOption, WithName}

sealed trait $className$

object $className$ extends Enumerable.Implicits {

  case object Option1 extends WithName("option1") with $className$
  case object Option2 extends WithName("option2") with $className$

  val values: Seq[$className$] = Seq(
    Option1, Option2
  )

  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"myOptionsPage.\${value.toString}")
  }

  implicit val enumerable: Enumerable[$className$] =
    Enumerable(values.map(v => v.toString -> v): _*)
}