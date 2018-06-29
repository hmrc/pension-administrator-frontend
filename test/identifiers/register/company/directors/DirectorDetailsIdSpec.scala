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

package identifiers.register.company.directors

import java.time.LocalDate

import models.register.company.directors.DirectorDetails
import models.requests.DataRequest
import models.{PSAUser, UserType}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import utils.UserAnswers


class DirectorDetailsIdSpec extends WordSpec with MustMatchers {

  "DirectorDetailsId" must {
    "return isComplete flag" when {
      "the flag is present" in {

        val director = DirectorDetails("First", None, "Last", LocalDate.now())

        val fakeRequest: Request[AnyContent] = FakeRequest("/", "GET")

        implicit val request: DataRequest[AnyContent] = DataRequest(
          fakeRequest,
          "cacheId",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None),
          UserAnswers(Json.obj(
            "directors" -> Json.arr(
              Json.obj(
                DirectorDetailsId.toString -> director,
                IsDirectorCompleteId.toString -> false
              )
            )
          ))
        )

        DirectorDetailsId.isComplete(0) must be(Some(false))

      }
    }
  }

}
