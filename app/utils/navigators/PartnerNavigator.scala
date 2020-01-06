/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.register.partnership.partners.routes._
import controllers.register.partnership.routes.{AddPartnerController, MoreThanTenPartnersController, PartnershipReviewController}
import controllers.routes.SessionExpiredController
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
    normalAndUpdateRoutes(ua, NormalMode) orElse {
      case MoreThanTenPartnersId => PartnershipReviewController.onPageLoad()
    }
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case PartnerNameId(index) => checkYourAnswersPage(index, journeyMode(mode))
    case PartnerDOBId(index) => checkYourAnswersPage(index, journeyMode(mode))
    case HasPartnerNINOId(index) if hasNino(ua, index) =>
      PartnerEnterNINOController.onPageLoad(mode, index)
    case HasPartnerNINOId(index) => PartnerNoNINOReasonController.onPageLoad(mode, index)
    case PartnerEnterNINOId(index) =>
      checkYourAnswersPage(index, journeyMode(mode))
    case PartnerNoNINOReasonId(index) => checkYourAnswersPage(index, journeyMode(mode))

    case HasPartnerUTRId(index) if hasUtr(ua, index) => PartnerEnterUTRController.onPageLoad(mode, index)
    case HasPartnerUTRId(index) => PartnerNoUTRReasonController.onPageLoad(mode, index)
    case PartnerEnterUTRId(index) => checkYourAnswersPage(index, journeyMode(mode))
    case PartnerNoUTRReasonId(index) => checkYourAnswersPage(index, journeyMode(mode))

    case PartnerAddressPostCodeLookupId(index) => PartnerAddressListController.onPageLoad(mode, index)
    case PartnerAddressId(index) => checkYourAnswersPage(index, journeyMode(mode))
    case PartnerAddressYearsId(index) => partnerAddressYearsCheckRoutes(index, ua, mode)
    case PartnerPreviousAddressPostCodeLookupId(index) => PartnerPreviousAddressListController.onPageLoad(mode, index)
    case PartnerPreviousAddressId(index) => checkYourAnswersPage(index, journeyMode(mode))
    case PartnerEmailId(index) => checkYourAnswersPage(index, journeyMode(mode))
    case PartnerPhoneId(index) => checkYourAnswersPage(index, journeyMode(mode))
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    normalAndUpdateRoutes(ua, UpdateMode) orElse {
      case MoreThanTenPartnersId => anyMoreChangesPage
      case PartnerConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, ua)
    }
  }

  //noinspection ScalaStyle
  private def normalAndUpdateRoutes(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AddPartnersId => addPartnerRoutes(ua, mode)
    case PartnerNameId(index) => PartnerDOBController.onPageLoad(mode, index)
    case PartnerDOBId(index) => HasPartnerNINOController.onPageLoad(mode, index)

    case HasPartnerNINOId(index) if hasNino(ua, index) => PartnerEnterNINOController.onPageLoad(mode, index)
    case HasPartnerNINOId(index) => PartnerNoNINOReasonController.onPageLoad(mode, index)
    case PartnerEnterNINOId(index) => ninoRoutes(index, ua, mode)
    case PartnerNoNINOReasonId(index) => ninoRoutes(index, ua, mode)

    case HasPartnerUTRId(index) if hasUtr(ua, index) => PartnerEnterUTRController.onPageLoad(mode, index)
    case HasPartnerUTRId(index) => PartnerNoUTRReasonController.onPageLoad(mode, index)
    case PartnerEnterUTRId(index) => utrRoutes(index, ua, mode)
    case PartnerNoUTRReasonId(index) => utrRoutes(index, ua, mode)

    case PartnerAddressPostCodeLookupId(index) => PartnerAddressListController.onPageLoad(mode, index)
    case PartnerAddressId(index) if mode == NormalMode => PartnerAddressYearsController.onPageLoad(mode, index)
    case PartnerAddressId(index) => redirectBasedOnIsNew(ua, index, PartnerAddressYearsController.onPageLoad(mode, index), PartnerConfirmPreviousAddressController.onPageLoad(index))
    case PartnerAddressYearsId(index) => partnerAddressYearsRoutes(index, ua, mode)
    case PartnerPreviousAddressPostCodeLookupId(index) => PartnerPreviousAddressListController.onPageLoad(mode, index)
    case PartnerPreviousAddressId(index) => previousAddressRoutes(index, ua, mode)
    case PartnerEmailId(index) => emailRoutes(index, ua, mode)
    case PartnerPhoneId(index) => phoneRoutes(index, ua, mode)
    case CheckYourAnswersId => AddPartnerController.onPageLoad(mode)
  }

  private def checkYourAnswersPage(index: Int, mode: Mode = NormalMode) = CheckYourAnswersController.onPageLoad(index, mode)

  private def anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private def sessionExpired: Call = SessionExpiredController.onPageLoad()

  private def hasNino(answers: UserAnswers, index: Index): Boolean = answers.get(HasPartnerNINOId(index)).getOrElse(false)

  private def hasUtr(answers: UserAnswers, index: Index): Boolean = answers.get(HasPartnerUTRId(index)).getOrElse(false)

  private def confirmPreviousAddressRoutes(index: Int, answers: UserAnswers): Call =
    answers.get(PartnerConfirmPreviousAddressId(index)) match {
      case Some(true) => anyMoreChangesPage
      case Some(false) => PartnerPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode, index)
      case _ => sessionExpired
    }

  private def previousAddressRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode =>
        PartnerEmailController.onPageLoad(mode, index)
      case UpdateMode =>
        redirectBasedOnIsNew(
          answers,
          index,
          PartnerEmailController.onPageLoad(mode, index),
          anyMoreChangesPage
        )
      case _ => sessionExpired
    }
  }

  private def ninoRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => HasPartnerUTRController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        HasPartnerUTRController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def emailRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => PartnerPhoneController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        PartnerPhoneController.onPageLoad(mode, index), anyMoreChangesPage)
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
  
  private def utrRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode => PartnerAddressPostCodeLookupController.onPageLoad(mode, index)
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        PartnerAddressPostCodeLookupController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def partnerAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    (answers.get(PartnerAddressYearsId(index)), mode) match {
      case (Some(AddressYears.UnderAYear), NormalMode) => PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index)
      case (Some(AddressYears.UnderAYear), UpdateMode) =>
        redirectBasedOnIsNew(answers, index,
          PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index),
          PartnerConfirmPreviousAddressController.onPageLoad(index)
        )
      case (Some(AddressYears.OverAYear), NormalMode) => PartnerEmailController.onPageLoad(mode, index)
      case (Some(AddressYears.OverAYear), UpdateMode) =>
        redirectBasedOnIsNew(answers, index,
          PartnerEmailController.onPageLoad(mode, index),
          anyMoreChangesPage
        )
      case _ => sessionExpired
    }
  }

  private def redirectBasedOnIsNew(answers: UserAnswers, index: Int, ifNewRoute: Call, ifNotNew: Call): Call = {
    answers.get(PartnerNameId(index)).map { person =>
      if (person.isNew) {
        ifNewRoute
      } else {
        ifNotNew
      }
    }.getOrElse(sessionExpired)
  }

  private def addPartnerRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(AddPartnersId) match {
      case Some(false) if mode == NormalMode => PartnershipReviewController.onPageLoad()
      case Some(false) if mode == UpdateMode => anyMoreChangesPage
      case _ =>
        val index = answers.allPartnersAfterDelete(mode).length
        if (index >= config.maxPartners) {
          MoreThanTenPartnersController.onPageLoad(mode)
        } else {
          PartnerNameController.onPageLoad(mode, answers.partnersCount)
        }
    }
  }

  private def partnerAddressYearsCheckRoutes(index: Int, answers: UserAnswers, mode: Mode): Call = {
    answers.get(PartnerAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index)
      case Some(AddressYears.OverAYear) =>
        checkYourAnswersPage(index, journeyMode(mode))
      case None => sessionExpired
    }
  }
}
