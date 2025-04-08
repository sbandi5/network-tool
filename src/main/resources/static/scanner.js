function startScan() {
    const target = document.getElementById("target").value;
    const startPort = parseInt(document.getElementById("startPort").value);
    const endPort = parseInt(document.getElementById("endPort").value);
    const scanType = document.getElementById("scanType").value;
    const output = document.getElementById("scannerOutput");
    const tableBody = document.querySelector("#scanTable tbody");

    // Clear previous results
    output.textContent = "Scanning...";
    tableBody.innerHTML = "";

    let url = '';
    if(scanType === 'full') {
        url = `/api/scanner/full?target=${target}&startPort=${startPort}&endPort=${endPort}`;
    } else {  
        url =`/api/scanner/${scanType}?target=${target}&port=${startPort}`;
    }

    fetch(url)
        .then(res => res.json())
        .then(data => {
            // Display raw output
            output.textContent = JSON.stringify(data, null, 2);

            // Set OS info
            const osSpan = document.getElementById("osResult");
            osSpan.textContent = "Completed";

            // Handle the ports data
            if (data && data.ports) {
                Object.entries(data.ports).forEach(([port, info]) => {
                    const tr = document.createElement("tr");
                    const status = info.status || "Unknown";
                    const service = (info.service || "").replace(/[\u0000-\u001F]/g, "").trim() || "N/A";
                    
                    tr.innerHTML = `
                        <td>${port}</td>
                        <td>${service}</td>
                        <td>${status}</td>
                    `;
                    tableBody.appendChild(tr);
                });
            }
        })
        .catch(error => {
            output.textContent = `Error: ${error.message}`;
            console.error('Scan error:', error);
        });
}