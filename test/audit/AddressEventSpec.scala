/*
 * Copyright 2022 HM Revenue & Customs
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
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class AddressEventSpec extends AsyncFlatSpec with Matchers {

  import AddressEventSpec._

  "addressEntryEvent" should "return a Manual Address Event when there is no previous or selected address" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Manual, context, address1))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, None, None, context)

    actual shouldBe expected
  }

  it should "return a Manual Address Event when address has changed and there is no selected address" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Manual, context, address1))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address2), None, context)

    actual shouldBe expected
  }

  it should "return a Lookup Address Event when there is no previous address and address matches selected" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Lookup, context, address1))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, None, Some(tolerant(address1)), context)

    actual shouldBe expected
  }

  it should "return a Lookup Address Event when address has changed and address matches selected" in {
    val expected = Some(AddressEvent(externalId, AddressAction.Lookup, context, address1))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address2), Some(tolerant(address1)), context)

    actual shouldBe expected
  }

  it should "return a LookupChanged Address Event when there is no previous address and address and selected differ and details should be correct" in {
    val expected = Some(AddressEvent(externalId, AddressAction.LookupChanged, context, address1))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, None, Some(tolerant(address2)), context)

    actual shouldBe expected

    actual.map(_.details) shouldBe expected.map(_.details)

  }

  it should "return a LookupChanged Address Event when address has changed and address and selected differ" in {
    val expected = Some(AddressEvent(externalId, AddressAction.LookupChanged, context, address1))
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address2), Some(tolerant(address2)), context)

    actual shouldBe expected
  }

  it should "return None when address and previous address match" in {
    val actual = AddressEvent.addressEntryEvent(externalId, address1, Some(address1), None, context)

    actual shouldBe None
  }

}

object AddressEventSpec {

  val externalId: String = "test-external-id"
  val context: String = "test-context"

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
