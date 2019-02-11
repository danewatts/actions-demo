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
import connectors.BackendConnector
import models.ResponseModel
import models.ResponseModel.SuccessfulResponseModel
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import org.mockito.Mockito.when
import org.mockito.Matchers._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.ConfidenceLevel
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OptionalDataTransformerSpec extends SpecBase {

  lazy val mockBackendConnector: BackendConnector = mock[BackendConnector]
  val authenticatedRequest = AuthenticatedRequest(fakeRequest, Some("nino"), ConfidenceLevel.L200)

  class FakeController extends OptionalDataTransformerImpl(mockBackendConnector) {
    def callTransform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "OptionalDataTransformer" should {
    "return OptionalDataRequest" when {
      "backend connector returns a response" in {
        val response = SuccessfulResponseModel("", 1, None)
        when(mockBackendConnector.getData(any())(any()))
          .thenReturn(Future.successful(response))
        val controller = new FakeController
        val result = await(controller.callTransform(authenticatedRequest))
        result mustBe OptionalDataRequest(authenticatedRequest.request, Some("nino"), ConfidenceLevel.L200, response)
      }
    }

    "throw an exception" when {
      "backend connector throws an exception" in {
        when(mockBackendConnector.getData(any())(any()))
          .thenReturn(Future.failed(new Exception("")))
        val controller = new FakeController
        intercept[Exception] {
          await(controller.callTransform(authenticatedRequest))
        }
      }
    }
  }
}

object FakeOptionalDataTransformer extends OptionalDataTransformer {
  override def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    Future.successful(OptionalDataRequest(request.request, request.nino, request.cl, SuccessfulResponseModel("", 1, None)))
  }
}
