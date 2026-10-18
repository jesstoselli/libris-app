# Libris Mobile Visual Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the approved “Arquivo calmo + fita vinho” identity to every LibrisMobile screen without changing navigation, API behavior, ViewModels, or domain rules.

**Architecture:** Keep the existing Compose Multiplatform screen and ViewModel structure. Extend the small shared UI primitives (`Ficha`, `Stamp`, `PageHeading`, `RibbonBar`, fields) so screen work composes a consistent visual system instead of duplicating styling. Android and iOS continue using the same `commonMain` UI.

**Tech Stack:** Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material3 1.9.0, Navigation Compose 2.9.2, existing Compose Resources and Koin setup.

**Spec:** `docs/superpowers/specs/2026-10-17-libris-mobile-visual-refresh-design.md`

## Global Constraints

- Preserve all existing API calls, ViewModels, routes, validation, scanner, cover picker, and session behavior.
- Keep all visual implementation in shared Compose code unless a platform-specific API is unavoidable.
- Use the existing colors in `Libris`; oxblood is the recurring strong accent and brass remains rare.
- Keep Aleo for titles/stamps and Yantramanav for navigation, fields, metadata, and body copy.
- Bottom navigation has exactly four destinations: Acervo, Wishlist, Números, Registrar; logout moves to the page header.
- Maintain minimum 48 dp touch targets, system insets, selected semantics, and usable layouts from 320 dp width.
- Do not add dependencies or generic rounded Material card styling.

## Review Focus

- **320 dp width:** no essential action or bottom destination is clipped; status filters remain horizontally scrollable.
- **Long content:** long book titles, author lists, error messages, and genre names wrap or ellipsize without covering actions.
- **System insets:** status/navigation bars never overlap headings, the wine ribbon, or the last scrollable action.
- **Large text:** 1.3× font scaling preserves usable headings, form controls, and four navigation destinations.
- **All states:** loading, empty, error, loaded, dialogs, and destructive confirmations remain readable in the new palette.

---

### Task 1: Shared visual primitives

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/theme/Theme.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/Desk.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/Ficha.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/PageHeading.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/Stamp.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/ProgressBar.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/Fields.kt`

**Interfaces:**
- Produces: `Ficha(modifier, padding, foldedCorner = true, content)` with paper gradient/shadow/fold treatment.
- Produces: `Stamp(..., quiet: Boolean = false)`; quiet stamps are used only as list filters, full stamps remain available on a book sheet.
- Produces: `PageHeading(title, subtitle, modifier, action: (@Composable () -> Unit)? = null)` for root-page session actions.
- Consumes: existing `Libris` color tokens and `LocalLibrisType`.

- [ ] **Step 1:** Add shared visual constants for paper depth, low-contrast field borders, ribbon texture, and content spacing to `Theme.kt` only when reused by at least two components.
- [ ] **Step 2:** Retune `Desk` so the ledger image reads as a larger, lower-contrast field while preserving the current overlay and tiled resource.
- [ ] **Step 3:** Extend `Ficha` with the short offset shadow, subtle paper depth, and optional folded top-right corner without changing caller layout contracts.
- [ ] **Step 4:** Add the quiet variant to `Stamp`, retaining selected semantics and a 48 dp touch target while removing rotation/background/border from quiet filters.
- [ ] **Step 5:** Extend `PageHeading` with a trailing action slot and adjust its weight/letter spacing to match the Mac heading.
- [ ] **Step 6:** Reduce progress-bar weight, align ruled fields/buttons with the palette, and preserve existing error and enabled behavior.
- [ ] **Step 7:** Run `./gradlew :composeApp:jvmTest` and expect all common/JVM tests to pass.

### Task 2: Wine ribbon shell and login

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/nav/MainShell.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/RibbonBar.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/login/LoginScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/acervo/AcervoScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/wishlist/WishlistScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/numeros/NumerosScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/form/BookFormScreen.kt`

**Interfaces:**
- Produces: `RibbonBar(items)` rendering four wine-fabric destinations with paper/brass active treatment.
- Produces: root screen callbacks `onLogout: () -> Unit`; `BookFormScreen` receives nullable `onLogout` only for the root Registrar route.
- Consumes: `AuthSession.logout()` in `MainShell` and `PageHeading.action` from Task 1.

- [ ] **Step 1:** Remove the Sair `RibbonItem`, keep the four destination-selection rules unchanged, and route logout callbacks from `MainShell` into root screens.
- [ ] **Step 2:** Rebuild `RibbonBar` as a continuous oxblood textile strip using Compose drawing/gradients, paper icons/text, brass active line, 48 dp targets, and `navigationBarsPadding`.
- [ ] **Step 3:** Place the existing exit icon as an accessible “Sair” action in each root heading; nested Ficha/Edit/Gêneros screens keep their back/edit actions instead.
- [ ] **Step 4:** Redesign `LoginScreen` as a centered ex-libris paper plate with linear fields and the existing login behavior/error states.
- [ ] **Step 5:** Run `./gradlew :composeApp:jvmTest :androidApp:assembleDebug` and expect success.
- [ ] **Step 6:** On a 320–390 dp preview/device, verify four destinations fit, the last scroll item clears the ribbon, and large text does not overlap the session action.

