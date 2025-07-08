import sbt.Def
import scoverage.ScoverageKeys

object ScoverageSettings {
  val excludedPackages: Seq[String] =
    Seq(
      "<empty>;Reverse.*;",
      ".*filters.*;",
      ".*handlers.*;",
      ".*components.*;",
      ".*models.*;",
      ".*repositories.*;",
      ".*BuildInfo.*;",
      ".*javascript.*;",
      ".*FrontendAuditConnector.*;",
      ".*Routes.*;",
      ".*GuiceInjector;",
      ".*UserAnswersCacheConnector;",
      ".*ControllerConfiguration;",
      ".*LanguageSwitchController"
    )

  def apply: Seq[Def.Setting[?]] =
    Seq(
      ScoverageKeys.coverageMinimumStmtTotal := 80,
      ScoverageKeys.coverageFailOnMinimum    := true,
      ScoverageKeys.coverageHighlighting     := true,
      ScoverageKeys.coverageExcludedFiles    := excludedPackages.mkString
    )
}
