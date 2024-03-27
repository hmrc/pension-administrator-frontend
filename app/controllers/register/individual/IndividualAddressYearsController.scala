/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.individual

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.individual.{IndividualAddressYearsId, IndividualDetailsId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{AddressYears, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{AuthWithIV, Individual, NoRLSCheck}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext

class IndividualAddressYearsController @Inject()(@Individual override val navigator: Navigator,
                                                 override val appConfig: FrontendAppConfig,
                                                 override val cacheConnector: UserAnswersCacheConnector,
                                                 @AuthWithIV authenticate: AuthAction,
                                                 @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: AddressYearsFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: addressYears
                                                )(implicit val executionContext: ExecutionContext) extends AddressYearsController with I18nSupport {

  private def viewmodel(mode: Mode): Retrieval[AddressYearsViewModel] =
    Retrieval(
      implicit request =>
        IndividualDetailsId.retrieve.map {
          details =>
            val questionText = "individualAddressYears.title"
            AddressYearsViewModel(
              postCall = routes.IndividualAddressYearsController.onSubmit(mode),
              title = Message(questionText, details.fullName),
              heading = Message(questionText, details.fullName),
              legend = Message(questionText, details.fullName),
              psaName = psaName()
            )
        }
    )

  private def form()(implicit request: DataRequest[AnyContent]): Form[AddressYears] = formProvider.applyIndividual()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(mode).retrieve.map {
          vm =>
            get(IndividualAddressYearsId, form(), vm, mode)
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.map {
        vm =>
          post(IndividualAddressYearsId, mode, form(), vm)
      }
  }

}
