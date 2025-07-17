/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.actions.{AuthAction, DataRetrievalAction, FakeAllowAccessProvider}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UpdateMode, UserType}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.when
import play.api.mvc.{AnyContent, BodyParser, Request, Result}
import play.api.test.Helpers.{contentAsString, status, *}
import services.PsaDetailsService
import utils.FakeNavigator
import utils.testhelpers.ViewPsaDetailsBuilder.*
import viewmodels.{PsaViewDetailsViewModel, SuperSection}
import views.html.psa_details

import scala.concurrent.{ExecutionContext, Future}

class PsaDetailsControllerSpec extends ControllerSpecBase {

  private val externalId = "test-external-id"

  val fakePsaDataService: PsaDetailsService = mock[PsaDetailsService]

  val view: psa_details = app.injector.instanceOf[psa_details]

  private val title = "test-title"

  "Psa details Controller" must {
    "return 200 and  correct view for a GET for PSA company" in {
      when(fakePsaDataService.retrievePsaDataAndGenerateViewModel(any(), any(), any(), any()))
        .thenReturn(Future.successful(PsaViewDetailsViewModel(companyWithChangeLinks, "Test company name",
          isUserAnswerUpdated = false, userAnswersIncompleteMessage = Some("incomplete.alert.message"), title = title)))

      val result = controller(userType = UserType.Organisation, psaId = Some("test Psa id")).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyWithChangeLinks, "Test company name")
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, userType: UserType, psaId : Option[String]) =
    new PsaDetailsController(
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
    view(model, controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode))(fakeRequest, messages).toString
  }
}
