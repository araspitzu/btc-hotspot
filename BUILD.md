#Development instructions

##Prerequisites 
The current setup has been tried only on ubuntu 17.10 but should work on other platforms too,
to start developing `btc-hotspot` you  need the following tools:
- java JDK-8 (both open-jdk and oracle-jdk are fine) 
- sbt [https://www.scala-sbt.org/download.html]
- Optionally eclair [https://github.com/ACINQ/eclair]

This minimal setup will let you run `btc-hotspot` locally but remember to wire it to a lightning protocol
server (eclair) to test the payment process, if in doubt feel free to contact the repo owner for help or 
to use the project's test instance.

##Setup
Clone this repo with:

```git clone https://github.com/araspitzu/btc-hotspot```

Now change directory with:

```cd btc-hotspot```

To compile and run type:

```sbt reStart```

protip: to avoid killing sbt and restarting it you can launch `sbt` and then issue 
the commands from the interactive shell.

To run the test type: 

```sbt test```

When running locally the configuration will be read from "src/main/resources/application.conf",
this is where you need to set host and api-token of the eclair node. The finished application aims
at having an _internal_ (inside the raspberry-pi) eclair node to connect to.