package identifiers.register.individual

import identifiers.TypedIdentifier
import models.Address

case object IndividualPreviousAddressListId extends TypedIdentifier[Address] {
  override def toString: String = "individualPreviousAddressList"
}
