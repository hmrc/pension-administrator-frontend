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

package handlers

import views.html.error_template

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Request, RequestHeader}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class ErrorHandler @Inject() (
                               errorTemplate: error_template,
                               val messagesApi: MessagesApi
                             )(implicit override val ec: ExecutionContext)
  extends FrontendErrorHandler with I18nSupport {

  override def standardErrorTemplate(
                                      pageTitle: String,
                                      heading: String,
                                      message: String
                                    )(implicit request: RequestHeader): Future[Html] = {
    def requestImplicit: Request[?] = Request(request, "")
    def messages: Messages = messagesApi.preferred(request)
    Future.successful(errorTemplate(pageTitle, heading, message)(requestImplicit, messages))
  }
}

