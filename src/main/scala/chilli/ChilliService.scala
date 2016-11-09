package chilli

import java.io.{InputStreamReader, BufferedReader}
import chilli.domain.Report

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andrea on 09/11/16.
  */
class ChilliService extends LazyLogging{

  private val chilli_query = "chilli_query"

  implicit class ShellExecutor(cmd:String) {
    def exec:Future[String] = Future {

      val proc = Runtime.getRuntime.exec(cmd)
      val exitValue = proc.waitFor
      if(exitValue != 0)
        throw new IllegalStateException(s"\'$cmd\' exited with code $exitValue")

      val reader = new BufferedReader(
        new InputStreamReader (proc.getInputStream )
      )

      reader.lines.iterator.asScala.fold("")(_ + _)
    }

    def withParams(params:String):String = {
      s"$cmd $params"
    }
  }

  def cmd(cmd:String) = {
    s"$chilli_query $cmd"
  }

  //00:0D:XX:XX:XX:XX 10.1.0.3 dnat 46c83f70000 0 - 0/0 0/0 http://url.com
  def status:Future[Report] = {
    cmd("list").exec.map{ result =>
      val elems = result.split("")

      Report(
        mac = elems(0),
        ipAddress = elems(1),
        status = elems(2),
        sessionId = elems(3),
        username = "temp",
        durationMax = 123L,
        idleMax = 123L,
        inputOctetsMax = 123L,
        outputOctetsMax = 123L,
        totalOctetsMax = 123L,
        usingSwapOctets = false,
        bandwidthDownMax = 123L,
        bandwidthUpMax = 123L,
        originalUrl = "http://url.com"
      )

    }


  }

}


