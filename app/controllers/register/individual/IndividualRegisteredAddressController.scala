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

package controllers.register.individual

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.address.NonUKAddressFormProvider
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsId}
import javax.inject.Inject
import models._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request}
import play.api.mvc.Results._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Individual
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.Future

class IndividualRegisteredAddressController @Inject()(
                                                       val appConfig: FrontendAppConfig,
                                                       val messagesApi: MessagesApi,
                                                       val dataCacheConnector: UserAnswersCacheConnector,
                                                       @Individual val navigator: Navigator,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: NonUKAddressFormProvider,
                                                       val countryOptions: CountryOptions
                                                     ) extends FrontendController with Retrievals with I18nSupport{

  protected val form: Form[Address] = formProvider()

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      IndividualDetailsId.retrieve.right.map { individual =>

        val preparedForm = request.userAnswers.get(IndividualAddressId).fold(form)(v => form.fill(v.toAddress))
        val view = createView(appConfig, preparedForm, addressViewModel(individual.fullName))

        Future.successful(Ok(view()))
      }
  }

  private def addressViewModel(companyName: String) = ManualAddressViewModel(
    routes.IndividualRegisteredAddressController.onSubmit(),
    countryOptions.options,
    Message("individualRegisteredNonUKAddress.title"),
    Message("individualRegisteredNonUKAddress.heading", companyName),
    None,
    Some(Message("individualRegisteredNonUKAddress.hintText"))
  )

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      (IndividualDetailsId and IndividualDateOfBirthId).retrieve.right.map {
        case individual ~ dob =>
          form.bindFromRequest().fold(
            (formWithError: Form[_]) => {
              val view = createView(appConfig, formWithError, addressViewModel(individual.fullName))
              Future.successful(BadRequest(view()))
            },
            address => {
              for {
                cacheMap <- dataCacheConnector.save(request.externalId, IndividualAddressId, address.toTolerantAddress)
              } yield {
                Redirect(navigator.nextPage(IndividualAddressId, NormalMode, UserAnswers(cacheMap)))
              }
            }
          )
      }
  }

  private def createView(appConfig: FrontendAppConfig, preparedForm: Form[_], viewModel: ManualAddressViewModel)(
    implicit request: Request[_], messages: Messages): () => HtmlFormat.Appendable = () =>
    nonukAddress(appConfig, preparedForm, viewModel)(request, messages)
}
