function displayContainer() {
    var scanType = document.getElementById("scanType").value;
    console.log(scanType);
    console.log(scanType === "full");
    if (scanType == "full") {
        document.getElementById("range_input").style.display = "flex";
        document.getElementById("single_input").style.display = "none";
    } else {
        document.getElementById("single_input").style.display = "block";
        document.getElementById("range_input").style.display = "none";
    }
}
async function startScan() {
    let target = document.getElementById("target").value;
    let port = parseInt(document.getElementById("port").value);
    let scanType = document.getElementById("scanType").value;
    let resultsDiv = document.getElementById("results");
    let startPort = parseInt(document.getElementById("startPort").value);
    let endPort = parseInt(document.getElementById("endPort").value);
    resultsDiv.textContent = "Scanning...";


    if (scanType !== null) {

        if (scanType === "full") {
            try {
                let response = await fetch(`/api/scanner/full?target=${target}&startPort=${startPort}&endPort=${endPort}`, {
                    method: 'GET'
                });
                let results = await response.json();
                resultsDiv.textContent = "Scan Results: " + JSON.stringify(results, null, 2);

            } catch (error) {
                resultsDiv.textContent = "Error: " + error;
            }

        } else if (scanType === "exploit") {
            try {
                let response = await fetch(`/api/exploit?target=${target}&port=${port}`,{
                    method: 'POST',
                });
                console.log("response is: "+response.json());
                let result = await response.json();
                resultsDiv.textContent = "Exploit Result: " + JSON.stringify(result, null, 2);
            } catch (error) {
                resultsDiv.textContent = "Error: " + error;
            }
        } else {
            console.log("Scan type: " + scanType + " Target: " + target + " Port: " + port);
            let response = await fetch(`/api/scanner/${scanType}?target=${target}&port=${port}`,{
                method: 'GET'
            });

            let result = await response.json();
            resultsDiv.textContent = "Scan Results: " + JSON.stringify(result, null, 2);
        }
    }
}
