/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.company.AddCompanyDirectorsFormProvider
import identifiers.register.company.AddCompanyDirectorsId
import models.{Mode, NormalMode, UpdateMode}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.CompanyDirector
import utils.{Navigator, UserAnswers}
import viewmodels.Person
import views.html.register.company.{addCompanyDirectors, addCompanyDirectorsv2}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddCompanyDirectorsController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               @CompanyDirector navigator: Navigator,
                                               authenticate: AuthAction,
                                               allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddCompanyDirectorsFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: addCompanyDirectors,
                                               val viewv2: addCompanyDirectorsv2
                                             )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  private val logger = Logger(classOf[AddCompanyDirectorsController])

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
      implicit request =>
        val directors: Seq[Person] = request.userAnswers.allDirectorsAfterDelete(mode)
        val directorsComplete = directors.filter(_.isComplete)
        val directorsIncomplete = directors.filterNot(_.isComplete)
        Ok(viewv2(form, mode, directorsComplete,directorsIncomplete, psaName()))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData) {
      implicit request =>
        val directors: Seq[Person] = request.userAnswers.allDirectorsAfterDelete(mode)

        if (directors.isEmpty || directors.lengthCompare(appConfig.maxDirectors) >= 0) {
          Redirect(navigator.nextPage(AddCompanyDirectorsId, mode, request.userAnswers))
        }
        else {
            form.bindFromRequest().fold(
              (formWithErrors: Form[_]) =>{
                val directorsComplete = directors.filter(_.isComplete)
                val directorsIncomplete = directors.filterNot(_.isComplete)
                BadRequest(viewv2(formWithErrors, mode, directorsComplete, directorsIncomplete, psaName()))
              },
              value =>
                request.userAnswers.set(AddCompanyDirectorsId)(value).fold(
                  errors => {
                    logger.error("Unable to set user answer", JsResultException(errors))
                    (InternalServerError)
                  },
                  userAnswers => {
                    addCompanyDirectorNavigation(mode, userAnswers)
                  }
                )
            )

        }
    }

  private def addCompanyDirectorNavigation(mode: Mode, userAnswers: UserAnswers):Result = {
    userAnswers.get(AddCompanyDirectorsId) match {
      case Some(false) if mode == NormalMode =>
        Redirect(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad())
      case Some(false) if mode == UpdateMode =>
        Redirect(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad())
      case _ =>
        val index = userAnswers.allDirectorsAfterDelete(mode).length
        maxDirectorNavigation(mode, userAnswers, index)
    }
  }

  private def maxDirectorNavigation(mode: Mode, userAnswers: UserAnswers, index: Int) = {
    if (index >= appConfig.maxDirectors) {
      Redirect(controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(mode))
    } else {
      Redirect(controllers.register.company.directors.routes.DirectorNameController.onPageLoad(mode, userAnswers.directorsCount))
    }
  }
}
