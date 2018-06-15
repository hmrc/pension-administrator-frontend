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

package utils

import connectors.{DataCacheConnector, FakeDataCacheConnector}
import identifiers.Identifier
import models.{Mode, NormalMode}
import play.api.mvc.Call

class FakeNavigator2(desiredRoute: Call, mode: Mode = NormalMode) extends Navigator2 {

  private[this] var userAnswers: Option[UserAnswers] = None

  def lastUserAnswers: Option[UserAnswers] = userAnswers

  override protected def dataCacheConnector: DataCacheConnector = FakeDataCacheConnector

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = NavigateTo.dontSave(desiredRoute)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = None
}

object FakeNavigator2 extends FakeNavigator2(Call("GET", "www.example.com"), NormalMode){

}
