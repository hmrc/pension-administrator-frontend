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

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.AnyMoreChangesFormProvider
import identifiers.register.AnyMoreChangesId
import javax.inject.Inject
import models.{Mode, UpdateMode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Variations
import utils.{Navigator, UserAnswers}
import views.html.register.anyMoreChanges

import scala.concurrent.{ExecutionContext, Future}

class AnyMoreChangesController @Inject()(appConfig: FrontendAppConfig,
                                         dataCacheConnector: UserAnswersCacheConnector,
                                         @Variations navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AnyMoreChangesFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: anyMoreChanges
                                        )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode = UpdateMode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(Ok(view(form, psaName())))
  }

  def onSubmit(mode: Mode = UpdateMode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, psaName()))),
        value =>
          dataCacheConnector.save(request.externalId, AnyMoreChangesId, value).map(cacheMap =>
            Redirect(navigator.nextPage(AnyMoreChangesId, UpdateMode, UserAnswers(cacheMap))))
      )
  }
}

