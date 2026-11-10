# ifs25018-pbo2026-springboot-cashflow

Aplikasi web pencatat keuangan (Pemasukan / Pengeluaran + saldo) berbasis **Spring Boot 4.1.2**, **Maven**, dan **Vaadin 25**, dengan **Clean Architecture**. Data in-memory (hilang saat restart). Port dari `.env` (`APP_PORT`, default 8080).

## Menjalankan
```
copy .env.example .env
.\mvnw.cmd spring-boot:run
```
Build JAR: `.\mvnw.cmd clean package -Pproduction` lalu `java -jar target\ifs25018-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar`
Test + coverage: `.\mvnw.cmd clean verify` (laporan `target/site/jacoco/index.html`).

## Struktur
domain/{entity,exception,repository} → usecase (FinanceUseCase) → adapter/{repository,presenter} → framework/view (FinanceView) ; config/AppConfig = composition root.

## Jika error Vaadin "Invalid lock file" / "FileAlreadyExistsException"
Hentikan aplikasi (Ctrl+C), jalankan `.\bersihkan-vaadin.ps1`, lalu `.\mvnw.cmd spring-boot:run` lagi. Disarankan folder proyek di path tanpa spasi.

## Ganti username
`./rename-username.sh username_anda` (Git Bash/WSL) — mengganti nama folder, artifactId, package, dan semua import.
