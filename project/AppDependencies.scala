import sbt.*

object AppDependencies {
  private val bootstrapVersion = "9.13.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"             % bootstrapVersion,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"             % "12.7.0",
    "uk.gov.hmrc"                   %% "domain-play-30"                         % "10.0.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                   % "2.19.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
    "org.scalatest"           %% "scalatest"                  % "3.2.19",
    "org.scalatestplus"       %% "scalacheck-1-17"            % "3.2.18.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "7.0.2",
    "org.mockito"             %% "mockito-scala"              % "2.0.0",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"      % "1.1.0"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
