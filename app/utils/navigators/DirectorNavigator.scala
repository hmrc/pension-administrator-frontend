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

package utils.navigators

import com.google.inject.{Inject, Singleton}
import controllers.register.company.directors.routes
import identifiers.Identifier
import identifiers.register.company.directors._
import models.{CheckMode, Mode, NormalMode}
import models.register.company.directors.DirectorAddressYears
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class DirectorNavigator @Inject() extends Navigator {

  private def checkYourAnswers(index: Int)(answers: UserAnswers): Call =
    controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(index)

  override protected val routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case DirectorDetailsId(id) => _ => routes.DirectorNinoController.onPageLoad(NormalMode, id)
    case DirectorNinoId(id) => _ => routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, id)
    case DirectorUniqueTaxReferenceId(id) => _ => routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(NormalMode, id)
    case CompanyDirectorAddressPostCodeLookupId(id) => _ => routes.CompanyDirectorAddressListController.onPageLoad(NormalMode, id)
    case CompanyDirectorAddressListId(id) => _ => routes.DirectorAddressController.onPageLoad(NormalMode, id)
    case DirectorAddressId(id) => _ => routes.DirectorAddressYearsController.onPageLoad(NormalMode, id)
    case DirectorAddressYearsId(id) => directorAddressYearsRoutes(id)
    case DirectorPreviousAddressPostCodeLookupId(id) => _ => routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, id)
    case DirectorPreviousAddressListId(id) => _ => routes.DirectorPreviousAddressController.onPageLoad(NormalMode, id)
    case DirectorPreviousAddressId(id) => _ => routes.DirectorContactDetailsController.onPageLoad(NormalMode, id)
    case DirectorContactDetailsId(id) => checkYourAnswers(id)
  }

  override protected val editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case DirectorDetailsId(id) => checkYourAnswers(id)
    case DirectorNinoId(id) => checkYourAnswers(id)
    case DirectorUniqueTaxReferenceId(id) => checkYourAnswers(id)
    case CompanyDirectorAddressPostCodeLookupId(id) => _ => routes.CompanyDirectorAddressListController.onPageLoad(CheckMode, id)
    case CompanyDirectorAddressListId(id) => _ => routes.DirectorAddressController.onPageLoad(CheckMode, id)
    case DirectorAddressId(id) => checkYourAnswers(id)
    case DirectorAddressYearsId(id) => directorAddressYearsCheckRoutes(id)
    case DirectorPreviousAddressPostCodeLookupId(id) => _ => routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, id)
    case DirectorPreviousAddressListId(id) => _ => routes.DirectorPreviousAddressController.onPageLoad(CheckMode, id)
    case DirectorPreviousAddressId(id) => checkYourAnswers(id)
    case DirectorContactDetailsId(id) => checkYourAnswers(id)
  }

  private def directorAddressYearsRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(DirectorAddressYearsId(index)) match {
      case Some(DirectorAddressYears.LessThanTwelve) =>
        routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(NormalMode, index)
      case Some(DirectorAddressYears.MoreThanTwelve) =>
        routes.DirectorContactDetailsController.onPageLoad(NormalMode, index)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def directorAddressYearsCheckRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(DirectorAddressYearsId(index)) match {
      case Some(DirectorAddressYears.LessThanTwelve) =>
        routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(CheckMode, index)
      case Some(DirectorAddressYears.MoreThanTwelve) =>
        routes.CheckYourAnswersController.onPageLoad(index)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}