package identifiers.register.partnership

import identifiers.TypedIdentifier
import models.TolerantAddress
import play.api.libs.json.JsPath

case class PartnershipContactAddressListId(index: Int) extends TypedIdentifier[TolerantAddress]{

  override def path: JsPath = JsPath \ "partnership" \ index \ PartnershipContactAddressListId.toString

}

object PartnershipContactAddressListId {

  override def toString: String = "partnershipContactAddressList"

}