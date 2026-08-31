# AGENTS.md

QuranHub Android app: a Quran reader (Hafs & Warsh mushaf, audio recitations, tafsir/translation downloads). Package `app.quranhub`.

> **Note:** This file is symlinked to `CLAUDE.md` so that Claude Code picks it up. Whenever this file is edited, keep `CLAUDE.md` in sync — i.e. always symlink `CLAUDE.md` → `AGENTS.md` (see `.github` workflow for symlink enforcement or the commit hook). When renaming or moving, update the symlink: `ln -sf AGENTS.md CLAUDE.md`.

## Toolchain & commands

- JDK 21 (Temurin), Gradle 9.7.1 wrapper, AGP 9.3.2, Kotlin 2.4.10, KSP. compileSdk 37 / targetSdk 36 / minSdk 23.
- Verify changes with `./gradlew build` — this is exactly what CI runs (GitHub Actions `android.yml`, master push/PR only). `lint` has `abortOnError = false`, so lint failures don't fail the build.
- Tests are scaffolding only (`ExampleUnitTest`, `ExampleInstrumentedTest`). There is no real test suite; don't treat `test`/`connectedAndroidTest` as meaningful verification.

## Modules

- `:app` — the entire application (single source tree under `app/src/main/java/app/quranhub/`).
- `:prdownloader-service` — local Android library wrapping `com.github.amitshekhariitbhu:PRDownloader` (served via jitpack, declared in `settings.gradle.kts`). Used by download services.

## Architecture notes (not obvious from filenames)

- Classic XML Views only (ViewBinding + DataBinding enabled) — no Jetpack Compose. Kotlin-only sources.
- No DI framework; wiring is manual. Cross-component communication uses greenrobot EventBus; async uses RxJava2 (not RxJava3) + Retrofit.
- `ui/` is feature-organized (`main`, `mushaf`, `settings`, `downloads_manager`, `first_wizard`); `data/` splits into `local` (Room), `remote` (api.quranhub.app REST), `repository`, `service` (FCM, audio downloader).
- Entry point `QuranhubApplication` is a `MultiDexApplication`. It re-applies the app locale in **three** places (`attachBaseContext`, `onCreate`, `onConfigurationChanged`) via `LocaleUtils.initAppLanguage` — keep all three in sync if touching locale logic.
- Firebase: `app/google-services.json` is committed. Crashlytics is toggled via the `enableCrashlytics` manifest placeholder (true in release, false in debug), not by build-type-specific code.

## Room gotchas

- Three databases: `UserDatabase` (normal Room DB, schema exported to committed `app/schemas/`, currently v5 — bump `@Database(version)` and export a new schema on change) and two **asset-backed** DBs (`MushafDatabase`, `TranslationDatabase`) pre-populated via the custom `RoomAsset` helper from `app/src/main/assets/databases/`.
- For asset-backed DBs, `@Database(version)` must **stay at 2**; to migrate, bump the `version` argument passed to `RoomAsset.databaseBuilder` instead. Don't "fix" the version number.

## Conventions

- Code style follows Ribot's Android guidelines (see README); versioning is SemVer.
- Release builds have `isMinifyEnabled = false` — ProGuard rules exist but are effectively unused.
- App release version lives in `app/build.gradle.kts` (`versionCode`/`versionName`), not in a catalog.

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues on QuranHub/quranhub-android (via the `gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role triage vocabulary (labels equal their names). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: root `CONTEXT.md` + `docs/adr/`. See `docs/agents/domain.md`.
