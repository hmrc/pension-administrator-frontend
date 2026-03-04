import sbt.*

object AppDependencies {
  private val bootstrapVersion = "10.4.0"
  private val playVersion      = "play-30"
  private val mongoFeatureTogglesClientVersion = "2.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% s"bootstrap-frontend-$playVersion"           % bootstrapVersion,
    "uk.gov.hmrc"                   %% s"play-frontend-hmrc-$playVersion"           % "12.20.0",
    "uk.gov.hmrc"                   %% s"domain-$playVersion"                       % "13.0.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                       % "2.21.1",
    "uk.gov.hmrc"                   %% s"mongo-feature-toggles-client-$playVersion" % mongoFeatureTogglesClientVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"                    % bootstrapVersion,
    "org.scalatest"           %% "scalatest"                                       % "3.2.19",
    "org.scalatestplus"       %% "scalacheck-1-17"                                 % "3.2.18.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"                              % "7.0.2",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"                           % "1.1.0",
    "uk.gov.hmrc"             %% s"mongo-feature-toggles-client-test-$playVersion" % mongoFeatureTogglesClientVersion,
    "org.jsoup"                % "jsoup"                                           % "1.22.1"
   
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
