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

package controllers.register.individual

import java.time.LocalDate

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.individual.CheckYourAnswersController.postUrl
import models._
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val dob = LocalDate.now().minusYears(20)
  private val addressYears = AddressYears.OverAYear
  private val email = "test@email"
  private val phone = "123"
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

  val cyaView: check_your_answers = app.injector.instanceOf[check_your_answers]

  private val completeUserAnswers = UserAnswers().individualDetails(individual).
    individualSameContactAddress(areSame = true).nonUkIndividualAddress(address).
    individualDob(dob).individualContactAddress(address).individualAddressYears(addressYears).individualPreviousAddress(address).
    individualEmail(email).individualPhone(phone)

  "CheckYourAnswersController" when {
    "on a GET request" must {
      "render the view correctly for all the rows of answer section" in {
        val retrievalAction = completeUserAnswers.dataRetrievalAction
        val rows = Seq(
          answerRow(
            "individualDetailsCorrect.name",
            Seq(individual.fullName)
          ),
          answerRow(
            label = "individualDateOfBirth.heading",
            answer = Seq(DateHelper.formatDate(dob)),
            changeUrl = Some(Link(controllers.register.individual.routes.IndividualDateOfBirthController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some("individualDateOfBirth.visuallyHidden.text")
          ),
          answerRow(
            "individualDetailsCorrect.address",
            address.lines(countryOptions)
          ),
          answerRow(
            "cya.label.individual.same.contact.address",
            Seq("site.yes"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(controllers.register.individual.routes.IndividualSameContactAddressController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some("individualContactAddress.visuallyHidden.text")
          ),
          answerRow(
            "cya.label.individual.contact.address",
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            )
          ),
          answerRow(
            Message("individualAddressYears.title", "Joe Bloggs").resolve,
            Seq(s"common.addressYears.${addressYears.toString}"),
            answerIsMessageKey = true,
            Some(Link(controllers.register.individual.routes.IndividualAddressYearsController.onPageLoad(CheckMode).url)),
            Some("individualAddressYears.visuallyHidden.text")
          ),
          answerRow(
            "individualPreviousAddress.checkYourAnswersLabel",
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            ),
            changeUrl = Some(Link(controllers.register.individual.routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some("individualPreviousAddress.visuallyHidden.text")
          ),
          answerRow(
            label = messages("individual.email.title"),
            answer = Seq(email),
            changeUrl = Some(Link(controllers.register.individual.routes.IndividualEmailController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some("individualEmail.visuallyHidden.text")
          ),
          answerRow(
            label = messages("individual.phone.title"),
            answer = Seq(phone),
            changeUrl = Some(Link(controllers.register.individual.routes.IndividualPhoneController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some("individualPhone.visuallyHidden.text")
          )
        )

        val sections = Seq(AnswerSection(None, rows))
        testRenderedView(sections, retrievalAction)
      }

      "redirect to Session Expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

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

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private def fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  private def controller(getData: DataRetrievalAction = getIndividual): CheckYourAnswersController = {
    new CheckYourAnswersController(
      appConfig = frontendAppConfig,
      authenticate = FakeAuthAction,
      FakeAllowAccessProvider(),
      getData = getData,
      requireData = new DataRequiredActionImpl(),
      navigator = fakeNavigator,
      messagesApi = messagesApi,
      countryOptions = countryOptions,
      controllerComponents = stubMessagesControllerComponents(),
      view = cyaView
    )
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link] = None, visuallyHiddenLabel: Option[Message] = None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)
    status(result) mustBe OK
    contentAsString(result) mustBe
      cyaView(
        sections,
        postUrl,
        None,
        NormalMode,
        isComplete = true
      )(fakeRequest, messagesApi.preferred(fakeRequest)).toString()
  }
}
