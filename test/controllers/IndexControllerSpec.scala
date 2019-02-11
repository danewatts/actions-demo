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

import connectors.BackendConnector
import controllers.actions.{FakeAuthenticatedActionRefiner, FakeOptionalDataTransformer, FakeRequestDataFilter}
import models.ResponseModel.SuccessfulResponseModel
import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.index

import scala.concurrent.Future

class IndexControllerSpec extends ControllerSpecBase with FrontendController {

  "Index Controller" must {
    "return 200 for a GET" in {
      when(connector.getData(any())(any())).thenReturn(Future.successful(SuccessfulResponseModel("", 1, None)))
      val result = new IndexController(frontendAppConfig, messagesApi, connector, FakeAuthenticatedActionRefiner, FakeOptionalDataTransformer, FakeRequestDataFilter)
        .onPageLoad()(fakeRequest)
      status(result) mustBe OK
    }

    "return the correct view for a GET" in {
      when(connector.getData(any())(any())).thenReturn(Future.successful(SuccessfulResponseModel("", 1, None)))
      val result = new IndexController(frontendAppConfig, messagesApi, connector, FakeAuthenticatedActionRefiner, FakeOptionalDataTransformer, FakeRequestDataFilter)
        .onPageLoad()(fakeRequest)
      contentAsString(result) mustBe index(frontendAppConfig, SuccessfulResponseModel("", 1, None))(fakeRequest, messages).toString
    }
  }

  lazy val connector: BackendConnector = mock[BackendConnector]
}
