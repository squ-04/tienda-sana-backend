# Prueba de humo HTTP: orden coherente (publico -> login -> JWT -> rutas protegidas).
# Requiere: backend http://localhost:8080 y Mongo con datos minimos (cuentas admin/cliente).
# Windows PowerShell 5.1: powershell -ExecutionPolicy Bypass -File scripts/api-smoke-test.ps1

$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$script:passed = 0
$script:failed = 0

function Step {
    param([string]$Name, [scriptblock]$Action)
    try {
        & $Action
        Write-Host "[OK]  $Name" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "[FAIL] $Name - $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

function Assert-NoError {
    param($Msg)
    if ($null -eq $Msg) { throw 'Respuesta vacia' }
    if ($Msg.PSObject.Properties['error'] -and $Msg.error -eq $true) {
        throw "error=true reply=$($Msg.reply | ConvertTo-Json -Compress -Depth 4)"
    }
}

function Headers-Auth([string]$Token) {
    return @{ Authorization = "Bearer $Token" }
}

Write-Host ''
Write-Host "=== API smoke test ($base) ===" -ForegroundColor Cyan
Write-Host ''

# --- Publico (sin criterios vacios: el backend exige al menos un filtro) ---
Step 'GET /api/public/productos/get-all/1' {
    $r = Invoke-RestMethod -Uri "$base/api/public/productos/get-all/1" -Method Get
    Assert-NoError $r
}
Step 'GET /api/public/mesas/get-all/1' {
    $r = Invoke-RestMethod -Uri "$base/api/public/mesas/get-all/1" -Method Get
    Assert-NoError $r
}
Step 'GET /api/public/productos/get-types' {
    $r = Invoke-RestMethod -Uri "$base/api/public/productos/get-types" -Method Get
    Assert-NoError $r
}
Step 'GET /api/public/mesas/get-locality' {
    $r = Invoke-RestMethod -Uri "$base/api/public/mesas/get-locality" -Method Get
    Assert-NoError $r
}
Step 'POST /api/public/productos/filter-products (criterio nombre)' {
    $body = '{"nombre":"Ens","cantidad":0,"categoria":"","pagina":0}'
    $r = Invoke-RestMethod -Uri "$base/api/public/productos/filter-products" -Method Post -ContentType 'application/json' -Body $body
    Assert-NoError $r
}
Step 'POST /api/public/mesas/filter-tables (criterio nombre)' {
    $body = '{"nombre":"Mesa","capacidad":0,"localidad":"","pagina":0}'
    $r = Invoke-RestMethod -Uri "$base/api/public/mesas/filter-tables" -Method Post -ContentType 'application/json' -Body $body
    Assert-NoError $r
}
Step 'POST /api/public/venta/receive-notification' {
    $r = Invoke-RestMethod -Uri "$base/api/public/venta/receive-notification" -Method Post -ContentType 'application/json' -Body '{}'
    Assert-NoError $r
}
Step 'POST /api/public/reserva/receive-notification' {
    $r = Invoke-RestMethod -Uri "$base/api/public/reserva/receive-notification" -Method Post -ContentType 'application/json' -Body '{}'
    Assert-NoError $r
}

# --- Auth (tokens para rutas siguientes) ---
$script:adminToken = $null
$script:clienteToken = $null
Step 'POST /api/auth/login (ADMIN)' {
    $body = '{"email":"admin@tiendasana.local","contrasenia":"Admin123!"}'
    $r = Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -ContentType 'application/json' -Body $body
    Assert-NoError $r
    $script:adminToken = $r.reply.token
    if (-not $script:adminToken) { throw 'Sin token admin' }
}
Step 'POST /api/auth/login (CLIENTE)' {
    $body = '{"email":"cliente.demo@tiendasana.local","contrasenia":"Cliente123!"}'
    $r = Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -ContentType 'application/json' -Body $body
    Assert-NoError $r
    $script:clienteToken = $r.reply.token
    if (-not $script:clienteToken) { throw 'Sin token cliente' }
}

$HAdmin = Headers-Auth $script:adminToken
$HCliente = Headers-Auth $script:clienteToken

# Id de producto real (catalogo admin; el listado publico puede estar vacio si no hay stock)
$script:seedProductId = $null
Step 'GET /api/admin/products (semilla para get-info y carrito)' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/products" -Method Get -Headers $HAdmin
    Assert-NoError $r
    $arr = $r.reply
    if (-not $arr -or $arr.Count -eq 0) { throw 'No hay productos en Mongo para pruebas' }
    $script:seedProductId = $arr[0].id
}

Step "GET /api/public/productos/get-info/$($script:seedProductId)" {
    $r = Invoke-RestMethod -Uri "$base/api/public/productos/get-info/$($script:seedProductId)" -Method Get
    Assert-NoError $r
}

# --- Admin ---
Step 'GET /api/admin/suppliers' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/suppliers" -Method Get -Headers $HAdmin
    Assert-NoError $r
}
Step 'GET /api/admin/tables' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/tables" -Method Get -Headers $HAdmin
    Assert-NoError $r
}
Step 'GET /api/admin/lots' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/lots" -Method Get -Headers $HAdmin
    Assert-NoError $r
}
Step 'GET /api/admin/inventory' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/inventory" -Method Get -Headers $HAdmin
    Assert-NoError $r
}

