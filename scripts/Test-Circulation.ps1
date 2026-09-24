param(
    [string]$BaseUrl = 'http://127.0.0.1:18080'
)

$ErrorActionPreference = 'Stop'
$suffix = [Guid]::NewGuid().ToString('N')

$specification = Invoke-RestMethod -Uri "$BaseUrl/v3/api-docs"
if ($specification.info.title -ne 'Library API' -or
        $specification.paths.'/api/loans'.post.operationId -ne 'checkout') {
    throw 'OpenAPI metadata or circulation documentation is missing.'
}
$swaggerConfiguration = Invoke-RestMethod -Uri "$BaseUrl/v3/api-docs/swagger-config"
if ($swaggerConfiguration.url -ne '/v3/api-docs') {
    throw 'Swagger UI is not configured to use the local API specification.'
}
$swaggerPage = Invoke-WebRequest -Uri "$BaseUrl/swagger-ui.html"
if ($swaggerPage.StatusCode -ne 200 -or $swaggerPage.Content -notmatch 'swagger-ui-bundle.js') {
    throw 'Swagger UI did not serve its application page.'
}
Write-Output 'Swagger smoke test passed: OpenAPI document, UI configuration, and UI page.'

function Invoke-Api([string]$Method, [string]$Path, $Payload = $null) {
    $parameters = @{ Method = $Method; Uri = "$BaseUrl$Path" }
    if ($null -ne $Payload) {
        $parameters.ContentType = 'application/json'
        $parameters.Body = $Payload | ConvertTo-Json -Depth 5
    }
    Invoke-RestMethod @parameters
}

$member = Invoke-Api POST '/api/members' @{
    cardNumber = "SMOKE-$suffix"; name = 'Smoke Test Member'; email = 'smoke@example.org'
}
$book = Invoke-Api POST '/api/books' @{
    title = 'Circulation smoke test'; author = 'Test Author'; isbn = "smoke-$suffix"
}
$copy = Invoke-Api POST "/api/books/$($book.id)/copies" @{
    barcode = "SMOKE-$suffix"; shelfLocation = 'TEST-1'
}
$loanId = Invoke-Api POST '/api/loans' @{ isbn = $book.isbn; memberId = $member.id; copyId = $copy.id }
$loan = Invoke-Api GET "/api/loans/$loanId"
if ($loan.memberId -ne $member.id -or $loan.copyId -ne $copy.id -or -not $loan.dueDate) {
    throw 'Checkout did not persist the expected member, copy, and due date.'
}
$renewed = Invoke-Api POST "/api/loans/$loanId/renew"
if ($renewed.renewalCount -ne 1 -or [datetime]$renewed.dueDate -le [datetime]$loan.dueDate) {
    throw 'Renewal did not extend the due date.'
}
Invoke-Api PATCH "/api/loans/$loanId" @{ returned = $true } | Out-Null
$history = Invoke-Api GET "/api/members/$($member.id)/loans"
if ($history.totalElements -ne 1 -or -not $history.content[0].returned -or -not $history.content[0].returnedDate) {
    throw 'Member history did not record the return.'
}
$available = Invoke-Api GET "/api/books/$($book.id)/copies?status=AVAILABLE"
if ($available.totalElements -ne 1) { throw 'Returned copy is not available.' }
Write-Output 'Circulation smoke test passed: member, inventory, checkout, renewal, return, and history.'
