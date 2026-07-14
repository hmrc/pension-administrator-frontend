import sbt.*

object AppDependencies {
  private val playVersion      = "play-30"
  private val mongoFeatureTogglesClientVersion = "2.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% s"play-frontend-hmrc-$playVersion"           % "12.32.0",
    "uk.gov.hmrc"                   %% s"domain-$playVersion"                       % "13.0.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                       % "2.22.1",
    "uk.gov.hmrc"                   %% s"mongo-feature-toggles-client-$playVersion" % mongoFeatureTogglesClientVersion,
    "com.networknt"                 %  "json-schema-validator"                      % "1.5.8",
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"                                       % "3.2.20",
    "org.scalatestplus"       %% "scalacheck-1-17"                                 % "3.2.18.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"                              % "7.0.2",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"                           % "1.1.0",
    "uk.gov.hmrc"             %% s"mongo-feature-toggles-client-test-$playVersion" % mongoFeatureTogglesClientVersion
   
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
