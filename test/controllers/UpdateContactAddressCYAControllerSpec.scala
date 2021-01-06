/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import controllers.actions.{AuthAction, DataRetrievalAction, FakeAllowAccessProvider}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UpdateMode, UserType}
import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.mvc.{AnyContent, BodyParser, Request, Result}
import play.api.test.Helpers.{contentAsString, status, _}
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import utils.testhelpers.ViewPsaDetailsBuilder._
import viewmodels.{AnswerRow, AnswerSection, PsaViewDetailsViewModel, SuperSection}
import views.html.updateContactAddressCYA

import scala.concurrent.{ExecutionContext, Future}

class UpdateContactAddressCYAControllerSpec extends ControllerSpecBase {

  val organisationSuperSections: Seq[SuperSection] = Seq(
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("vat.label", Seq("12345678"), answerIsMessageKey = false, None),
            AnswerRow("paye.label", Seq("9876543210"), answerIsMessageKey = false, None),
            AnswerRow("crn.label", Seq("1234567890"), answerIsMessageKey = false, None),
            AnswerRow("utr.label", Seq("121414151"), answerIsMessageKey = false, None),
            AnswerRow("company.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), answerIsMessageKey = false, None),
            AnswerRow("Has Test company name been at their address for more than 12 months?", Seq("No"), answerIsMessageKey = false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), answerIsMessageKey = false, None),
            AnswerRow("company.email.label", Seq("aaa@aa.com"), answerIsMessageKey = false, None),
            AnswerRow("company.phone.label", Seq("0044-09876542312"), answerIsMessageKey = false, None))))),
    SuperSection(
      Some("director.supersection.header"),
      Seq(
        AnswerSection(
          Some("abcdef dfgdsfff dfgfdgfdg"),
          Seq(
            AnswerRow("cya.label.dob", Seq("1950-03-29"), answerIsMessageKey = false, None),
            AnswerRow("common.nino", Seq("AA999999A"), answerIsMessageKey = false, None),
            AnswerRow("utr.label", Seq("1234567892"), answerIsMessageKey = false, None),
            AnswerRow("cya.label.address", Seq("addressline1,", "addressline2,", "addressline3,", "addressline4,", "B5 9EX,", "Country of GB"), answerIsMessageKey = false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("line1,", "line2,", "line3,", "line4,", "567253,", "Country of AD"), answerIsMessageKey = false, None),
            AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), answerIsMessageKey = false, None),
            AnswerRow("phone.label", Seq("0044-09876542312"), answerIsMessageKey = false, None))),
        AnswerSection(
          Some("sdfdff sdfdsfsdf dfdsfsf"),
          Seq(
            AnswerRow("cya.label.dob", Seq("1950-07-29"), answerIsMessageKey = false, None),
            AnswerRow("common.nino", Seq("AA999999A"), answerIsMessageKey = false, None),
            AnswerRow("utr.label", Seq("7897700000"), answerIsMessageKey = false, None),
            AnswerRow("cya.label.address", Seq("fgfdgdfgfd,", "dfgfdgdfg,", "fdrtetegfdgdg,", "dfgfdgdfg,", "56546,", "Country of AD"), answerIsMessageKey = false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("werrertqe,", "ereretfdg,", "asafafg,", "fgdgdasdf,", "23424,", "Country of AD"), answerIsMessageKey = false, None),
            AnswerRow("email.label", Seq("aaa@gmail.com"), answerIsMessageKey = false, None),
            AnswerRow("phone.label", Seq("0044-09876542334"), answerIsMessageKey = false, None))))),
    SuperSection(
      Some("pensionAdvisor.section.header"),
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("pensions.advisor.label", Seq("Pension Advisor"), answerIsMessageKey = false, None),
            AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), answerIsMessageKey = false, None),
            AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4 ,", "56765,", "Country of AD"), answerIsMessageKey = false, None))))))

  private val externalId = "test-external-id"

  val fakePsaDataService: PsaDetailsService = mock[PsaDetailsService]

  val view: updateContactAddressCYA = app.injector.instanceOf[updateContactAddressCYA]

  "Update contact address CYA Controller" must {
    "return 200 and  correct view for a GET for PSA company" in {
      when(fakePsaDataService.retrievePsaDataAndGenerateContactDetailsOnlyViewModel(any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(PsaViewDetailsViewModel(companyWithChangeLinks, "Test company name",
          isUserAnswerUpdated = false, userAnswersIncompleteMessage = Some("incomplete.alert.message"))))

      val result = controller(userType = UserType.Organisation, psaId = Some("test Psa id")).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyWithChangeLinks, "Test company name")
    }

    "redirect to session expired if psa id not present" in {
      when(fakePsaDataService.retrievePsaDataAndGenerateContactDetailsOnlyViewModel(any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(PsaViewDetailsViewModel(companyWithChangeLinks, "Test company name",
          isUserAnswerUpdated = false, userAnswersIncompleteMessage = Some("incomplete.alert.message"))))

      val result = controller(userType = UserType.Organisation, psaId = None).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, userType: UserType, psaId : Option[String]) =
    new UpdateContactAddressCYAController(
      frontendAppConfig,
      FakeNavigator,
      new FakeAuthAction(userType, psaId),
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      fakePsaDataService,
      stubMessagesControllerComponents(),
      view
    )

  class FakeAuthAction(userType: UserType, psaId : Option[String]) extends AuthAction {
    val parser: BodyParser[AnyContent] = stubMessagesControllerComponents().parsers.defaultBodyParser
    implicit val executionContext: ExecutionContext = inject[ExecutionContext]
    override def invokeBlock[A](request: Request[A],
                                block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, externalId, PSAUser(userType, None, isExistingPSA = false, None, psaId)))
  }

  private def viewAsString(superSections: Seq[SuperSection], name: String,
                           isUserAnswerUpdated: Boolean = false,
                           userAnswersIncompleteMessage: Option[String] = Some("incomplete.alert.message")): String = {
    val model = PsaViewDetailsViewModel(superSections, name, isUserAnswerUpdated, userAnswersIncompleteMessage)
    view(model, FakeNavigator.desiredRoute)(fakeRequest, messages).toString
  }
}
