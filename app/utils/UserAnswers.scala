/*
 * Copyright 2024 HM Revenue & Customs
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
import identifiers.register._
import identifiers.register.company._
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerNameId
import models._
import play.api.libs.json._
import utils.dataCompletion.DataCompletion
import viewmodels.Person

import scala.annotation.tailrec

case class UserAnswers(json: JsValue = Json.obj()) {
  def get[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[A] = {
    get[A](id.path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

  def getOrException[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): A =
    get(id).getOrElse(throw new RuntimeException("Unexpected empty option"))

  def getOrException[A](path: JsPath)(implicit rds: Reads[A]): A =
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A])
      .getOrElse(throw new RuntimeException("Unexpected empty option"))

  def setOrException[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)(implicit writes: Writes[id.Data]): UserAnswers =
    set(id)(value) match {
      case JsSuccess(v, _) => v
      case JsError(errors) => throw new RuntimeException("Unable to set value in user answers:" + errors)
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

  def allDirectors: Seq[PersonName] = {
    getAll[PersonName](DirectorNameId.collectionPath).getOrElse(Nil)
  }

  def allDirectorsAfterDelete(mode: Mode): Seq[Person] = {
    val directors = for ((director, index) <- allDirectors.zipWithIndex) yield {
      if (director.isDeleted) {
        Seq.empty
      } else {
        val isComplete = DataCompletion.isDirectorComplete(UserAnswers(json), index)
        val editUrl = if (isComplete) {
          routes.CheckYourAnswersController.onPageLoad(mode, Index(index)).url
        } else {
          routes.DirectorNameController.onPageLoad(mode, Index(index)).url
        }

        Seq(
          Person(
            index,
            director.fullName,
            routes.ConfirmDeleteDirectorController.onPageLoad(mode, index).url,
            editUrl,
            director.isDeleted,
            isComplete,
            director.isNew
          )
        )
      }
    }
    directors.flatten
  }

  def directorsCount: Int = {
    getAll[PersonName](DirectorNameId.collectionPath)
      .getOrElse(Nil).length
  }

  def allPartners: Seq[PersonName] = {
    getAll[PersonName](PartnerNameId.collectionPath).getOrElse(Nil)
  }

  def countPartnersAfterDelete:Int = getAll[PersonName](PartnerNameId.collectionPath).getOrElse(Nil).count(!_.isDeleted)

  def allPartnersAfterDelete(mode: Mode): Seq[Person] = {
    val partners = for ((partner, index) <- allPartners.zipWithIndex) yield {
      if (partner.isDeleted) {
        Seq.empty
      } else {
        val isComplete = DataCompletion.isPartnerComplete(UserAnswers(json), index)
        val editUrl = if (isComplete) {
          controllers.register.partnership.partners.routes.CheckYourAnswersController.onPageLoad(Index(index), mode).url
        } else {
          controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(mode, Index(index)).url
        }

        Seq(
          Person(
            index,
            partner.fullName,
            controllers.register.partnership.partners.routes.ConfirmDeletePartnerController.onPageLoad(index, mode).url,
            editUrl,
            partner.isDeleted,
            isComplete,
            partner.isNew
          )
        )
      }
    }
    partners.flatten
  }

  def allPartnersAfterDeleteV2(mode: Mode): Seq[Person] = {
    val partners = for ((partner, index) <- allPartners.zipWithIndex) yield {
      if (partner.isDeleted) {
        Seq.empty
      } else {
        val isComplete = DataCompletion.isPartnerComplete(UserAnswers(json), index)
        val editUrl:String = controllers.register.administratorPartnership.partners.routes.CheckYourAnswersController.onPageLoad(Index(index), mode).url
        Seq(
          Person(
            index,
            partner.fullName,
            controllers.register.administratorPartnership.partners.routes.ConfirmDeletePartnerController.onPageLoad(index, mode).url,
            editUrl,
            partner.isDeleted,
            isComplete,
            partner.isNew
          )
        )
      }
    }
    partners.flatten
  }

  def partnersCount: Int = {
    getAll[PersonName](PartnerNameId.collectionPath)
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
        id.cleanup(Some(value), UserAnswers(newValue))
      }
    }
  }

  def set[A: Writes](path: JsPath)(value: A): JsResult[UserAnswers] =
    JsLens.fromPath(path).set(Json.toJson(value), json).map(UserAnswers)

  def setAllFlagsToValue[I <: TypedIdentifier[Boolean]](ids: List[I], value: Boolean)(implicit writes: Writes[Boolean]): JsResult[UserAnswers] = {

    @tailrec
    def setRec[II <: TypedIdentifier[Boolean]](localIds: List[II], result: JsResult[UserAnswers])(implicit writes: Writes[Boolean]): JsResult[UserAnswers] = {
      result match {
        case JsSuccess(_, _) =>
          localIds match {
            case Nil => result
            case id :: tail => setRec(tail, result.flatMap(_.set(id)(value)))
          }
        case failure => failure
      }
    }

    setRec(ids, JsSuccess(this))
  }

  def setExpiryDate(millis: Long): JsResult[UserAnswers] = set[Long](__ \ "expireAt")(millis)

  def setAllExistingAddress(ids: Map[TypedIdentifier[Address], TypedIdentifier[TolerantAddress]]): JsResult[UserAnswers] = {

    @tailrec
    def setRec(addressIds: Map[TypedIdentifier[Address], TypedIdentifier[TolerantAddress]], resultAnswers: JsResult[UserAnswers])(
      implicit writes: Writes[Address]): JsResult[UserAnswers] = {
      resultAnswers match {
        case JsSuccess(_, _) =>
          if (addressIds.nonEmpty) {
            val updatedUserAnswers = resultAnswers.map { userAnswers =>
              userAnswers.get(addressIds.head._1).map { address =>
                userAnswers.set(addressIds.head._2)(address.toTolerantAddress)
              }
            }.asOpt.flatten.getOrElse(resultAnswers)

            setRec(addressIds.tail, updatedUserAnswers)
          } else {
            resultAnswers
          }
        case failure => failure
      }
    }

    setRec(ids, JsSuccess(this))
  }

  def remove[I <: TypedIdentifier.PathDependent](id: I): JsResult[UserAnswers] =
    JsLens.fromPath(id.path).get(json).asOpt
      .fold[JsResult[JsValue]](JsSuccess(json))(_=>JsLens.fromPath(id.path).remove(json))
      .flatMap( json => id.cleanup(None, UserAnswers(json)))

  def removeAllOf[I <: TypedIdentifier.PathDependent](ids: List[I]): JsResult[UserAnswers] = {

    @tailrec
    def removeRec[II <: TypedIdentifier.PathDependent](localIds: List[II], result: JsResult[UserAnswers]): JsResult[UserAnswers] = {
      result match {
        case JsSuccess(_, _) =>
          localIds match {
            case Nil => result
            case id :: tail => removeRec(tail, result.flatMap(_.remove(id)))
          }
        case failure => failure
      }
    }

    removeRec(ids, JsSuccess(this))
  }

  def isUserAnswerUpdated: Boolean = {
    List(
      get[Boolean](DeclarationChangedId),
      get[Boolean](DirectorsOrPartnersChangedId),
      get[Boolean](MoreThanTenDirectorsOrPartnersChangedId),
      get[Boolean](CompanyContactAddressChangedId),
      get[Boolean](CompanyContactDetailsChangedId),
      get[Boolean](CompanyPreviousAddressChangedId),
      get[Boolean](IndividualContactAddressChangedId),
      get[Boolean](IndividualContactDetailsChangedId),
      get[Boolean](IndividualPreviousAddressChangedId),
      get[Boolean](PartnershipContactAddressChangedId),
      get[Boolean](PartnershipContactDetailsChangedId),
      get[Boolean](PartnershipPreviousAddressChangedId)
    ).flatten.contains(true)
  }

  def expireAt: Long = getOrException[Long](__ \ "expireAt")
}
