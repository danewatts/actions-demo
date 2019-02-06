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

package models

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

sealed abstract class ResponseModel(id: String)

object ResponseModel {
  final case class SuccessfulResponseModel(id: String, amount: Int, email: Option[String]) extends ResponseModel(id)
  final case class FailureResponseModel(id: String, error: String) extends ResponseModel(id)

  implicit def formatSuccess: Format[SuccessfulResponseModel] = Json.format[SuccessfulResponseModel]
  implicit def formatFailure: Format[FailureResponseModel] = Json.format[FailureResponseModel]

  implicit def httpReads: HttpReads[ResponseModel] = new HttpReads[ResponseModel] {
    override def read(method: String, url: String, response: HttpResponse): ResponseModel = {
      if(response.status == 200) {
        response.json.as[SuccessfulResponseModel]
      } else {
        response.json.as[FailureResponseModel]
      }
    }
  }
}
