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
import models.register.company.directors.DirectorDetails
import models.{AddressYears, CheckMode, NormalMode}
import utils.{Navigator2, UserAnswers}

@Singleton
class DirectorNavigator2 @Inject()(val dataCacheConnector: DataCacheConnector, appConfig: FrontendAppConfig) extends Navigator2 {

  private def checkYourAnswers(index: Int): Option[NavigateTo] =
    NavigateTo.save(controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(index))

  //noinspection ScalaStyle
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddCompanyDirectorsId => addCompanyDirectorRoutes(from.userAnswers)
    case MoreThanTenDirectorsId => NavigateTo.save(controllers.register.company.routes.CompanyReviewController.onPageLoad())
    case DirectorDetailsId(id) => NavigateTo.save(routes.DirectorNinoController.onPageLoad(NormalMode, id))
    case DirectorNinoId(id) => NavigateTo.save(routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, id))
    case DirectorUniqueTaxReferenceId(id) => NavigateTo.save(routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(NormalMode, id))
    case CompanyDirectorAddressPostCodeLookupId(id) => NavigateTo.dontSave(routes.CompanyDirectorAddressListController.onPageLoad(NormalMode, id))
    case CompanyDirectorAddressListId(id) => NavigateTo.save(routes.DirectorAddressController.onPageLoad(NormalMode, id))
    case DirectorAddressId(id) => NavigateTo.save(routes.DirectorAddressYearsController.onPageLoad(NormalMode, id))
    case DirectorAddressYearsId(id) => directorAddressYearsRoutes(id, from.userAnswers)
    case DirectorPreviousAddressPostCodeLookupId(id) => NavigateTo.dontSave(routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, id))
    case DirectorPreviousAddressListId(id) => NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(NormalMode, id))
    case DirectorPreviousAddressId(id) => NavigateTo.save(routes.DirectorContactDetailsController.onPageLoad(NormalMode, id))
    case DirectorContactDetailsId(id) => checkYourAnswers(id)
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddCompanyDirectorsId => addCompanyDirectorRoutes(from.userAnswers)
    case DirectorDetailsId(id) => checkYourAnswers(id)
    case DirectorNinoId(id) => checkYourAnswers(id)
    case DirectorUniqueTaxReferenceId(id) => checkYourAnswers(id)
    case CompanyDirectorAddressPostCodeLookupId(id) => NavigateTo.dontSave(routes.CompanyDirectorAddressListController.onPageLoad(CheckMode, id))
    case CompanyDirectorAddressListId(id) => NavigateTo.save(routes.DirectorAddressController.onPageLoad(CheckMode, id))
    case DirectorAddressId(id) => checkYourAnswers(id)
    case DirectorAddressYearsId(id) => directorAddressYearsCheckRoutes(id, from.userAnswers)
    case DirectorPreviousAddressPostCodeLookupId(id) => NavigateTo.dontSave(routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, id))
    case DirectorPreviousAddressListId(id) => NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(CheckMode, id))
    case DirectorPreviousAddressId(id) => checkYourAnswers(id)
    case DirectorContactDetailsId(id) => checkYourAnswers(id)
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
        val index = answers.getAll(DirectorDetailsId.collectionPath)(DirectorDetails.format) match {
          case Some(seq@Seq(_*)) => seq.length
          case None => 0
        }
        if (index >= appConfig.maxDirectors) {
          NavigateTo.save(controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(NormalMode))
        } else {
          NavigateTo.save(controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(NormalMode, index))
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