$script:supplierId = $null
$script:lotId = $null
$script:tableId = $null
$script:newProductId = $null

Step 'POST /api/admin/suppliers' {
    $body = @{
        category = 'E2E'
        name     = "Proveedor E2E $(Get-Date -Format 'HHmmss')"
        product  = 'Insumo prueba'
        contact  = '3000000000'
        address  = 'Calle 1'
        city     = 'Quindio'
    } | ConvertTo-Json -Compress
    $r = Invoke-RestMethod -Uri "$base/api/admin/suppliers" -Method Post -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
    $script:supplierId = $r.reply.id
}

Step 'POST /api/admin/products' {
    $body = @{
        name        = "Producto E2E $(Get-Date -Format 'HHmmss')"
        description = 'Desc prueba'
        category    = 'Ensaladas'
        price       = 1000
        imageUrl    = 'https://placehold.co/64x64/e9ecef/198754?text=E2E'
        outOfStock  = $false
    } | ConvertTo-Json -Compress
    $r = Invoke-RestMethod -Uri "$base/api/admin/products" -Method Post -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
    $script:newProductId = $r.reply.id
}

Step 'POST /api/admin/tables' {
    $body = '{"capacity":2,"location":"E2E","active":true}'
    $r = Invoke-RestMethod -Uri "$base/api/admin/tables" -Method Post -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
    $script:tableId = $r.reply.id
}

Step 'PUT /api/admin/products/{id}' {
    $body = @{
        name        = 'Producto E2E actualizado'
        description = 'Desc'
        category    = 'Ensaladas'
        price       = 1500
        imageUrl    = 'https://placehold.co/64x64/e9ecef/198754?text=E2E'
        outOfStock  = $false
    } | ConvertTo-Json -Compress
    $r = Invoke-RestMethod -Uri "$base/api/admin/products/$($script:newProductId)" -Method Put -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
}

Step 'PATCH /api/admin/products/{id}/status' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/products/$($script:newProductId)/status" -Method Patch -Headers $HAdmin
    Assert-NoError $r
}

Step 'PUT /api/admin/tables/{id}' {
    $body = '{"capacity":4,"location":"E2E-2","active":true}'
    $r = Invoke-RestMethod -Uri "$base/api/admin/tables/$($script:tableId)" -Method Put -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
}

Step 'PATCH /api/admin/tables/{id}/status' {
    $body = '{"status":"RESERVED"}'
    $r = Invoke-RestMethod -Uri "$base/api/admin/tables/$($script:tableId)/status" -Method Patch -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
}

$today = (Get-Date).ToString('yyyy-MM-dd')
Step 'POST /api/admin/lots' {
    $body = @{
        productId   = $script:newProductId
        supplierId  = $script:supplierId
        entryDate   = $today
        quantity    = 5
        unitValue   = 100
    } | ConvertTo-Json -Compress
    $r = Invoke-RestMethod -Uri "$base/api/admin/lots" -Method Post -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
    $script:lotId = $r.reply.id
}

