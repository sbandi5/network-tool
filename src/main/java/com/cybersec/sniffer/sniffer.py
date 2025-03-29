import socket
import struct
import json
import sys

def parse_ip_header(data):
    version_header_len = data[0]
    header_len = (version_header_len & 15) * 4
    proto = data[9]
    src = socket.inet_ntoa(data[12:16])
    dst = socket.inet_ntoa(data[16:20])
    return header_len, proto, src, dst

def sniff():
    try:
        s = socket.socket(socket.AF_PACKET, socket.SOCK_RAW, socket.ntohs(3))  # Ethernet frame
    except PermissionError:
        print("Must run as root")
        sys.exit(1)

    while True:
        raw_data, addr = s.recvfrom(65535)

        # IP Packet starts after Ethernet header (14 bytes)
        eth_proto = struct.unpack('!H', raw_data[12:14])[0]

        if eth_proto == 0x0800:  # IPv4
            ip_header_len, proto, src_ip, dst_ip = parse_ip_header(raw_data[14:34])
            proto_name = {1: "ICMP", 6: "TCP", 17: "UDP"}.get(proto, f"OTHER({proto})")

            packet_info = {
                "source_ip": src_ip,
                "dest_ip": dst_ip,
                "protocol": proto_name,
                "length": len(raw_data)
            }

            print(json.dumps(packet_info), flush=True)

if __name__ == '__main__':
    sniff()
