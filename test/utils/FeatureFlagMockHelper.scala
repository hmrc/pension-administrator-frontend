/*
 * Copyright 2024 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.eq as mockEq
import org.mockito.Mockito.{mock, when}
import org.mockito.stubbing.OngoingStubbing
import play.api.inject.{Binding, bind}
import uk.gov.hmrc.mongoFeatureToggles.model.{FeatureFlag, FeatureFlagName}
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import scala.concurrent.Future

trait FeatureFlagMockHelper {

  lazy val mockFeatureFlagService: FeatureFlagService = mock(classOf[FeatureFlagService])

  lazy val featureFlagServiceBinding: Binding[FeatureFlagService] = bind[FeatureFlagService].toInstance(mockFeatureFlagService)

  def featureFlagMock[A <: FeatureFlagName](toggle: A, isEnabled: Boolean = false): OngoingStubbing[Future[FeatureFlag]] = {
    when(mockFeatureFlagService.get(mockEq(toggle)))
      .thenReturn(Future.successful(FeatureFlag(toggle, isEnabled = isEnabled)))
  }
}
