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

package utils.navigators

import base.SpecBase
import connectors.FakeDataCacheConnector
import identifiers.Identifier
import identifiers.register.company._
import identifiers.register.partnership.ConfirmPartnershipDetailsId
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {
  import PartnershipNavigatorSpec._
  val navigator = new PartnershipNavigator(FakeDataCacheConnector, frontendAppConfig)

  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                              "User Answers",                   "Next Page (Normal Mode)",        "Save(NormalMode)",   "Next Page (Check Mode)",               "Save(CheckMode"),
    (BusinessDetailsId,                 emptyAnswers,                     confirmPartnershipDetailsPage,    true,                 None,                                   false),
    (ConfirmPartnershipDetailsId,       confirmPartnershipDetailsTrue,    whatYouWillNeedPage,              false,                None,                                   false),
    (ConfirmPartnershipDetailsId,       confirmPartnershipDetailsFalse,   companyUpdateDetailsPage,         false,                None,                                   false),
    (WhatYouWillNeedId,                 partnerSameContactAddressFalse,   partnershipContactAddressPage,    true,                 Some(partnershipContactAddressPage),    true),

  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes(), dataDescriber)
  }
}

object PartnershipNavigatorSpec extends OptionValues {
  private lazy val confirmPartnershipDetailsPage = controllers.register.partnership.routes.ConfirmPartnershipDetailsController.onPageLoad()
  private lazy val whatYouWillNeedPage = controllers.register.company.routes.WhatYouWillNeedController.onPageLoad()
  private lazy val companyUpdateDetailsPage = controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()



  val emptyAnswers = UserAnswers(Json.obj())
  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(true).asOpt.value
  private val confirmPartnershipDetailsFalse = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(false).asOpt.value
  private val partnerSameContactAddressTrue = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(true).asOpt.value
  private val partnerSameContactAddressFalse = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}

