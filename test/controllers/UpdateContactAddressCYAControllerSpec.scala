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

package controllers

import controllers.actions.{FakeAllowAccessProvider, AuthAction, DataRetrievalAction}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{UserType, PSAUser, UpdateMode}
import org.mockito.ArgumentMatchers._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import play.api.mvc.{Request, Result, AnyContent, BodyParser}
import play.api.test.Helpers.{status, contentAsString, _}
import services.PsaDetailsService

import utils.FakeNavigator
import utils.testhelpers.ViewPsaDetailsBuilder._
import viewmodels.{PsaViewDetailsViewModel, SuperSection}
import views.html.updateContactAddressCYA

import scala.concurrent.{Future, ExecutionContext}

class UpdateContactAddressCYAControllerSpec extends ControllerSpecBase {

  private val externalId = "test-external-id"

  val fakePsaDataService: PsaDetailsService = mock[PsaDetailsService]

  val view: updateContactAddressCYA = app.injector.instanceOf[updateContactAddressCYA]

  private val title = "test-title"

  "Update contact address CYA Controller" must {
    "return 200 and  correct view for a GET for PSA company" in {
      when(fakePsaDataService.retrievePsaDataAndGenerateContactDetailsOnlyViewModel(any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(PsaViewDetailsViewModel(companyWithChangeLinks, "Test company name",
          isUserAnswerUpdated = false, userAnswersIncompleteMessage = Some("incomplete.alert.message"), title = title)))

      val result = controller(userType = UserType.Organisation, psaId = Some("test Psa id")).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyWithChangeLinks, "Test company name")
    }

    "redirect to session expired if psa id not present" in {
      when(fakePsaDataService.retrievePsaDataAndGenerateContactDetailsOnlyViewModel(any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(PsaViewDetailsViewModel(companyWithChangeLinks, "Test company name",
          isUserAnswerUpdated = false, userAnswersIncompleteMessage = Some("incomplete.alert.message"), title = title)))

      val result = controller(userType = UserType.Organisation, psaId = None).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, userType: UserType, psaId : Option[String]) =
    new UpdateContactAddressCYAController(
      frontendAppConfig,
      FakeNavigator,
      new FakeAuthAction(userType, psaId),
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      fakePsaDataService,
      controllerComponents,
      view
    )

  class FakeAuthAction(userType: UserType, psaId : Option[String]) extends AuthAction {
    val parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
    implicit val executionContext: ExecutionContext = inject[ExecutionContext]
    override def invokeBlock[A](request: Request[A],
                                block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, externalId, PSAUser(userType, None, isExistingPSA = false, None, psaId)))
  }

  private def viewAsString(superSections: Seq[SuperSection], name: String,
                           isUserAnswerUpdated: Boolean = false,
                           userAnswersIncompleteMessage: Option[String] = Some("incomplete.alert.message")): String = {
    val model = PsaViewDetailsViewModel(superSections, name, isUserAnswerUpdated, userAnswersIncompleteMessage, title = title)
    view(model, FakeNavigator.desiredRoute)(fakeRequest, messages).toString
  }
}
