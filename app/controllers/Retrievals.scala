/*
 * Copyright 2019 HM Revenue & Customs
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
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.PartnershipDetailsId
import identifiers.register.partnership.partners.PartnerDetailsId
import identifiers.register.{BusinessNameId, RegistrationInfoId}
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.requests.DataRequest
import models.{PersonDetails, PersonName}
import play.api.libs.json.Reads
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future
import scala.language.implicitConversions

trait Retrievals {

  this: FrontendController =>

  private[controllers] def retrieveDirectorName(index: Int)
                                               (f: String => Future[Result])
                                               (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[PersonName](DirectorNameId(index)) { directorDetails =>
      f(directorDetails.fullName)
    }
  }

  private[controllers] def retrievePartnerName(index: Int)
                                              (f: String => Future[Result])
                                              (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[PersonDetails](PartnerDetailsId(index)) { partnerDetails =>
      f(partnerDetails.fullName)
    }
  }

  private[controllers] def retrieve[A](id: TypedIdentifier[A])
                                      (f: A => Future[Result])
                                      (implicit request: DataRequest[AnyContent], r: Reads[A]): Future[Result] = {
    request.userAnswers.get(id).map(f).getOrElse {
      Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
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

    def static[A](a: A): Retrieval[A] =
      Retrieval {
        implicit request =>
          Right(a)
      }
  }

  implicit def fromId[A](id: TypedIdentifier[A])(implicit request: DataRequest[AnyContent], reads: Reads[A]): Retrieval[A] =
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

      case Some(LimitedCompany) => request.userAnswers.get(BusinessNameId)

      case Some(Partnership) => request.userAnswers.get(PartnershipDetailsId).map(_.companyName)

      case _ => None
    }
  }
}
