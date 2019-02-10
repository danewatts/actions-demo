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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import models.ResponseModel.{FailureResponseModel, SuccessfulResponseModel}
import models.requests.OptionalDataRequest
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{ActionFilter, Result}
import views.html.{index, unauthorised}
import play.api.mvc.Results.InternalServerError

import scala.concurrent.Future

class RequestDataFilter @Inject()(
                                   appConfig: FrontendAppConfig,
                                   messages: MessagesApi
                                 ) extends ActionFilter[OptionalDataRequest] {
  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    Future.successful(
      request.data match {
        case _: SuccessfulResponseModel =>
          None
        case _: FailureResponseModel =>
          Some(InternalServerError(unauthorised(appConfig)(request.request, messages.preferred(request))))
      }
    )
  }
}