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

import com.google.inject.AbstractModule
import controllers.actions.{FullAuthentication, AuthAction, _}
import utils.Navigator
import utils.annotations.PsaDeRegistration
import utils.annotations._
import utils.countryOptions.{CountryOptions, CountryOptionsEUAndEEA}
import utils.navigators._

class PODSModule extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[AuthAction])
      .to(classOf[FullAuthentication])

    bind(classOf[AuthAction])
      .annotatedWith(classOf[AuthWithNoIV])
      .to(classOf[AuthenticationWithNoIV])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Register])
      .to(classOf[RegisterNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[RegisterV2])
      .to(classOf[RegisterNavigatorV2])

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
      .annotatedWith(classOf[RegisterCompanyV2])
      .to(classOf[RegisterCompanyNavigatorV2])

    bind(classOf[Navigator])
      .annotatedWith(classOf[PartnershipV2])
      .to(classOf[RegisterPartnershipNavigatorV2])

    bind(classOf[Navigator])
      .annotatedWith(classOf[RegisterContactV2])
      .to(classOf[RegisterContactNavigatorV2])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Adviser])
      .to(classOf[AdviserNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Partnership])
      .to(classOf[PartnershipNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[PartnershipPartner])
      .to(classOf[PartnerNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[PartnershipPartnerV2])
      .to(classOf[PartnerNavigatorV2])

    bind(classOf[CountryOptions])
      .annotatedWith(classOf[EUAndEEA])
      .to(classOf[CountryOptionsEUAndEEA])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Variations])
      .to(classOf[VariationsNavigator])

    bind(classOf[AllowAccessActionProvider])
      .to(classOf[AllowAccessActionProviderImpl])

    bind(classOf[AllowAccessActionProvider])
      .annotatedWith(classOf[NoRLSCheck])
      .to(classOf[AllowAccessActionProviderNoRLSCheckImpl])

    bind(classOf[AllowAccessActionProvider])
      .annotatedWith(classOf[NoSuspendedCheck])
      .to(classOf[AllowAccessActionProviderNoSuspendedCheckImpl])

    bind(classOf[AllowDeclarationActionProvider])
      .to(classOf[AllowDeclarationActionProviderImpl])


    bind(classOf[Navigator])
      .annotatedWith(classOf[PsaDeRegistration])
      .to(classOf[PsaDeRegistrationNavigator])
  }
}
