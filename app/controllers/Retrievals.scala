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

package controllers

import identifiers.TypedIdentifier
import identifiers.register.company.directors.DirectorDetailsId
import models.register.company.directors.DirectorDetails
import models.requests.DataRequest
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
    retrieve[DirectorDetails](DirectorDetailsId(index)) { directorDetails =>
      f(directorDetails.fullName)
    }
  }

  private[controllers] def retrieve[A](id: TypedIdentifier[A])
                                      (f: (A) => Future[Result])
                                      (implicit request: DataRequest[AnyContent], r: Reads[A]): Future[Result] = {
    request.userAnswers.get(id).map(f).getOrElse {
      Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }

  }

  case class ~[A, B](a: A, b: B)

  trait Retrieval[A] {
    self =>

    def retrieve: Either[Future[Result], A]

    def and[B](query: Retrieval[B]): Retrieval[A ~ B] =
      new Retrieval[A ~ B] {
        override def retrieve: Either[Future[Result], A ~ B] = {
          for {
            a <- self.retrieve.right
            b <- query.retrieve.right
          } yield new ~(a, b)
        }
      }
  }

  implicit def fromId[A](id: TypedIdentifier[A])(implicit request: DataRequest[AnyContent], reads: Reads[A]): Retrieval[A] =
    new Retrieval[A] {
      override def retrieve: Either[Future[Result], A] =
        request.userAnswers.get(id) match {
          case Some(value) => Right(value)
          case None        => Left(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
        }
    }

  implicit def merge(f: Either[Future[Result], Future[Result]]): Future[Result] =
    f.merge
}
