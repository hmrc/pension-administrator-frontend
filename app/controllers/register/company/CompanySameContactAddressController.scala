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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.SameContactAddressController
import forms.address.SameContactAddressFormProvider
import identifiers.register.company.{CompanyAddressId, CompanyAddressListId, CompanyContactAddressId, CompanySameContactAddressId}
import javax.inject.{Inject, Singleton}
import models.Mode
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import routes.CompanySameContactAddressController

@Singleton()
class CompanySameContactAddressController @Inject()(
                                           @RegisterCompany val navigator: Navigator,
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val dataCacheConnector: DataCacheConnector,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: SameContactAddressFormProvider
                                         ) extends SameContactAddressController {

  val form: Form[Boolean] = formProvider()

  private[controllers] val postCall = CompanySameContactAddressController.onSubmit _
  private[controllers] val title: Message = "individual.same.contact.address.title"
  private[controllers] val heading: Message = "company.same.contact.address.heading"
  private[controllers] val hint: Message = "company.same.contact.address.hint"
  private[controllers] val secondaryHeader: Message = "site.secondaryHeader"


  private def viewmodel(mode: Mode) =
    Retrieval(
      implicit request =>
        CompanyAddressId.retrieve.right.map {
          address =>
            SameContactAddressViewModel(
              postCall(mode),
              title = Message(title),
              heading = Message(heading),
              hint = Some(Message(hint)),
              secondaryHeader = Some(secondaryHeader),
              address = address
            )
        }
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map { vm =>
        get(CompanySameContactAddressId, vm)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map { vm =>
        post(CompanySameContactAddressId, CompanyAddressListId, CompanyContactAddressId, vm, mode)
      }
  }

}
