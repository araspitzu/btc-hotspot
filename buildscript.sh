## 
##  btc-hotspot build script
##
##  OS: 2018-03-13-raspbian-stretch-lite.img
##
##  Raspberry Pi3

#! /bin/sh

IPTABLES="/sbin/iptables"

ESSID="swagger"	    # Hotspot ap name
IFACE_WAN=eth0      # Toward the internet
IFACE_LAN=wlan0     # Offered to local clients
SUBNET=10.0.0.1     # Subnet for hotspot
MASK=255.255.255.0  # Subnet's mask
DEFAULT_DNS=8.8.8.8 # Default dns server for hotspot

echo -e "######   RUNNING UPDATE   ##########"
# Update packages
sudo apt-get update

# Get dependencies and tools
echo -e "######   INSTALLING DEPENDENCIES   #########"
sudo apt-get install -y \
  iptables \
  hostapd \
  dnsmasq \
  openjdk-8-jre-headless


# Create hostapd conf
echo -e "######   HOSTAPD CONFIGURATION   #########"
sudo su -c "echo $'
auth_algs=1
channel=3
country_code=IT
disassoc_low_ack=1
# Require clients to know the network name
ignore_broadcast_ssid=0
hw_mode=g
interface=$IFACE_LAN
driver=nl80211
ieee80211n=1
ssid=$ESSID' > /etc/hostapd/hostapd.conf"

echo -e "######   EDIT HOSTAPD INIT SCRIPT   #########"
sudo sed -i '/DAEMON_CONF=/c\DAEMON_CONF=/etc/hostapd/hostapd.conf' /etc/init.d/hostapd

# Enable hostapd at boot
echo -e "######   ENABLE HOSTAPD AT BOOT   #########"
sudo systemctl enable hostapd

echo -e "######   SETTING UP DHCPCD   #########"
sudo su -c "echo $'
nohook
interface $IFACE_LAN
static ip_address=$SUBNET/24
static routers=$SUBNET
#static domain_name_servers=$DEFAULT_DNS
' >> /etc/dhcpcd.conf"

echo -e "########    SETTING UP DNSMASK   #########"
sudo su -c "echo $'
interface=$IFACE_LAN
bind-dynamic
domain-needed
bogus-priv
dhcp-range=$SUBNET,10.0.0.255,$MASK,12h
' >> /etc/dnsmasq.conf"

echo -e "######   ENABLE IPV4 PACKET FORWARDING IN KERNEL  #########"
sudo sed -i '/net.ipv4.ip_forward/c\net.ipv4.ip_forward=1' /etc/sysctl.conf

echo -e "######   SETUP IPTABLES   #########"
# nat-ing for eth0-wlan0
sudo $IPTABLES -t nat -A POSTROUTING -o $IFACE_WAN -j MASQUERADE
# accept packets coming from outside
sudo $IPTABLES -t filter -A FORWARD -i $IFACE_WAN -o $IFACE_LAN -m state --state RELATED,ESTABLISHED -j ACCEPT
# accept packets from wlan0 to eth0
sudo $IPTABLES -t filter -A FORWARD -i $IFACE_LAN -o $IFACE_WAN -j ACCEPT

# Create new chains 'internet_incoming' and 'internet_outgoing' for catching the traffic, non auth clients will be marked with 99 while
# authenticated clients will jump off the chain asap. Packets with the mark will be redirected to the hotspot's captive portal.

# internet_incoming chain will see downlink packets
sudo $IPTABLES -t mangle -N internet_incoming
# internet_outgoing chain will see uplink packets
sudo $IPTABLES -t mangle -N internet_outgoing

# Intercept uplink packets in prerouting from iface wlan0 and send them to internet_outgoing chain
sudo $IPTABLES -t mangle -A PREROUTING -i wlan0 -j internet_outgoing
# mark non authorized packets with '99'
sudo $IPTABLES -t mangle -A internet_outgoing -j MARK --set-mark 99

# Intercept downlink packets after routing going to iface wlan0 and send them to internet_incoming
sudo $IPTABLES -t mangle -A POSTROUTING -o wlan0 -j internet_incoming

# redirect non authorized http/https requests
sudo $IPTABLES -t nat -A PREROUTING -m mark --mark 99 -p tcp --dport 80 -j DNAT --to-destination $SUBNET:8081
sudo $IPTABLES -t nat -A PREROUTING -m mark --mark 99 -p tcp --dport 443 -j DNAT --to-destination $SUBNET:8081

# drop all the unauthorized packets
sudo $IPTABLES -t filter -A FORWARD -m mark --mark 99 -j DROP

# Enable ssh for ott0disk
sudo $IPTABLES -I internet_outgoing 1 -t mangle -p tcp --dport 22 -m mac --mac-source c4:8e:8f:f8:e4:37 -j RETURN

# Persist iptables
echo iptables-persistent iptables-persistent/autosave_v4 boolean true | sudo debconf-set-selections
echo iptables-persistent iptables-persistent/autosave_v6 boolean true | sudo debconf-set-selections
sudo apt-get install -y iptables-persistent


echo -e "########  DOWNLOAD ECLAIR  (Skipped) #########"
#wget https://github.com/ACINQ/eclair/releases/download/v0.2-beta2/eclair-node-0.2-beta2-7598615.jar

echo -e "########  DOWNLOAD btc-hotspot RELEASE   #########"
wget https://github.com/araspitzu/btc-hotspot/releases/download/v0.0.2-alpha/btc-hotspot_0.0.2-alpha_all.deb
sudo dpkg -i btc-hotspot_0.0.2-alpha_all.deb

echo -e "########  ENABLE SUDOERS FOR btc-hotspot   ########"
# btc user append to /etc/sudoers
sudo su -c "echo $'btc-hotspot ALL = NOPASSWD: /sbin/iptables -t mangle -I internet_outgoing 1 -m mac --mac-source ??\:??\:??\:??\:??\:?? -j RETURN' >> /etc/sudoers"
sudo su -c "echo $'btc-hotspot ALL = NOPASSWD: /sbin/iptables -t mangle -D internet_outgoing -m mac --mac-source ??\:??\:??\:??\:??\:?? -j RETURN' >> /etc/sudoers"

echo -e "#######   REBOOTING   #######"
echo -e "To continue the installation please wait ~1min and connect to the wifi network '$ESSID'"
sudo shutdown -r now