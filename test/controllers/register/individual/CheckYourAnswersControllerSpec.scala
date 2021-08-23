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

package controllers.register.individual

import java.time.LocalDate

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.individual.CheckYourAnswersController.postUrl
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._

import utils._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isIndividualComplete(any(), any())).thenReturn(true)
  }

  "CheckYourAnswersController" when {
    "on a GET request" must {

      "render the view correctly - with NO change link for contact address - for all the rows of answer section if individual name and address is present" in {
        val retrievalAction = UserAnswers().completeIndividual.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad(NormalMode)(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows()))
        testRenderedView(sections, result)
      }

      "render the view correctly - including a change link for contact address - for all the rows of answer section " +
        "if individual name and address is present and not the same address" in {
        val retrievalAction = UserAnswers().completeIndividualNotSameAddress.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad(NormalMode)(fakeRequest)

        val sections = Seq(
          AnswerSection(None,
            answerRows(
              changeUrlContactAddress = Some(Link(
                controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(CheckMode).url))
            )
          )
        )
        testRenderedView(sections, result)
      }

      "redirect to register as business page when individual name and address is not present for UK" in {
        val result = controller(UserAnswers().dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
      }

      "redirect to Session Expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "on a POST Request" must {
      "redirect to the next page when data is complete" in {
        val result = controller().onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "load the same cya page when data is not complete" in {
        when(mockDataCompletion.isIndividualComplete(any(), any())).thenReturn(false)
        val retrievalAction = UserAnswers().completeIndividual.dataRetrievalAction
        val result = controller(retrievalAction).onSubmit(NormalMode)(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows()))
        testRenderedView(sections, result, isComplete = false)
      }

      "redirect to Session expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val dob = LocalDate.now().minusYears(20)
  private val addressYears = AddressYears.UnderAYear
  private val email = "test@test.com"
  private val phone = "111"
  private val individual = TolerantIndividual(Some("first"), None, Some("last"))
  private val address = Address("Telford1", "Telford2", None, None, Some("TF3 4ER"), "Country of GB")

  private val cyaView: check_your_answers = app.injector.instanceOf[check_your_answers]

  private def fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  private val mockDataCompletion = mock[DataCompletion]

  private def controller(getData: DataRetrievalAction = getIndividual): CheckYourAnswersController = {
    new CheckYourAnswersController(
      appConfig = frontendAppConfig,
      authenticate = FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      getData = getData,
      requireData = new DataRequiredActionImpl(),
      mockDataCompletion,
      navigator = fakeNavigator,
      messagesApi = messagesApi,
      countryOptions = countryOptions,
      controllerComponents = controllerComponents,
      view = cyaView
    )
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link] = None, visuallyHiddenLabel: Option[Message] = None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def testRenderedView(sections: Seq[AnswerSection], result: Future[Result], isComplete: Boolean = true): Unit = {
    status(result) mustBe OK

    contentAsString(result) mustBe cyaView(
      sections,
      postUrl,
      None,
      NormalMode,
      isComplete
    )(fakeRequest, messagesApi.preferred(fakeRequest)).toString()
  }

  // scalastyle:off method.length
  private def answerRows(changeUrlContactAddress: Option[Link] = None) = Seq(
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
      if (changeUrlContactAddress.isDefined) Seq("site.no") else Seq("site.yes"),
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
      ),
      changeUrl = changeUrlContactAddress
    ),
    answerRow(
      Message("individualAddressYears.title", "Joe Bloggs"),
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

}
