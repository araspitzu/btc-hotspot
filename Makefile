run:
	sbt run

test:
	sbt test

compile:
	sbt compile

package:
	sbt -Denv=$(env) -Dversion=$(version) debian:packageBin

deploy:
	sbt -Denv=$(env) -Dversion=$(version) debian:packageBin &&
	scp target/btc-hotspot_$(version)_all.deb pi@$(host):/home/pi/container &&
	ssh pi@$(host) 'sudo dpkg -i /home/pi/container/btc-hotspot_$(version)_all.deb'

