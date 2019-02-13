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
import identifiers.register.partnership.partners.IsPartnerCompleteId
import models.requests.DataRequest
import models.{CheckMode, Index, NormalMode}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val index = Index(0)
  val companyName = "Test Company Name"
  val partnerName = "test first name test middle name test last name"
  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  object FakeSectionComplete extends SectionComplete with FakeUserAnswersCacheConnector {

    override def setComplete(id: TypedIdentifier[Boolean], userAnswers: UserAnswers)
                            (implicit request: DataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] = {
      save("cacheId", id, true) map UserAnswers
    }

  }

  def answersDD: Seq[AnswerRow] = Seq(
    AnswerRow(
      "cya.label.name",
      Seq("test first name test last name"),
      answerIsMessageKey = false,
      Link(routes.PartnerDetailsController.onPageLoad(CheckMode, index).url)
    ),
    AnswerRow(
      "cya.label.dob",
      Seq(DateHelper.formatDate(LocalDate.now)),
      answerIsMessageKey = false,
      Link(routes.PartnerDetailsController.onPageLoad(CheckMode, index).url)
    ))

  def call = controllers.register.partnership.partners.routes.CheckYourAnswersController.onSubmit(0)

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
      countryOptions
    )

  def viewAsString(): String = check_your_answers(
    frontendAppConfig,
    Seq(
      AnswerSection(Some("partnerCheckYourAnswers.partnerDetails.heading"), answersDD),
      AnswerSection(Some("partnerCheckYourAnswers.contactDetails.heading"), Seq.empty)
    ),
    Some(partnerName),
    call
  )(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(index, NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired page" when {
      "partner name is not present" in {
        val result = controller(getEmptyData).onPageLoad(index, NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

      "no existing data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(index, NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "mark partner as complete on submit" in {

      val result = controller().onSubmit(index, NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeSectionComplete.verify(IsPartnerCompleteId(index), true)

    }

  }
}
