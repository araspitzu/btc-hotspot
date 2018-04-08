## 
##  btc hotspot build script
##
##  OS: 2018-03-13-raspbian-stretch-lite.img
##
##  Raspberry Pi3

#! /bin/sh


ESSID="swagger"	# Hotspot ap name
IFACE_WAN=eth0  # Toward the internet
IFACE_LAN=wlan0 # Offered to local clients


echo -e "######   RUNNING UPDATE   ##########"
# Update packages
sudo apt-get update

#https://www.gnu.org/software/bash/manual/html_node/ANSI_002dC-Quoting.html
# Get dependencies and tools

echo -e "######   INSTALLING DEPENDENCIES   #########"
sudo apt-get install -y \
  iptables \
  net-tools \
  hostapd \
  openjdk-8-jre


# Alter network interfaces  TODO: add WAN section
echo -e "######   ALTERING NETWORK INTERFACES   #########"
sudo su -c "echo $'
auto lo $IFACE_LAN
iface $IFACE_LAN inet static
address 10.0.0.1
netmast 255.255.255.0
gateway 10.0.0.1' >> /etc/network/interfaces"

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

# Edit hostapd init script to use the conf # BROKEN (must change only the first occurrence)!!!
echo -e "######   EDIT HOSTAPD INIT SCRIPT   #########"
sudo sed -i '/DAEMON_CONF/c\DAEMON_CONF=/etc/hostapd/hostapd.conf/' /etc/init.d/hostapd

# Allow ipv4 packet forwarding in kernel -- is that persistent?
echo -e "######   ENABLE IPV4 PACKET FORWARDING IN KERNEL   #########"
sudo su -c "echo 1 > /proc/sys/net/ipv4/ip_forward"

# Enable hostapd at boot
echo -e "######   ENABLE HOSTAPD AT BOOT   #########"
sudo systemctl enable hostapd


# btc user append to /etc/sudoers
#btc-hotspot ALL = NOPASSWD: /sbin/iptables -I internet 1 -t mangle -m mac --mac-source ??\:??\:??\:??\:??\:?? -j RETURN
#btc-hotspot ALL = NOPASSWD: /sbin/iptables -D internet -t mangle -m mac --mac-source ??\:??\:??\:??\:??\:?? -j RETURN
