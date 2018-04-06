package ln

import org.json4s.JsonAST.JValue

package object model {

  case class JsonRPCRequest(
    jsonrpc: String = "1.0",
    id: String = "btc-hotspot",
    method: String,
    params: Seq[Any] = Seq.empty
  )

  case class JsonRPCError(code: Int, message: String)

  case class JsonRPCResponse(
    result: JValue,
    error: Option[JsonRPCError],
    id: String
  )

  case class EclairGetInfoResponse(
    nodeId: String,
    alias: String,
    port: Int,
    chainHash: String,
    blockHeight: Long
  )

  case class EclairPeerResponse(
    nodeId: String,
    state: String, //TODO map with enum
    address: String,
    channels: Int
  )

  case class EclairChannelResponse(
    nodeId: String,
    channelId: String,
    status: String //TODO map with Enum
  )

  case class EclairSendResponse(
    amountMsat: Long,
    paymentHash: String
  )

}
