# Libris Mobile

App do Libris pra Android e iOS (Kotlin Multiplatform + Compose Multiplatform). Plano em `docs/plano-v1.md`.

## Módulos

- `shared/`: `domain` (modelos, interfaces de repositório, regras, erros, "hoje"), `data` (cliente Ktor + DTOs, sessão/tokens com Prefs no Android e Keychain no iOS, repositórios) e `di` (módulo Koin de dados). Testes: `./gradlew :shared:jvmTest`.
- `composeApp/`: a apresentação, em `ui/` — `feature/` (tela + ViewModel de cada fluxo), `common/` (estado de carga, mensagens pt-BR), `format/` (datas pt-BR), `components/`, `theme/`, `nav/`, `platform/` e `di/` (ViewModels). As telas só conhecem `domain`.
- `androidApp/`: app Android.
- `iosApp/`: projeto Xcode que embute o framework `ComposeApp`.

## Android

```bash
./gradlew :androidApp:assembleDebug     # produção; pra API local no emulador, ponha libris.apiUrl=http://10.0.2.2:5090 no local.properties
./gradlew :androidApp:assembleRelease   # produção (libris-api.billfolder.app)
```

APKs em `androidApp/build/outputs/apk/{debug,release}/`. No celular físico, use o de release; o de debug só alcança a API local pelo emulador.

## iOS

Abra `iosApp/iosApp.xcodeproj` no Xcode. Debug usa a API local (`http://localhost:5090`, que o simulador compartilha com o Mac); Release usa produção. Pra rodar num iPhone de verdade, coloque o seu Team ID em `iosApp/Configuration/Config.xcconfig`.

Pela linha de comando (simulador):

```bash
cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17 Pro' -derivedDataPath ../build/ios build
```
