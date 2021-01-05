/*
 * Copyright 2021 HM Revenue & Customs
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

import com.typesafe.config.ConfigException
import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.{Address, InternationalRegion, TolerantAddress}
import models.InternationalRegion.{EuEea, RestOfTheWorld, UK}
import play.api.Environment
import play.api.libs.json.Json
import utils.InputOption

@Singleton
class CountryOptions @Inject()(environment: Environment, config: FrontendAppConfig) {

  def options: Seq[InputOption] = CountryOptions.getCountries(environment, config.locationCanonicalList)
  def regions(countryCode: String): InternationalRegion =
    CountryOptions.getInternationalRegion(environment, config, countryCode)

  def getCountryNameFromCode(code: String): String =
    options
      .find(_.value == code)
      .map(_.label)
      .getOrElse(code)

  def getCountryNameFromCode(address: Address): String = getCountryNameFromCode(address.country)

  def getCountryNameFromCode(address: TolerantAddress): Option[String] = {
    address.country.map(getCountryNameFromCode)
  }

}

object CountryOptions {

  def getCountries(environment: Environment, fileName: String): Seq[InputOption] = {
    environment.resourceAsStream(fileName).flatMap {
      in =>
        val locationJsValue = Json.parse(in)
        Json.fromJson[Seq[Seq[String]]](locationJsValue).asOpt.map {
          _.map { countryList =>
            InputOption(countryList(1).replaceAll("country:", ""), countryList.head)
          }
        }
    }.getOrElse {
      throw new ConfigException.BadValue(fileName, "country json does not exist")
    }
  }

  def getCountryCodes(environment: Environment, fileName: String) : Seq[String] = {
    environment.resourceAsStream(fileName).map { in =>
        val locationJsValue = Json.parse(in)
        Json.fromJson[Seq[Seq[String]]](locationJsValue).asOpt.map {
          _.map { countryList =>
            countryList(1).replaceAll("country:", "")
          }
        }.fold[Seq[String]](List.empty)(identity)
    }.getOrElse {
      throw new ConfigException.BadValue(fileName, "country json does not exist")
    }
  }

  def getInternationalRegion(environment: Environment, config: FrontendAppConfig, countryCode: String): InternationalRegion = {
    val regionEuEea = getCountryCodes(environment, config.locationCanonicalListEUAndEEA)
      countryCode match {
      case "GB" => UK
      case code if regionEuEea.contains(code) => EuEea
      case _ => RestOfTheWorld
    }
  }

}
