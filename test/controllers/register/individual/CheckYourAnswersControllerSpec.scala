/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register.individual

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import controllers.register.individual.CheckYourAnswersController.postUrl
import identifiers.register.individual._
import models._
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswersController" when {
    "on a GET request" must {
      "render the view correctly for no answers" in {
        val sections = Seq(AnswerSection(None, Seq.empty[AnswerRow]))
        testRenderedView(sections, getEmptyData)
      }

      "render the view correctly for the Name section" in {
        val sections = answerSections("individualDetailsCorrect.name", Seq(individual.fullName))
        val retrievalAction = dataRetrievalAction(
          IndividualDetailsId.toString -> individual
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for the registered address section" in {
        val address = TolerantAddress(
          Some("address-line-1"),
          Some("address-line-2"),
          None,
          None,
          Some("post-code"),
          Some("country")
        )
        val sections = answerSections("individualDetailsCorrect.address", address.lines)
        val retrievalAction = dataRetrievalAction(
          IndividualAddressId.toString -> address
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for the Is this address same as your contact address" in {
        val sections = answerSections("cya.label.individual.same.contact.address", Seq("site.yes"), true,
          Some(controllers.register.individual.routes.IndividualSameContactAddressController.onPageLoad(CheckMode).url
          ))
        val retrievalAction = dataRetrievalAction(
          IndividualSameContactAddressId.toString -> true
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for the Contact Address section" in {
        val sections = answerSections(
          "cya.label.individual.contact.address",
          Seq(
            s"${address.addressLine1},",
            s"${address.addressLine2},",
            s"${address.postcode.value},",
            address.country
          ))
        val retrievalAction = dataRetrievalAction(
          IndividualContactAddressId.toString -> address
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for the How Long at Address section" in {
        val addressYears = AddressYears.OverAYear
        val sections = Seq(
          AnswerSection(
            None,
            Seq(
              AnswerRow("individualDetailsCorrect.name", Seq(individual.fullName), false, None),
              AnswerRow(
                Message("individualAddressYears.title", "Joe Bloggs").resolve,
                Seq(s"common.addressYears.${addressYears.toString}"),
                answerIsMessageKey = true,
                controllers.register.individual.routes.IndividualAddressYearsController.onPageLoad(CheckMode).url
              )
            )
          )
        )
        val retrievalAction = new FakeDataRetrievalAction(Some(Json.obj(
          IndividualDetailsId.toString -> individual,
          IndividualAddressYearsId.toString -> addressYears.toString
        )))
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for the Previous Address section" in {
        val sections = answerSections(
          "individualPreviousAddress.checkYourAnswersLabel",
          Seq(
            s"${address.addressLine1},",
            s"${address.addressLine2},",
            s"${address.postcode.value},",
            address.country
          ),
          changeUrl = Some(controllers.register.individual.routes.IndividualPreviousAddressController.onPageLoad(CheckMode).url)
        )
        val retrievalAction = dataRetrievalAction(
          IndividualPreviousAddressId.toString -> address
        )

        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly on a GET request where there is data for the Contact Details sections" in {
        val contactDetails = ContactDetails("email@domain", "phone-no")
        val sections = Seq(
          AnswerSection(
            None,
            Seq(
              AnswerRow(
                "contactDetails.email.checkYourAnswersLabel",
                Seq(contactDetails.email),
                answerIsMessageKey = false,
                controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url
              ),
              AnswerRow(
                "contactDetails.phone.checkYourAnswersLabel",
                Seq(contactDetails.phone),
                answerIsMessageKey = false,
                controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url
              )
            )
          )
        )
        val retrievalAction = dataRetrievalAction(
          IndividualContactDetailsId.toString -> contactDetails
        )
        testRenderedView(sections, retrievalAction)
      }

      "redirect to Session Expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "on a POST Request" must {
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

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  private def fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)
  val individual = TolerantIndividual(
    Some("Joe"),
    None,
    Some("Bloggs")
  )
  val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )

  private def controller(getData: DataRetrievalAction = getIndividual): CheckYourAnswersController = {
    new CheckYourAnswersController(
      appConfig = frontendAppConfig,
      authenticate = FakeAuthAction,
      getData = getData,
      requireData = new DataRequiredActionImpl(),
      navigator = fakeNavigator,
      messagesApi = messagesApi,
      checkYourAnswersFactory = checkYourAnswersFactory
    )
  }

  private def answerSections(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false, changeUrl: Option[String] = None): Seq[AnswerSection] = {
    val answerRow = AnswerRow(label, answer, answerIsMessageKey, changeUrl)
    val section = AnswerSection(None, Seq(answerRow))
    Seq(section)
  }

  private def dataRetrievalAction(fields: (String, Json.JsValueWrapper)*): DataRetrievalAction = {
    val data = Json.obj(fields: _*)
    new FakeDataRetrievalAction(Some(data))
  }

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)
    status(result) mustBe OK
    contentAsString(result) mustBe
      check_your_answers(
        frontendAppConfig,
        sections,
        Some(messages("site.secondaryHeader")),
        postUrl
      )(fakeRequest, messages).toString()
  }
}
