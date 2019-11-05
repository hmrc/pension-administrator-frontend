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
import controllers.register.company.directors.routes
import identifiers.Identifier
import identifiers.register.company.directors._
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import models.Mode._
import models._
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class DirectorNavigator @Inject()(appConfig: FrontendAppConfig) extends Navigator {

  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    normalAndUpdateRoutes(ua, NormalMode) orElse {
      case MoreThanTenDirectorsId => controllers.register.company.routes.CompanyReviewController.onPageLoad()
    }
  }
  //noinspection ScalaStyle
  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AddCompanyDirectorsId => addCompanyDirectorRoutes(ua, mode)
    case DirectorNameId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case DirectorDOBId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case HasDirectorNINOId(index) if hasNino(ua, index) =>
        routes.DirectorEnterNINOController.onPageLoad(mode, index)
      case HasDirectorNINOId(index) => routes.DirectorNoNINOReasonController.onPageLoad(mode, index)
      case DirectorEnterNINOId(index) =>
        checkYourAnswersPage(index, journeyMode(mode))
      case DirectorNoNINOReasonId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case HasDirectorUTRId(index) if hasUtr(ua, index) =>
        routes.DirectorEnterUTRController.onPageLoad(mode, index)
      case HasDirectorUTRId(index) => routes.DirectorNoUTRReasonController.onPageLoad(mode, index)
      case DirectorEnterUTRId(index) =>
        checkYourAnswersPage(index, journeyMode(mode))
      case DirectorNoUTRReasonId(index) => checkYourAnswersPage(index, journeyMode(mode))

      case DirectorAddressId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case DirectorAddressYearsId(index) => directorAddressYearsCheckRoutes(index, ua, journeyMode(mode))
      case DirectorPreviousAddressId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case DirectorEmailId(index) =>
        checkYourAnswersPage(index, journeyMode(mode))
      case DirectorPhoneId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case MoreThanTenDirectorsId => controllers.register.company.routes.CompanyReviewController.onPageLoad()
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    normalAndUpdateRoutes(ua, UpdateMode) orElse {
      case MoreThanTenDirectorsId => anyMoreChangesPage
      case DirectorConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, ua)
    }
  }

  //noinspection ScalaStyle
  private def normalAndUpdateRoutes(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AddCompanyDirectorsId => addCompanyDirectorRoutes(ua, mode)
    case DirectorNameId(index) => routes.DirectorDOBController.onPageLoad(mode, index)
    case DirectorDOBId(index) => routes.HasDirectorNINOController.onPageLoad(mode, index)
    case HasDirectorNINOId(index) if hasNino(ua, index) => routes.DirectorEnterNINOController.onPageLoad(mode, index)
    case HasDirectorNINOId(index) => routes.DirectorNoNINOReasonController.onPageLoad(mode, index)
    case DirectorEnterNINOId(index) => ninoRoutes(index, ua, mode)
    case DirectorNoNINOReasonId(index) => ninoRoutes(index, ua, mode)

    case HasDirectorUTRId(index) if hasUtr(ua, index) => routes.DirectorEnterUTRController.onPageLoad(mode, index)
    case HasDirectorUTRId(index) => routes.DirectorNoUTRReasonController.onPageLoad(mode, index)
    case DirectorEnterUTRId(index) => utrRoutes(index, ua, mode)
    case DirectorNoUTRReasonId(index) => utrRoutes(index, ua, mode)

    case CompanyDirectorAddressPostCodeLookupId(index) => routes.CompanyDirectorAddressListController.onPageLoad(mode, index)
    case CompanyDirectorAddressListId(index) => routes.DirectorAddressController.onPageLoad(mode, index)
    case DirectorAddressId(index) => routes.DirectorAddressYearsController.onPageLoad(mode, index)
    case DirectorAddressYearsId(index) => directorAddressYearsRoutes(index, ua, mode)
    case DirectorPreviousAddressPostCodeLookupId(index) => routes.DirectorPreviousAddressListController.onPageLoad(mode, index)
    case DirectorPreviousAddressListId(index) => routes.DirectorPreviousAddressController.onPageLoad(mode, index)
    case DirectorPreviousAddressId(index) => previousAddressRoutes(index, ua, mode)
    case DirectorEmailId(index) => emailRoutes(index, ua, mode)
    case DirectorPhoneId(index) => phoneRoutes(index, ua, mode)
    case CheckYourAnswersId => controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(mode)
  }

  private def checkYourAnswersPage(index: Int, mode: Mode): Call =
    controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(mode, index)

  private def anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private def sessionExpired: Call = controllers.routes.SessionExpiredController.onPageLoad()

  private def hasUtr(answers: UserAnswers, index: Index): Boolean = answers.get(HasDirectorUTRId(index)).getOrElse(false)

  private def hasNino(answers: UserAnswers, index: Index): Boolean = answers.get(HasDirectorNINOId(index)).getOrElse(false)

  private def ninoRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => routes.HasDirectorUTRController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.HasDirectorUTRController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def utrRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def emailRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => routes.DirectorPhoneController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.DirectorPhoneController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, answers: UserAnswers): Call =
    answers.get(DirectorConfirmPreviousAddressId(index)) match {
      case Some(true) => anyMoreChangesPage
      case Some(false) => routes.DirectorPreviousAddressController.onPageLoad(UpdateMode, index)
      case _ => sessionExpired
    }

  private def directorAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    (answers.get(DirectorAddressYearsId(index)), mode) match {
      case (Some(AddressYears.UnderAYear), NormalMode) => routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index)
      case (Some(AddressYears.OverAYear), NormalMode) => routes.DirectorEmailController.onPageLoad(mode, index)
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

  private def phoneRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => checkYourAnswersPage(index, mode)
      case UpdateMode => redirectBasedOnIsNew(answers, index, checkYourAnswersPage(index, mode), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def redirectBasedOnIsNew(answers: UserAnswers, index: Int, ifNewRoute: Call, ifNotNew: Call): Call = {
    answers.get(DirectorNameId(index)).map { person =>
      if (person.isNew) {
        ifNewRoute
      } else {
        ifNotNew
      }
    }.getOrElse(sessionExpired)
  }

  private def previousAddressRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode =>
        routes.DirectorEmailController.onPageLoad(mode, index)
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

  private def addCompanyDirectorRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(AddCompanyDirectorsId) match {
      case Some(false) if mode == NormalMode => controllers.register.company.routes.CompanyReviewController.onPageLoad()
      case Some(false) if mode == UpdateMode => anyMoreChangesPage
      case _ =>
        val index = answers.allDirectorsAfterDelete(mode).length
        if (index >= appConfig.maxDirectors) {
          controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(mode)
        } else {
          routes.DirectorNameController.onPageLoad(mode, answers.directorsCount)
        }
    }
  }

  private def directorAddressYearsCheckRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    answers.get(DirectorAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode), index)
      case Some(AddressYears.OverAYear) => checkYourAnswersPage(index, mode)
      case None => sessionExpired
    }
  }
}