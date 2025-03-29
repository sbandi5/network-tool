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
            const log = data.map(p => 
                `[${p.timestamp}] ${p.sourceIP} → ${p.destIP} | ${p.protocol} | Length: ${p.length}`
            ).join("\n\n");

            resultsDiv.textContent = log || "No packets found.";
        });
}
