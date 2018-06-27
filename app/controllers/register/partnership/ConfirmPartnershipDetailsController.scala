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

package controllers.register.partnership

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.{DataCacheConnector, RegistrationConnector}
import controllers.actions._
import config.FrontendAppConfig
import controllers.Retrievals
import forms.register.partnership.ConfirmPartnershipDetailsFormProvider
import identifiers.register.BusinessTypeId
import identifiers.register.partnership.{ConfirmPartnershipDetailsId, PartnershipDetailsId, PartnershipRegisteredAddressId}
import models.register.company.BusinessDetails
import models.requests.DataRequest
import models.{Mode, NormalMode, Organisation, OrganizationRegistration}
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.NotFoundException
import utils.annotations.Partnership
import utils.{Navigator, UserAnswers}
import views.html.register.partnership.confirmPartnershipDetails

import scala.concurrent.Future

class ConfirmPartnershipDetailsController @Inject() (
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: DataCacheConnector,
                                                     @Partnership navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     registrationConnector: RegistrationConnector,
                                                     formProvider: ConfirmPartnershipDetailsFormProvider
                                                   ) extends FrontendController with I18nSupport with Retrievals{

  private val form: Form[Boolean] = formProvider()

  def onPageLoad = (authenticate andThen getData andThen requireData).async {
    implicit request =>
          getPartnershipDetails { case (_, registration) =>
            dataCacheConnector.remove(request.externalId, ConfirmPartnershipDetailsId)
            Future.successful(Ok(confirmPartnershipDetails(appConfig, form, registration.response.organisation.organisationName, registration.response.address)))
      }
  }

  def onSubmit = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getPartnershipDetails { case (partnershipDetails, registration) =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(confirmPartnershipDetails(appConfig, formWithErrors, registration.response.organisation.organisationName, registration.response.address))),
          (value) =>
            dataCacheConnector.save(request.externalId, ConfirmPartnershipDetailsId, value).map(cacheMap =>
              Redirect(navigator.nextPage(ConfirmPartnershipDetailsId, NormalMode, UserAnswers(cacheMap))))
        )
      }
  }


      private def getPartnershipDetails(fn: (BusinessDetails, OrganizationRegistration) => Future[Result])
                                   (implicit request: DataRequest[AnyContent]) = {

        (PartnershipDetailsId and BusinessTypeId).retrieve.right.map {
          case businessDetails ~ businessType =>
            val organisation = Organisation(businessDetails.name, businessType)
            registrationConnector.registerWithIdOrganisation(businessDetails.uniqueTaxReferenceNumber, organisation).flatMap {
              registration =>
                fn(businessDetails, registration)
          } recoverWith {
              case _: NotFoundException =>
                Future.successful(Redirect(controllers.routes.UnauthorisedController.onPageLoad()))
            }
      }
    }
}
