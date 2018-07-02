package viewmodels

import play.api.mvc.Call

case class VatViewModel (
                          postCall: Call,
                          title: Message,
                          heading: Message,
                          body: Message,
                          subHeading: Option[Message] = None
                        )
