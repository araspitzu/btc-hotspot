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

Now you need to locate your PI in the local network, depending on your router the address might look like: `192.168.1.XXX`. An easy
way to do this is to open the control panel of the router and check for connected devices, then look for 'raspberrypi' and figure out
its network address. If you are in trouble please refer to the official guide from raspberrypi.org [https://www.raspberrypi.org/documentation/remote-access/ip-address.md].

Assuming you found the address of the PI run the following command on your terminal to start the installation 
process: 

```
    curl -s https://raw.githubusercontent.com/araspitzu/btc-hotspot/master/buildscript.sh | ssh pi@<ADDRESS_OF_PI>
```

You can grap a cup of coffee while the installer runs on the PI, it takes ~3 minutes at the moment. After the installer finishes
it tells you the name of the hotspot wifi network but you don't need to connect to it now. 

To continue please visit `http://<ADDRESS_OF_PI>:8082` you will find instructions in the admin panel.
