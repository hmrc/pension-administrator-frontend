/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.countryOptions

import base.SpecBase
import com.typesafe.config.ConfigException
import config.FrontendAppConfig
import models.InternationalRegion._
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import utils.InputOption

class CountryOptionsSpec extends SpecBase with MockitoSugar {

  "Country Options" must {

    "build correctly the InputOptions with EU and EEA country list and country code" in {

      val app =
        new GuiceApplicationBuilder()
          .configure(Map(
            "location.canonical.list.EUAndEEA" -> "eu-eea-countries-canonical-list-test.json",
            "metrics.enabled" -> "false"
          )).build()

      running(app) {
        val countryOption: CountryOptions = app.injector.instanceOf[CountryOptionsEUAndEEA]
        countryOption.options mustEqual Seq(InputOption("AT", "Austria"), InputOption("BE", "Belgium"))
      }

    }

    "build correctly the list of country codes with EU and EEA country list and country code" in {

      val app =
        new GuiceApplicationBuilder()
          .configure(Map(
            "location.canonical.list.EUAndEEA" -> "eu-eea-countries-canonical-list-test.json",
            "metrics.enabled" -> "false"
          )).build()


      running(app) {
        val countryOption: CountryOptions = app.injector.instanceOf[CountryOptions]
        countryOption.regions("GB") mustEqual UK
        countryOption.regions("AT") mustEqual EuEea
        countryOption.regions("XX") mustEqual RestOfTheWorld
      }

    }

    "throw the error if the country json does not exist" in {
      val builder = new GuiceApplicationBuilder()
        .configure(Map(
          "location.canonical.list.all" -> "country-canonical-test.json",
          "metrics.enabled" -> "false"
        ))

      an[ConfigException.BadValue] shouldBe thrownBy {
        new CountryOptions(builder.environment, builder.injector.instanceOf[FrontendAppConfig]).options
      }
    }
  }

}
