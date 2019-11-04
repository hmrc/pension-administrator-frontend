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

package controllers.register.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.partnership._
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId, EnterVATId, HasVATId, _}
import models._
import models.register.BusinessType
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" when {

    "GET" must {

      "renders name and utr" in {
        val rows = Seq(
          answerRow(Message("businessName.heading",
            Message("businessType.limitedPartnership").resolve.toLowerCase()).resolve, Seq(partnershipName)),
          answerRow(Message("utr.heading",
            Message("businessType.limitedPartnership").resolve.toLowerCase()).resolve, Seq("Test UTR"))
        )

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          BusinessTypeId.toString -> BusinessType.LimitedPartnership.toString,
          BusinessNameId.toString -> partnershipName,
          BusinessUTRId.toString -> "Test UTR"
        )
        testRenderedView(sections, retrievalAction)
      }

      "renders paye number" in {
        val answerRows = Seq(
          answerRow(Message("businessName.heading",
            Message("businessType.limitedPartnership").resolve.toLowerCase()).resolve, Seq(businessName)),
          answerRow(Message("hasPAYE.heading", businessName), Seq("site.yes"), answerIsMessageKey = true,
            Some(Link(controllers.register.partnership.routes.HasPartnershipPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasPAYE.visuallyHidden.text", businessName))),
          answerRow(Message("enterPAYE.heading", businessName), Seq("Test Paye"), answerIsMessageKey = false,
            Some(Link(controllers.register.partnership.routes.PartnershipEnterPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterPAYE.visuallyHidden.text", businessName))))

        val sections = answerSections(None, answerRows)

        val retrievalAction = dataRetrievalAction(
          BusinessTypeId.toString -> BusinessType.LimitedPartnership.toString,
          BusinessNameId.toString -> businessName,
          HasPAYEId.toString -> true,
          EnterPAYEId.toString -> "Test Paye"
        )
        testRenderedView(sections, retrievalAction)
      }

      "renders vat registration number" in {
        val rows = Seq(
          answerRow(Message("businessName.heading",
            Message("businessType.limitedPartnership").resolve.toLowerCase()).resolve, Seq(partnershipName)),
          answerRow(
            Message("hasVAT.heading", partnershipName),
            Seq("site.yes"),
            answerIsMessageKey = true,
            Some(Link(controllers.register.partnership.routes.HasPartnershipVATController.onPageLoad(CheckMode).url)),
            Some(Message("hasVAT.visuallyHidden.text", partnershipName))
          ),
          answerRow(
            Message("enterVAT.heading", partnershipName),
            Seq("Test Vat"),
            answerIsMessageKey = false,
            Some(Link(controllers.register.partnership.routes.PartnershipEnterVATController.onPageLoad(CheckMode).url)),
            Some(Message("enterVAT.visuallyHidden.text", partnershipName))
          ))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          BusinessTypeId.toString -> BusinessType.LimitedPartnership.toString,
          BusinessNameId.toString -> partnershipName,
          HasVATId.toString -> true,
          EnterVATId.toString -> "Test Vat"
        )
        testRenderedView(sections, retrievalAction)
      }

      "renders contact address" in {
        val rows = Seq(answerRow(
          Message("cya.label.contact.address", defaultBusiness),
          Seq(
            address.addressLine1,
            address.addressLine2,
            address.postcode.value,
            address.country
          ),
          answerIsMessageKey = false,
          Some(Link(routes.PartnershipContactAddressController.onPageLoad(CheckMode).url)),
          visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultBusiness))
        ))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          PartnershipContactAddressId.toString -> address
        )
        testRenderedView(sections, retrievalAction)
      }

      "renders the address years" in {
        val addressYears = AddressYears.OverAYear
        val rows = Seq(
          answerRow(
            Message("addressYears.heading", defaultPartnership),
            Seq(s"common.addressYears.${addressYears.toString}"),
            answerIsMessageKey = true,
            Some(Link(controllers.register.partnership.routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultPartnership))
          ))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          PartnershipAddressYearsId.toString -> addressYears.toString
        )

        testRenderedView(sections, retrievalAction)
      }

      "renders the previous address" in {
        val address = Address(
          "address-line-1",
          "address-line-2",
          None,
          None,
          Some("post-code"),
          "country"
        )
        val rows = Seq(answerRow(
          Message("previousAddress.checkYourAnswersLabel", defaultBusiness),
          Seq(
            address.addressLine1,
            address.addressLine2,
            address.postcode.value,
            address.country
          ),
          answerIsMessageKey = false,
          Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressController.onPageLoad(CheckMode).url)),
          visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultBusiness))
        ))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          PartnershipPreviousAddressId.toString -> address
        )

        testRenderedView(sections, retrievalAction)
      }
    }

    "display Contact Details section " which {

      "render the view correctly for email and phone" in {


        val rows = Seq(
          answerRow(
            label = messages("email.title", defaultPartnership),
            answer = Seq("test@email"),
            changeUrl = Some(Link(controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultPartnership))
          ),
          answerRow(
            label = messages("phone.title", defaultPartnership),
            answer = Seq("1234567890"),
            changeUrl = Some(Link(controllers.register.partnership.routes.PartnershipPhoneController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultPartnership))
          )
        )

        val sections = Seq(AnswerSection(None, rows))

        val retrievalAction = dataRetrievalAction(
          "partnershipContactDetails" -> Json.obj(
            PartnershipPhoneId.toString -> "1234567890",
            PartnershipEmailId.toString -> "test@email"
          )
        )

        testRenderedView(
          sections = sections, dataRetrievalAction = retrievalAction
        )
      }

    }

    "redirect to session expired page" when {
      "no existing data" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }


    "POST" must {
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
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val partnershipName = "Test Partnership Name"
  private val defaultBusiness = Message("theBusiness").resolve
  private val defaultPartnership = Message("thePartnership").resolve
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val businessName = "Test Partnership"

  def controller(dataRetrievalAction: DataRetrievalAction = getPartnership) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      new FakeCountryOptions(environment, frontendAppConfig)
    )

  private val partnershipContactDetails = AnswerSection(
    Some("checkyouranswers.partnership.contact.details.heading"),
    Seq.empty
  )
  private val partnershipDetails = AnswerSection(
    Some("checkyouranswers.partnership.details"),
    Seq.empty
  )
  private val contactDetails = AnswerSection(
    Some("common.checkYourAnswers.contact.details.heading"),
    Seq.empty
  )
  private val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )

  private def call = routes.CheckYourAnswersController.onSubmit()

  private def answerSections(sectionLabel: Option[String] = None, rows: Seq[AnswerRow]): Seq[AnswerSection] = {
    val section = AnswerSection(sectionLabel, rows)
    Seq(section)
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false, changeUrl: Option[Link] = None,
                        visuallyHiddenLabel: Option[Message]= None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def dataRetrievalAction(fields: (String, Json.JsValueWrapper)*): DataRetrievalAction = {
    val data = Json.obj(fields: _*)
    new FakeDataRetrievalAction(Some(data))
  }

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)
    status(result) mustBe OK
    contentAsString(result) mustBe
      check_your_answers(
        frontendAppConfig,
        sections,
         call,
        None,
        NormalMode
      )(fakeRequest, messages).toString()
  }
}
