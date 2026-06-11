# Free a port held by a stale process (default 8080).
#
# Why: when the JVM runs out of memory or exits abnormally, the backend can keep
# holding the port, so the next start fails with "Port 8080 was already in use".
# This script finds the listening process and force-kills it.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts\free-port.ps1
#   powershell -ExecutionPolicy Bypass -File scripts\free-port.ps1 -Port 9090

param(
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'

function Get-ListenerPids([int]$p) {
    try {
        return Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction Stop |
            Select-Object -ExpandProperty OwningProcess -Unique
    } catch {
        return netstat -ano |
            Select-String ":$p\s" |
            Select-String 'LISTENING' |
            ForEach-Object { ($_ -split '\s+')[-1] } |
            Sort-Object -Unique
    }
}

$pids = @(Get-ListenerPids $Port)

if ($pids.Count -eq 0) {
    Write-Host "Port $Port is free, nothing to do." -ForegroundColor Green
    exit 0
}

foreach ($processId in $pids) {
    try {
        $proc = Get-Process -Id $processId -ErrorAction Stop
        Write-Host ("Killing PID={0} ({1}) -> free port {2}" -f $processId, $proc.ProcessName, $Port) -ForegroundColor Yellow
        Stop-Process -Id $processId -Force
    } catch {
        Write-Host ("Failed to kill PID={0}: {1}" -f $processId, $_.Exception.Message) -ForegroundColor Red
    }
}

Start-Sleep -Milliseconds 600

if (@(Get-ListenerPids $Port).Count -eq 0) {
    Write-Host "Port $Port released." -ForegroundColor Green
} else {
    Write-Host "Port $Port still in use, retry as Administrator." -ForegroundColor Red
    exit 1
}
