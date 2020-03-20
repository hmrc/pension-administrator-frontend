/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import models._
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val partnershipName = "test company"
  private val defaultBusiness = Message("theBusiness").resolve
  private val defaultPartnership = Message("thePartnership").resolve
  private val vat = "Test Vat"
  private val paye = "Test Paye"
  private val addressYears = AddressYears.OverAYear
  private val email = "test@email"
  private val phone = "123"
  private val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )

  private val businessName = "Test Partnership"

  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]

  private val completeUserAnswers = UserAnswers().businessName().businessUtr().hasPaye(flag = true).enterPaye(paye).
    hasVatRegistrationNumber(flag = true).enterVat(vat).
    partnershipContactAddress(address).partnershipAddressYears(addressYears).partnershipPreviousAddress(address).
    partnershipEmail(email).partnershipPhone(phone)

  "CheckYourAnswers Controller" when {

    "on GET" must {

      "render the view correctly for all the rows of answer section" in {
        val retrievalAction = completeUserAnswers.dataRetrievalAction
        val rows = Seq(
          answerRow(
            Message("businessName.heading",
              Message("businessType.limitedPartnership").resolve.toLowerCase()).resolve, Seq(partnershipName)
          ),
          answerRow(
            Message("utr.heading",
              Message("businessType.limitedPartnership").resolve.toLowerCase()).resolve, Seq("1111111111")
          ),
          answerRow(
            Message("hasPAYE.heading", businessName), Seq("site.yes"), answerIsMessageKey = true,
            Some(Link(controllers.register.partnership.routes.HasPartnershipPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasPAYE.visuallyHidden.text", businessName))
          ),
          answerRow(
            Message("enterPAYE.heading", businessName), Seq(paye), answerIsMessageKey = false,
            Some(Link(controllers.register.partnership.routes.PartnershipEnterPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterPAYE.visuallyHidden.text", businessName))
          ),
          answerRow(
            Message("hasVAT.heading", partnershipName),
            Seq("site.yes"),
            answerIsMessageKey = true,
            Some(Link(controllers.register.partnership.routes.HasPartnershipVATController.onPageLoad(CheckMode).url)),
            Some(Message("hasVAT.visuallyHidden.text", partnershipName))
          ),
          answerRow(
            Message("enterVAT.heading", partnershipName),
            Seq(vat),
            answerIsMessageKey = false,
            Some(Link(controllers.register.partnership.routes.PartnershipEnterVATController.onPageLoad(CheckMode).url)),
            Some(Message("enterVAT.visuallyHidden.text", partnershipName))
          ),
          answerRow(
            Message("cya.label.contact.address", defaultBusiness),
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            ),
            answerIsMessageKey = false,
            Some(Link(routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultBusiness))
          ),
          answerRow(
            Message("addressYears.heading", defaultPartnership),
            Seq(s"common.addressYears.${addressYears.toString}"),
            answerIsMessageKey = true,
            Some(Link(controllers.register.partnership.routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultPartnership))
          ),
          answerRow(
            Message("previousAddress.checkYourAnswersLabel", defaultBusiness),
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            ),
            answerIsMessageKey = false,
            Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultBusiness))
          ),
          answerRow(
            label = messages("email.title", defaultPartnership),
            answer = Seq(email),
            changeUrl = Some(Link(controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultPartnership))
          ),
          answerRow(
            label = messages("phone.title", defaultPartnership),
            answer = Seq(phone),
            changeUrl = Some(Link(controllers.register.partnership.routes.PartnershipPhoneController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultPartnership))
          )
        )

        val sections = Seq(AnswerSection(None, rows))
        testRenderedView(sections, retrievalAction)
      }

      "redirect to session expired page if no existing data" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "on POST" must {
      "redirect to the next page" in {
        val result = controller().onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "redirect to Session expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getPartnership) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      new FakeCountryOptions(environment, frontendAppConfig),
      stubMessagesControllerComponents(),
      view
    )

  private def call = routes.CheckYourAnswersController.onSubmit()

  private def answerSections(sectionLabel: Option[String] = None, rows: Seq[AnswerRow]): Seq[AnswerSection] = {
    val section = AnswerSection(sectionLabel, rows)
    Seq(section)
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false, changeUrl: Option[Link] = None,
                        visuallyHiddenLabel: Option[Message] = None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)
    status(result) mustBe OK
    contentAsString(result) mustBe
      view(
        sections,
        call,
        None,
        NormalMode,
        isComplete = true
      )(fakeRequest, messages).toString()
  }
}
