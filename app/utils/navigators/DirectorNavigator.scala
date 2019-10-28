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

package utils.navigators

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.register.company.directors.routes
import identifiers.register.company.directors._
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import models._
import models.Mode._
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class DirectorNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {

  private def checkYourAnswersPage(index: Int, mode: Mode): Call =
    controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(mode, index)

  private def checkYourAnswers(index: Int, mode: Mode): Option[NavigateTo] =
    NavigateTo.save(checkYourAnswersPage(index, mode))

  private def anyMoreChangesPage = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private def anyMoreChanges: Option[NavigateTo] = NavigateTo.dontSave(anyMoreChangesPage)
  private def sessionExpired: Option[NavigateTo] = NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case MoreThanTenDirectorsId => NavigateTo.save(controllers.register.company.routes.CompanyReviewController.onPageLoad())
    case _ => commonMap(from, NormalMode)
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case MoreThanTenDirectorsId => anyMoreChanges
    case DirectorConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, from.userAnswers)
    case _ => commonMap(from, UpdateMode)
  }

  //noinspection ScalaStyle
  private def commonMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {

    case AddCompanyDirectorsId => addCompanyDirectorRoutes(from.userAnswers, mode)
    case MoreThanTenDirectorsId => NavigateTo.save(controllers.register.company.routes.CompanyReviewController.onPageLoad())
    case DirectorDetailsId(index) => NavigateTo.save(routes.HasDirectorNINOController.onPageLoad(mode, index))
    case DirectorDOBId(index) => NavigateTo.save(routes.HasDirectorNINOController.onPageLoad(mode, index))
    case HasDirectorNINOId(index) if hasNino(from.userAnswers, index) => NavigateTo.save(routes.DirectorEnterNINOController.onPageLoad(mode, index))
    case HasDirectorNINOId(index) => NavigateTo.save(routes.DirectorNoNINOReasonController.onPageLoad(mode, index))
    case DirectorEnterNINOId(index) => ninoRoutes(index, from.userAnswers, mode)
    case DirectorNoNINOReasonId(index) => ninoRoutes(index, from.userAnswers, mode)
    case DirectorUniqueTaxReferenceId(index) => utrRoutes(index, from.userAnswers, mode)
    case CompanyDirectorAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.CompanyDirectorAddressListController.onPageLoad(mode, index))
    case CompanyDirectorAddressListId(index) => NavigateTo.save(routes.DirectorAddressController.onPageLoad(mode, index))
    case DirectorAddressId(index) => NavigateTo.save(routes.DirectorAddressYearsController.onPageLoad(mode, index))
    case DirectorAddressYearsId(index) => directorAddressYearsRoutes(index, from.userAnswers, mode)
    case DirectorPreviousAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.DirectorPreviousAddressListController.onPageLoad(mode, index))
    case DirectorPreviousAddressListId(index) => NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(mode, index))
    case DirectorPreviousAddressId(index) => previousAddressRoutes(index, from.userAnswers, mode)
    case DirectorEmailId(index) => emailRoutes(index, from.userAnswers, mode)
    case DirectorPhoneId(index) => phoneRoutes(index, from.userAnswers, mode)
    case CheckYourAnswersId => NavigateTo.save(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(mode))
    case _ => sessionExpired
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case DirectorDetailsId(index) => checkYourAnswers(index, journeyMode(mode))
    case HasDirectorNINOId(index) if hasNino(from.userAnswers, index) =>
      NavigateTo.save(routes.DirectorEnterNINOController.onPageLoad(mode, index))
    case HasDirectorNINOId(index) => NavigateTo.save(routes.DirectorNoNINOReasonController.onPageLoad(mode, index))
    case DirectorEnterNINOId(index) =>
      checkYourAnswers(index, journeyMode(mode))
    case DirectorNoNINOReasonId(index) => checkYourAnswers(index, journeyMode(mode))
    case DirectorUniqueTaxReferenceId(index) => checkYourAnswers(index, journeyMode(mode))
    case DirectorAddressId(index) => checkYourAnswers(index, journeyMode(mode))
    case DirectorAddressYearsId(index) => directorAddressYearsCheckRoutes(index, from.userAnswers, journeyMode(mode))
    case DirectorPreviousAddressId(index) => checkYourAnswers(index, journeyMode(mode))
    case DirectorEmailId(index) =>
      checkYourAnswers(index, journeyMode(mode))
    case DirectorPhoneId(index) => checkYourAnswers(index, journeyMode(mode))
    case _ => commonMap(from, mode)
  }

  private def hasNino(answers: UserAnswers, index: Index): Boolean = answers.get(HasDirectorNINOId(index)).getOrElse(false)

  private def ninoRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode => NavigateTo.save(routes.DirectorUniqueTaxReferenceController.onPageLoad(mode, index))
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.DirectorUniqueTaxReferenceController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def utrRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode => NavigateTo.save(routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index))
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def emailRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode => NavigateTo.save(routes.DirectorPhoneController.onPageLoad(mode, index))
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.DirectorPhoneController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] =
    answers.get(DirectorConfirmPreviousAddressId(index)) match {
      case Some(true) => anyMoreChanges
      case Some(false) => NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(UpdateMode, index))
      case _ => sessionExpired
    }

  private def directorAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    (answers.get(DirectorAddressYearsId(index)), mode) match {
      case (Some(AddressYears.UnderAYear), NormalMode) => NavigateTo.save(routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index))
      case (Some(AddressYears.OverAYear), NormalMode) => NavigateTo.save(routes.DirectorEmailController.onPageLoad(mode, index))
      case (Some(AddressYears.UnderAYear), UpdateMode) => redirectBasedOnIsNew(
        answers,
        index,
        routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index),
        routes.DirectorConfirmPreviousAddressController.onPageLoad(index))
      case (Some(AddressYears.OverAYear), UpdateMode) => redirectBasedOnIsNew(
        answers,
        index,
        routes.DirectorEmailController.onPageLoad(mode, index),
        anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def phoneRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode => checkYourAnswers(index, mode)
      case UpdateMode => redirectBasedOnIsNew(answers, index, checkYourAnswersPage(index, mode), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def redirectBasedOnIsNew(answers: UserAnswers, index: Int, ifNewRoute: Call, ifNotNew: Call): Option[NavigateTo] = {
    answers.get(DirectorDetailsId(index)).map { person =>
      if (person.isNew) { NavigateTo.save(ifNewRoute) } else { NavigateTo.save(ifNotNew) }
    }.getOrElse(sessionExpired)
  }

  private def previousAddressRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode =>
        NavigateTo.save(routes.DirectorEmailController.onPageLoad(mode, index))
      case UpdateMode =>
        redirectBasedOnIsNew(
          answers,
          index,
          routes.DirectorEmailController.onPageLoad(mode, index),
          anyMoreChangesPage
        )
      case _ => sessionExpired
    }
  }

  private def addCompanyDirectorRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(AddCompanyDirectorsId) match {
      case Some(false) if mode == NormalMode => NavigateTo.save(controllers.register.company.routes.CompanyReviewController.onPageLoad())
      case Some(false) if mode == UpdateMode => anyMoreChanges
      case _ =>
        val index = answers.allDirectorsAfterDelete(mode).length
        if (index >= appConfig.maxDirectors) {
          NavigateTo.save(controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(mode))
        } else {
          NavigateTo.save(controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(mode, answers.directorsCount))
        }
    }
  }

  private def directorAddressYearsCheckRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(DirectorAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode), index))
      case Some(AddressYears.OverAYear) => checkYourAnswers(index, mode)
      case None => sessionExpired
    }
  }
}