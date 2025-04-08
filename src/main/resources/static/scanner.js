import ApiCalls from "./ApiCalls";
const apiCalls = new ApiCalls();
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
        output.textContent = JSON.stringify(data, null, 2);
      })
      .catch(err => {
        output.textContent = "Error: " + err;
      });
  }