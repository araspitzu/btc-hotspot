package iptables

import java.io.{InputStreamReader, BufferedReader}

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andrea on 09/11/16.
  *
  */
class IpTablesService extends LazyLogging{

  private implicit class CmdExecutor(cmd:String) {
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


  }

  private def iptables(params:String) = {
    s"sudo /sbin/iptables $params"
  }

  def enableClient(mac:String):Future[String] = {
    iptables(s"-I internet 1 -t mangle -m mac --mac-source $mac -j RETURN").exec
  }

  def disableClient(mac:String):Future[String] = {
    iptables(s"-D internet -t mangle -m mac --mac-source $mac -j RETURN").exec
  }

}