### Task 3: Acervo, Wishlist, and book cards

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/acervo/AcervoScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/wishlist/WishlistScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/BookCard.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/BookCover.kt`

**Interfaces:**
- Consumes: quiet `Stamp`, folded `Ficha`, root `PageHeading.action`, and unchanged ViewModel state/actions.
- Produces: the common single-column index-card presentation used by Acervo and Wishlist.

- [ ] **Step 1:** Change Acervo status filters to quiet horizontally scrollable stamps; style genre/search as low-contrast dark fields and keep managing-genres access secondary.
- [ ] **Step 2:** Apply the same header, search, empty/loading/error spacing, and card rhythm to Wishlist without inventing new filters.
- [ ] **Step 3:** Update `BookCard` to folded paper, 58–64 dp covers, oxblood title/progress, and neutral underlined genre labels; remove `TAG_COLORS`.
- [ ] **Step 4:** Verify long titles use at most three lines, metadata remains readable, and cards remain usable at 320 dp width.
- [ ] **Step 5:** Run `./gradlew :composeApp:jvmTest` and expect all tests to pass.

### Task 4: Book sheet and reading history

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/ficha/FichaScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/Dialogs.kt`

**Interfaces:**
- Consumes: full `Stamp`, folded/continuous `Ficha`, `RuledField`, `PrimaryButton`, and unchanged `FichaViewModel` functions.
- Produces: one visual reading order: identity → status/progress → primary reading action → history → destructive actions.

- [ ] **Step 1:** Restructure `BookSheet` into a continuous paper hierarchy with cover/title first, neutral genres, publication metadata, and top back/edit actions.
- [ ] **Step 2:** Preserve full physical status stamps inside the sheet and visually separate progress/date editing from metadata.
- [ ] **Step 3:** Make registrar leitura the single dominant oxblood action; style history rows as ruled entries and keep edit/delete behavior unchanged.
- [ ] **Step 4:** Retune date/edit/confirm dialogs to paper, ink, oxblood, and brass while preserving dismissal and validation behavior.
- [ ] **Step 5:** Verify a long history scrolls above the wine ribbon and destructive actions cannot be confused with the primary action.
- [ ] **Step 6:** Run `./gradlew :composeApp:jvmTest` and expect all tests to pass.

### Task 5: Register/edit form and genre management

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/form/BookFormScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/genres/GenresScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/Fields.kt`

**Interfaces:**
- Consumes: the existing `BookFormViewModel`, picker/scanner expect/actual APIs, `RuledField`, `PrimaryButton`, and paper primitives.
- Produces: neutral genre labels, brass dashed suggestions, and consistent section spacing for long forms.

- [ ] **Step 1:** Present the form as one long paper sheet with clear sections and linear fields; preserve every field, validation message, and submit path.
- [ ] **Step 2:** Restyle ISBN lookup, scanner, and cover actions as secondary ink/brass tools without reducing their touch targets.
- [ ] **Step 3:** Replace filled genre chips with neutral underlined chosen labels and brass dashed suggestions, retaining add/remove behavior.
- [ ] **Step 4:** Restyle Genres as an index list on paper with creation at the top and existing rename/delete confirmation behavior.
- [ ] **Step 5:** Verify keyboard scrolling keeps focused fields and the save action reachable; test long genre names and form errors.
- [ ] **Step 6:** Run `./gradlew :composeApp:jvmTest :androidApp:assembleDebug` and expect success.

### Task 6: Numbers and complete-state polish

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/numeros/NumerosScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/components/Snackbar.kt`
- Modify: `composeApp/src/commonMain/kotlin/app/billfolder/libris/ui/nav/MainShell.kt`

**Interfaces:**
- Consumes: root heading action, continuous paper sheet, existing statistics state and month-selection callbacks.
- Produces: one long statistics sheet using ink, oxblood, brass, dividers, and aligned numbers instead of independent Material cards.

- [ ] **Step 1:** Recompose the KPI summary, monthly bars, genres, and book rankings into one continuous ruled sheet without changing calculations or selection behavior.
- [ ] **Step 2:** Limit chart colors to ink/oxblood/brass and keep selected month distinguishable without color alone.
- [ ] **Step 3:** Align snackbar colors and bottom offset with the wine ribbon so messages never render behind navigation.
- [ ] **Step 4:** Inspect loading, empty, error, loaded, and dialog states across every screen for contrast and consistent spacing.
- [ ] **Step 5:** Run `./gradlew :composeApp:jvmTest :androidApp:assembleDebug` and expect success.

### Task 7: Cross-platform and visual verification

**Files:**
- Modify if findings require it: files from Tasks 1–6 only
- Update: `docs/plano-v1.md`

**Interfaces:**
- Consumes: completed shared UI.
- Produces: verified Android/iOS-compatible implementation and updated design documentation.

- [ ] **Step 1:** Run `./gradlew :shared:jvmTest :composeApp:jvmTest :androidApp:assembleDebug :composeApp:compileKotlinIosSimulatorArm64`; expect all tasks to succeed.
- [ ] **Step 2:** Install a build on the connected Pixel, capture Acervo plus one long screen, and compare against the approved option A mock.
- [ ] **Step 3:** Check 320 dp width, 1.3× font scale, long title/genre/error strings, system insets, and all load states listed under Review Focus.
- [ ] **Step 4:** Remove one unnecessary decorative treatment if the final screenshots feel busier than the approved mock.
- [ ] **Step 5:** Update the visual section in `docs/plano-v1.md` to point at the approved visual-refresh spec and final navigation behavior.
- [ ] **Step 6:** Run `git diff --check`, inspect `git status --short`, and confirm `.superpowers/` remains ignored.
