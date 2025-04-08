function startScan() {
    const target = document.getElementById("target").value;
    const startPort = parseInt(document.getElementById("startPort").value);
    const endPort = parseInt(document.getElementById("endPort").value);
    const scanType = document.getElementById("scanType").value;
    const output = document.getElementById("scannerOutput");

    output.textContent = "Scanning...";

    let url = '';
    if(scanType === 'full') {
      url = `/api/scanner/full?target=${target}&startPort=${startPort}&endPort=${endPort}`;
    } else {  
       url =`/api/scanner/${scanType}?target=${target}&port=${startPort}`;
    }

    fetch(url)
      .then(res => res.json())
      .then(data => {
        output.textContent = JSON.stringify(data, null, 2); // still show raw result
      
        // Set OS info
        const osSpan = document.getElementById("osResult");
        osSpan.textContent = data.os || "-";
      
        // Fill table
        const tableBody = document.querySelector("#scanTable tbody");
        tableBody.innerHTML = ""; // clear old rows
      
        const ports = data.ports || {};
        for (const key in ports) {
          const row = ports[key];
          const tr = document.createElement("tr");
      
          const cleanService = (row.service || "").replace(/[\u0000-\u001F]/g, "").trim();
      
          tr.innerHTML = `
            <td>${row.port}</td>
            <td>${cleanService || "N/A"}</td>
            <td>${row.status}</td>
          `;
          tableBody.appendChild(tr);
        }
      })
      
  }