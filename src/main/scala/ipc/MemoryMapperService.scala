package ipc

import java.io.{IOException, RandomAccessFile, File}
import java.nio.MappedByteBuffer
import encoding.EnrichedTypes._
import encoding.Parsing._
import java.nio.channels.FileChannel.MapMode._
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration._
import SharedStruct._
import scala.util.{Success, Failure, Try}

/**
  * Created by andrea on 08/09/16.
  *
  * List-like interface to add and remove elems of type SharedStruct to the
  * shared memory.
  */
class MemoryMapperService private (final val buffer:MappedByteBuffer) extends LazyLogging {

  var numEntries = 0

  def position:Int = buffer.position

  def size:Int = buffer.capacity


  def addEntry(struct:SharedStruct):Unit = {
    if(position + STRUCT_SIZE > size)
      throw new IOException("Exceeding buffer capacity (buffer overflow)")

    buffer.put(struct.byteFormat)
    numEntries = numEntries + 1
  }

  def printEntries:Unit = {

    val oldPos = position

    buffer.position(0)
    val temp = new Array[Byte](STRUCT_SIZE)


    while(buffer.position < STRUCT_SIZE * numEntries){

      buffer.get(temp)

      parse[SharedStruct](temp) match {
        case ParseSuccess(struct, used) => logger.info(struct.toString)
        case ParseFailure(err, optEx) => logger.error(err)
      }

    }

    buffer.position(oldPos)

  }

  def printRawBuffer:Unit = {

    val oldPos = position

    buffer.position(0) //
    val arr = new Array[Byte](buffer.limit)
    buffer.get(arr)

    buffer.position(oldPos)

    logger.info(s"Buffer:  ------ \n ${arr.bytes2hex} \n ------ \n")
  }

}

object MemoryMapperService {

  def apply():MemoryMapperService = {

    val filePath = config.getString("sharedMemory.filePath")
    val maxEntries = config.getInt("sharedMemory.maxEntries")
    val fileSize = maxEntries * STRUCT_SIZE

    Try {

      val file = new RandomAccessFile(filePath, "rw")
      val channel = file.getChannel

      val mappedBuffer = channel.map(READ_WRITE, 0, fileSize) // 0 to start with cursor at the beginning

      if(!mappedBuffer.isLoaded)
        mappedBuffer.load

      if(mappedBuffer.isReadOnly)
        throw new IOException("Unable to continue, buffer is READONLY")

      //Set 0x00 to all bytes in the buffer.
      mappedBuffer.position(0)
      while(mappedBuffer.position < mappedBuffer.limit)
        mappedBuffer.put(0x00.toByte)

      mappedBuffer.position(0) // set position to be at start

      mappedBuffer
    } match {
      case Success(buffer) => new MemoryMapperService(buffer)
      case Failure(thr) => throw new IOException(
        s"Unable to initialize shared memory for path: ${filePath} and size:$fileSize",thr
      )
    }

  }

}
