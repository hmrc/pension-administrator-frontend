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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.CompanyNameFormProvider
import identifiers.register.BusinessTypeId
import identifiers.register.company.CompanyNameId
import models.Mode
import models.register.BusinessType
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.{Navigator, UserAnswers}
import viewmodels.Message
import views.html.register.company.companyName

import scala.concurrent.{ExecutionContext, Future}

class CompanyNameController @Inject()(appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      dataCacheConnector: UserAnswersCacheConnector,
                                      @RegisterCompany navigator: Navigator,
                                      authenticate: AuthAction,
                                      allowAccess: AllowAccessActionProvider,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: CompanyNameFormProvider
                                     )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
  implicit request =>
  val prepareForm = request.userAnswers.get(CompanyNameId).fold(form)(v => form.fill(v))
    BusinessTypeId.retrieve.right.map { businessType =>
      Future.successful(Ok(companyName(appConfig, prepareForm, mode, toString(businessType))))
    }

}

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          BusinessTypeId.retrieve.right.map { businessType =>
            Future.successful(BadRequest(companyName(appConfig, formWithErrors, mode, toString(businessType))))
          },
        value =>
          dataCacheConnector.save(request.externalId, CompanyNameId, value).map(cacheMap =>
            Redirect(navigator.nextPage(CompanyNameId, mode, UserAnswers(cacheMap))))
      )
  }

  def toString(businessType: BusinessType): String = Message(s"businessType.${businessType.toString}").toLowerCase()

}
