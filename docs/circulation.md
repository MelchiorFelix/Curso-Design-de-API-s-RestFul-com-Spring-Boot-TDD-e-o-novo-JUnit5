# Circulation walkthrough

Start the application, then run the following in PowerShell 7. The example
creates a member, a title, and two physical copies; captures generated IDs;
checks out one copy; renews it; and records its return.

```powershell
$baseUrl = 'http://localhost:8080'

function Invoke-LibraryApi($method, $path, $payload = $null) {
    $parameters = @{ Method = $method; Uri = "$baseUrl$path" }
    if ($null -ne $payload) {
        $parameters.ContentType = 'application/json'
        $parameters.Body = $payload | ConvertTo-Json
    }
    Invoke-RestMethod @parameters
}

$member = Invoke-LibraryApi POST '/api/members' @{
    cardNumber = 'LIB-0001'
    name = 'Sam Taylor'
    email = 'sam@example.org'
}

$book = Invoke-LibraryApi POST '/api/books' @{
    title = 'The Adventures'
    author = 'Alex Smith'
    isbn = '9780000000001'
}

$firstCopy = Invoke-LibraryApi POST "/api/books/$($book.id)/copies" @{
    barcode = 'COPY-0001'
    shelfLocation = 'FICTION-A1'
}

$secondCopy = Invoke-LibraryApi POST "/api/books/$($book.id)/copies" @{
    barcode = 'COPY-0002'
    shelfLocation = 'FICTION-A1'
}

$loanId = Invoke-LibraryApi POST '/api/loans' @{
    isbn = $book.isbn
    memberId = $member.id
    copyId = $firstCopy.id
}

Invoke-LibraryApi GET "/api/loans/$loanId"
Invoke-LibraryApi GET "/api/books/$($book.id)/copies?status=AVAILABLE"
Invoke-LibraryApi POST "/api/loans/$loanId/renew"
Invoke-LibraryApi PATCH "/api/loans/$loanId" @{ returned = $true }
Invoke-LibraryApi GET "/api/members/$($member.id)/loans"
Invoke-LibraryApi GET '/api/loans/overdue?sort=dueDate,asc'
```

The second copy remains available throughout the first copy's loan. Omitting
`copyId` automatically allocates an available copy of the requested ISBN.
Examples use fixed unique identifiers, so run them once per fresh database or
change the card number, ISBN, and barcodes.

To suspend and reactivate the member:

```powershell
Invoke-LibraryApi PATCH "/api/members/$($member.id)" @{ active = $false }
Invoke-LibraryApi PATCH "/api/members/$($member.id)" @{ active = $true }
```

To withdraw an available copy:

```powershell
Invoke-LibraryApi POST "/api/copies/$($secondCopy.id)/withdraw"
```

## Compatibility with the earlier API

- Checkout now requires `memberId` instead of a free-text `customer`. Register
  the member and at least one copy before checkout. The numeric loan-ID response
  is preserved.
- Loan responses retain `customer`, `isbn`, and `book`, and add `memberId`,
  `copyId`, `barcode`, `loanDate`, `dueDate`, `returnedDate`, `returned`,
  and `renewalCount`. The customer name is a historical snapshot.
- Loan filters now combine with AND. Previously ISBN and customer combined with
  OR, and an unfiltered request did not list all loans.
- `PATCH /api/loans/{id}` accepts only `{"returned":true}`. Reopening a returned
  loan is not supported; create a new checkout so each borrowing has its own
  history.
- `PUT /api/books/{id}` now consumes validated JSON. Send title, author, and ISBN;
  only title and author are updated. The ISBN remains the catalog identifier.
- The schema adds members, copies, and required loan associations/dates, plus an
  ISBN uniqueness constraint. The default in-memory H2 database starts fresh.
  An externally configured persistent database needs an explicit data migration:
  backfill members, one physical copy per historical loan's actual item, due
  dates, returned flags, and inventory status before applying the constraints.
  This change does not provide a migration for external databases.

## Implementation notes

Dates are generated on the server from an injectable clock in the configured
time zone. A due date is inclusive. Renewals extend the existing due date; limits
and suspension/overdue restrictions apply without staff overrides.

Circulation changes run in database transactions. Checkout and loan changes
acquire member locks before book locks. Copy registration/withdrawal and catalog
deletion also acquire the book lock. This serializes allocation for the same
title and active-loan checks for the same member. Database unique constraints
protect card numbers, barcodes, and ISBNs.

Repeated returns are idempotent, including when the returned copy is already on
a later loan. Records with inventory/history are retained instead of deleting
the data needed to reconstruct past borrowing. There are no member/copy deletion
endpoints.

Reservations, fines, notifications, staff roles, and production database
provisioning are outside this core circulation implementation.
