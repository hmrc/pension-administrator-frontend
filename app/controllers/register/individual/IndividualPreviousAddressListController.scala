package controllers.register.individual

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.individual.IndividualPreviousAddressListId
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future


class IndividualPreviousAddressListController @Inject()(
                                                       override val appConfig: FrontendAppConfig,
                                                       override val messagesApi: MessagesApi,
                                                       override val cacheConnector: DataCacheConnector,
                                                       override val navigator: Navigator,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).right.map(get)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).right.map(vm => post(vm,IndividualPreviousAddressListId(index),IndividialPreviousAddressId,mode))
  }

  private def viewmodel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    (CompanyDetailsId(index) and CompanyPreviousAddressPostcodeLookupId(index)).retrieve.right.map{
      case companyDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.IndividualPreviousAddressListController.onSubmit(mode, index),
          manualInputCall = routes.IndividualPreviousAddressController.onPageLoad(mode, index),
          addresses = addresses,
          subHeading = Some(companyDetails.companyName)
        )
    }.left.map(_ => Future.successful(Redirect(routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index))))
  }
}