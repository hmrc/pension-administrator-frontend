package controllers.register

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.VatFormProvider
import identifiers.register.VatId
import models.Mode
import utils.{Navigator, UserAnswers}
import views.html.register.vat

import scala.concurrent.Future

class VatController @Inject() (
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: DataCacheConnector,
                                                     navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: VatFormProvider
                                                   ) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode) = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(VatId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(vat(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(vat(appConfig, formWithErrors, mode))),
        (value) =>
          dataCacheConnector.save(request.externalId, VatId, value).map(cacheMap =>
            Redirect(navigator.nextPage(VatId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
