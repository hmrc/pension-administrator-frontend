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

package controllers.register.partnership.partners

import java.time.LocalDate

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.TypedIdentifier
import identifiers.register.{BusinessNameId, DirectorsOrPartnersChangedId}
import identifiers.register.partnership.partners.{IsPartnerCompleteId, PartnerDOBId, PartnerNameId}
import models.requests.DataRequest
import models.{BusinessDetails, CheckMode, Index, NormalMode, PersonName, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val index = Index(0)
  val companyName = "Test Company Name"
  val partnerName = "test first name test last name"
  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  object FakeSectionComplete extends SectionComplete with FakeUserAnswersCacheConnector {

    override def setComplete(id: TypedIdentifier[Boolean], userAnswers: UserAnswers)
                            (implicit request: DataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] = {
      save("cacheId", id, true) map UserAnswers
    }

  }

  val userAnswers = UserAnswers(Json.obj(
    BusinessNameId.toString -> "Test Partnership Name",
  "partners" -> Json.arr(
    Json.obj(
      PartnerNameId.toString ->
        PersonName("test first name", "test last name")
    )
  )
  ))

  def nameAnswerRow: Seq[AnswerRow] = Seq(
    AnswerRow(
      "partnerName.cya.label",
      Seq("test first name test last name"),
      answerIsMessageKey = false,
      Some(Link(routes.PartnerNameController.onPageLoad(CheckMode, index).url)),
      Some(Message("partnerName.visuallyHidden.text"))
    ))

  def call = controllers.register.partnership.partners.routes.CheckYourAnswersController.onSubmit(0, NormalMode)

  def controller(dataRetrievalAction: DataRetrievalAction = getPartner) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeNavigator,
      messagesApi,
      FakeSectionComplete,
      countryOptions,
      FakeUserAnswersCacheConnector
    )

  def viewAsString(answers: Seq[AnswerRow]): String = check_your_answers(
    frontendAppConfig,
    Seq(
      AnswerSection(Some("partnerCheckYourAnswers.partnerDetails.heading"), answers),
      AnswerSection(Some("partnerCheckYourAnswers.contactDetails.heading"), Seq.empty)
    ),
    call,
    None,
    NormalMode
  )(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "render name row for partner" in {
      val result = controller().onPageLoad(index, NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(nameAnswerRow)
    }

    "render dob row for partner" in {

      val answers = new FakeDataRetrievalAction(Some(userAnswers.set(PartnerDOBId(0))(LocalDate.now).asOpt.value.json))
      val result = controller(answers).onPageLoad(index, NormalMode)(fakeRequest)

      val rows = nameAnswerRow ++ Seq(
      AnswerRow(
        Message("dob.heading", partnerName),
        Seq(DateHelper.formatDate(LocalDate.now)),
        answerIsMessageKey = false,
        Some(Link(routes.PartnerDOBController.onPageLoad(CheckMode, index).url)),
        Some(Message("dob.visuallyHidden.text", partnerName))
      ))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(rows)
    }

    "redirect to Session Expired page" when {
      "no existing data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(index, NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "mark partner as complete on submit" in {
      FakeUserAnswersCacheConnector.reset()
      val result = controller().onSubmit(index, NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeSectionComplete.verify(IsPartnerCompleteId(index), true)
      FakeUserAnswersCacheConnector.verifyNot(DirectorsOrPartnersChangedId)
    }

    "save the change flag for UpdateMode on submit" in {
      val result = controller().onSubmit(index, UpdateMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(DirectorsOrPartnersChangedId, true)
    }
  }
}
