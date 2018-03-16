/*
 * btc-hotspot
 * Copyright (C) 2016  Andrea Raspitzu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package commons

import commons.Configuration.EmailConfig
import org.apache.commons.mail._

object MailService {

  sealed abstract class MailType
  case object Plain extends MailType
  case object Rich extends MailType

  case class Mail(
    from: (String, String), // (email -> name)
    to: Seq[String],
    cc: Seq[String] = Seq.empty,
    bcc: Seq[String] = Seq.empty,
    subject: String,
    message: String,
    richMessage: Option[String] = None)

  def send(mail: Mail) {

    val format =
      if (mail.richMessage.isDefined) Rich
      else Plain

    val commonsMail: Email = format match {
      case Plain => new SimpleEmail().setMsg(mail.message)
      case Rich  => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
    }

    commonsMail.setCharset(EmailConstants.UTF_8)
    commonsMail.setHostName(EmailConfig.smtpServer)
    commonsMail.setSmtpPort(EmailConfig.port)
    commonsMail.setAuthenticator(new DefaultAuthenticator(EmailConfig.username, EmailConfig.password))
    commonsMail.setSSLOnConnect(true)

    // Can't add these via fluent API because it produces exceptions
    mail.to foreach commonsMail.addTo
    mail.cc foreach commonsMail.addCc
    mail.bcc foreach commonsMail.addBcc

    commonsMail.
      setFrom(mail.from._1, mail.from._2).
      setSubject(mail.subject).
      send()
  }

}
