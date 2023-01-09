/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company._
import models.FeatureToggleName.PsaRegistration
import models.requests.DataRequest
import models.{Mode, TolerantAddress}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.{NoRLSCheck, RegisterCompany}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class CompanyContactAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                    override val cacheConnector: UserAnswersCacheConnector,
                                                    @RegisterCompany override val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: AddressListFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: addressList,
                                                    featureToggleConnector: FeatureToggleConnector
                                                   )(implicit val executionContext: ExecutionContext) extends AddressListController with Retrievals {

  def form(addresses: Seq[TolerantAddress])(implicit request: DataRequest[AnyContent]): Form[Int] =
    formProvider(addresses, Message("select.address.required.error").withArgs(companyName))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        viewmodel(mode, returnLink).map(vm =>
          get(vm, mode, form(vm.addresses))
        )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        viewmodel(mode, returnLink).map(vm =>
          post(vm, CompanyContactAddressId, CompanyContactAddressListId, CompanyContactAddressPostCodeLookupId, mode, form(vm.addresses))
        )
      }
  }

  def viewmodel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    (BusinessNameId and CompanyContactAddressPostCodeLookupId).retrieve.map {
      case name ~ addresses =>
        AddressListViewModel(
          postCall = routes.CompanyContactAddressListController.onSubmit(mode),
          manualInputCall = routes.CompanyContactAddressController.onPageLoad(mode),
          addresses = addresses,
          Message("select.address.heading", Message("theCompany")),
          Message("select.address.heading", name),
          Message("select.address.hint.text"),
          Message("manual.entry.link"),
          psaName = Some(name),
          returnLink = returnLink
        )
    }.left.map(_ => Future.successful(Redirect(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(mode))))
  }
}
