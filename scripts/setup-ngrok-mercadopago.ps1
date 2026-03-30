param(
    [Parameter(Mandatory = $true)]
    [string]$NgrokAuthtoken,

    [switch]$PersistUserEnv,

    [string]$FrontendBaseUrl = "http://localhost:4200"
)

$ErrorActionPreference = "Stop"

$ngrok = Get-Command ngrok -ErrorAction SilentlyContinue
if (-not $ngrok) {
    throw "ngrok no está instalado o no está en PATH."
}

Write-Host "[1/4] Configurando authtoken de ngrok..." -ForegroundColor Cyan
ngrok config add-authtoken $NgrokAuthtoken | Out-Host

Write-Host "[2/4] Iniciando túnel ngrok a backend local (http://localhost:8080)..." -ForegroundColor Cyan
$proc = Start-Process -FilePath ngrok -ArgumentList @("http", "8080", "--log=stdout") -PassThru

$apiUrl = "http://127.0.0.1:4040/api/tunnels"
$publicUrl = $null
for ($i = 0; $i -lt 20; $i++) {
    Start-Sleep -Milliseconds 500
    try {
        $data = Invoke-RestMethod -Uri $apiUrl -Method Get
        $httpsTunnel = $data.tunnels | Where-Object { $_.public_url -like "https://*" } | Select-Object -First 1
        if ($httpsTunnel) {
            $publicUrl = $httpsTunnel.public_url
            break
        }
    } catch {
        # Espera a que ngrok levante la API local
    }
}

if (-not $publicUrl) {
    Write-Warning "No se pudo leer la URL pública de ngrok desde $apiUrl."
    Write-Host "Verifica manualmente en la consola de ngrok y usa esa URL para MERCADOPAGO_WEBHOOK_BASE_URL." -ForegroundColor Yellow
    exit 1
}

Write-Host "[3/4] URL de ngrok detectada: $publicUrl" -ForegroundColor Green

$env:MERCADOPAGO_WEBHOOK_BASE_URL = $publicUrl
$env:MERCADOPAGO_FRONTEND_BASE_URL = $FrontendBaseUrl

if ($PersistUserEnv) {
    [System.Environment]::SetEnvironmentVariable("MERCADOPAGO_WEBHOOK_BASE_URL", $publicUrl, "User")
    [System.Environment]::SetEnvironmentVariable("MERCADOPAGO_FRONTEND_BASE_URL", $FrontendBaseUrl, "User")
    Write-Host "Variables persistidas en entorno de usuario." -ForegroundColor Green
}

Write-Host "[4/4] Configuración lista." -ForegroundColor Green
Write-Host "- MERCADOPAGO_WEBHOOK_BASE_URL=$publicUrl"
Write-Host "- MERCADOPAGO_FRONTEND_BASE_URL=$FrontendBaseUrl"
Write-Host ""
Write-Host "Arranca backend en este mismo terminal para heredar variables:" -ForegroundColor Cyan
Write-Host "  .\\gradlew.bat bootRun"
Write-Host ""
Write-Host "PID ngrok: $($proc.Id)"
Write-Host "Para detener ngrok: Stop-Process -Id $($proc.Id) -Force"
