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

package controllers

import identifiers.TypedIdentifier
import identifiers.register.adviser.AdviserNameId
import identifiers.register.company.CompanyEmailId
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.individual.{IndividualDetailsId, IndividualEmailId}
import identifiers.register.partnership.PartnershipEmailId
import identifiers.register.partnership.partners.PartnerNameId
import identifiers.register.{BusinessNameId, RegistrationInfoId}
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.requests.DataRequest
import models.{Mode, PersonName}
import play.api.libs.json.Reads
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Call, Result}

import scala.concurrent.Future
import scala.language.implicitConversions

trait Retrievals {

  private[controllers] def retrieveDirectorName(mode: Mode, index: Int)
                                               (f: String => Future[Result])
                                               (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[PersonName](DirectorNameId(index),
      controllers.register.company.directors.routes.DirectorNameController.onPageLoad(mode, index)
    ) { directorDetails =>
      f(directorDetails.fullName)
    }
  }

  private[controllers] def retrievePartnerName(mode: Mode, index: Int)
                                              (f: String => Future[Result])
                                              (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[PersonName](PartnerNameId(index),
      controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(mode, index)) { partnerDetails =>
      f(partnerDetails.fullName)
    }
  }

  private[controllers] def retrieveAdviserName(mode: Mode)
                                               (f: String => Future[Result])
                                               (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[String](AdviserNameId,
      controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(mode))(f(_))
  }

  private[controllers] def retrieve[A](id: TypedIdentifier[A],
                                       failedCall: Call = controllers.routes.SessionExpiredController.onPageLoad())
                                      (f: A => Future[Result])
                                      (implicit request: DataRequest[AnyContent], r: Reads[A]): Future[Result] = {
    request.userAnswers.get(id).map(f).getOrElse {
      Future.successful(Redirect(failedCall))
    }

  }

  case class ~[A, B](a: A, b: B)

  trait Retrieval[A] {
    self =>

    def retrieve(implicit request: DataRequest[AnyContent]): Either[Future[Result], A]

    def and[B](query: Retrieval[B]): Retrieval[A ~ B] =
      new Retrieval[A ~ B] {
        override def retrieve(implicit request: DataRequest[AnyContent]): Either[Future[Result], A ~ B] = {
          for {
            a <- self.retrieve.right
            b <- query.retrieve.right
          } yield new ~(a, b)
        }
      }
  }

  object Retrieval {

    def apply[A](f: DataRequest[AnyContent] => Either[Future[Result], A]): Retrieval[A] =
      new Retrieval[A] {
        override def retrieve(implicit request: DataRequest[AnyContent]): Either[Future[Result], A] =
          f(request)
      }
  }

  implicit def fromId[A](id: TypedIdentifier[A])(implicit reads: Reads[A]): Retrieval[A] =
    Retrieval {
      implicit request =>
        request.userAnswers.get(id) match {
          case Some(value) => Right(value)
          case None => Left(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
        }
    }

  implicit def merge(f: Either[Future[Result], Future[Result]]): Future[Result] =
    f.merge

  private[controllers] def psaName()(implicit request: DataRequest[AnyContent]): Option[String] = {

    val legalStatus =  request.userAnswers.get(RegistrationInfoId).map(_.legalStatus)

    legalStatus match {
      case Some(Individual) => request.userAnswers.get(IndividualDetailsId).map(_.fullName)

      case Some(LimitedCompany) | Some(Partnership) => request.userAnswers.get(BusinessNameId)

      case _ => None
    }
  }

  private[controllers] def psaEmail(implicit request: DataRequest[AnyContent]): Option[String] = {
    val answers = request.userAnswers
    answers.get(RegistrationInfoId).flatMap { registrationInfo =>
      registrationInfo.legalStatus match {
        case Individual => answers.get(IndividualEmailId)
        case LimitedCompany => answers.get(CompanyEmailId)
        case Partnership => answers.get(PartnershipEmailId)
      }
    }
  }
}
