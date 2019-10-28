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

package controllers.register.company.directors

import java.time.LocalDate

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerWithCommonBehaviour
import controllers.register.company.directors.routes.{DirectorEmailController, DirectorPhoneController}
import identifiers.register.DirectorsOrPartnersChangedId
import identifiers.register.company.directors.IsDirectorCompleteId
import models._
import play.api.test.Helpers._
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers
import models.Mode.checkMode
import models.Mode._
import play.api.mvc.Call

class CheckYourAnswersControllerSpec extends ControllerWithCommonBehaviour {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" when {

    "on a GET" must {

      Seq(NormalMode, UpdateMode).foreach { mode =>
        s"render the view correctly for name and dob in ${jsLiteral.to(mode)}" in {
          val retrievalAction = UserAnswers().directorName(index, directorName).directorDob(index, LocalDate.now).dataRetrievalAction
          val rows = Seq(
            AnswerRow(
              "cya.label.name",
              Seq("Test Name"),
              answerIsMessageKey = false,
              Link(routes.DirectorNameController.onPageLoad(checkMode(mode), index).url),
              None
            ),
            AnswerRow(
              "cya.label.dob",
              Seq(DateHelper.formatDate(LocalDate.now)),
              answerIsMessageKey = false,
              Link(routes.DirectorDOBController.onPageLoad(checkMode(mode), index).url),
              None
            ))

          val sections = Seq(AnswerSection(None, rows))

          testRenderedView(
            sections = sections, dataRetrievalAction = retrievalAction, mode = mode
          )
        }

        s"render the view correctly for email and phone in ${jsLiteral.to(mode)}" in {
          val retrievalAction = UserAnswers().directorEmail(index, email).directorPhone(index, phone).dataRetrievalAction
          val rows = Seq(
            answerRow(
              label = messages("email.title", defaultDirectorName),
              answer = Seq(email),
              changeUrl = Some(Link(DirectorEmailController.onPageLoad(checkMode(mode), index).url)),
              visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultDirectorName))
            ),
            answerRow(
              label = messages("phone.title", defaultDirectorName),
              answer = Seq(phone),
              changeUrl = Some(Link(DirectorPhoneController.onPageLoad(checkMode(mode), index).url)),
              visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultDirectorName))
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
      "mark director as complete on submit" in {
        FakeUserAnswersCacheConnector.reset()
        val result = controller().onSubmit(NormalMode, index)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeSectionComplete.verify(IsDirectorCompleteId(index), value = true)
        FakeUserAnswersCacheConnector.verifyNot(DirectorsOrPartnersChangedId)
      }

      "save the change flag for UpdateMode on submit" in {
        val result = controller().onSubmit(UpdateMode, index)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DirectorsOrPartnersChangedId, value = true)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {
  private val email = "test@test.com"
  private val phone = "1234"
  private val index = Index(0)
  private val directorName = PersonName("Test", "Name")
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)
  private val defaultDirectorName = Message("theDirector").resolve

  private def call(mode: Mode): Call = controllers.register.company.directors.routes.CheckYourAnswersController.onSubmit(mode, index)

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link] = None, visuallyHiddenLabel: Option[Message] = None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAllowAccessProvider(),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeNavigator,
      messagesApi,
      checkYourAnswersFactory,
      FakeSectionComplete,
      FakeUserAnswersCacheConnector
    )

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction, mode: Mode = NormalMode): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(mode, index)(fakeRequest)
    val expectedResult = check_your_answers(
      frontendAppConfig,
      sections,
      call(mode),
      None,
      mode
    )(fakeRequest, messages).toString()

    status(result) mustBe OK
    contentAsString(result) mustBe expectedResult
  }
}
