package controllers.$routeFile$

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.$routeFile$.$className$FormProvider
import identifiers.$routeFile$.$className$Id
import models.$routeFile$.$className$
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.$routeFile$.$className;format="decap"$
import models.Mode
import scala.concurrent.Future

class $className$Controller @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: DataCacheConnector,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: $className$FormProvider
                                     ) extends FrontendController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode) = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get($className$Id) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok($className;format="decap"$(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest($className;format="decap"$(appConfig, formWithErrors, mode))),
        (value) =>
          dataCacheConnector.save[$className$](request.externalId, $className$Id, value).map(cacheMap =>
            Redirect(navigator.nextPage($className$Id, mode)(new UserAnswers(cacheMap))))
    )
  }
}
