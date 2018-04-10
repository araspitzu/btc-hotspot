# Development instructions

## Prerequisites 
The current setup has been tried only on ubuntu 17.10 but should work on other platforms too,
to start developing `btc-hotspot` you  need the following tools:
- java JDK-8 (both open-jdk and oracle-jdk are fine) 
- sbt [https://www.scala-sbt.org/download.html]
- Optionally eclair [https://github.com/ACINQ/eclair]


:warning: This minimal setup will let you run `btc-hotspot` locally but remember to wire it to a lightning protocol
server (eclair) to test the payment process, if in doubt feel free to contact the repo owner for help or 
to use the project's test instance. :warning:

## Overview
This is captive portal system accepting bitcoin payments over the lightning network protocol and it's designed to run on a 
home server like the raspberry-pi. The app is written in Scala and uses eclair as payment gateway, clients connected to the hotspot
are captured via an iptable based setup (check it out [here](https://github.com/araspitzu/btc-hotspot/blob/master/buildscript.sh)), 
the build script also serves as one-liner installer for the users of btc-hotspot. In the local instance the database is wiped everytime 
you restart the server, 3 test offers are inserted by default ([TestData.scala](https://github.com/araspitzu/btc-hotspot/blob/master/src/main/scala/commons/TestData.scala)) for ease of testing, 
also when running locally your mac address is mocked with 'unknown'. To check out the hotspot main page visit `http://127.0.0.1:8081/anything`,
you will be caught and redirected to the index.html, in this process a new session  for your MAC address is being created. 
You can now choose an offer and click __buy__, the backend will create an invoice for you and redirect your browser to the invoice page, 
there you can pay with a :zap: wallet. As the owner of the hotspot you get an admin panel where you can configure the offers and check 
the current active clients connected, to open the admin panel visit `http://127.0.0.1:8082`


## Setup
Clone and enter the project folder with:

```git clone https://github.com/araspitzu/btc-hotspot && cd btc-hotspot```

To compile and run type:

```sbt reStart```

protip: to avoid killing sbt and restarting it you can launch `sbt` and then issue 
the commands from the interactive shell.

To run the test type: 

```sbt test```

When running locally the configuration will be read from __src/main/resources/application.conf__,
this is where you need to set host and api-token of the eclair node. The finished application aims
at having an _internal_ (inside the raspberry-pi) eclair node to connect to.