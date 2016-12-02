run:
	sbt run

compile:
	sbt compile

package:
	sbt debian:packageBin

piDeploy:
	sbt debian:packageBin && scp target/btc-hotspot_0.0.1_all.deb pi@192.168.0.1:/home/pi/container && ssh pi@192.168.0.1 'sudo dpkg -i /home/pi/container/btc-hotspot_0.0.1_all.deb'

