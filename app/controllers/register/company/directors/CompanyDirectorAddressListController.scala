/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.company.directors

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.company.directors.{CompanyDirectorAddressListId, CompanyDirectorAddressPostCodeLookupId, DirectorAddressId, DirectorNameId}
import models.FeatureToggleName.PsaRegistration
import models.requests.DataRequest
import models.{Index, Mode, TolerantAddress}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class CompanyDirectorAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                     override val cacheConnector: UserAnswersCacheConnector,
                                                     @CompanyDirector override val navigator: Navigator,
                                                     override val allowAccess: AllowAccessActionProvider,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: AddressListFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: addressList,
                                                     featureToggleConnector: FeatureToggleConnector
                                                    )(implicit val executionContext: ExecutionContext) extends AddressListController with Retrievals {

  def form(addresses: Seq[TolerantAddress], name: String)(implicit request: DataRequest[AnyContent]): Form[Int] =
    formProvider(addresses, Message("select.address.error.required").withArgs(name))

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        viewModel(mode, index, returnLink).map(vm =>
          get(vm, mode, form(vm.addresses, entityName(index)))
        )
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        viewModel(mode, index, returnLink).map(vm =>
          post(vm, DirectorAddressId(index), CompanyDirectorAddressListId(index), CompanyDirectorAddressPostCodeLookupId(index), mode,
            form(vm.addresses, entityName(index)))
        )
      }
  }

  private def viewModel(mode: Mode, index: Index, returnLink: Option[String])
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    CompanyDirectorAddressPostCodeLookupId(index).retrieve.map { addresses =>
      AddressListViewModel(
        postCall = routes.CompanyDirectorAddressListController.onSubmit(mode, index),
        manualInputCall = routes.DirectorAddressController.onPageLoad(mode, index),
        addresses = addresses,
        Message("select.address.heading", Message("theDirector")),
        Message("select.address.heading", entityName(index)),
        Message("select.address.hint.text"),
        Message("manual.entry.link"),
        psaName = psaName(),
        returnLink = returnLink
      )
    }.left.map(_ => Future.successful(Redirect(routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index))))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))
}
