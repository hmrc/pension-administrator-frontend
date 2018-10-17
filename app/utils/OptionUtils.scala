package utils

object OptionUtils {
  def getOrException[A](option:Option[A]): A =
    option.getOrElse(throw new RuntimeException("No element found in option"))
}
