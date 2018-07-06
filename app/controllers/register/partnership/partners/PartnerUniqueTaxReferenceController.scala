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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.UniqueTaxReferenceFormProvider
import identifiers.register.partnership.partners.PartnerUniqueTaxReferenceId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.PartnershipPartner
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.partnership.partners.partnerUniqueTaxReference

import scala.concurrent.Future

class PartnerUniqueTaxReferenceController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: DataCacheConnector,
                                                     @PartnershipPartner navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: UniqueTaxReferenceFormProvider
                                                   ) extends FrontendController with I18nSupport with Enumerable.Implicits with Retrievals {

  private val form = formProvider.apply(
    requiredKey = "partnerUniqueTaxReference.error.required",
    requiredReasonKey = "partnerUniqueTaxReference.error.reason.required"
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnerName(index) { partnerName =>
        val redirectResult = request.userAnswers.get(PartnerUniqueTaxReferenceId(index)) match {
          case None => Ok(partnerUniqueTaxReference(appConfig, form, mode, index, partnerName))
          case Some(value) => Ok(partnerUniqueTaxReference(appConfig, form.fill(value), mode, index, partnerName))
        }
        Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnerName(index) { partnerName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(partnerUniqueTaxReference(appConfig, formWithErrors, mode, index, partnerName))),
          value =>
            dataCacheConnector.save(request.externalId, PartnerUniqueTaxReferenceId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(PartnerUniqueTaxReferenceId(index), mode, UserAnswers(cacheMap))))
        )
      }
  }
}
