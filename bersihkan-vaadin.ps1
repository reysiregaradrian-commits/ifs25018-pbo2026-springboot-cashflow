# Membersihkan sisa build frontend Vaadin yang rusak/terkunci, lalu bisa dijalankan ulang.
# Pakai: .\bersihkan-vaadin.ps1   (pastikan aplikasi sebelumnya sudah berhenti)
$ErrorActionPreference = "SilentlyContinue"
Set-Location $PSScriptRoot
Get-Process java | Where-Object { $_.Path -like "*java*" } | Out-Null
foreach ($p in @("src\main\frontend\generated", "node_modules", "target", "package.json", "package-lock.json", "vite.config.ts", "vite.generated.ts", "tsconfig.json", "types.d.ts")) {
    if (Test-Path $p) { Remove-Item $p -Recurse -Force; Write-Host "dihapus: $p" }
}
Write-Host "Selesai. Jalankan: .\mvnw.cmd spring-boot:run" -ForegroundColor Green
