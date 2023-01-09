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

package controllers.register.individual

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import forms.register.individual.IndividualNameFormProvider
import identifiers.register.individual.IndividualDetailsId
import javax.inject.Inject
import models.requests.DataRequest
import models.{Mode, TolerantIndividual}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Individual
import utils.{Navigator, UserAnswers}
import viewmodels.{Message, PersonDetailsViewModel}
import views.html.register.individual.individualName

import scala.concurrent.{ExecutionContext, Future}

class IndividualNameController @Inject()(val appConfig: FrontendAppConfig,
                                         val dataCacheConnector: UserAnswersCacheConnector,
                                         @Individual val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: IndividualNameFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: individualName
                                        )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {


  val form: Form[TolerantIndividual] = formProvider()

  private[individual] def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    PersonDetailsViewModel(
      title = "individualName.title",
      heading = Message("individualName.title"),
      postCall = routes.IndividualNameController.onSubmit(mode)
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndividualDetailsId) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, viewModel(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, viewModel(mode)))),
        value =>
          dataCacheConnector.save(request.externalId, IndividualDetailsId, value).map(cacheMap =>
            Redirect(navigator.nextPage(IndividualDetailsId, mode, UserAnswers(cacheMap))))
      )
  }

}
