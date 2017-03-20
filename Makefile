run:
	sbt run

compile:
	sbt compile

package:
	sbt debian:packageBin

piDeploy:
	sbt debian:packageBin && scp target/btc-hotspot_0.0.1_all.deb pi@$(host):/home/pi/container && ssh pi@$(host) 'sudo dpkg -i /home/pi/container/btc-hotspot_0.0.1_all.deb'