Step 'PUT /api/admin/lots/{id}' {
    $body = @{
        productId   = $script:newProductId
        supplierId  = $script:supplierId
        entryDate   = $today
        quantity    = 6
        unitValue   = 100
    } | ConvertTo-Json -Compress
    $r = Invoke-RestMethod -Uri "$base/api/admin/lots/$($script:lotId)" -Method Put -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
}

Step 'DELETE /api/admin/lots/{id}' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/lots/$($script:lotId)" -Method Delete -Headers $HAdmin
    Assert-NoError $r
}

Step 'DELETE /api/admin/products/{id}' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/products/$($script:newProductId)" -Method Delete -Headers $HAdmin
    Assert-NoError $r
}

Step 'DELETE /api/admin/suppliers/{id}' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/suppliers/$($script:supplierId)" -Method Delete -Headers $HAdmin
    Assert-NoError $r
}

Step 'PATCH /api/admin/suppliers/{id}/activate' {
    $r = Invoke-RestMethod -Uri "$base/api/admin/suppliers/$($script:supplierId)/activate" -Method Patch -ContentType 'application/json' -Body '{}' -Headers $HAdmin
    Assert-NoError $r
}

Step 'PUT /api/admin/tables/{id} (desactivar mesa E2E)' {
    $body = '{"capacity":4,"location":"E2E-2","active":false}'
    $r = Invoke-RestMethod -Uri "$base/api/admin/tables/$($script:tableId)" -Method Put -ContentType 'application/json' -Body $body -Headers $HAdmin
    Assert-NoError $r
}

# --- Cuenta (CLIENTE) ---
$clienteEmail = 'cliente.demo@tiendasana.local'
Step "GET /api/account/get/$clienteEmail" {
    $r = Invoke-RestMethod -Uri "$base/api/account/get/$clienteEmail" -Method Get -Headers $HCliente
    Assert-NoError $r
}

Step 'PUT /api/cliente/carrito/add-item' {
    $body = @{
        idUsuario   = $clienteEmail
        idProducto  = "$($script:seedProductId)"
        cantidad    = 1
    } | ConvertTo-Json -Compress
    $r = Invoke-RestMethod -Uri "$base/api/cliente/carrito/add-item" -Method Put -ContentType 'application/json' -Body $body -Headers $HCliente
    Assert-NoError $r
}

Step 'POST /api/cliente/venta/create' {
    $body = (@{ emailUsuario = $clienteEmail; idPromocion = $null } | ConvertTo-Json -Compress)
    $r = Invoke-RestMethod -Uri "$base/api/cliente/venta/create" -Method Post -ContentType 'application/json' -Body $body -Headers $HCliente
    Assert-NoError $r
}

Step 'DELETE /api/cliente/carrito/clear-all-items' {
    $r = Invoke-RestMethod -Uri "$base/api/cliente/carrito/clear-all-items" -Method Delete -Headers $HCliente
    Assert-NoError $r
}

Step "GET /api/cliente/carrito/get-items/$clienteEmail" {
    $r = Invoke-RestMethod -Uri "$base/api/cliente/carrito/get-items/$clienteEmail" -Method Get -Headers $HCliente
    Assert-NoError $r
}
Step "GET /api/cliente/gestor-reservas/get-items/$clienteEmail" {
    $r = Invoke-RestMethod -Uri "$base/api/cliente/gestor-reservas/get-items/$clienteEmail" -Method Get -Headers $HCliente
    Assert-NoError $r
}
Step "GET /api/cliente/venta/history/$clienteEmail" {
    $r = Invoke-RestMethod -Uri "$base/api/cliente/venta/history/$clienteEmail" -Method Get -Headers $HCliente
    Assert-NoError $r
}
Step "GET /api/cliente/reserva/history/$clienteEmail" {
    $r = Invoke-RestMethod -Uri "$base/api/cliente/reserva/history/$clienteEmail" -Method Get -Headers $HCliente
    Assert-NoError $r
}

$color = if ($script:failed -eq 0) { 'Green' } else { 'Yellow' }
Write-Host ''
$msg = 'Resumen: OK=' + $script:passed + ' FAIL=' + $script:failed
Write-Host $msg -ForegroundColor $color
Write-Host ''
if ($script:failed -gt 0) { exit 1 }
