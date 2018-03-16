#! /bin/sh

IPTABLES=/sbin/iptables

# Create new chain "internet" for catching the traffic, non auth clients will be marked with 99 while
# authenticated clients will jump off the chain asap
$IPTABLES -N internet -t mangle

# route traffic to chain "internet"
$IPTABLES -t mangle -A PREROUTING -j internet

# mark non authorized packets with '99'
$IPTABLES -t mangle -A internet_out -j MARK --set-mark 99

# redirect non authorized http/https requests
$IPTABLES -t nat -A PREROUTING -m mark --mark 99 -p tcp --dport 80 -j DNAT --to-destination 192.168.0.1:8081
$IPTABLES -t nat -A PREROUTING -m mark --mark 99 -p tcp --dport 443 -j DNAT --to-destination 192.168.0.1:8081

$IPTABLES -t nat -A POSTROUTING -o eth0 -j MASQUERADE

# accept packets coming from outside 
$IPTABLES -t filter -A FORWARD -i eth0 -o wlan0 -m state --state RELATED,ESTABLISHED -j ACCEPT

# drop all the unauthorized packets
$IPTABLES -t filter -A FORWARD -m mark --mark 98 -j DROP

# accept packets from wlan0 to eth0 
$IPTABLES -t filter -A FORWARD -i wlan0 -o eth0 -j ACCEPT 

# Enable ssh for ott0disk
$IPTABLES -I internet_out 1 -t mangle -p tcp --dport 22 -m mac --mac-source c4:8e:8f:f8:e4:37 -j RETURN

# Enable a mac address
# $IPTABLES -t mangle -I internet 1 -m mac --mac-source $mac -j RETURN

# Disable a mac
# $IPTABLES -t mangle -D internet -m mac --mac-source $mac -j RETURN


##v2.0
# internet_in chain will see downlink packets
$IPTABLES -t mangle -N internet_in
# internet_out chain will see uplink packets
$IPTABLES -t mangle -N internet_out

# Intercept uplink packets in prerouting from iface wlan0 and send them to internet_out chain
$IPTABLES -t mangle -A PREROUTING -i wlan0 -j internet_out

# Intercept downlink packets after routing going to iface wlan0 and send them to internet_in
$IPTABLES -t mangle -A POSTROUTING -o wlan0 -j internet_in

# Mark non authorized outgoing traffic with 99
$IPTABLES -t mangle -A internet_out -j MARK --set-mark 99

#Enable ssh for ott0disk
$IPTABLES -t mangle -I internet_out 1 -p tcp --dport 22 -m mac --mac-source c4:8e:8f:f8:e4:37 -j RETURN

# Set 10MB quota per IP
$IPTABLES -t mangle -A internet_in -d 192.168.0.67 -m quota ! --quota 10000000 -j DROP

