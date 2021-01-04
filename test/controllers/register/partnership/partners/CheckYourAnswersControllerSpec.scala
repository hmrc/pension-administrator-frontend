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

package controllers.register.partnership.partners

import java.time.LocalDate

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.partnership.partners.routes._
import identifiers.register.DirectorsOrPartnersChangedId
import models.Mode.{checkMode, _}
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    FakeUserAnswersCacheConnector.reset()
    when(mockDataCompletion.isPartnerComplete(any(), any())).thenReturn(true)
  }

  "CheckYourAnswers Controller" when {

    "on a GET" must {

      Seq(NormalMode, UpdateMode).foreach { mode =>
        s"render the view correctly for all the rows of answer section in ${jsLiteral.to(mode)}" in {
          val retrievalAction = completeUserAnswers.dataRetrievalAction
          val rows = answerRows(mode)
          val sections = Seq(AnswerSection(None, rows))

          testRenderedView(
            sections, retrievalAction, mode = mode)
        }

        s"redirect to partner name page when partner name is not entered for ${jsLiteral.to(mode)}" in {
          val result = controller(getEmptyData).onPageLoad(index, mode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.PartnerNameController.onPageLoad(mode, index).url
        }
      }
    }

    "on a POST" must {
      "not save the change flag but redirect to the next page on Normal mode when data is complete" in {
        val result = controller().onSubmit(index, NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verifyNot(DirectorsOrPartnersChangedId)
      }

      "not save the change flag and load the cya page on submit when data is not complete" in {
        when(mockDataCompletion.isPartnerComplete(any(), any())).thenReturn(false)
        val retrievalAction = completeUserAnswers.dataRetrievalAction
        val rows = answerRows(UpdateMode)
        val sections = Seq(AnswerSection(None, rows))

        testRenderedView(
          sections, retrievalAction, mode = UpdateMode, isComplete = false)

        FakeUserAnswersCacheConnector.verifyNot(DirectorsOrPartnersChangedId)
      }

      "save the change flag and redirect to the next page for UpdateMode on submit when data is complete" in {
        val result = controller().onSubmit(index, UpdateMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DirectorsOrPartnersChangedId, value = true)
      }
    }
  }

  private val mockDataCompletion = mock[DataCompletion]
  private val address = Address("line1", "line2", None, None, Some("zz11zz"), "country")
  private val email = "test@test.com"
  private val phone = "1234"
  private val index = Index(0)
  private val partnerDetails = PersonName("Test", "Name")
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val defaultPartnerName = Message("thePartner")
  private val nino = ReferenceValue("AB100100A")
  private val reason = "test reason"
  private val utr = ReferenceValue("1111111111")
  private val addressYears = AddressYears.UnderAYear
  private val dob = LocalDate.now().minusYears(20)

  private val completeUserAnswers = UserAnswers().partnerName(index = 0, partnerDetails).
    partnerDob(index = 0, dob).
    partnerHasNINO(index = 0, flag = true).partnerEnterNINO(index = 0, nino).partnerNoNINOReason(index = 0, reason).
    partnerHasUTR(index = 0, flag = true).partnerEnterUTR(index = 0, utr).partnerNoUTRReason(index = 0, reason = reason).
    partnerAddress(index, address).partnerAddressYears(index, addressYears).partnerPreviousAddress(index, address).
    partnerEmail(index = 0, email).partnerPhone(index = 0, phone)

  private def call(mode: Mode): Call = CheckYourAnswersController.onSubmit(index, mode)

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link], visuallyHiddenLabel: Option[Message]): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]

  def controller(dataRetrievalAction: DataRetrievalAction = getPartner) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockDataCompletion,
      FakeNavigator,
      countryOptions,
      FakeUserAnswersCacheConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction,
                               mode: Mode, isComplete: Boolean = true): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(index, mode)(fakeRequest)
    val expectedResult = view(sections,
      call(mode),
      None,
      mode,
      isComplete
    )(fakeRequest, messages).toString()

    status(result) mustBe OK
    contentAsString(result) mustBe expectedResult
  }

  private def answerRows(mode: Mode) = Seq(
    answerRow(
      label = "partnerName.cya.label",
      answer = Seq(s"${partnerDetails.firstName} ${partnerDetails.lastName}"),
      changeUrl = Some(Link(routes.PartnerNameController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("partnerName.visuallyHidden.text"))
    ),
    answerRow(
      label = messages("dob.heading", partnerDetails.fullName),
      answer = Seq(s"${DateHelper.formatDate(dob)}"),
      changeUrl = Some(Link(PartnerDOBController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("dob.visuallyHidden.text", partnerDetails.fullName))
    ),
    answerRow(
      label = messages("hasNINO.heading", defaultPartnerName),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(HasPartnerNINOController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("hasNINO.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(
      label = messages("enterNINO.heading", defaultPartnerName),
      answer = Seq(nino.value),
      changeUrl = Some(Link(PartnerEnterNINOController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("enterNINO.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(
      label = messages("whyNoNINO.heading", defaultPartnerName),
      answer = Seq(reason),
      changeUrl = Some(Link(PartnerNoNINOReasonController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("whyNoNINO.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(
      label = messages("hasUTR.heading", defaultPartnerName),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(HasPartnerUTRController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("hasUTR.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(
      label = messages("enterUTR.heading", defaultPartnerName),
      answer = Seq(utr.value),
      changeUrl = Some(Link(PartnerEnterUTRController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("enterUTR.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(
      label = messages("whyNoUTR.heading", defaultPartnerName),
      answer = Seq(reason),
      changeUrl = Some(Link(PartnerNoUTRReasonController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("whyNoUTR.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(Message("address.checkYourAnswersLabel", defaultPartnerName),
      Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      answerIsMessageKey = false,
      Some(Link(PartnerAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("address.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(Message("addressYears.heading", defaultPartnerName),
      Seq(s"common.addressYears.${addressYears.toString}"), answerIsMessageKey = true,
      Some(Link(PartnerAddressYearsController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(Message("previousAddress.checkYourAnswersLabel", defaultPartnerName),
      Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      answerIsMessageKey = false,
      Some(Link(PartnerPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(
      label = messages("email.title", defaultPartnerName),
      answer = Seq(email),
      changeUrl = Some(Link(PartnerEmailController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultPartnerName))
    ),
    answerRow(
      label = messages("phone.title", defaultPartnerName),
      answer = Seq(phone),
      changeUrl = Some(Link(PartnerPhoneController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultPartnerName))
    )
  )
}
