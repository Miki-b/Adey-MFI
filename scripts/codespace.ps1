<#
.SYNOPSIS
  Drive this repo's GitHub Codespace from Windows with the GitHub CLI (gh).

.DESCRIPTION
  .\scripts\codespace.ps1 start     create the Codespace if there is none, else wake it up; wait until the stack is READY
  .\scripts\codespace.ps1 stop      STOP it (not delete): no more compute billing, files and data are kept
  .\scripts\codespace.ps1 status    state, stack status, memory and containers inside
  .\scripts\codespace.ps1 logs      last startup log lines (add -Backend for the backend log)
  .\scripts\codespace.ps1 url       the backend URL (port 8080) and its visibility
  .\scripts\codespace.ps1 public    make port 8080 PUBLIC (anyone with the URL) - only for a short test
  .\scripts\codespace.ps1 private   make port 8080 private again (GitHub login needed)

  Needs: gh logged in with the "codespace" scope (gh auth refresh -h github.com -s codespace).
  Cost: 2-core machine = 2 core-hours per hour. It also auto-stops after 30 minutes idle.
#>
param(
    [Parameter(Position = 0)]
    [ValidateSet('start', 'stop', 'status', 'logs', 'url', 'public', 'private')]
    [string]$Command = 'status',
    [switch]$Backend,
    [int]$Lines = 60
)

$ErrorActionPreference = 'Stop'
$Repo = 'Miki-b/Adey-MFI'
$Machine = 'basicLinux32gb'   # 2 cores, 8 GB
$IdleTimeout = '30m'
$Workdir = '/workspaces/Adey-MFI'

function Get-Codespace {
    $json = gh codespace list -R $Repo --json name,state,machineName,lastUsedAt | Out-String
    $list = @($json | ConvertFrom-Json)
    if ($list.Count -eq 0) { return $null }
    return $list | Sort-Object lastUsedAt -Descending | Select-Object -First 1
}

function Require-Codespace {
    $cs = Get-Codespace
    if (-not $cs) { throw "No Codespace for $Repo. Run: .\scripts\codespace.ps1 start" }
    return $cs
}

# Runs a shell command inside the Codespace (this also wakes a stopped Codespace).
function Invoke-Remote([string]$Name, [string]$Cmd) {
    gh codespace ssh -c $Name -- "cd $Workdir && $Cmd"
}

function Get-Port8080([string]$Name) {
    $ports = @(gh codespace ports -c $Name --json sourcePort,browseUrl,visibility,label | Out-String | ConvertFrom-Json)
    return $ports | Where-Object { $_.sourcePort -eq 8080 } | Select-Object -First 1
}

switch ($Command) {
    'start' {
        $cs = Get-Codespace
        if (-not $cs) {
            Write-Host "Creating Codespace for $Repo ($Machine, idle timeout $IdleTimeout)..."
            $name = gh codespace create -R $Repo -b main -m $Machine --idle-timeout $IdleTimeout
            Write-Host "Created: $name"
        } else {
            $name = $cs.name
            Write-Host "Codespace $name is $($cs.state); waking it up (if needed)..."
        }
        $deadline = (Get-Date).AddMinutes(20)
        $shown = 0
        while ((Get-Date) -lt $deadline) {
            $out = Invoke-Remote $name "cat logs/stack.status 2>/dev/null; echo '---'; cat logs/startup.log 2>/dev/null" 2>$null
            $text = ($out | Out-String) -split "---", 2
            $status = $text[0].Trim()
            $logLines = @(($text[1] -split "`n") | Where-Object { $_.Trim() -ne '' })
            if ($logLines.Count -gt $shown) { $logLines[$shown..($logLines.Count - 1)] | ForEach-Object { Write-Host "  $_" }; $shown = $logLines.Count }
            if ($status -eq 'READY') { Write-Host "Stack READY."; break }
            if ($status -like 'FAILED*') { throw "Stack failed: $status" }
            Start-Sleep -Seconds 15
        }
        & $PSCommandPath url
    }
    'stop' {
        $cs = Require-Codespace
        gh codespace stop -c $cs.name
        Start-Sleep -Seconds 3
        $after = Get-Codespace
        Write-Host "Codespace $($after.name): $($after.state) (stopped, not deleted)"
    }
    'status' {
        $cs = Get-Codespace
        if (-not $cs) { Write-Host "No Codespace for $Repo."; break }
        Write-Host "Codespace $($cs.name): $($cs.state) on $($cs.machineName), last used $($cs.lastUsedAt)"
        if ($cs.state -ne 'Available') { Write-Host "(not running: 'start' wakes it up)"; break }
        Invoke-Remote $cs.name "echo stack: `$(cat logs/stack.status 2>/dev/null); free -h | head -2; docker ps --format '{{.Names}}  {{.Status}}  {{.Ports}}'"
    }
    'logs' {
        $cs = Require-Codespace
        $file = if ($Backend) { 'logs/backend.log' } else { 'logs/startup.log' }
        Invoke-Remote $cs.name "tail -n $Lines $file"
    }
    'url' {
        $cs = Require-Codespace
        $p = Get-Port8080 $cs.name
        if ($p) { Write-Host "Adey backend: $($p.browseUrl)  (visibility: $($p.visibility))" }
        else { Write-Host "Port 8080 is not forwarded yet (is the stack READY?)" }
    }
    'public' {
        $cs = Require-Codespace
        Write-Warning "Port 8080 becomes reachable by ANYONE with the URL. Run 'private' as soon as your test is done."
        gh codespace ports visibility 8080:public -c $cs.name
        & $PSCommandPath url
    }
    'private' {
        $cs = Require-Codespace
        gh codespace ports visibility 8080:private -c $cs.name
        & $PSCommandPath url
    }
}
