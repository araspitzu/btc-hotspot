package ipc

import encoding.ByteWritable
import encoding.CommonByteConverters._
import encoding.Parsing._
import encoding.EnrichedTypes._

/**
  * Created by andrea on 14/09/16.
  *
  * Shared data structure between tap_adapter and this project,
  * @param source_mac will be serialized as 48-bit (6 bytes)
  * @param expire_time will be serialized as unsigned long (4 bytes)
  *
  */
case class SharedStruct(source_mac:Array[Byte], expire_time: Long) extends ByteWritable {
  require(source_mac.length == 6, s"Found source_mac of length ${source_mac.length} instead of 6")

  override def byteFormat: Array[Byte] = source_mac ++ uint32ByteFormatBE(expire_time)

  override def toString:String =
   s"""
      {
        source_mac: ${source_mac.map("%02x".format(_)).mkString(":")}
        expire_time: $expire_time
      }
    """.stripMargin


}


object SharedStruct {

  val SOURCE_MAC_LENGTH = 6
  val STRUCT_SIZE = SOURCE_MAC_LENGTH + 4


  implicit val sharedStructByteReadable = new {} with ByteReadable[SharedStruct] {

    override def read(bytes: Array[Byte], offset: Int): ParseResult[SharedStruct] = {
        for {
          source_mac <- parseBytes(bytes, offset, SOURCE_MAC_LENGTH)
          expire_time <- parse[Long](bytes, offset + SOURCE_MAC_LENGTH)
        } yield SharedStruct(
          source_mac = source_mac,
          expire_time = expire_time
        )
    }

  }

  val aStruct = SharedStruct(
    source_mac = "090203040102".hex2bytes,
    expire_time = 31231
  )

}