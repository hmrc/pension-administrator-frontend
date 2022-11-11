import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "logback-json-logger"            % "5.1.0",
    "uk.gov.hmrc"       %% "govuk-template"                 % "5.78.0-play-28",
    "uk.gov.hmrc"       %% "play-ui"                        % "9.11.0-play-28",
    "uk.gov.hmrc"       %% "http-caching-client"            % "10.0.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.12.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % "7.8.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "3.32.0-play-28",
    "uk.gov.hmrc"       %% "domain"                         % "8.1.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "hmrctest"                   % "3.10.0-play-26",
    "org.scalatest"           %% "scalatest"                  % "3.2.14",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.pegdown"             %  "pegdown"                    % "1.6.0",
    "org.jsoup"               %  "jsoup"                      % "1.15.3",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "org.mockito"             %  "mockito-core"               % "4.8.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.17.0",
    "wolfendale"              %% "scalacheck-gen-regexp"      % "0.1.1",
    "com.github.tomakehurst"  %  "wiremock-jre8"              % "2.35.0"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
