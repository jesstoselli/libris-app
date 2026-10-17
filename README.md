# Libris Mobile

App do Libris pra Android e iOS (Kotlin Multiplatform + Compose Multiplatform). Plano em `docs/plano-v1.md`.

## Módulos

- `shared/`: modelos da API, cliente Ktor, sessão/tokens, datas. Testes: `./gradlew :shared:jvmTest`.
- `composeApp/`: telas em Compose Multiplatform, tema, Koin; Keychain no iOS.
- `androidApp/`: app Android.
- `iosApp/`: projeto Xcode que embute o framework `ComposeApp`.

## Android

```bash
./gradlew :androidApp:assembleDebug     # API local (http://10.0.2.2:5090, só no emulador)
./gradlew :androidApp:assembleRelease   # produção (libris-api.billfolder.app)
```

APKs em `androidApp/build/outputs/apk/{debug,release}/`. No celular físico, use o de release; o de debug só alcança a API local pelo emulador.

## iOS

Abra `iosApp/iosApp.xcodeproj` no Xcode. Debug usa a API local (`http://localhost:5090`, que o simulador compartilha com o Mac); Release usa produção. Pra rodar num iPhone de verdade, coloque o seu Team ID em `iosApp/Configuration/Config.xcconfig`.

Pela linha de comando (simulador):

```bash
cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17 Pro' -derivedDataPath ../build/ios build
```
