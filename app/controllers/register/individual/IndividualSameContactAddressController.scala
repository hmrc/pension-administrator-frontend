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

import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.SameContactAddressController
import controllers.register.individual.routes._
import forms.address.SameContactAddressFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{AuthWithIV, Individual}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualSameContactAddressController @Inject()(
                                                        val dataCacheConnector: UserAnswersCacheConnector,
                                                        @Individual val navigator: Navigator,
                                                        @AuthWithIV authenticate: AuthAction,
                                                        allowAccess: AllowAccessActionProvider,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: SameContactAddressFormProvider,
                                                        val countryOptions: CountryOptions,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val view: sameContactAddress
                                                      )(implicit val executionContext: ExecutionContext) extends SameContactAddressController {

  private[controllers] val postCall = IndividualSameContactAddressController.onSubmit _
  private[controllers] val title: Message = "individual.same.contact.address.title"
  private[controllers] val heading: Message = "individual.same.contact.address.heading"

  protected def form()(implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider(Message("same.contact.address.error").withArgs(Message("common.you")))

  private def viewmodel(mode: Mode): Retrieval[SameContactAddressViewModel] =
    Retrieval(
      implicit request =>
        (IndividualDetailsId and IndividualAddressId).retrieve.map {
          case individual ~ address =>
            SameContactAddressViewModel(
              postCall(mode),
              title = Message(title),
              heading = Message(heading),
              hint = None,
              address = address,
              psaName = individual.fullName,
              mode = mode,
              displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
            )
        }
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(AreYouInUKId) match {
        case Some(true) =>
          viewmodel(mode).retrieve.map { vm =>
            get(IndividualSameContactAddressId, vm, form())
          }
        case _ => Future.successful(Redirect(controllers.register.individual.routes.IndividualAreYouInUKController.onPageLoad(mode)))
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.map { vm =>
        post(IndividualSameContactAddressId, IndividualContactAddressListId, IndividualContactAddressId, vm, mode, form())
      }
  }

}
