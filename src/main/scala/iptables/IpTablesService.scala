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
object IpTablesService extends LazyLogging {

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

    def withDelay(minutes:Int):String = {
      s"""echo "$cmd" |at now + $minutes minutes"""
    }

  }

  private def iptables(params:String) = {
    s"sudo /sbin/iptables $params"
  }

  /**
    * Enables the mac client in iptables and then schedules another iptable command to remove the previous rule,
    * it effectively restrict a client to use internet for a certain amount of @param minutes
    * TODO parse the date from the output of "at" and return it inside the future
    * @param mac
    * @param minutes
    * @return the output of the scheduling
    */
  def enableClient(mac:String, minutes:Int):Future[String] = {
    for {
      enabled <- enableClient(mac)
      scheduledAt <- iptables(disableClientRule(mac)) withDelay(minutes) exec
    } yield {
      logger.info(scheduledAt)
      scheduledAt
    }
  }

  def enableClient(mac:String):Future[String] = {
    iptables(enableClientRule(mac)).exec
  }

  def disableClient(mac:String):Future[String] = {
    iptables(disableClientRule(mac)) exec
  }

  private def enableClientRule(mac:String):String =
    s"-I internet 1 -t mangle -m mac --mac-source $mac -j RETURN"

  private def disableClientRule(mac:String):String =
    s"-D internet -t mangle -m mac --mac-source $mac -j RETURN"

}


