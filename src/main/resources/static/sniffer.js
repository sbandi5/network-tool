function togglePortInput() {
    const mode = document.getElementById("mode").value;
    const portDiv = document.getElementById("portInput");
    portDiv.style.display = (mode === "port") ? "block" : "none";
  }

  function startSniffer() {
    const mode = document.getElementById("mode").value;
    let url = "http://localhost:8080/api/sniffer/start";

    if (mode === "port") {
      const port = document.getElementById("port").value;
      url += `?port=${port}`;
    }

    fetch(url, { method: "POST" })
      .then(res => res.text())
      .then(msg => {
        document.getElementById("packetLog").textContent = msg;
      });
  }

  function stopSniffer() {
    fetch("http://localhost:8080/api/sniffer/stop", {
      method: "POST"
    })
      .then(res => res.text())
      .then(msg => {
        document.getElementById("packetLog").textContent = msg;
      });
  }

  function fetchPackets() {
    const filter = prompt("Enter protocol to filter (TCP, UDP, ICMP), or leave blank:");
    const resultsDiv = document.getElementById("packetLog");
    let url = "http://localhost:8080/api/sniffer/packets";
    if (filter) {
        url += "?protocol=" + filter.toUpperCase();
    }

    fetch(url)
        .then(res => res.json())
        .then(data => {
          const log = data.map(p => {
            let line = `[${p.timestamp}] ${p.sourceIP}:${p.src_port || ''} → ${p.destIP}:${p.dest_port || ''}`;
            line += ` | Protocol: ${p.protocol}`;
            if (p.payload) line += ` | Payload (hex): ${p.payload}`;
            return line;
          }).join("\n\n");
          

            resultsDiv.textContent = log || "No packets found.";
        });
}
function startLive() {
  const logBox = document.getElementById("liveLog");
  const socket = new WebSocket("ws://localhost:8080/ws/packets");

  socket.onmessage = function (event) {
      const pkt = JSON.parse(event.data);
      const line = `[${pkt.protocol}] ${pkt.source_ip}:${pkt.src_port || ''} → ${pkt.dest_ip}:${pkt.dest_port || ''}`;
      const div = document.createElement("div");
      div.textContent = line;
      logBox.appendChild(div);
      logBox.scrollTop = logBox.scrollHeight;
  };

  socket.onopen = () => console.log("Live WebSocket connected");
  socket.onclose = () => console.log("WebSocket closed");
}
