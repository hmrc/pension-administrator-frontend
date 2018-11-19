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

import com.google.inject.AbstractModule
import controllers.actions.{AuthAction, AuthAction1, AuthAction2}
import utils.Navigator
import utils.annotations._
import utils.countryOptions.{CountryOptions, CountryOptionsEUAndEEA}
import utils.navigators._

class PODSModule extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[AuthAction])
      .to(classOf[AuthAction1])

    bind(classOf[AuthAction])
      .annotatedWith(classOf[AuthActionPrimary])
      .to(classOf[AuthAction2])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Register])
      .to(classOf[RegisterNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[CompanyDirector])
      .to(classOf[DirectorNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Individual])
      .to(classOf[IndividualNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[RegisterCompany])
      .to(classOf[RegisterCompanyNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Adviser])
      .to(classOf[AdviserNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Partnership])
      .to(classOf[PartnershipNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[PartnershipPartner])
      .to(classOf[PartnerNavigator])

    bind(classOf[CountryOptions])
      .annotatedWith(classOf[EUAndEEA])
      .to(classOf[CountryOptionsEUAndEEA])
  }
}
