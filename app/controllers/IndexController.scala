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

import config.FrontendAppConfig
import connectors.BackendConnector
import controllers.actions.AuthenticatedAction
import javax.inject.Inject
import models.ResponseModel.{FailureResponseModel, SuccessfulResponseModel}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{index, unauthorised}
import uk.gov.hmrc.auth.core.retrieve.~

class IndexController @Inject()(
                                 appConfig: FrontendAppConfig,
                                 val messagesApi: MessagesApi,
                                 connector: BackendConnector,
                                 authenticate: AuthenticatedAction
                               ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate.async {
    implicit request =>
      authorised(ConfidenceLevel.L200).retrieve(Retrievals.nino and Retrievals.credentials and Retrievals.confidenceLevel and Retrievals.description) {
        case nino ~ credentials ~ confidenceLevel ~ description =>
          connector.getData("").map {
            case data: SuccessfulResponseModel =>
              Ok(index(appConfig, data))
            case data: FailureResponseModel =>
              InternalServerError(unauthorised(appConfig))
          }
      } recover {
        case _: NoActiveSession =>
          Redirect(routes.SessionExpiredController.onPageLoad())
        case _: InsufficientConfidenceLevel =>
          Redirect(routes.UnauthorisedController.onPageLoad())
        case e: Exception =>
          //go to tech error page
          Logger.error("Nah", e)
          InternalServerError("error page")
      }
  }
}
