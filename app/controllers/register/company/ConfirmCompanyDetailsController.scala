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
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.company.CompanyAddressFormProvider
import identifiers.TypedIdentifier
import identifiers.register.BusinessTypeId
import identifiers.register.company.{BusinessDetailsId, CompanyAddressId}
import models._
import models.register.company.BusinessDetails
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsResultException, Writes}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.{Navigator, UserAnswers}
import views.html.register.company.confirmCompanyDetails

import scala.concurrent.Future

class ConfirmCompanyDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                dataCacheConnector: DataCacheConnector,
                                                @RegisterCompany navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                registrationConnector: RegistrationConnector,
                                                formProvider: CompanyAddressFormProvider
                                        ) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getCompanyDetails(mode){ case (_, response) =>
        Future.successful(Ok(confirmCompanyDetails(appConfig, form, response.address, response.organisation.organisationName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getCompanyDetails(mode) { case (companyDetails, response) =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(confirmCompanyDetails(appConfig, formWithErrors, response.address, response.organisation.organisationName))),
          {
            case true =>
              upsert(request.userAnswers, CompanyAddressId)(response.address){ userAnswers =>
                upsert(userAnswers, BusinessDetailsId)(companyDetails.copy(response.organisation.organisationName)){ userAnswers =>
                  dataCacheConnector.upsert(request.externalId, userAnswers.json).map{ _ =>
                    Redirect(navigator.nextPage(CompanyAddressId, mode)(userAnswers))
                  }
                }
              }
            case false =>
              dataCacheConnector.remove(request.externalId, CompanyAddressId).map { _ =>
                Redirect(navigator.nextPage(CompanyAddressId, mode)(request.userAnswers))
              }
          }
        )
      }
  }

  def getCompanyDetails(mode: Mode)(fn: (BusinessDetails, OrganizationRegisterWithIdResponse) => Future[Result])(implicit request: DataRequest[AnyContent]) = {
    (BusinessDetailsId and BusinessTypeId).retrieve.right.map {
      case (businessDetails ~ businessType) =>
        val organisation = Organisation(businessDetails.companyName, businessType)
        registrationConnector.registerWithIdOrganisation(businessDetails.uniqueTaxReferenceNumber, organisation).flatMap { response =>
          fn(businessDetails, response)
        } recoverWith {
          case _: NotFoundException =>
            Future.successful(Redirect(navigator.nextPage(CompanyAddressId, mode)(request.userAnswers)))
        }
    }
  }

  private def upsert[I <: TypedIdentifier.PathDependent](userAnswers: UserAnswers, id: I)(value: id.Data)
                                                        (fn: UserAnswers => Future[Result])
                                                        (implicit writes: Writes[id.Data]) = {

    userAnswers
      .set(id)(value)
      .fold(
        errors => {
          Logger.error("Unable to set user answer", JsResultException(errors))
          Future.successful(InternalServerError)
        },
        userAnswers => fn(userAnswers)
      )
  }

}
