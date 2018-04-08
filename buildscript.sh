## 
##  btc-hotspot build script
##
##  OS: 2018-03-13-raspbian-stretch-lite.img
##
##  Raspberry Pi3

#! /bin/sh

IPTABLES="/sbin/iptables"

ESSID="swagger"	# Hotspot ap name
IFACE_WAN=eth0  # Toward the internet
IFACE_LAN=wlan0 # Offered to local clients


echo -e "######   RUNNING UPDATE   ##########"
# Update packages
sudo apt-get update

# Get dependencies and tools
echo -e "######   INSTALLING DEPENDENCIES   #########"
sudo apt-get install -y \
  iptables \
  net-tools \
  hostapd \
  openjdk-8-jre-headless


echo -e "######   ALTERING NETWORK INTERFACES   #########"
sudo su -c "echo $'
interface wlan0
static ip_address=10.0.0.1/24
#static ip6_address=fd51:42f8:caae:d92e::ff/64
static routers=10.0.0.1
static domain_name_servers=8.8.8.8' >> /etc/dhcpcd.conf"

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

echo -e "######   ENABLE IPV4 PACKET FORWARDING IN KERNEL   #########"
sudo sysctl -w net.ipv4.ip_forward=1

# Enable hostapd at boot
echo -e "######   ENABLE HOSTAPD AT BOOT   #########"
sudo systemctl enable hostapd

echo -e "######   SETUP IPTABLES   #########"
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
sudo $IPTABLES -t nat -A PREROUTING -m mark --mark 99 -p tcp --dport 80 -j DNAT --to-destination 10.0.0.1:8081
sudo $IPTABLES -t nat -A PREROUTING -m mark --mark 99 -p tcp --dport 443 -j DNAT --to-destination 10.0.0.1:8081

# nat-ing for eth0-wlan0
sudo $IPTABLES -t nat -A POSTROUTING -o eth0 -j MASQUERADE

# accept packets coming from outside
sudo $IPTABLES -t filter -A FORWARD -i eth0 -o wlan0 -m state --state RELATED,ESTABLISHED -j ACCEPT

# drop all the unauthorized packets
sudo $IPTABLES -t filter -A FORWARD -m mark --mark 99 -j DROP

# accept packets from wlan0 to eth0
sudo $IPTABLES -t filter -A FORWARD -i wlan0 -o eth0 -j ACCEPT

# Enable ssh for ott0disk
sudo $IPTABLES -I internet_outgoing 1 -t mangle -p tcp --dport 22 -m mac --mac-source c4:8e:8f:f8:e4:37 -j RETURN


echo -e "########  ENABLE SUDOERS FOR btc-hotspot   ########"
# btc user append to /etc/sudoers
sudo su -c "echo $'btc-hotspot ALL = NOPASSWD: /sbin/iptables -I internet 1 -t mangle -m mac --mac-source ??\:??\:??\:??\:??\:?? -j RETURN' >> /etc/sudoers"
sudo su -c "echo $'btc-hotspot ALL = NOPASSWD: /sbin/iptables -D internet -t mangle -m mac --mac-source ??\:??\:??\:??\:??\:?? -j RETURN' >> /etc/sudoers"