package chilli

/**
  * Created by andrea on 09/11/16.
  */
package object domain {

  /**
    *        # chilli_query list | awk '{
           ($5 == 1) {
             print "User " i++
             print " MAC:                    " $1
             print " IP Address:             " $2
             print " Session ID:             " $4
             print " Username:               " $6
             print " Duration / Max:         " $7
             print " Idle / Max:             " $8
             print " Input Octets / Max:     " $9
             print " Output Octets / Max:    " $10
             print " Max Total Octets:       " $11
             print " Using swapoctets:       " $12
             print " % / Max Up Bandwidth:   " $13
             print " % / Max Down Bandwidth: " $14
             print " Original URL:           " $15
           }
         }'

    */
  case class Report(
    mac:String,
    ipAddress:String,
    status:String,
    sessionId:String,
    username:String,
    durationMax:Long,
    idleMax:Long,
    inputOctetsMax:Long,
    outputOctetsMax:Long,
    totalOctetsMax:Long,
    usingSwapOctets:Boolean,
    bandwidthUpMax:Long,
    bandwidthDownMax:Long,
    originalUrl:String
  )

}
