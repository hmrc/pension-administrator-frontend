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
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.register.company.directors.routes
import identifiers.register.company.directors._
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import models.{AddressYears, CheckMode, NormalMode}
import utils.{Navigator, UserAnswers}

@Singleton
class DirectorNavigator @Inject()(val dataCacheConnector: DataCacheConnector, appConfig: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(index: Int): Option[NavigateTo] =
    NavigateTo.save(controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(index))

  //noinspection ScalaStyle
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddCompanyDirectorsId => addCompanyDirectorRoutes(from.userAnswers)
    case MoreThanTenDirectorsId => NavigateTo.save(controllers.register.company.routes.CompanyReviewController.onPageLoad())
    case DirectorDetailsId(index) => NavigateTo.save(routes.DirectorNinoController.onPageLoad(NormalMode, index))
    case DirectorNinoId(index) => NavigateTo.save(routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, index))
    case DirectorUniqueTaxReferenceId(index) => NavigateTo.save(routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(NormalMode, index))
    case CompanyDirectorAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.CompanyDirectorAddressListController.onPageLoad(NormalMode, index))
    case CompanyDirectorAddressListId(index) => NavigateTo.save(routes.DirectorAddressController.onPageLoad(NormalMode, index))
    case DirectorAddressId(index) => NavigateTo.save(routes.DirectorAddressYearsController.onPageLoad(NormalMode, index))
    case DirectorAddressYearsId(index) => directorAddressYearsRoutes(index, from.userAnswers)
    case DirectorPreviousAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, index))
    case DirectorPreviousAddressListId(index) => NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(NormalMode, index))
    case DirectorPreviousAddressId(index) => NavigateTo.save(routes.DirectorContactDetailsController.onPageLoad(NormalMode, index))
    case DirectorContactDetailsId(index) => checkYourAnswers(index)
    case CheckYourAnswersId => NavigateTo.save(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode))
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddCompanyDirectorsId => addCompanyDirectorRoutes(from.userAnswers)
    case DirectorDetailsId(index) => checkYourAnswers(index)
    case DirectorNinoId(index) => checkYourAnswers(index)
    case DirectorUniqueTaxReferenceId(index) => checkYourAnswers(index)
    case CompanyDirectorAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.CompanyDirectorAddressListController.onPageLoad(CheckMode, index))
    case CompanyDirectorAddressListId(index) => NavigateTo.save(routes.DirectorAddressController.onPageLoad(CheckMode, index))
    case DirectorAddressId(index) => checkYourAnswers(index)
    case DirectorAddressYearsId(index) => directorAddressYearsCheckRoutes(index, from.userAnswers)
    case DirectorPreviousAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, index))
    case DirectorPreviousAddressListId(index) => NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(CheckMode, index))
    case DirectorPreviousAddressId(index) => checkYourAnswers(index)
    case DirectorContactDetailsId(index) => checkYourAnswers(index)
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def directorAddressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(DirectorAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) => NavigateTo.save(routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(NormalMode, index))
      case Some(AddressYears.OverAYear) => NavigateTo.save(routes.DirectorContactDetailsController.onPageLoad(NormalMode, index))
      case None => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addCompanyDirectorRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AddCompanyDirectorsId) match {
      case Some(false) => NavigateTo.save(controllers.register.company.routes.CompanyReviewController.onPageLoad())
      case _ =>
        val index = answers.allDirectorsAfterDelete.length
        if (index >= appConfig.maxDirectors) {
          NavigateTo.save(controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(NormalMode))
        } else {
          NavigateTo.save(controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(NormalMode, answers.directorsCount))
        }
    }
  }

  private def directorAddressYearsCheckRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(DirectorAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(CheckMode, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}