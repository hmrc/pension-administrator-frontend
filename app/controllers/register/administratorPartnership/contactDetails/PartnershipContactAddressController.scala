/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.register.administratorPartnership.contactDetails

import com.google.inject.Inject
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.{Retrievals, Variations}
import forms.UKOnlyAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.{PartnershipContactAddressListId, PartnershipUKContactAddressId}
import models.requests.DataRequest
import models.{AddressUKOnly, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{NoRLSCheck, PartnershipV2}
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddressUKOnly

import scala.concurrent.{ExecutionContext, Future}

class PartnershipContactAddressController @Inject()(
                                                     val cacheConnector: UserAnswersCacheConnector,
                                                     @PartnershipV2 val navigator: Navigator,
                                                     @NoRLSCheck val allowAccess: AllowAccessActionProvider,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: UKOnlyAddressFormProvider,
                                                     val countryOptions: CountryOptions,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: manualAddressUKOnly
                                                   )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Retrievals with Variations {

  private val form: Form[AddressUKOnly] = formProvider()

  private def viewModel(
                         mode: Mode,
                         partnershipName: String
                       )(implicit request: DataRequest[AnyContent]): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall = routes.PartnershipContactAddressController.onSubmit(mode),
      countryOptions = countryOptions.options,
      title = Message("enter.address.heading").withArgs(Message("thePartnership")),
      heading = Message("enter.address.heading").withArgs(partnershipName),
      psaName = psaName(),
      partnershipName = Some(partnershipName),
      returnLink = Some(
        controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url
      )
    )

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        BusinessNameId.retrieve.map { name =>
          val preparedForm =
            request.userAnswers.get(PartnershipUKContactAddressId) match {
              case Some(address) =>
                form.fill(address)
              case None =>
                request.userAnswers.get(PartnershipContactAddressListId) match {
                  case Some(address) =>
                    form.fill(AddressUKOnly.fromTolerant(address))
                  case None =>
                    form
                }
            }
          Future.successful(Ok(view(preparedForm, viewModel(mode, name), mode)))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        BusinessNameId.retrieve.map { name =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, viewModel(mode, name), mode))),
            address =>
              cacheConnector.save(PartnershipUKContactAddressId, address).flatMap { userAnswersJson =>
                saveChangeFlag(mode, PartnershipUKContactAddressId).map { _ =>
                  Redirect(navigator.nextPage(PartnershipUKContactAddressId, mode, UserAnswers(userAnswersJson)))
                }
              }
          )
        }
    }
}