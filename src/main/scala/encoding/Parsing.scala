package encoding

import scala.reflect.ClassTag
import scala.util.control.ControlThrowable
import scala.util.{Failure, Success, Try}

/**
  * Created by andrea on 25/05/15.
  */
object Parsing {

  /**
    * Container for a successful or failed parse result
    * @tparam T
    */
  sealed trait ParseResult[+T]{

    def withOffset = this match {
      case ParseSuccess(result,used) => ParseSuccess( (result,used) , used )
      case e:ParseFailure => e
    }

    def getOrElse[U >: T](default: => U):(U,Int) = this match {
      case ParseSuccess(result,used) => (result,used)
      case e:ParseFailure =>(default,0)
    }

    def toOpt = this match {
      case ParseSuccess(result,_) => Some(result)
      case _ => None
    }

    private def filter(p: T => Boolean):ParseResult[T] = this match {
      case ParseSuccess(result,used) if(p(result)) => ParseSuccess(result,used)
      case e:ParseFailure => e
    }

    def withFilter(p: T => Boolean) = new WithFilter(p)

    def map[U](f: T => U):ParseResult[U] = this match {
      case ParseSuccess(result,used) => flatMap( r =>  ParseSuccess(f(r),0))
      case e:ParseFailure => e
    }

    /**
      * Bind operation between two ParseResults carries the offset
      */
    def flatMap[U](f: T => ParseResult[U]):ParseResult[U] = this match {
      //IF THIS IS A SUCCESS
      case ParseSuccess(result,used) => {
        //Chain the parsing
        f(result) match {
          //If chained parsing is successful
          case ParseSuccess(newResult,newOffset) => ParseSuccess(newResult,used + newOffset)
          case e:ParseFailure => e
        }
      }
      case e:ParseFailure => e
    }

    /**
      * Provides monadic filtering interface
      * @param p the predicate for the test
      */
    protected[encoding] class WithFilter(p: T => Boolean) {
      def map[U](f: T => U): ParseResult[U] = filter(p).map(f)
      def flatMap[U](f: T => ParseResult[U]): ParseResult[U] = filter(p).flatMap(f)
      def foreach[U](f: T => U): Unit = filter(p) match {
        case ParseSuccess(t, used) => f(t)
        case f:ParseFailure => ()
      }
      def withFilter(q: T => Boolean): WithFilter = new WithFilter(x => p(x) && q(x))
    }


  }


  case class ParseSuccess[T](result: T,bytesUsed:Int) extends ParseResult[T]
  case class ParseFailure(err: String, optThr: Option[Throwable]) extends ParseResult[Nothing] {
    override def toString:String = optThr match {
      case Some(thr) =>
        val parserStackTrace = thr.getStackTrace.filter(_.getMethodName == "read").fold("\n ======= PARSER STACK TRACE =======")(_ + "\n" + _)
        val exceptionStackTrace = thr.getStackTrace.take(10).fold("\n ======= ERROR AT =======")(_ + "\n" + _)

        s"$err, reason: ${thr.getMessage} $parserStackTrace $exceptionStackTrace"
      case None => err
    }
  }

  trait ByteReadable[T] {
    def read(bytes: Array[Byte], offset: Int): ParseResult[T]

    def safeRead(bytes: Array[Byte], offset: Int):ParseResult[T] = Try {
      read(bytes,offset)
    } match {
      case Success(t) => t
      case Failure(thr) => ParseFailure(s"Error parsing ${this.getClass.getName} at byte $offset",Some(thr))
    }

  }

  case class ParseHalt(err:ParseFailure,remainingSteps:Int) extends ControlThrowable

  def parse[T](bytes:Array[Byte],offset:Int = 0)(implicit reader:ByteReadable[T]):ParseResult[T] = reader.safeRead(bytes,offset)

  def parse[T](bytes:String)(implicit reader:ByteReadable[T]):ParseResult[T] = {
    import encoding.EnrichedTypes._
    parse[T](bytes.hex2bytes,0)
  }

  def parseBytes(bytes:Array[Byte],offset:Int = 0,numElem:Int):ParseResult[Array[Byte]] = ParseSuccess(
    result = bytes.slice(offset,offset + numElem),
    bytesUsed = numElem
  )

  def parseList[T](bytes:Array[Byte],offset:Int,numElem:Int)(implicit reader:ByteReadable[T], ct:ClassTag[T]):ParseResult[Array[T]] = try {
    val (list,used) = seqParse[T](bytes,offset,numElem)
    ParseSuccess(list.toArray,used)
  } catch {
    case halt:ParseHalt => halt.err.copy(err = s"${halt.err} at step ${numElem - halt.remainingSteps}")
    case thr:Throwable => ParseFailure(s"Error while parsing list of type ${this.getClass.getName} at byte $offset",Some(thr))
  }

  //TODO make this tail recursive
  private def seqParse[T](bytes:Array[Byte],offset:Int,remaining:Int)(implicit reader:ByteReadable[T]):(List[T],Int) = {
    if(remaining > 0)
      parse[T](bytes, offset) match {
        case ParseSuccess(t,used) => {
          val (list, usedAcc) = seqParse(bytes, offset + used, remaining - 1)
          (t :: list, usedAcc + used)
        }
        case e:ParseFailure => throw ParseHalt(e,remaining)
      }
    else
      (List.empty,0)
  }


}

