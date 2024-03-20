/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.register.routes
import identifiers.Identifier
import identifiers.register._
import models.register.{BusinessType, DeclarationWorkingKnowledge, NonUKBusinessType}
import models.requests.IdentifiedRequest
import models.{CheckMode, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswers}

class RegisterNavigatorV2Spec extends SpecBase with NavigatorBehaviour {

  import RegisterNavigatorV2Spec._

  val navigator: Navigator = injector.instanceOf[RegisterNavigatorV2]

  "RegisterNavigatorV2 in NormalMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (BusinessTypeId, unlimitedCompany, companyUTRPage),
      (BusinessTypeId, limitedCompany, companyUTRPage),
      (BusinessTypeId, businessPartnership, partnershipUTRPage),
      (BusinessTypeId, limitedPartnership, partnershipUTRPage),
      (BusinessTypeId, limitedLiabilityPartnership, partnershipUTRPage),


      (DeclarationWorkingKnowledgeId, haveDeclarationWorkingKnowledge, declarationFitAndProperPage),
      (DeclarationWorkingKnowledgeId, haveAnAdviser, adviserName),
      (DeclarationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage),

      (DeclarationFitAndProperId, emptyAnswers, declarationPage),
      (DeclarationId, emptyAnswers, confirmation),

      (AreYouInUKId, inUk, ukBusinessType),
      (AreYouInUKId, notInUk, nonUkBusinessType),

      (RegisterAsBusinessId, registerAsBusinessIdCompanyOrPartnership, businessWynPage),
      (RegisterAsBusinessId, registerAsBusinessIdIndividual, individualWynPage),

      (NonUKBusinessTypeId, nonUkCompany, nonUkCompanyRegisteredName),
      (NonUKBusinessTypeId, nonUkPartnership, nonUkPartnershipRegisteredName)
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "RegisterNavigatorV2 in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),

      (AreYouInUKId, inUk, registerAsBusiness),
      (AreYouInUKId, notInUk, registerAsBusiness)
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

}
object RegisterNavigatorV2Spec extends OptionValues {

  lazy val emptyAnswers: UserAnswers = UserAnswers(Json.obj())
  lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad
  lazy val companyUTRPage: Call = controllers.register.company.routes.CompanyUTRController.onPageLoad
  lazy val partnershipUTRPage: Call = controllers.register.administratorPartnership.routes.PartnershipUTRController.onPageLoad
  lazy val declarationPage: Call = routes.DeclarationController.onPageLoad()
  lazy val declarationFitAndProperPage: Call = routes.DeclarationFitAndProperController.onPageLoad()
  lazy val adviserName: Call = controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode)
  lazy val confirmation: Call = routes.ConfirmationController.onPageLoad()
  lazy val survey: Call = controllers.routes.LogoutController.onPageLoad
  lazy val ukBusinessType: Call = controllers.register.routes.BusinessTypeController.onPageLoad(NormalMode)
  lazy val nonUkBusinessType: Call = controllers.register.routes.NonUKBusinessTypeController.onPageLoad()
  lazy val nonUkCompanyRegisteredName: Call = controllers.register.company.routes.CompanyRegisteredNameController.onPageLoad(NormalMode)
  lazy val registerAsBusiness: Call = controllers.register.routes.RegisterAsBusinessController.onPageLoad()
  lazy val nonUkCompanyAddress: Call = controllers.register.company.routes.CompanyRegisteredAddressController.onPageLoad()
  lazy val nonUkPartnershipAddress: Call = controllers.register.administratorPartnership.routes.PartnershipRegisteredAddressController.onPageLoad()
  lazy val nonUkPartnershipRegisteredName: Call = controllers.register.administratorPartnership.routes.PartnershipRegisteredNameController.onPageLoad()
  lazy val ukIndividualDetailsCorrect: Call = controllers.register.individual.routes.IndividualDetailsCorrectController.onPageLoad(NormalMode)

  lazy val individualWynPage: Call = controllers.register.individual.routes.WhatYouWillNeedController.onPageLoad()
  lazy val businessWynPage: Call = controllers.register.routes.WhatYouWillNeedController.onPageLoad(NormalMode)

  val haveDeclarationWorkingKnowledge: UserAnswers = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge).asOpt.value
  val haveAnAdviser: UserAnswers = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser).asOpt.value
  val unlimitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.UnlimitedCompany).asOpt.value
  val limitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedCompany).asOpt.value
  val businessPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.BusinessPartnership).asOpt.value
  val limitedPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedPartnership).asOpt.value
  val limitedLiabilityPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedLiabilityPartnership).asOpt.value
  val inUk: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(true).asOpt.value
  val notInUk: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false).asOpt.value

  val notInUkCompanyCheckMode: UserAnswers = UserAnswers(Json.obj())
    .areYouInUk(false)
    .set(BusinessNameId)("test company name").asOpt.value
    .set(NonUKBusinessTypeId)(NonUKBusinessType.Company).asOpt.value

  val notInUkCompanyCheckModeNoBusinessTypeId: UserAnswers = UserAnswers(Json.obj())
    .areYouInUk(false)
    .set(BusinessNameId)("test company name").asOpt.value

  val notInUkPartnershipCheckMode: UserAnswers = UserAnswers(Json.obj())
    .areYouInUk(false)
    .set(BusinessNameId)("test partnership name").asOpt.value
    .set(NonUKBusinessTypeId)(NonUKBusinessType.BusinessPartnership).asOpt.value

  val nonUkBusiness: UserAnswers = UserAnswers(Json.obj()).areYouInUk(false)
    .set(RegisterAsBusinessId)(true).asOpt.value
  val nonUkIndividual: UserAnswers = UserAnswers(Json.obj()).areYouInUk(false)
    .set(RegisterAsBusinessId)(false).asOpt.value

  val ukBusiness: UserAnswers = UserAnswers(Json.obj()).areYouInUk(true)
    .set(RegisterAsBusinessId)(true).asOpt.value
  val ukIndividual: UserAnswers = UserAnswers(Json.obj()).areYouInUk(true)
    .set(RegisterAsBusinessId)(false).asOpt.value

  val nonUkCompany: UserAnswers = notInUk.set(NonUKBusinessTypeId)(NonUKBusinessType.Company).asOpt.value
  val nonUkPartnership: UserAnswers = notInUk.set(NonUKBusinessTypeId)(NonUKBusinessType.BusinessPartnership).asOpt.value

  val registerAsBusinessIdCompanyOrPartnership: UserAnswers = UserAnswers()
    .set(RegisterAsBusinessId)(true).asOpt.value

  val registerAsBusinessIdIndividual: UserAnswers = UserAnswers()
    .set(RegisterAsBusinessId)(false).asOpt.value

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}

