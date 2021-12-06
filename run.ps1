 param (
    [int]$clients = 1
 )

New-Item -ItemType Directory -Force -Path .\log

for ($num = 1; $num -le $clients; $num++) {

    $outPath = ".\log\console" + $num + ".out"
    $errPath = ".\log\console" + $num + ".err"

    Start-Process -NoNewWindow java -ArgumentList '-jar', 'jars\WoCoClient.jar', 'localhost', '8080', '20', '2', '1' `
    -RedirectStandardOutput $outPath -RedirectStandardError $errPath
}
