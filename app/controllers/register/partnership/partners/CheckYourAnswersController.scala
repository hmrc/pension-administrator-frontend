/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register.partnership.partners._
import javax.inject.Inject
import models.Mode.checkMode
import models.requests.DataRequest
import models.{Index, Mode, PersonDetails, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.PartnershipPartner
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{Navigator, SectionComplete}
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @PartnershipPartner navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            sectionComplete: SectionComplete,
                                            implicit val countryOptions: CountryOptions,
                                            userAnswersCacheConnector: UserAnswersCacheConnector
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val answersSection = Seq(
        AnswerSection(
          Some("partnerCheckYourAnswers.partnerDetails.heading"),
          PartnerDetailsId(index).row(Some(Link(routes.PartnerDetailsController.onPageLoad(checkMode(mode), index).url))) ++
            PartnerNinoId(index).row(Some(Link(routes.PartnerNinoController.onPageLoad(checkMode(mode), index).url))) ++
            PartnerUniqueTaxReferenceId(index).row(Some(Link(routes.PartnerUniqueTaxReferenceController.onPageLoad(checkMode(mode), index).url)))
        ),
        AnswerSection(
          Some("partnerCheckYourAnswers.contactDetails.heading"),
          PartnerAddressId(index).row(Some(Link(routes.PartnerAddressController.onPageLoad(checkMode(mode), index).url))) ++
            PartnerAddressYearsId(index).row(Some(Link(routes.PartnerAddressYearsController.onPageLoad(checkMode(mode), index).url))) ++
            PartnerPreviousAddressId(index).row(None) ++
            PartnerContactDetailsId(index).row(Some(Link(routes.PartnerContactDetailsController.onPageLoad(checkMode(mode), index).url)))
        ))

      Future.successful(Ok(check_your_answers(
        appConfig,
        answersSection,
        routes.CheckYourAnswersController.onSubmit(index, mode),
        psaName(),
        mode))
      )
  }

  def onSubmit(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerDetailsId(index).retrieve.right.map { details =>
        setNewFlagInUpdateMode(index, mode, details).flatMap { _ =>
          sectionComplete.setComplete(IsPartnerCompleteId(index), request.userAnswers) map { _ =>
            Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers))
          }
        }
      }
  }

  private def setNewFlagInUpdateMode(index: Index, mode: Mode, partnerDetails: PersonDetails)
                                    (implicit request: DataRequest[_], hc: HeaderCarrier): Future[JsValue] = {
    if(mode == UpdateMode) {
      userAnswersCacheConnector.save(request.externalId, PartnerDetailsId(index), partnerDetails.copy(isNew = true))
    } else { Future(request.userAnswers.json)}
  }

}
