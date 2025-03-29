import socket
import struct
import json
import sys

def ethernet_frame(data):
    dest_mac, src_mac, proto = struct.unpack('!6s6sH', data[:14])
    return socket.ntohs(proto), data[14:]

def ipv4_packet(data):
    version_header_len = data[0]
    header_length = (version_header_len & 15) * 4
    ttl, proto, src, target = struct.unpack('!8xBB2x4s4s', data[:20])
    return {
        'version': version_header_len >> 4,
        'header_length': header_length,
        'ttl': ttl,
        'protocol': proto,
        'source_ip': socket.inet_ntoa(src),
        'dest_ip': socket.inet_ntoa(target),
        'data': data[header_length:]
    }

def tcp_segment(data):
    src_port, dest_port, sequence, ack, offset_reserved_flags = struct.unpack('!HHLLH', data[:14])
    offset = (offset_reserved_flags >> 12) * 4
    flags = offset_reserved_flags & 0x3F
    return {
        'src_port': src_port,
        'dest_port': dest_port,
        'sequence': sequence,
        'flags': flags,
        'header_length': offset,
        'data': data[offset:]
    }

def udp_segment(data):
    src_port, dest_port, size = struct.unpack('!HHH2x', data[:8])
    return {
        'src_port': src_port,
        'dest_port': dest_port,
        'length': size,
        'data': data[8:]
    }

def sniff():
    try:
        s = socket.socket(socket.AF_PACKET, socket.SOCK_RAW, socket.ntohs(3))
    except PermissionError:
        print("Run as root", file=sys.stderr)
        sys.exit(1)

    while True:
        raw_data, _ = s.recvfrom(65535)

        eth_proto, ip_data = ethernet_frame(raw_data)
        if eth_proto == 8:  # IPv4
            ip = ipv4_packet(ip_data)
            protocol = ip['protocol']
            payload = ""

            pkt = {
                'source_ip': ip['source_ip'],
                'dest_ip': ip['dest_ip'],
                'ttl': ip['ttl'],
                'protocol': {1: "ICMP", 6: "TCP", 17: "UDP"}.get(protocol, str(protocol))
            }

            if protocol == 6:  # TCP
                tcp = tcp_segment(ip['data'])
                pkt.update({
                    'src_port': tcp['src_port'],
                    'dest_port': tcp['dest_port'],
                    'sequence': tcp['sequence'],
                    'flags': tcp['flags'],
                    'payload': tcp['data'][:40].hex()  # First 40 bytes as hex
                })

            elif protocol == 17:  # UDP
                udp = udp_segment(ip['data'])
                pkt.update({
                    'src_port': udp['src_port'],
                    'dest_port': udp['dest_port'],
                    'length': udp['length'],
                    'payload': udp['data'][:40].hex()
                })

            print(json.dumps(pkt), flush=True)

if __name__ == '__main__':
    sniff()
