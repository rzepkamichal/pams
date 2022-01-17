 param (
    [int]$clients = 1
 )

New-Item -ItemType Directory -Force -Path .\log-clients

for ($num = 1; $num -le $clients; $num++) {

    $outPath = ".\log-clients\console" + $num + ".out"
    $errPath = ".\log-clients\console" + $num + ".err"

    Start-Process -NoNewWindow java -ArgumentList '-jar', 'jars\WoCoClient.jar', 'localhost', '25000', '16', '4', '1' `
    -RedirectStandardOutput $outPath -RedirectStandardError $errPath
}
