# How to install btc-hotspot on my rpi3

# Pre-requisites

To follow this guide you need a working raspberry-pi 3, access to your home's wifi router and a bit
of knowledge with the command line (nothing over the top really).

# Installation

Download the latest raspbian-stretch-lite from [raspberrypi.org](https://www.raspberrypi.org/downloads/raspbian/), then 
follow the official instructions [here](https://www.raspberrypi.org/documentation/installation/installing-images/README.md) in order 
to write the image to an SD card. After that you need SSH access to the PI, a quick way is to create an empty file called __ssh__ in the
`/boot` partition of your SD card. Example command: 
> echo "" > /media/SD_CARD/boot/ssh

Then plug the ethernet cable in the raspberry and connect it to your internet router.
![connecting your pi to the internet router](https://cdn-learn.adafruit.com/assets/assets/000/002/920/original/learn_raspberry_pi_router_connection.jpg?1396788892)

Now you need to locate your PI in the local network, depending on your router the address might look like: `192.168.1.XXX`. An easy
way to do this is to open the control panel of the router and check for connected devices, then look for 'raspberrypi' and figure out
its network address. If you are in trouble please refer to the official guide from raspberrypi.org [https://www.raspberrypi.org/documentation/remote-access/ip-address.md].

Assuming you found the address of the PI run the following command on your terminal to start the installation 
process: 

```
    curl -s https://raw.githubusercontent.com/araspitzu/btc-hotspot/master/buildscript.sh | ssh pi@[ADDRESS_OF_PI]
```

You can grap a cup of coffee while the installer runs on the PI, it takes ~3 minutes at the moment. After the installer finishes
it tells you the name of the hotspot wifi network but you don't need to connect to it now. 

To continue please visit `http://[ADDRESS_OF_PI]:8082` you will find in the admin panel.
The alpha release goes by default on tesnet and after the installation you should be just ready to pay and enjoy btc-hotspot, the process
has been tested with eclair android wallet. If you are having trouble paying the hotspot you can open a channel with the hotspot node, in testnet
it is "03ed43580a606ca5db076740227b87642937fda912293c11a881edc63356e22983@173.249.21.93:9735"

# :zap: Lightning setup
//TODO
Connect to node
Open a channel
Make sure you're not 100% foundee 

