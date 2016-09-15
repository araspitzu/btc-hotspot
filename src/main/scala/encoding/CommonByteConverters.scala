package encoding

import Parsing.{ParseSuccess, ParseResult, ByteReadable}
import EnrichedTypes._
/**
  * Created by andrea on 17/06/15.
  */
object CommonByteConverters {

  /**
    * Byte formatters for common types, uint[length]ByteFormat[bit_order]
    */
  def uint32ByteFormatBE(uint:Long):Array[Byte] = Array(
    0xff & (uint >> 24) toByte,
    0xff & (uint >> 16) toByte,
    0xff & (uint >> 8) toByte,
    0xff & uint toByte
  )

  def uint32ByteFormatLE(uint:Long):Array[Byte] = Array(
    0xff & uint toByte,
    0xff & (uint >> 8) toByte,
    0xff & (uint >> 16) toByte,
    0xff & (uint >> 24) toByte
  )

  def uint16ByteFormatLE(uint:Int):Array[Byte] = Array(
    0xff & uint toByte,
    0xff & (uint >> 8) toByte
  )

  def uint8ByteFormat(uint:Int):Array[Byte] = Array(0xff & uint toByte)

  def uint8ByteFormat(uint:Short):Array[Byte] = uint8ByteFormat(uint.toInt)

  def uint64ByteFormatLE(uint:BigInt):Array[Byte] = Array(
    0xff & uint toByte,
    0xff & (uint >> 8) toByte,
    0xff & (uint >> 16) toByte,
    0xff & (uint >> 24) toByte,
    0xff & (uint >> 32) toByte,
    0xff & (uint >> 40) toByte,
    0xff & (uint >> 48) toByte,
    0xff & (uint >> 56) toByte
  )

  def int64ByteFormatLE(uint:Long):Array[Byte] = Array(
    0xff & uint toByte,
    0xff & (uint >> 8) toByte,
    0xff & (uint >> 16) toByte,
    0xff & (uint >> 24) toByte,
    0xff & (uint >> 32) toByte,
    0xff & (uint >> 40) toByte,
    0xff & (uint >> 48) toByte,
    0xff & (uint >> 56) toByte
  )


  /**
    *  Byte readers for common numeric types, uint[length]ByteReader[bit_order]
    */
  implicit val uint8ByteReader = new {} with ByteReadable[Short] {
    def read(bytes: Array[Byte], offset: Int):ParseResult[Short] = ParseSuccess(
      result = (bytes(offset).unsigned).toShort,
      bytesUsed = 1
    )
  }

  implicit val uint16ByteReaderLE = new {} with ByteReadable[Int] {
    override def read(bytes: Array[Byte], offset: Int): ParseResult[Int] = ParseSuccess(
      result = bytes(offset).unsigned | (bytes(offset + 1).unsigned) << 8 ,
      bytesUsed = 2
    )
  }

  val uint32ByteReaderLE = new {} with ByteReadable[Long] {
    override def read(bytes: Array[Byte], offset:Int):ParseResult[Long] = ParseSuccess(
      result =
        (bytes(offset + 0) & 0xffL) << 0  |
          (bytes(offset + 1) & 0xffL) << 8  |
          (bytes(offset + 2) & 0xffL) << 16 |
          (bytes(offset + 3) & 0xffL) << 24  ,
      bytesUsed = 4
    )
  }

  implicit val uint32ByteReaderBE = new {} with ByteReadable[Long] {
    override def read(bytes: Array[Byte], offset:Int):ParseResult[Long] = ParseSuccess(
      result =
        (bytes(offset + 0) & 0xffL) << 24 |
          (bytes(offset + 1) & 0xffL) << 16 |
          (bytes(offset + 2) & 0xffL) << 8  |
          (bytes(offset + 3) & 0xffL) << 0  ,
      bytesUsed = 4
    )
  }

  val int64ByteReaderLE = new {} with ByteReadable[Long] {
    override def read(bytes: Array[Byte], offset:Int):ParseResult[Long] = ParseSuccess(
      result = java.lang.Long.parseLong(bytes.slice(offset,offset + 8).reverse.bytes2hex,16),
      bytesUsed = 8
    )
  }

  implicit val uint64ByteReaderLE = new {} with ByteReadable[BigInt] {
    override def read(bytes: Array[Byte], offset: Int): ParseResult[BigInt] = ParseSuccess(
      result = BigInt(bytes.slice(offset,offset + 8).reverse.bytes2hex, 16),
      bytesUsed = 8
    )
  }

}

