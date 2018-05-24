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

package audit

import models.{Address, TolerantAddress}
import org.scalatest.{FlatSpec, Matchers}

class AddressEventSpec extends FlatSpec with Matchers {

  import AddressEventSpec._

  "addressEntryEvent" should "return a Manual Address Event when there is no previous or selected address" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Manual))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, None, None)

    actual shouldBe expected
  }

  it should "return a Manual Address Event when address has changed and there is no selected address" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Manual))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address2), None)

    actual shouldBe expected
  }

  it should "return a Lookup Address Event when there is no previous address and address matches selected" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Lookup))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, None, Some(tolerant(address1)))

    actual shouldBe expected
  }

  it should "return a Lookup Address Event when address has changed and address matches selected" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Lookup))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address2), Some(tolerant(address1)))

    actual shouldBe expected
  }

  it should "return a LookupChanged Address Event when there is no previous address and address and selected differ" in {
    val expected = Some(AddressEvent(externalId, AddressAction.LookupChanged))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, None, Some(tolerant(address2)))

    actual shouldBe expected
  }

  it should "return a LookupChanged Address Event when address has changed and address and selected differ" in {
    val expected = Some(AddressEvent(externalId, AddressAction.LookupChanged))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address2), Some(tolerant(address2)))

    actual shouldBe expected
  }

  it should "return None when address and previous address match" in {
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address1), None)

    actual shouldBe None
  }

}

object AddressEventSpec {

  val externalId: String = "test-external-id"

  val address1 = Address(
    "address-1-line-1",
    "address-1-line-2",
    None,
    None,
    Some("postcode-1"),
    "country-1"
  )

  val address2 = Address(
    "address-2-line-1",
    "address-2-line-2",
    None,
    None,
    Some("postcode-2"),
    "country-2"
  )

  def tolerant(address: Address): TolerantAddress =
    TolerantAddress(
      Some(address.addressLine1),
      Some(address.addressLine2),
      address.addressLine3,
      address.addressLine4,
      address.postcode,
      Some(address.country)
    )

}
