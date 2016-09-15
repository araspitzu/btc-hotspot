package encoding

/**
  * Created by andrea on 06/03/16.
  */
object EnrichedTypes {

  val alphabet:Array[Char] = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray
  val encodedZero:Char = alphabet.head

  implicit class ByteWritableArray[T <: ByteWritable](array:Array[T]) extends ByteWritable {
    override def byteFormat: Array[Byte] = {
      array.foldRight[Array[Byte]](Array.emptyByteArray)((el, acc) => el.byteFormat ++ acc )
    }
  }

  implicit class ByteVector(bytes: Array[Byte]) {

    def bytes2hex: String = bytes.map("%02x".format(_)).mkString

    def toBase58 = base58encode(bytes)

  }

  implicit class HexString(hex:String){
    def hex2bytes:Array[Byte] = hex.sliding(2, 2).map(Integer.parseInt(_, 16).toByte).toArray
  }

  implicit class RichByte(byte:Byte){
    def unsigned:Int = byte & 0xff
  }

  /**
    * Bitcoin's custom base58 encoding
    *
    * @param bytes
    * @return
    */
  private def base58encode(bytes: Array[Byte]):String = {
    if(bytes.length == 0)
      return ""

    //count leading zeros
    var zeros = bytes.count(_ == 0)

    //the resulting char array, upper bound
    val encoded = new Array[Char](bytes.length * 2)

    val copy = java.util.Arrays.copyOf(bytes, bytes.length)
    var outputStart = encoded.length
    var inputStart = zeros
    while(inputStart < bytes.length){
      outputStart -= 1
      encoded(outputStart) = alphabet(divmod(bytes, inputStart, 256, 58))
      if(bytes(inputStart) == 0)
        inputStart += 1
    }

    while (outputStart < encoded.length && encoded(outputStart) == encodedZero)
      outputStart += 1

    zeros -= 1
    while(zeros >= 0){
      outputStart -= 1
      encoded(outputStart) = encodedZero
      zeros -=  1
    }

    new String(encoded, outputStart, encoded.length - outputStart)
  }

  private def divmod(number: Array[Byte], firstDigit: Int, base: Int, divisor: Int) = {
    var reminder: Int = 0

    var i: Int = firstDigit
    while (i < number.length) {

      val digit: Int = number(i).toInt & 0xFF
      val temp: Int = reminder * base + digit
      number(i) = (temp / divisor).toByte
      reminder = temp % divisor

      i += 1

    }

    reminder.toByte
  }

}
