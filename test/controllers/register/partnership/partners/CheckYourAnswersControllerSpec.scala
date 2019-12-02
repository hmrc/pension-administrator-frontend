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

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerWithCommonBehaviour
import controllers.register.partnership.partners.routes._
import identifiers.register.DirectorsOrPartnersChangedId
import identifiers.register.partnership.partners._
import models.Mode.{checkMode, _}
import models._
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerWithCommonBehaviour {

  private val email = "test@test.com"
  private val phone = "1234"
  private val index = Index(0)
  private val partnerDetails = PersonName("Test", "Name")
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val defaultPartnerName = Message("thePartner").resolve

  private def call(mode: Mode): Call = CheckYourAnswersController.onSubmit(index, mode)

  private val address = Address("line1", "line2", None, None, Some("zz11zz"), "country")

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link] = None, visuallyHiddenLabel: Option[Message] = None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  val view: check_your_answers = app.injector.instanceOf[check_your_answers]

  "CheckYourAnswers Controller" when {

    "on a GET" must {

      Seq(NormalMode, UpdateMode).foreach { mode =>
        s"render the view correctly for name and dob in ${jsLiteral.to(mode)}" in {
          val retrievalAction = UserAnswers().set(PartnerNameId(index))(partnerDetails).flatMap(
            _.set(PartnerDOBId(0))(LocalDate.now)).asOpt.value.dataRetrievalAction
          val rows = Seq(
            AnswerRow(
              "partnerName.cya.label",
              Seq(s"${partnerDetails.firstName} ${partnerDetails.lastName}"),
              answerIsMessageKey = false,
              Some(Link(routes.PartnerNameController.onPageLoad(checkMode(mode), index).url)),
              Some(Message("partnerName.visuallyHidden.text"))
            ),
            AnswerRow(
              messages("dob.heading", partnerDetails.fullName),
              Seq(s"${DateHelper.formatDate(LocalDate.now)}"),
              answerIsMessageKey = false,
              Some(Link(PartnerDOBController.onPageLoad(checkMode(mode), index).url)),
              Some(Message("dob.visuallyHidden.text", partnerDetails.fullName))
            ))
          val sections = Seq(AnswerSection(None, rows))
          testRenderedView(
            sections = sections, dataRetrievalAction = retrievalAction, mode = mode
          )
        }

        s"render the view correctly for nino in ${jsLiteral.to(mode)}" in {
          val nino = ReferenceValue("AB100100A")
          val reason = "test reason"
          val retrievalAction = UserAnswers().partnerHasNINO(index, flag = true).partnerEnterNINO(index, nino)
            .partnerNoNINOReason(index, reason).dataRetrievalAction
          val rows = Seq(
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
            )
          )

          val sections = Seq(AnswerSection(None, rows))

          testRenderedView(
            sections = sections, dataRetrievalAction = retrievalAction, mode = mode
          )
        }

        s"render the view correctly for utr in ${jsLiteral.to(mode)}" in {
          val utr = ReferenceValue("1111111111")
          val reason = "test reason"
          val retrievalAction = UserAnswers().partnerHasUTR(index, flag = true).partnerEnterUTR(index, utr)
            .partnerNoUTRReason(index, reason).dataRetrievalAction
          val rows = Seq(
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
            )
          )

          val sections = Seq(AnswerSection(None, rows))

          testRenderedView(
            sections = sections, dataRetrievalAction = retrievalAction, mode = mode
          )
        }

        s"render the view correctly for address in ${jsLiteral.to(mode)}" in {
          val addressYears = AddressYears.UnderAYear
          val retrievalAction = UserAnswers().partnerAddress(index, address).partnerAddressYears(index, addressYears)
            .partnerPreviousAddress(index, address).dataRetrievalAction
          val rows = Seq(
            answerRow(Message("address.checkYourAnswersLabel", defaultPartnerName),
              Seq(
                address.addressLine1,
                address.addressLine2,
                address.postcode.value,
                address.country
              ),
              answerIsMessageKey = false,
              Some(Link(PartnerAddressController.onPageLoad(checkMode(mode), index).url)),
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
              Some(Link(PartnerPreviousAddressController.onPageLoad(checkMode(mode), index).url)),
              visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultPartnerName)))
          )

          val sections = Seq(AnswerSection(None, rows))

          testRenderedView(
            sections = sections, dataRetrievalAction = retrievalAction, mode = mode
          )
        }

        s"render the view correctly for email and phone in ${jsLiteral.to(mode)}" in {
          val retrievalAction = UserAnswers().set(PartnerEmailId(index))(email).flatMap(
            _.set(PartnerPhoneId(index))(phone)).asOpt.value.dataRetrievalAction
          val rows = Seq(
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

          val sections = Seq(AnswerSection(None, rows))

          testRenderedView(
            sections = sections, dataRetrievalAction = retrievalAction, mode = mode
          )
        }
      }
    }

    "on a POST" must {
      "mark partner as complete on submit" in {
        FakeUserAnswersCacheConnector.reset()
        val result = controller().onSubmit(index, NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeSectionComplete.verify(IsPartnerCompleteId(index), value = true)
        FakeUserAnswersCacheConnector.verifyNot(DirectorsOrPartnersChangedId)
      }

      "save the change flag for UpdateMode on submit" in {
        val result = controller().onSubmit(index, UpdateMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DirectorsOrPartnersChangedId, value = true)
      }
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getPartner) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeNavigator,
      FakeSectionComplete,
      countryOptions,
      FakeUserAnswersCacheConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction, mode: Mode = NormalMode): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(index, mode)(fakeRequest)
    val expectedResult = view(sections,
      call(mode),
      None,
      mode
    )(fakeRequest, messages).toString()

    status(result) mustBe OK
    contentAsString(result) mustBe expectedResult
  }
}
