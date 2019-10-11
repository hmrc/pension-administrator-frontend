/*
 * Copyright 2019 HM Revenue & Customs
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
import config.FeatureSwitchManagementService
import connectors.FakeUserAnswersCacheConnector
import controllers.register.routes
import identifiers.Identifier
import identifiers.register._
import identifiers.register.company.BusinessDetailsId
import identifiers.register.partnership.PartnershipDetailsId
import models.register.{BusinessType, DeclarationWorkingKnowledge, NonUKBusinessType}
import models.requests.IdentifiedRequest
import models.{BusinessDetails, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class RegisterNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  //scalastyle:off line.size.limit
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
    (BusinessTypeId, unlimitedCompany, businessDetailsPage, false, None, false),
    (BusinessTypeId, limitedCompany, businessDetailsPage, false, None, false),
    (BusinessTypeId, businessPartnership, partnershipBusinessDetails, false, None, false),
    (BusinessTypeId, limitedPartnership, partnershipBusinessDetails, false, None, false),
    (BusinessTypeId, limitedLiabilityPartnership, partnershipBusinessDetails, false, None, false),

    (DeclarationId, emptyAnswers, declarationWorkingKnowledgePage, true, None, false),

    (DeclarationWorkingKnowledgeId, haveDeclarationWorkingKnowledge, declarationFitAndProperPage, true, None, false),
    (DeclarationWorkingKnowledgeId, haveAnAdviser, adviserName, true, None, false),
    (DeclarationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage, false, None, false),

    (DeclarationFitAndProperId, emptyAnswers, confirmation, false, None, false),



        (AreYouInUKId, inUk, ukBusinessType, false, Some(registerAsBusiness), false),
        (AreYouInUKId, notInUk, nonUkBusinessType, false, Some(registerAsBusiness), false),



        (RegisterAsBusinessId, registerAsBusinessIdCompanyOrPartnership, isCompanyRegisteredInUKPage, false, None, false),
        (RegisterAsBusinessId, registerAsBusinessIdIndividual, isIndividualBasedInUKPage, false, None, false),





//    (AreYouInUKId, inUk, ukBusinessType, false, Some(registerAsBusiness), false),
//    (AreYouInUKId, notInUk, nonUkBusinessType, false, Some(registerAsBusiness), false),
//    (AreYouInUKId, notInUkCompanyCheckMode, registerAsBusiness, false, Some(nonUkCompanyAddress), false),
//    (AreYouInUKId, notInUkCompanyCheckModeNoBusinessTypeId, registerAsBusiness, false, Some(registerAsBusiness), false),
//    (AreYouInUKId, notInUkPartnershipCheckMode, registerAsBusiness, false, Some(nonUkPartnershipAddress), false),

//    (RegisterAsBusinessId, nonUkBusiness, nonUkBusinessType, false, None, false),
//    (RegisterAsBusinessId, nonUkIndividual, nonUkIndividualName, false, None, false),
//    (RegisterAsBusinessId, ukBusiness, ukBusinessType, false, None, false),
//    (RegisterAsBusinessId, ukIndividual, ukIndividualDetailsCorrect, false, None, false),

    (NonUKBusinessTypeId, nonUkCompany, nonUkCompanyRegisteredName, false, None, false),
    (NonUKBusinessTypeId, nonUkPartnership, nonUkPartnershipRegisteredName, false, None, false)
  )

//  def routesWithIVDisabled(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
//    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
//    (AreYouInUKId, inUk, ukBusinessType, false, Some(ukBusinessType), false),
//    (AreYouInUKId, notInUk, registerAsBusiness, false, Some(registerAsBusiness), false),
//    (AreYouInUKId, notInUkCompanyCheckMode, registerAsBusiness, false, Some(nonUkCompanyAddress), false),
//    (AreYouInUKId, notInUkCompanyCheckModeNoBusinessTypeId, registerAsBusiness, false, Some(registerAsBusiness), false),
//    (AreYouInUKId, notInUkPartnershipCheckMode, registerAsBusiness, false, Some(nonUkPartnershipAddress), false),
//
//    (RegisterAsBusinessId, nonUkBusiness, nonUkBusinessType, false, None, false),
//    (RegisterAsBusinessId, nonUkIndividual, nonUkIndividualName, false, None, false)
//  )

  //scalastyle:on line.size.limit
  val navigator = new RegisterNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, fakeFeatureSwitchManagerService())
  s"${navigator.getClass.getSimpleName} when toggle is on" must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
  }
}

object RegisterNavigatorSpec extends OptionValues {
  def fakeFeatureSwitchManagerService(isIvEnabled: Boolean = true): FeatureSwitchManagementService = new FeatureSwitchManagementService {
    override def change(name: String, newValue: Boolean): Boolean = ???

    override def get(name: String): Boolean = isIvEnabled

    override def reset(name: String): Unit = ???
  }

  lazy val emptyAnswers = UserAnswers(Json.obj())
  lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()
  lazy val businessDetailsPage: Call = controllers.register.company.routes.CompanyBusinessDetailsController.onPageLoad()
  lazy val partnershipBusinessDetails: Call = controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad()
  lazy val declarationWorkingKnowledgePage: Call = routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
  lazy val declarationFitAndProperPage: Call = routes.DeclarationFitAndProperController.onPageLoad()
  lazy val adviserName: Call = controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode)
  lazy val confirmation: Call = routes.ConfirmationController.onPageLoad()
  lazy val survey: Call = controllers.routes.LogoutController.onPageLoad()
  lazy val ukBusinessType: Call = controllers.register.routes.BusinessTypeController.onPageLoad(NormalMode)
  lazy val nonUkBusinessType: Call = controllers.register.routes.NonUKBusinessTypeController.onPageLoad()
  lazy val nonUkCompanyRegisteredName: Call = controllers.register.company.routes.CompanyRegisteredNameController.onPageLoad(NormalMode)
  lazy val registerAsBusiness: Call = controllers.register.routes.RegisterAsBusinessController.onPageLoad()
  lazy val nonUkIndividualName: Call = controllers.register.individual.routes.IndividualNameController.onPageLoad(NormalMode)
  lazy val nonUkCompanyAddress: Call = controllers.register.company.routes.CompanyRegisteredAddressController.onPageLoad()
  lazy val nonUkPartnershipAddress: Call = controllers.register.partnership.routes.PartnershipRegisteredAddressController.onPageLoad()
  lazy val nonUkPartnershipRegisteredName: Call = controllers.register.partnership.routes.PartnershipRegisteredNameController.onPageLoad()
  lazy val ukIndividualDetailsCorrect: Call = controllers.register.individual.routes.IndividualDetailsCorrectController.onPageLoad(NormalMode)

  val isCompanyRegisteredInUKPage = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(NormalMode)
  val isIndividualBasedInUKPage = controllers.register.individual.routes.IndividualAreYouInUKController.onPageLoad(NormalMode)

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
    .set(BusinessDetailsId)(BusinessDetails("test company name", Some("1234567890"))).asOpt.value
    .set(NonUKBusinessTypeId)(NonUKBusinessType.Company).asOpt.value

  val notInUkCompanyCheckModeNoBusinessTypeId: UserAnswers = UserAnswers(Json.obj())
    .areYouInUk(false)
    .set(BusinessDetailsId)(BusinessDetails("test company name", Some("1234567890"))).asOpt.value

  val notInUkPartnershipCheckMode: UserAnswers = UserAnswers(Json.obj())
    .areYouInUk(false)
    .set(PartnershipDetailsId)(BusinessDetails("test partnership name", Some("1234567890"))).asOpt.value
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



  //

  val registerAsBusinessIdCompanyOrPartnership: UserAnswers = UserAnswers()
    .set(RegisterAsBusinessId)(true).asOpt.value

  val registerAsBusinessIdIndividual: UserAnswers = UserAnswers()
    .set(RegisterAsBusinessId)(false).asOpt.value



  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
