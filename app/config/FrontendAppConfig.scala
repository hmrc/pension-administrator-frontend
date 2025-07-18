/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(runModeConfiguration: Configuration, servicesConfig: ServicesConfig) {
  def localFriendlyUrl(uri: String): String = loadConfig("host") + uri

  private def loadConfig(key: String) = runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val administratorOrPractitionerUrl: String = loadConfig("urls.manage-pensions-frontend.administratorOrPractitioner")

  def cannotAccessPageAsPractitionerUrl(continueUrl: String): String =
    loadConfig("urls.manage-pensions-frontend.cannotAccessPageAsPractitioner").format(continueUrl)

  val betaFeedbackUnauthenticatedUrl: String =
    loadConfig("microservice.services.contact-frontend.beta-feedback-url.unauthenticated")

  lazy val timeoutSeconds: Int = runModeConfiguration.getOptional[Int]("hmrc-timeout-dialog.timeoutSeconds").getOrElse(900)
  lazy val countdownInSeconds: Int = runModeConfiguration.getOptional[Int]("hmrc-timeout-dialog.countdownInSeconds").getOrElse(120)
  lazy val govUkUrl: String = loadConfig("urls.gov-uk")
  lazy val pensionAdministratorUrl: String = s"${servicesConfig.baseUrl("pension-administrator")}"
  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val serviceSignOut: String = loadConfig("urls.logout")
  lazy val loginContinueUrl: String = loadConfig("urls.loginContinue")
  lazy val tellHMRCChangesUrl: String = loadConfig("urls.tellHMRCChanges")
  lazy val tellCompaniesHouseCompanyChangesUrl: String = loadConfig("urls.companyChangesCompaniesHouse")
  lazy val tellHMRCCompanyChangesUrl: String = loadConfig("urls.companyChangesHMRC")
  lazy val registerSchemeUrl: String = loadConfig("urls.pensions-scheme-frontend.registerScheme")
  lazy val schemesOverviewUrl: String = loadConfig("urls.manage-pensions-frontend.schemesOverview")
  lazy val youMustContactHMRCUrl: String = loadConfig("urls.manage-pensions-frontend.youMustContactHMRC")
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
  lazy val subscriptionDetailsSelfUrl: String = s"${
    s"${servicesConfig.baseUrl("pension-administrator")}${
      runModeConfiguration
        .underlying.getString("urls.pension-administrator.subscriptionDetailsSelf")
    }"
  }"
  lazy val deregisterPsaSelfUrl: String = s"${servicesConfig.baseUrl("pension-administrator")}${
    runModeConfiguration
      .underlying.getString("urls.pension-administrator.deregisterPsaSelf")
  }"

  lazy val updateSubscriptionDetailsUrl: String = s"${
    s"${servicesConfig.baseUrl("pension-administrator")}${
      runModeConfiguration
        .underlying.getString("urls.pension-administrator.updateSubscriptionDetails")
    }"
  }"

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  lazy val addressLookUp: String = s"${servicesConfig.baseUrl("address-lookup")}"

  lazy val registerWithIdOrganisationUrl: String = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.registerWithIdOrganisation")
  }"

  lazy val registerWithNoIdOrganisationUrl: String = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.registerWithNoIdOrganisation")
  }"

  lazy val registerWithNoIdIndividualUrl: String = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.registerWithNoIdIndividual")
  }"

  lazy val registerWithIdIndividualUrl: String = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.registerWithIdIndividual")
  }"

  lazy val registerPsaUrl: String = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.registerPsa")
  }"

  def updatePsaSelfUrl: String = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.updatePsaSelf")
  }"

  def canDeRegisterPsaUrl: String = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.canDeRegister")
  }"

  def psaEmailCallback(encryptedPsaId: String, journeyType: String) = s"${
    servicesConfig.baseUrl("pension-administrator") +
      runModeConfiguration.underlying.getString("urls.pension-administrator.emailCallback").format(journeyType, encryptedPsaId)
  }"

  def taxEnrolmentsUrl(serviceName: String): String = s"${
    servicesConfig.baseUrl("tax-enrolments") +
      runModeConfiguration.underlying.getString("urls.tax-enrolments") +
      s"service/$serviceName/enrolment"
  }"

  def taxDeEnrolmentUrl: String = s"${
    servicesConfig.baseUrl("tax-enrolments") +
      runModeConfiguration.underlying.getString("urls.tax-de-enrolment")
  }"

  def emailUrl = s"${s"${servicesConfig.baseUrl("email")}/${runModeConfiguration.underlying.getString("urls.email")}"}"

  lazy val appName: String = runModeConfiguration.underlying.getString("appName")

  lazy val retryAttempts: Int = runModeConfiguration.getOptional[Int]("retry.max.attempts").getOrElse(1)
  lazy val retryWaitMs: Int = runModeConfiguration.getOptional[Int]("retry.initial.wait.ms").getOrElse(1)
  lazy val retryWaitFactor: Double = runModeConfiguration.getOptional[Double]("retry.wait.factor").getOrElse(1)
  lazy val daysDataSaved: Int = loadConfig("daysDataSaved").toInt

  lazy val minimalPsaDetailsUrl: String = s"${
    s"${servicesConfig.baseUrl("pension-administrator")}${
      runModeConfiguration.underlying.getString("urls.pension-administrator.minimalPsaDetails")
    }"
  }"

  def identityValidationFrontEndEntry(relativeCompletionURL: RedirectUrl, relativeFailureURL: RedirectUrl): String = {
    val url = loadConfig("urls.iv-uplift-entry")
    val query = s"?origin=pods&confidenceLevel=250&completionURL=${relativeCompletionURL.get(OnlyRelative).url}&failureURL=${relativeFailureURL.get(OnlyRelative).url}"
    url + query
  }

}
