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

package utils

import controllers.register.company.directors.routes
import identifiers.TypedIdentifier
import identifiers.register.company.directors.{DirectorDetailsId, IsDirectorCompleteId}
import identifiers.register.partnership.partners.{IsPartnerCompleteId, PartnerDetailsId}
import models.{Index, NormalMode, PersonDetails}
import play.api.libs.json._
import viewmodels.Person

import scala.annotation.tailrec
import scala.language.implicitConversions

case class UserAnswers(json: JsValue = Json.obj()) {

  def get[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[A] = {
    get[A](id.path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

  def getAll[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    JsLens
      .fromPath(path)
      .getAll(json)
      .asOpt
      .flatMap(vs =>
        Some(vs.map(v =>
          validate[A](v)
        )))
  }

  def allDirectors: Seq[PersonDetails] = {
    getAll[PersonDetails](DirectorDetailsId.collectionPath).getOrElse(Nil)
  }

  def allDirectorsAfterDelete: Seq[Person] = {
    val directors = allDirectors
    directors.filterNot(_.isDeleted).map { director =>
      val index = directors.indexOf(director)
      val isComplete = get(IsDirectorCompleteId(index)).getOrElse(false)
      val editUrl = if (isComplete) {
        routes.CheckYourAnswersController.onPageLoad(Index(index)).url
      } else {
        routes.DirectorDetailsController.onPageLoad(NormalMode, Index(index)).url
      }

      Person(
        index,
        director.fullName,
        routes.ConfirmDeleteDirectorController.onPageLoad(index).url,
        editUrl,
        director.isDeleted,
        get(IsDirectorCompleteId(index)).getOrElse(false)
      )
    }
  }

  def directorsCount: Int = {
    getAll[PersonDetails](DirectorDetailsId.collectionPath)
      .getOrElse(Nil).length
  }

  def allPartners: Seq[PersonDetails] = {
    getAll[PersonDetails](PartnerDetailsId.collectionPath).getOrElse(Nil)
  }

  def allPartnersAfterDelete: Seq[Person] = {
    val partners = allPartners
    partners.filterNot(_.isDeleted).map { partner =>
      val index = partners.indexOf(partner)
      Person(
        index,
        partner.fullName,
        controllers.register.partnership.partners.routes.ConfirmDeletePartnerController.onPageLoad(index).url,
        controllers.register.partnership.partners.routes.PartnerDetailsController.onPageLoad(NormalMode, Index(index)).url,
        partner.isDeleted,
        get(IsPartnerCompleteId(index)).getOrElse(false)
      )
    }
  }

  def partnersCount: Int = {
    getAll[PersonDetails](PartnerDetailsId.collectionPath)
      .getOrElse(Nil).length
  }

  private def validate[A](jsValue: JsValue)(implicit rds: Reads[A]): A = {
    jsValue.validate[A].fold(
      invalid =
        errors =>
          throw JsResultException(errors),
      valid =
        response => response
    )
  }

  def set[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)(implicit writes: Writes[id.Data]): JsResult[UserAnswers] = {
    val jsValue = Json.toJson(value)
    val oldValue = json
    val jsResultSetValue = JsLens.fromPath(id.path).set(jsValue, json)
    jsResultSetValue.flatMap { newValue =>
      if (oldValue == newValue) {
        JsSuccess(UserAnswers(newValue))
      } else {
        jsResultSetValue.flatMap(json => id.cleanup(Some(value), UserAnswers(json)))
      }
    }
  }

  def remove[I <: TypedIdentifier.PathDependent](id: I): JsResult[UserAnswers] = {

    JsLens.fromPath(id.path)
      .remove(json)
      .flatMap(json => id.cleanup(None, UserAnswers(json)))
  }

  def removeAllOf[I <: TypedIdentifier.PathDependent](ids: List[I]): JsResult[UserAnswers] = {

    @tailrec
    def removeRec[II <: TypedIdentifier.PathDependent](localIds: List[II], result: JsResult[UserAnswers]): JsResult[UserAnswers] = {
      result match {
        case JsSuccess(value, path) =>
          localIds match {
            case Nil => result
            case id :: tail => removeRec(tail, result.flatMap(_.remove(id)))
          }
        case failure => failure
      }
    }

    removeRec(ids, JsSuccess(this))
  }

}
