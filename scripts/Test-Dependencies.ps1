param(
    [string]$TreeFile = 'dependencies.json'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$packages = @{}

function Add-Dependency($node) {
    $name = "$($node.groupId):$($node.artifactId)"
    $key = "$name@$($node.version)"
    $packages[$key] = @{ package = @{ ecosystem = 'Maven'; name = $name }; version = $node.version }
    foreach ($child in $node.children) { Add-Dependency $child }
}

foreach ($module in @('primeiroteste', 'primeiro-projeto-rest', 'library-api')) {
    $path = Join-Path $root "$module/target/$TreeFile"
    $tree = Get-Content -LiteralPath $path -Raw | ConvertFrom-Json
    if (-not $tree.artifactId) { throw "Invalid dependency tree: $path" }
    foreach ($child in $tree.children) { Add-Dependency $child }
}

if ($packages.Count -eq 0) { throw 'No dependencies found; generate the Maven dependency trees first.' }
$queries = @($packages.GetEnumerator() | Sort-Object Name | ForEach-Object Value)
$body = @{ queries = $queries } | ConvertTo-Json -Depth 10
$response = Invoke-RestMethod -Method Post -Uri 'https://api.osv.dev/v1/querybatch' -ContentType 'application/json' -Body $body
if ($response.results.Count -ne $queries.Count) { throw 'Incomplete response from OSV.' }
$findings = @(
    for ($i = 0; $i -lt $queries.Count; $i++) {
        foreach ($vuln in $response.results[$i].vulns) {
            [PSCustomObject]@{
                package = $queries[$i].package.name
                version = $queries[$i].version
                advisory = $vuln.id
            }
        }
    }
)
$reportDir = Join-Path $root 'target'
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
@{
    checkedAt = (Get-Date).ToUniversalTime().ToString('o')
    source = 'https://api.osv.dev/v1/querybatch'
    dependencyCount = $queries.Count
    findings = $findings
} | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $reportDir "osv-$TreeFile") -Encoding utf8
Write-Output "Checked $($queries.Count) unique Maven dependency versions (including test scope)."
if ($findings.Count -gt 0) {
    $findings | Format-Table -AutoSize
    throw "OSV reported $($findings.Count) affected dependency/advisory pairs."
}
Write-Output 'No known vulnerabilities reported by OSV.'
