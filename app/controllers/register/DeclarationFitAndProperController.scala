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

package controllers.register

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import connectors.{DataCacheConnector, EnrolmentStoreConnector, InvalidBusinessPartnerException, PensionsSchemeConnector}
import forms.register.DeclarationFormProvider
import identifiers.register.{DeclarationFitAndProperId, ExistingPSAId, PsaSubscriptionResponseId}
import models.{ExistingPSA, NormalMode, UserType}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.HttpException
import utils.annotations.Register
import utils.{KnownFactsRetrieval, Navigator, UserAnswers}
import views.html.register.declarationFitAndProper

import scala.concurrent.Future

class DeclarationFitAndProperController @Inject()(appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  @Register navigator: Navigator,
                                                  formProvider: DeclarationFormProvider,
                                                  dataCacheConnector: DataCacheConnector,
                                                  pensionsSchemeConnector: PensionsSchemeConnector,
                                                  knownFactsRetrieval: KnownFactsRetrieval,
                                                  enrolments: EnrolmentStoreConnector
                                                 ) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      request.user.userType match {
        case UserType.Individual =>
          Future.successful(Ok(
            declarationFitAndProper(appConfig, form, individual.routes.WhatYouWillNeedController.onPageLoad())))

        case UserType.Organisation =>
          Future.successful(Ok(
            declarationFitAndProper(appConfig, form, company.routes.WhatYouWillNeedController.onPageLoad())))
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          request.user.userType match {
            case UserType.Individual =>
              Future.successful(BadRequest(
                declarationFitAndProper(appConfig, errors, individual.routes.WhatYouWillNeedController.onPageLoad())))

            case UserType.Organisation =>
              Future.successful(BadRequest(
                declarationFitAndProper(appConfig, errors, company.routes.WhatYouWillNeedController.onPageLoad())))
          },
        success =>
          dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, success).flatMap { cacheMap =>

            val answers = UserAnswers(cacheMap).set(ExistingPSAId)(ExistingPSA(
              request.user.isExistingPSA,
              request.user.existingPSAId
            )).asOpt.getOrElse(UserAnswers(cacheMap))

            pensionsSchemeConnector.registerPsa(answers) flatMap { psaResponse =>
              dataCacheConnector.save(request.externalId, PsaSubscriptionResponseId, psaResponse) flatMap { cacheMap =>
                knownFactsRetrieval.retrieve map { knownFacts =>
                  enrolments.enrol(psaResponse.psaId, knownFacts) map { _ =>
                    Redirect(navigator.nextPage(DeclarationFitAndProperId, NormalMode)(UserAnswers(cacheMap)))
                  }
                } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
              }
            } recoverWith {
              case _: HttpException =>
                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
              case _: InvalidBusinessPartnerException =>
                Future.successful(Redirect(controllers.register.routes.DuplicateRegistrationController.onPageLoad()))
            }

          }
      )
  }
}
