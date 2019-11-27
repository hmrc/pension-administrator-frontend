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

package controllers.register

import akka.stream.Materializer
import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import forms.register.AddEntityFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.{EntityViewModel, Person}
import views.html.register.addEntity

import scala.concurrent.{ExecutionContext, Future}


object AddEntityControllerSpec extends ControllerSpecBase {

  object FakeIdentifier extends TypedIdentifier[Boolean]

  val view: addEntity = inject[addEntity]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: AddEntityFormProvider,
                                  val view: addEntity
                                )(implicit val executionContext: ExecutionContext) extends AddEntityController {

    implicit val messages = controllerComponents.messagesApi.preferred(fakeRequest)

    def onPageLoad(viewmodel: EntityViewModel, answers: UserAnswers): Future[Result] = {
      implicit val request: DataRequest[AnyContent] = req(answers)
      get(FakeIdentifier, formProvider(), viewmodel, NormalMode)
    }

    def onSubmit(viewmodel: EntityViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      implicit val request: DataRequest[AnyContent] = req(answers, fakeRequest)
      post(FakeIdentifier, formProvider(), viewmodel, NormalMode)
    }

    override protected def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()

  }

  def req(answers: UserAnswers = UserAnswers(), fakeRequest: Request[AnyContent] = FakeRequest()): DataRequest[AnyContent] =
    DataRequest(fakeRequest, "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers)

}

class AddEntityControllerSpec extends SpecBase with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  import AddEntityControllerSpec._

  private def deleteLink(index: Int) = controllers.register.company.directors.routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, index).url

  private def editLink(index: Int) = controllers.register.company.directors.routes.DirectorNameController.onPageLoad(NormalMode, index).url

  // scalastyle:off magic.number
  private val johnDoePerson = Person(0, "John Doe", deleteLink(0), editLink(0), isDeleted = false, isComplete = true)
  private val joeBloggsPerson = Person(1, "Joe Bloggs", deleteLink(1), editLink(1), isDeleted = false, isComplete = true)
  private val entities = Seq(johnDoePerson, joeBloggsPerson)
  private val maxPartners = 10

  def viewmodel(entities: Seq[Person] = Seq.empty) = EntityViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    entities = entities,
    maxLimit = 10,
    entityType = "partners"
  )

  "get" must {

    "return a successful result when no partners are added" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[AddEntityFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel(), UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider(), viewmodel(), NormalMode)(req(), messages).toString
      }
    }

    "return a successful result when there are few partners" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[AddEntityFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel(entities), UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider(),
            viewmodel(entities),
            NormalMode
          )(req(), messages).toString
      }
    }

    "return a successful result when there are maximum allowed partners" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[AddEntityFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel(Seq.fill(maxPartners)(johnDoePerson)), UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider(),
            viewmodel(Seq.fill(maxPartners)(johnDoePerson)),
            NormalMode
          )(req(), messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      val cacheConnector = mock[UserAnswersCacheConnector]

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer
          when(
            cacheConnector.save[Boolean, FakeIdentifier.type](any(), eqTo(FakeIdentifier), any())(any(), any(), any())
          ).thenReturn(Future.successful(Json.obj()))

          val request = FakeRequest().withFormUrlEncodedBody(("value", "invalid value"))
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[AddEntityFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "invalid value"))
          val result = controller.onSubmit(viewmodel(entities), UserAnswers(), req(fakeRequest = postRequest))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider().bind(Map("value" -> "invalid value")),
            viewmodel(entities),
            NormalMode
          )(req(), messages).toString
      }
    }
  }

}
