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

import config.FrontendAppConfig
import connectors.{DataCacheConnector, RegistrationConnector}
import controllers.Retrievals
import controllers.actions._
import forms.company.CompanyAddressFormProvider
import identifiers.register.company.{CompanyAddressId, CompanyDetailsId, CompanyUniqueTaxReferenceId}
import models.requests.DataRequest
import models.{Mode, Organisation, OrganisationTypeEnum, TolerantAddress}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import views.html.register.company.companyAddress

import scala.concurrent.Future

class CompanyAddressController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         registrationConnector: RegistrationConnector,
                                         formProvider: CompanyAddressFormProvider
                                        ) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getCompanyAddress(mode){ response =>
        Future.successful(Ok(companyAddress(appConfig, form, response)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getCompanyAddress(mode) { response =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) => Future.successful(BadRequest(companyAddress(appConfig, formWithErrors, response))),
          (value) => {
            dataCacheConnector.save(request.externalId, CompanyAddressId, response).map( _ =>
              Redirect(navigator.nextPage(CompanyDetailsId, mode)(request.userAnswers))
            )
          }
        )
      }

  }

  def getCompanyAddress(mode: Mode)(fn: TolerantAddress => Future[Result])(implicit request: DataRequest[AnyContent]) = {
    retrieve(CompanyDetailsId)( companyDetails => {
      retrieve(CompanyUniqueTaxReferenceId)( utr => {
        val organisation = Organisation(companyDetails.companyName, OrganisationTypeEnum.CorporateBody)
          registrationConnector.registerWithIdOrganisation(utr, organisation).flatMap { response =>
            fn(response.address)
          } recoverWith {
            case _: NotFoundException =>
              Future.successful(Redirect(navigator.nextPage(CompanyDetailsId, mode)(request.userAnswers)))
          }
      })
    })
  }

}
