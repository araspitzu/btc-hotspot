package protocol

import protocol.domain.Offer
import commons.Configuration.MiniPortalConfig._

/**
  * Created by andrea on 17/11/16.
  */
package object webDto {

  case class WebOfferDto(
     offer:Offer,
     paymentURI:String
  )

  object WebOfferDto {
    def apply(offer:Offer):WebOfferDto = WebOfferDto(
      offer,
      paymentURI = s"bitcoin:?r=http://$miniPortalHost:$miniPortalPort/api/pay/${offer.offerId}"
    )
  }

}
