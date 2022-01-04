/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import models.ReportTechnicalIssue
import play.api.Mode
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(runModeConfiguration: Configuration, environment: Environment, servicesConfig: ServicesConfig) {
  def localFriendlyUrl(uri:String):String = loadConfig("host") + uri

  protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val timeout: String = loadConfig("session._timeoutSeconds")
  lazy val countdown: String = loadConfig("session._CountdownInSeconds")

  lazy val googleTagManagerIdAvailable: Boolean = runModeConfiguration.underlying.getBoolean(s"google-tag-manager.id-available")
  lazy val googleTagManagerId: String = loadConfig(s"google-tag-manager.id")

  lazy val administratorOrPractitionerUrl: String = loadConfig("urls.manage-pensions-frontend.administratorOrPractitioner")
  def cannotAccessPageAsPractitionerUrl(continueUrl:String): String =
    loadConfig("urls.manage-pensions-frontend.cannotAccessPageAsPractitioner").format(continueUrl)

  val reportAProblemNonJSUrl: String =
    loadConfig("microservice.services.contact-frontend.report-problem-url.non-js")
  val betaFeedbackUnauthenticatedUrl: String =
    loadConfig("microservice.services.contact-frontend.beta-feedback-url.unauthenticated")
  val reportTechnicalIssues: ReportTechnicalIssue =
    ReportTechnicalIssue(serviceId = "PODS", baseUrl = Some(reportAProblemNonJSUrl))

  lazy val manualIvUrl: String = loadConfig("urls.manualIvUrl")
  lazy val govUkUrl: String = loadConfig("urls.gov-uk")
  lazy val governmentGatewayUrl: String = loadConfig("urls.government-gateway")
  lazy val pensionsSchemeUrl: String = s"${servicesConfig.baseUrl("pensions-scheme")}"
  lazy val pensionAdministratorUrl: String = s"${servicesConfig.baseUrl ("pension-administrator")}"
  lazy val managePensionsUrl: String = s"${servicesConfig.baseUrl ("manage-pensions-frontend")}"
  lazy val authUrl: String = s"${servicesConfig.baseUrl("auth")}"
  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val serviceSignOut: String = loadConfig("urls.logout")
  lazy val loginContinueUrl: String = loadConfig("urls.loginContinue")
  lazy val ukJourneyContinueUrl: String = loadConfig("urls.ukJourneyContinue")
  lazy val tellHMRCChangesUrl: String = loadConfig("urls.tellHMRCChanges")
  lazy val tellCompaniesHouseCompanyChangesUrl: String = loadConfig("urls.companyChangesCompaniesHouse")
  lazy val tellHMRCCompanyChangesUrl: String = loadConfig("urls.companyChangesHMRC")
  lazy val registerSchemeUrl: String = loadConfig("urls.pensions-scheme-frontend.registerScheme")
  lazy val schemesOverviewUrl : String = loadConfig("urls.manage-pensions-frontend.schemesOverview")
  lazy val youMustContactHMRCUrl : String = loadConfig("urls.manage-pensions-frontend.youMustContactHMRC")
  lazy val managePensionsYourPensionSchemesUrl: String = loadConfig("urls.manage-pensions-frontend.yourPensionSchemes")
  lazy val locationCanonicalList: String = loadConfig("location.canonical.list.all")
  lazy val locationCanonicalListEUAndEEA: String = loadConfig("location.canonical.list.EUAndEEA")
  lazy val maxDirectors: Int = loadConfig("register.company.maxDirectors").toInt
  lazy val maxPartners: Int = loadConfig("register.partnership.maxPartners").toInt
  lazy val emailTemplateId: String = loadConfig("email.templateId")
  lazy val variationEmailTemplateId: String = loadConfig("email.variation.templateId")
  lazy val companyEmailTemplateId: String = loadConfig("company.email.templateId")
  lazy val emailSendForce: Boolean = runModeConfiguration.getOptional[Boolean]("email.force").getOrElse(false)
  lazy val tpssUrl: String = loadConfig("urls.tpss")
  lazy val contactHmrcUrl: String = loadConfig("urls.contactHmrcLink")
  lazy val subscriptionDetailsUrl: String = s"${s"${servicesConfig.baseUrl("pension-administrator")}${runModeConfiguration
    .underlying.getString("urls.pension-administrator.subscriptionDetails")}"}"
  lazy val deregisterPsaUrl : String = s"${servicesConfig.baseUrl("pension-administrator")}${runModeConfiguration
    .underlying.getString("urls.pension-administrator.deregisterPsa")}"

  lazy val updateSubscriptionDetailsUrl: String = s"${s"${servicesConfig.baseUrl("pension-administrator")}${runModeConfiguration
    .underlying.getString("urls.pension-administrator.updateSubscriptionDetails")}"}"

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage: String => Call = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val addressLookUp: String = s"${servicesConfig.baseUrl("address-lookup")}"

  lazy val identityVerification: String = s"${servicesConfig.baseUrl("identity-verification")}"

  lazy val identityVerificationFrontend: String = s"${servicesConfig.baseUrl("identity-verification-frontend")}"

  lazy val registerWithIdOrganisationUrl: String = s"${servicesConfig.baseUrl ("pension-administrator") +
        runModeConfiguration.underlying.getString ("urls.pension-administrator.registerWithIdOrganisation")}"

  lazy val registerWithNoIdOrganisationUrl: String = s"${servicesConfig.baseUrl ("pension-administrator") +
        runModeConfiguration.underlying.getString ("urls.pension-administrator.registerWithNoIdOrganisation")}"

  lazy val registerWithNoIdIndividualUrl: String = s"${servicesConfig.baseUrl ("pension-administrator") +
    runModeConfiguration.underlying.getString ("urls.pension-administrator.registerWithNoIdIndividual")}"

  lazy val registerWithIdIndividualUrl: String = s"${servicesConfig.baseUrl("pension-administrator") +
        runModeConfiguration.underlying.getString("urls.pension-administrator.registerWithIdIndividual")}"

  lazy val registerPsaUrl: String = s"${servicesConfig.baseUrl("pension-administrator") +
        runModeConfiguration.underlying.getString("urls.pension-administrator.registerPsa")}"

  def updatePsaUrl(psaId:String): String = s"${servicesConfig.baseUrl("pension-administrator") +
    runModeConfiguration.underlying.getString("urls.pension-administrator.updatePsa").format(psaId)}"

  def canDeRegisterPsaUrl(psaId: String): String = s"${servicesConfig.baseUrl("pension-administrator") +
    runModeConfiguration.underlying.getString("urls.pension-administrator.canDeRegister").format(psaId)}"

  def psaEmailCallback(encryptedPsaId: String, journeyType: String) = s"${servicesConfig.baseUrl("pension-administrator") +
    runModeConfiguration.underlying.getString("urls.pension-administrator.emailCallback").format(journeyType, encryptedPsaId)}"

  def taxEnrolmentsUrl(serviceName: String): String = s"${servicesConfig.baseUrl("tax-enrolments") +
    runModeConfiguration.underlying.getString("urls.tax-enrolments") +
    s"service/$serviceName/enrolment"}"

  def taxDeEnrolmentUrl: String = s"${servicesConfig.baseUrl("tax-enrolments") +
    runModeConfiguration.underlying.getString("urls.tax-de-enrolment")}"

  def emailUrl = s"${s"${servicesConfig.baseUrl("email")}/${runModeConfiguration.underlying.getString("urls.email")}"}"
  def ivRegisterOrganisationAsIndividualUrl =
    s"${s"${servicesConfig.baseUrl("identity-verification-proxy")}/${runModeConfiguration.underlying.getString("urls.ivRegisterOrganisationAsIndividual")}"}"

  lazy val appName: String = runModeConfiguration.underlying.getString("appName")

  lazy val languageTranslationEnabled: Boolean = runModeConfiguration.getOptional[Boolean]("features.welsh-translation").getOrElse(true)

  lazy val retryAttempts: Int = runModeConfiguration.getOptional[Int]("retry.max.attempts").getOrElse(1)
  lazy val retryWaitMs: Int = runModeConfiguration.getOptional[Int]("retry.initial.wait.ms").getOrElse(1)
  lazy val retryWaitFactor: Double = runModeConfiguration.getOptional[Double]("retry.wait.factor").getOrElse(1)
  lazy val daysDataSaved: Int = loadConfig("daysDataSaved").toInt

  lazy val minimalPsaDetailsUrl: String = s"${s"${servicesConfig.baseUrl("pension-administrator")}${
    runModeConfiguration.underlying.getString("urls.pension-administrator.minimalPsaDetails")}"}"

}
