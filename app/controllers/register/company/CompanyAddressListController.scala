/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.company.CompanyAddressListFormProvider
import identifiers.register.company.{CompanyAddressListId, CompanyDetailsId, CompanyPreviousAddressId, CompanyPreviousAddressPostCodeLookupId}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.company.companyAddressList
import models.Mode
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent, Result}

import scala.concurrent.Future

class CompanyAddressListController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: DataCacheConnector,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: CompanyAddressListFormProvider
                                     ) extends FrontendController with I18nSupport with Enumerable.Implicits {


  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
        retrieveCompanyName { companyName =>

          request.userAnswers.get(CompanyPreviousAddressPostCodeLookupId) match {
            case None =>
              Future.successful(Redirect(controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)))
            case Some(addresses) =>
              Future.successful(Ok(companyAddressList(appConfig, formProvider(addresses), mode, companyName, addresses)))
          }
      }
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName { companyName =>
        request.userAnswers.get(CompanyPreviousAddressPostCodeLookupId) match {
          case None =>
            Future.successful(Redirect(controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)))
          case Some(addresses) =>
            formProvider(addresses).bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                Future.successful(BadRequest(companyAddressList(appConfig, formWithErrors, mode, companyName, addresses))),
              (value) =>
                dataCacheConnector.save(
                  request.externalId,
                  CompanyPreviousAddressId,
                  addresses(value).copy(country = "GB")
                ).map( cacheMap =>
                  Redirect(navigator.nextPage(CompanyAddressListId, mode)(new UserAnswers(cacheMap)))
                )
            )
        }
      }
  }

  private def retrieveCompanyName(block: String => Future[Result])
                                     (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(CompanyDetailsId) match {
      case Some(value) =>
        block(value.companyName)
      case _ =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }

}
