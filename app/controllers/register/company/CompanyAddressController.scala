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

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import connectors.{DataCacheConnector, RegistrationConnector}
import controllers.Retrievals
import identifiers.register.company.{CompanyAddressId, CompanyDetailsId, CompanyUniqueTaxReferenceId}
import models.{Mode, Organisation, OrganisationTypeEnum}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.NotFoundException
import utils.Navigator
import views.html.register.company.companyAddress

class CompanyAddressController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         registrationConnector: RegistrationConnector
                                        ) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieve(CompanyDetailsId)( companyDetails => {
        retrieve(CompanyUniqueTaxReferenceId)( utr => {
          val organisation = Organisation(companyDetails.companyName, OrganisationTypeEnum.CorporateBody)

          registrationConnector.registerWithIdOrganisation(utr, organisation).flatMap { response =>
            dataCacheConnector.save(request.externalId, CompanyAddressId, response.address).map(_ =>
              Ok(companyAddress(appConfig, response.address)))
          } recover {
            case _: NotFoundException =>
              Redirect(navigator.nextPage(CompanyDetailsId, mode)(request.userAnswers))
          }
        })
      })
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CompanyDetailsId, mode)(request.userAnswers))
  }

}
