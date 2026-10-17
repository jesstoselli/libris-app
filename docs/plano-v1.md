# Libris Mobile — plano da v1

**Data**: 2026-10-17
**Repo**: `~/Repos/LibrisMobile` (novo), no mesmo molde do `VertoApp`.
**Base**: o design do Libris (`~/Repos/Libris/docs/superpowers/specs/2026-10-16-libris-design.md`), a API em produção (`https://libris-api.billfolder.app`) e o app do Mac (`~/Repos/LibrisApp`) como referência de comportamento.

## Decisões da Jess

- **Plataformas:** Android e iOS.
- **Escopo:** paridade com o Mac: Login, Acervo, Ficha + registro de leitura, Registrar/Editar com ISBN e capa, Wishlist com "Comprei", gerenciar gêneros e Números.
- **Scan do código de barras (ISBN) já na v1.**
- Online-only, como no Mac (cache/offline fica pra depois).

## Stack (minhas escolhas)

A mesma do VertoApp, nas mesmas versões, sem Room (não há banco local):

- Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material3, Navigation Compose, Lifecycle ViewModel, Koin 4.2.2, kotlinx-serialization, kotlinx-datetime.
- Ktor 3.3 com engine OkHttp (Android) e **Darwin** (iOS).
- Alvos: Android (minSdk 26), `iosArm64` + `iosSimulatorArm64`, e JVM só pra rodar testes.
- Pacote `app.billfolder.libris`.

**Dependências novas em relação ao Verto (preciso do seu ok):**

| Lib | Pra quê | Plataforma |
|---|---|---|
| Coil 3 (`coil-compose` + `coil-network-ktor3`) | Mostrar as capas (com cache) | Android + iOS |
| CameraX + ML Kit Barcode Scanning (modelo embutido) | Scan do ISBN | Só Android |

No iOS, o scan usa o AVFoundation nativo (`AVCaptureMetadataOutput`, EAN-13), sem lib. A escolha de capa usa o seletor nativo de cada sistema (Photo Picker no Android, PHPicker no iOS), também sem lib.

## Estrutura

```
shared/      KMP puro: modelos da API, cliente Ktor, auth (tokens + renovação single-flight),
             regras de data ("hoje" local), mensagens de erro; testes com Ktor MockEngine
composeApp/  UI em Compose Multiplatform (telas, tema, componentes) + ViewModels + Koin;
             expect/actual: armazenamento seguro, scanner, seletor de capa
androidApp/  Activity, manifest, permissão de câmera
iosApp/      projeto Xcode que embute o framework do composeApp
```

## Visual

O mesmo do Mac, adaptado pro celular:

- Mesa escura com o `libris-ledger-bg` (renderizado em PNG e repetido como textura), com o overlay escuro.
- Fichas de papel, carimbos, Aleo nos títulos e Yantramanav no texto (TTF embutidos como Compose resources).
- **A fita vira uma barra inferior de tecido** (cor `paper`), com os mesmos ícones: Acervo · Wishlist · Números · Registrar. No celular, uma fita lateral roubaria largura demais. *(Escolha minha; se preferir outra coisa, é só falar.)*
- Acervo em lista de uma coluna. Ficha numa coluna só (dados → progresso/carimbos → registrar leitura → histórico).

## Comportamentos que valem igual ao Mac

- Datas `YYYY-MM-DD` nunca passam por conversão de fuso; "hoje" é a data local do aparelho.
- Erro da API mostra a `message` dela; sem rede → "Sem conexão com o servidor."
- Renovação de token: uma só pra vários 401 simultâneos; só desloga se a API recusar a renovação (400/401).
- Busca por ISBN preenche só campos vazios; capa sugerida baixada pela API (`from-url`) ao salvar.
- Livro Lido mostra "Lido · N págs."; registro de leitura com "de → até · +N".

## Checkpoints (paro em cada um pra você revisar)

1. **Base:** projeto rodando no emulador Android e no simulador iOS; tema, fontes, mesa e barra inferior; cliente da API, login, armazenamento seguro do token (Android: arquivo privado do app; iOS: Keychain) e renovação.
2. **Acervo + Ficha:** lista com filtros (carimbos com rolagem horizontal, gênero, busca), Ficha, mudança de situação, editar datas, apagar, registrar/editar/apagar leitura.
3. **Registrar/Editar + ISBN + scan + capa:** formulário, chips de gênero, busca por ISBN, scan pela câmera (preenche o ISBN e já busca), capa por câmera ou galeria.
4. **Wishlist + Números + builds:** Wishlist, "Comprei", gerenciar gêneros, Números; APK de release instalável e app rodando num iPhone físico seu (assinatura com o seu Apple ID pessoal no Xcode).

**Testes:** do `shared` (cliente da API contra MockEngine, auth, datas, mensagens) e dos ViewModels com fakes, rodando na JVM. As telas eu confiro nos simuladores, com screenshot.

## Desenvolvimento local

- **Build de debug:** aponta pra API local (`http://10.0.2.2:5090` no emulador Android, `http://localhost:5090` no simulador iOS), com a mesma conta de teste do app do Mac.
- **Build de release:** aponta pra produção.

## Fora da v1

Offline/cache, notificações, widget, iPad/tablet com layout próprio.
