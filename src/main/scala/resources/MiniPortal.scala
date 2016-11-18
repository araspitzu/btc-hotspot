package resources

import akka.http.scaladsl.server.Route
import wallet.WalletServiceComponent

/**
  * Created by andrea on 09/09/16.
  */
trait MiniPortal extends PaymentChannelAPI with StaticFiles with OffersAPI {
  this: WalletServiceComponent =>

  val miniportalRoute: Route =
    paymentChannelRoute ~
    staticFilesRoute ~
    offersRoute ~
    enableMeRoute ~
    preloginRoute ~
    entryPointRoute  //must stay in the last position because it matches any request

}

