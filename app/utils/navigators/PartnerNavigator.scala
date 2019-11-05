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
import controllers.register.partnership.partners.routes
import identifiers.Identifier
import identifiers.register.partnership.partners._
import identifiers.register.partnership.{AddPartnersId, MoreThanTenPartnersId}
import models.Mode.journeyMode
import models._
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class PartnerNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    val routes: PartialFunction[Identifier, Call] = {
      case MoreThanTenPartnersId => controllers.register.partnership.routes.PartnershipReviewController.onPageLoad()
    }
    routes orElse commonRouteMap(ua, NormalMode)
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    val routesData: PartialFunction[Identifier, Call] = {
      case PartnerDetailsId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case HasPartnerNINOId(index) if hasNino(ua, index) =>
        routes.PartnerEnterNINOController.onPageLoad(mode, index)
      case HasPartnerNINOId(index) => routes.PartnerNoNINOReasonController.onPageLoad(mode, index)
      case PartnerEnterNINOId(index) =>
        checkYourAnswersPage(index, journeyMode(mode))
      case PartnerNoNINOReasonId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case PartnerUniqueTaxReferenceId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case PartnerAddressId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case PartnerAddressYearsId(index) => partnerAddressYearsCheckRoutes(index, ua, mode)
      case PartnerPreviousAddressId(index) => checkYourAnswersPage(index, journeyMode(mode))
      case PartnerContactDetailsId(index) => checkYourAnswersPage(index, journeyMode(mode))
    }
    routesData orElse commonRouteMap(ua, mode)
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    val routes: PartialFunction[Identifier, Call] = {
      case MoreThanTenPartnersId => anyMoreChangesPage
      case PartnerConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, ua)
    }
    routes orElse commonRouteMap(ua, UpdateMode)
  }

  //noinspection ScalaStyle
  private def commonRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AddPartnersId => addPartnerRoutes(ua, mode)
    case PartnerDetailsId(index) => routes.HasPartnerNINOController.onPageLoad(mode, index)
    case HasPartnerNINOId(index) if hasNino(ua, index) => routes.PartnerEnterNINOController.onPageLoad(mode, index)
    case HasPartnerNINOId(index) => routes.PartnerNoNINOReasonController.onPageLoad(mode, index)
    case PartnerEnterNINOId(index) => ninoRoutes(index, ua, mode)
    case PartnerNoNINOReasonId(index) => ninoRoutes(index, ua, mode)
    case PartnerUniqueTaxReferenceId(index) => utrRoutes(index, ua, mode)
    case PartnerAddressPostCodeLookupId(index) => routes.PartnerAddressListController.onPageLoad(mode, index)
    case PartnerAddressListId(index) => routes.PartnerAddressController.onPageLoad(mode, index)
    case PartnerAddressId(index) => routes.PartnerAddressYearsController.onPageLoad(mode, index)
    case PartnerAddressYearsId(index) => partnerAddressYearsRoutes(index, ua, mode)
    case PartnerPreviousAddressPostCodeLookupId(index) => routes.PartnerPreviousAddressListController.onPageLoad(mode, index)
    case PartnerPreviousAddressListId(index) => routes.PartnerPreviousAddressController.onPageLoad(mode, index)
    case PartnerPreviousAddressId(index) => previousAddressRoutes(index, ua, mode)
    case PartnerContactDetailsId(index) => contactDetailsRoutes(index, ua, mode)
    case CheckYourAnswersId => controllers.register.partnership.routes.AddPartnerController.onPageLoad(mode)
    case _ => sessionExpired
  }

  private def checkYourAnswersPage(index: Int, mode: Mode = NormalMode) =  routes.CheckYourAnswersController.onPageLoad(index, mode)
  private def anyMoreChangesPage = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private def sessionExpired: Call = controllers.routes.SessionExpiredController.onPageLoad()

  private def hasNino(answers: UserAnswers, index: Index): Boolean = answers.get(HasPartnerNINOId(index)).getOrElse(false)

  private def confirmPreviousAddressRoutes(index: Int, answers: UserAnswers): Call =
    answers.get(PartnerConfirmPreviousAddressId(index)) match {
      case Some(true) => anyMoreChangesPage
      case Some(false) => routes.PartnerPreviousAddressController.onPageLoad(UpdateMode, index)
      case _ => sessionExpired
    }

  private def previousAddressRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode =>
        routes.PartnerContactDetailsController.onPageLoad(mode, index)
      case UpdateMode =>
        redirectBasedOnIsNew(
          answers,
          index,
          routes.PartnerContactDetailsController.onPageLoad(mode, index),
          anyMoreChangesPage
        )
      case _ => sessionExpired
    }
  }

  private def ninoRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def utrRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def contactDetailsRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => checkYourAnswersPage(index, mode)
      case UpdateMode => redirectBasedOnIsNew(answers, index, checkYourAnswersPage(index, mode), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def partnerAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    (answers.get(PartnerAddressYearsId(index)), mode) match {
      case (Some(AddressYears.UnderAYear), NormalMode) =>
        routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index)
      case (Some(AddressYears.UnderAYear), UpdateMode) =>
        redirectBasedOnIsNew(answers, index,
            routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index),
            routes.PartnerConfirmPreviousAddressController.onPageLoad(index)
          )
      case (Some(AddressYears.OverAYear), NormalMode) =>
        routes.PartnerContactDetailsController.onPageLoad(mode, index)
      case (Some(AddressYears.OverAYear), UpdateMode) =>
        redirectBasedOnIsNew(answers, index,
          routes.PartnerContactDetailsController.onPageLoad(mode, index),
          anyMoreChangesPage
        )
      case _ => sessionExpired
    }
  }

  private def redirectBasedOnIsNew(answers: UserAnswers, index: Int, ifNewRoute: Call, ifNotNew: Call): Call = {
    answers.get(PartnerDetailsId(index)).map { person =>
      if (person.isNew) {
        ifNewRoute
      } else {
        ifNotNew
      }
    }.getOrElse(sessionExpired)
  }

  private def addPartnerRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(AddPartnersId) match {
      case Some(false) if mode == NormalMode => controllers.register.partnership.routes.PartnershipReviewController.onPageLoad()
      case Some(false) if mode == UpdateMode => anyMoreChangesPage
      case _ =>
        val index = answers.allPartnersAfterDelete(mode).length
        if (index >= config.maxPartners) {
          controllers.register.partnership.routes.MoreThanTenPartnersController.onPageLoad(mode)
        } else {
          controllers.register.partnership.partners.routes.PartnerDetailsController.onPageLoad(mode, answers.partnersCount)
        }
    }
  }

  private def partnerAddressYearsCheckRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    answers.get(PartnerAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index)
      case Some(AddressYears.OverAYear) =>
        checkYourAnswersPage(index, journeyMode(mode))
      case None => sessionExpired
    }
  }
}
