/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import com.google.inject.Inject
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRetrievalAction}
import forms.register.YesNoFormProvider
import identifiers.SecondPartnerId
import models._
import models.requests.OptionalDataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoRLSCheck
import utils.{Navigator, UserAnswers}
import viewmodels.Person
import views.html.secondPartner

import scala.concurrent.{ExecutionContext, Future}

class SecondPartnerController @Inject()(@utils.annotations.Variations navigator: Navigator,
                                        authenticate: AuthAction,
                                        @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        formProvider: YesNoFormProvider,
                                        psaDetailsService: PsaDetailsService,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: secondPartner
                                                 )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val form: Form[Boolean] = formProvider("secondPartner.error")

  def onPageLoad(): Action[AnyContent] = (authenticate andThen allowAccess(UpdateMode) andThen getData).async {
    implicit request =>
      getPartnerName.map(partnerName => Ok(view(form, partnerName, postCall)))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen allowAccess(UpdateMode) andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          getPartnerName.map(partnerName => BadRequest(view(formWithErrors, partnerName, postCall))),
        value =>
          userAnswersCacheConnector.save(request.externalId, SecondPartnerId, value).map { cacheMap =>
            Redirect(navigator.nextPage(SecondPartnerId, UpdateMode, UserAnswers(cacheMap)))

        })
  }

  private def postCall: Call = routes.SecondPartnerController.onSubmit()

  private def getPartnerName(implicit request: OptionalDataRequest[AnyContent]): Future[Option[String]] = {
    request.user.alreadyEnrolledPsaId.map { psaId =>
      psaDetailsService.getUserAnswers(psaId, request.externalId).map { userAnswers =>
        val partnersSeq: Seq[Person] = userAnswers.allPartnersAfterDelete(UpdateMode)
        if(partnersSeq.size == 1) {
          Some(partnersSeq.head.name)
        } else {
          None
        }
      }
    }.getOrElse(Future.successful(None))
  }
}
