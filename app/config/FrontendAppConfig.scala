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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Mode.Mode
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(override val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {

  override protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private lazy val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "pensionadministratorfrontend"

  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  lazy val googleTagManagerIdAvailable: Boolean = runModeConfiguration.underlying.getBoolean(s"google-tag-manager.id-available")
  lazy val googleTagManagerId: String = loadConfig(s"google-tag-manager.id")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  lazy val ivUpliftUrl: String = loadConfig("urls.ivUpliftUrl")

  lazy val govUkUrl: String = loadConfig("urls.gov-uk")
  lazy val governmentGatewayUrl: String = loadConfig("urls.government-gateway")
  lazy val pensionsSchemeUrl: String = baseUrl("pensions-scheme")
  lazy val pensionAdministratorUrl: String = baseUrl ("pension-administrator")
  lazy val authUrl: String = baseUrl("auth")
  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val serviceSignOut: String = loadConfig("urls.logout")
  lazy val loginContinueUrl: String = loadConfig("urls.loginContinue")
  lazy val ukJourneyContinueUrl: String = loadConfig("urls.ukJourneyContinue")
  lazy val tellHMRCChangesUrl: String = loadConfig("urls.tellHMRCChanges")
  lazy val tellCompaniesHouseCompanyChangesUrl: String = loadConfig("urls.companyChangesCompaniesHouse")
  lazy val tellHMRCCompanyChangesUrl: String = loadConfig("urls.companyChangesHMRC")
  lazy val registerSchemeUrl: String = loadConfig("urls.pensions-scheme-frontend.registerScheme")
  lazy val schemesOverviewUrl : String = loadConfig("urls.manage-pensions-frontend.schemesOverview")
  lazy val managePensionsYourPensionSchemesUrl: String = loadConfig("urls.manage-pensions-frontend.yourPensionSchemes")
  lazy val locationCanonicalList: String = loadConfig("location.canonical.list.all")
  lazy val locationCanonicalListEUAndEEA: String = loadConfig("location.canonical.list.EUAndEEA")
  lazy val maxDirectors: Int = loadConfig("register.company.maxDirectors").toInt
  lazy val maxPartners: Int = loadConfig("register.partnership.maxPartners").toInt
  lazy val emailTemplateId: String = loadConfig("email.templateId")
  lazy val emailSendForce: Boolean = runModeConfiguration.getBoolean("email.force").getOrElse(false)
  lazy val confirmationUri = "/register-as-pension-scheme-administrator/register/confirmation"
  lazy val duplicateRegUri = "/register-as-pension-scheme-administrator/register/duplicate-registration"
  lazy val registeredPsaDetailsUri = "/register-as-pension-scheme-administrator/registered-psa-details"
  lazy val tpssUrl: String = loadConfig("urls.tpss")
  lazy val subscriptionDetailsUrl: String = s"${baseUrl("pension-administrator")}${runModeConfiguration
    .underlying.getString("urls.pension-administrator.subscriptionDetails")}"

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage: String => Call = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val addressLookUp: String = baseUrl("address-lookup")

  lazy val registerWithIdOrganisationUrl: String = baseUrl ("pension-administrator") +
        runModeConfiguration.underlying.getString ("urls.pension-administrator.registerWithIdOrganisation")

  lazy val registerWithNoIdOrganisationUrl: String = baseUrl ("pension-administrator") +
        runModeConfiguration.underlying.getString ("urls.pension-administrator.registerWithNoIdOrganisation")

  lazy val registerWithNoIdIndividualUrl: String = baseUrl ("pension-administrator") +
    runModeConfiguration.underlying.getString ("urls.pension-administrator.registerWithNoIdIndividual")

  lazy val registerWithIdIndividualUrl: String = baseUrl("pension-administrator") +
        runModeConfiguration.underlying.getString("urls.pension-administrator.registerWithIdIndividual")

  lazy val registerPsaUrl: String = baseUrl("pension-administrator") +
        runModeConfiguration.underlying.getString("urls.pension-administrator.registerPsa")

  def psaSubmissionEmailCallback(encryptedPsaId: String) = baseUrl("pension-administrator") +
    runModeConfiguration.underlying.getString("urls.pension-administrator.emailCallback").format(encryptedPsaId)

  def taxEnrolmentsUrl(serviceName: String): String = baseUrl("tax-enrolments") +
    runModeConfiguration.underlying.getString("urls.tax-enrolments") +
    s"service/$serviceName/enrolment"

  def emailUrl = s"${baseUrl("email")}/${runModeConfiguration.underlying.getString("urls.email")}"

  lazy val appName: String = runModeConfiguration.underlying.getString("appName")

  lazy val languageTranslationEnabled: Boolean = runModeConfiguration.getBoolean("features.welsh-translation").getOrElse(true)

  lazy val nonUkJourneys: Boolean = runModeConfiguration.getBoolean("features.non-uk-journeys").getOrElse(false)
  lazy val isWorkPackageOneEnabled: Boolean = runModeConfiguration.getBoolean("features.work-package-one-enabled").getOrElse(false)
  lazy val retryAttempts: Int = runModeConfiguration.getInt("retry.max.attempts").getOrElse(1)
  lazy val retryWaitMs: Int = runModeConfiguration.getInt("retry.initial.wait.ms").getOrElse(1)
  lazy val retryWaitFactor: Double = runModeConfiguration.getDouble("retry.wait.factor").getOrElse(1)

}
