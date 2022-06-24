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

package controllers.register.administratorPartnership.contactDetails

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.administratorPartnership.partnershipDetails.routes._
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends ControllerSpecBase
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isPartnershipDetailsComplete(any())).thenReturn(true)
  }

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val defaultPartnership = "limited partnership"
  private val vat = "test-vat"
  private val paye = "test-paye"
  private val addressYears = AddressYears.UnderAYear
  private val email = "test@test.com"
  private val phone = "111"
  private val address = Address("Telford1", "Telford2", None, None, Some("TF3 4ER"), "Country of GB")
  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]
  private val mockDataCompletion = mock[DataCompletion]

  def controller(dataRetrievalAction: DataRetrievalAction) =
    new CheckYourAnswersController(
      controllerComponents,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      view
    )(new FakeCountryOptions(environment, frontendAppConfig))

  private def call = routes.CheckYourAnswersController.onSubmit()

  private def answerRow(
                         label: String,
                         answer: Seq[String],
                         answerIsMessageKey: Boolean = false,
                         changeUrl: Option[Link] = None,
                         visuallyHiddenLabel: Option[Message] = None
                       ): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def testRenderedView(
                                sections: Seq[AnswerSection],
                                result: Future[Result],
                                isComplete: Boolean = true
                              ): Unit = {

    status(result) mustBe OK

    contentAsString(result) mustBe
      view(
        answerSections = sections,
        postUrl = call,
        psaNameOpt = None,
        mode = NormalMode,
        isComplete = isComplete
      )(fakeRequest, messages).toString()
  }

  private val answerRows = Seq(
    answerRow(
      label = Message("businessName.heading", defaultPartnership),
      answer = Seq(defaultPartnership)
    ),
    answerRow(
      label = Message("utr.heading", defaultPartnership),
      answer = Seq("1111111111")
    ),
    answerRow(
      label = Message("cya.label.contact.address", defaultPartnership),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("addressYears.heading", defaultPartnership),
      answer = Seq(s"common.addressYears.${addressYears.toString}"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("previousAddress.checkYourAnswersLabel", defaultPartnership),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = messages("email.title", defaultPartnership),
      answer = Seq(email),
      changeUrl = Some(Link(routes.PartnershipEmailController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = messages("phone.title", defaultPartnership),
      answer = Seq(phone),
      changeUrl = Some(Link(routes.PartnershipPhoneController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultPartnership))
    )
  )

  private val answerRowsNonUK = Seq(
    answerRow(
      label = Message("businessName.heading", defaultPartnership),
      answer = Seq(defaultPartnership)
    ),
    answerRow(
      label = Message("cya.label.contact.address", defaultPartnership),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("addressYears.heading", defaultPartnership),
      answer = Seq(s"common.addressYears.${addressYears.toString}"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("previousAddress.checkYourAnswersLabel", defaultPartnership),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = messages("email.title", defaultPartnership),
      answer = Seq(email),
      changeUrl = Some(Link(routes.PartnershipEmailController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = messages("phone.title", defaultPartnership),
      answer = Seq(phone),
      changeUrl = Some(Link(routes.PartnershipPhoneController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultPartnership))
    )
  )

  "CheckYourAnswers Controller" when {

//    "on GET" must {
//
//      "render the view correctly for all the rows of answer section if business name and utr is present for UK" in {
//        val retrievalAction = UserAnswers().completePartnershipDetailsUK.dataRetrievalAction
//        val result = controller(retrievalAction).onPageLoad()(fakeRequest)
//
//        val sections = Seq(AnswerSection(None, answerRows))
//        testRenderedView(sections, result)
//      }
//
//      "render the view correctly for all the rows of answer section if business name and address is present for NON UK" in {
//        val retrievalAction = UserAnswers().completePartnershipDetailsNonUK.dataRetrievalAction
//        val result = controller(retrievalAction).onPageLoad()(fakeRequest)
//
//        val sections = Seq(AnswerSection(None, answerRowsNonUK))
//        testRenderedView(sections, result)
//      }
//
//      "redirect to register as business page when business name, registration info and utr is not present for UK" in {
//        val result = controller(UserAnswers().areYouInUk(answer = true).dataRetrievalAction).onPageLoad()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
//      }
//
//      "redirect to register as business page when business name, registration info and address is not present for NON UK" in {
//        val result = controller(UserAnswers().areYouInUk(answer = false).dataRetrievalAction).onPageLoad()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
//      }
//
//      "redirect to session expired page if no existing data" in {
//        val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//      }
//    }
//
//    "on POST" must {
//      "redirect to the next page when data is complete" in {
//        val retrievalAction = UserAnswers().completePartnershipDetailsUK.dataRetrievalAction
//        val result = controller(retrievalAction).onSubmit()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(onwardRoute.url)
//      }
//
//      "load the same cya page when data is not complete" in {
//        when(mockDataCompletion.isPartnershipDetailsComplete(any())).thenReturn(false)
//        val retrievalAction = UserAnswers().completePartnershipDetailsUK.dataRetrievalAction
//        val result = controller(retrievalAction).onSubmit()(fakeRequest)
//
//        val sections = Seq(AnswerSection(None, answerRows))
//        testRenderedView(sections, result, isComplete = false)
//      }
//
//      "redirect to Session expired if there is no cached data" in {
//        val result = controller(dontGetAnyData).onSubmit()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//      }
//    }
  }
}
