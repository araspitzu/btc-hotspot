package ln

import org.json4s.JsonAST.JValue

package object model {

  case class JsonRPCRequest(
    jsonrpc: String = "1.0",
    id: String = "btc-hotspot",
    method: String,
    params: Seq[Any]
  )

  case class JsonRPCError(code: Int, message: String)

  case class JsonRPCResponse(
    result: JValue,
    error: Option[JsonRPCError],
    id: String
  )

  case class GetEclairInvoice(
    msat: Long,
    msg: String
  )

}
