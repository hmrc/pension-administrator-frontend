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

package controllers.register

import play.api.data.Form
import play.api.mvc.Result

import scala.concurrent.Future
import play.api.mvc.Results._

trait NameCleansing {
  private val nameCleanseRegex = """[^a-zA-Z0-9 '&\\/]+"""

  private[controllers] def cleanse(data: Map[String, Seq[String]], fieldName: String): Map[String, String] =
    data.map(dataItem =>
      if (dataItem._1 == fieldName) {
        (dataItem._1, dataItem._2.head.replaceAll(nameCleanseRegex, ""))
      } else {
        (dataItem._1, dataItem._2.head)
      }
    )

  private[controllers] def cleanseAndBindOrRedirect[A](data: Option[Map[String, Seq[String]]],
                                                       fieldName: String, form: Form[A]): Either[Future[Result], Form[A]] =
    data match {
      case None => Left(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
      case Some(requestData) => Right(form.bind(cleanse(requestData, fieldName)))
    }
}
