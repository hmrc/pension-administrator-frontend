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
import connectors.{RegistrationConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.NonUKAddressController
import forms.address.NonUKAddressFormProvider
import identifiers.register.RegistrationInfoId
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsId}
import javax.inject.Inject
import models._
import models.requests.DataRequest
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Individual
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.Future

class IndividualRegisteredAddressController @Inject()(
                                                       override val appConfig: FrontendAppConfig,
                                                       override val messagesApi: MessagesApi,
                                                       override val dataCacheConnector: UserAnswersCacheConnector,
                                                       @Individual override val navigator: Navigator,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: NonUKAddressFormProvider,
                                                       val countryOptions: CountryOptions,
                                                       override val registrationConnector: RegistrationConnector
                                                     ) extends NonUKAddressController with Retrievals {

  protected val form: Form[Address] = formProvider()

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      IndividualDetailsId.retrieve.right.map { individual =>
        get(IndividualAddressId, addressViewModel(individual.fullName))
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
              if (address.country.equals("GB")) {
                redirectUkAddress(request.externalId, address, IndividualAddressId)
              } else {
                redirectNonUkAddress(request.externalId, individual, address,
                  new LocalDate(dob.getYear, dob.getMonthValue, dob.getDayOfMonth))
              }
            }
          )
      }
  }

  protected override def createView(appConfig: FrontendAppConfig, preparedForm: Form[_], viewModel: ManualAddressViewModel)(
    implicit request: Request[_], messages: Messages): () => HtmlFormat.Appendable = () =>
    nonukAddress(appConfig, preparedForm, viewModel)(request, messages)


  private def redirectNonUkAddress(extId: String, individual: TolerantIndividual,
                                   address: Address, dob: LocalDate
                                  )(implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[Result] = for {
    registrationInfo <- registrationConnector.registerWithNoIdIndividual(
      individual.firstName.getOrElse(error("First name missing")),
      individual.lastName.getOrElse(error("Last name missing")),
      address,
      dob)
    cacheMap <- dataCacheConnector.save(extId, IndividualAddressId, address.toTolerantAddress)
    _ <- dataCacheConnector.save(extId, RegistrationInfoId, registrationInfo)
  } yield {
    Redirect(navigator.nextPage(IndividualAddressId, NormalMode, UserAnswers(cacheMap)))
  }

  private def error(msg: String) = throw new MandatoryIndividualDetailsMissing(msg)

  case class MandatoryIndividualDetailsMissing(msg: String) extends Exception


}
