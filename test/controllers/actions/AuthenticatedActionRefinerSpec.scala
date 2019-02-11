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

import base.SpecBase
import controllers.routes
import models.requests.AuthenticatedRequest
import play.api.mvc.{Controller, Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{AuthConnector, BearerTokenExpired, ConfidenceLevel, InsufficientConfidenceLevel}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedActionRefinerSpec extends SpecBase {

  class FakeController(action: AuthenticatedActionRefiner) extends Controller {
    def onPageLoad = action {
      Ok
    }
  }

  "AuthenticatedActionRefiner" should {
    "redirect the user" when {
      "there is no active session" in {
        val action = new AuthenticatedActionRefiner(new FakeFailingAuthConnector(BearerTokenExpired("")))
        val controller = new FakeController(action)
        val result = controller.onPageLoad(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe routes.SessionExpiredController.onPageLoad().url
      }

      "Insufficient confidence level" in {
        val action = new AuthenticatedActionRefiner(new FakeFailingAuthConnector(InsufficientConfidenceLevel("")))
        val controller = new FakeController(action)
        val result = controller.onPageLoad(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe routes.UnauthorisedController.onPageLoad().url
      }
    }

    "return internal server error" when {
      "a different exception is returned" in {
        val action = new AuthenticatedActionRefiner(new FakeFailingAuthConnector(new Exception("")))
        val controller = new FakeController(action)
        val result = controller.onPageLoad(fakeRequest)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}

class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    Future.failed(exceptionToReturn)
  }
}

object FakeAuthenticatedActionRefiner extends AuthenticatedActionRefiner(authConnector = ???) {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    Future.successful(Right(AuthenticatedRequest(request, Some("nino"), ConfidenceLevel.L200)))
  }
}
