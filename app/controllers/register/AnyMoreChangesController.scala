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
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.AnyMoreChangesFormProvider
import identifiers.vary.AnyMoreChangesId
import javax.inject.Inject
import models.NormalMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Variations
import utils.{Navigator, UserAnswers}
import views.html.register.anyMoreChanges

import scala.concurrent.{ExecutionContext, Future}

class AnyMoreChangesController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: UserAnswersCacheConnector,
                                         @Variations navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AnyMoreChangesFormProvider)(implicit val ec: ExecutionContext)
  extends FrontendController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(Ok(anyMoreChanges(appConfig, form, psaName())))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(anyMoreChanges(appConfig, formWithErrors, psaName()))),
        value =>
          dataCacheConnector.save(request.externalId, AnyMoreChangesId, value).map(cacheMap =>
            Redirect(navigator.nextPage(AnyMoreChangesId, NormalMode, UserAnswers(cacheMap))))
      )
  }
}

