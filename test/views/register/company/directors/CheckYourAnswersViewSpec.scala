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

package views.register.company.directors

import models.Index
import utils.{CheckYourAnswersFactory, CountryOptions, InputOption}
import views.behaviours.ViewBehaviours
import views.html.register.company.directors.check_your_answers

class CheckYourAnswersViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "checkYourAnswers"
  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val index = Index(0)
  val companyName = "Test Company Name"
  val directorName = "Test Director Name"
  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)
  def createView = () => check_your_answers(frontendAppConfig, directorName, Seq.empty)(fakeRequest, messages)

  "CheckYourAnswers view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, directorName )
  }
}
