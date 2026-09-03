# AGENTS.md

QuranHub Android app: a Quran reader (Hafs & Warsh mushaf, audio recitations, tafsir/translation downloads). Package `app.quranhub`.

> **Note:** This file is symlinked to `CLAUDE.md` so that Claude Code picks it up. Whenever this file is edited, keep `CLAUDE.md` in sync — i.e. always symlink `CLAUDE.md` → `AGENTS.md` (see `.github` workflow for symlink enforcement or the commit hook). When renaming or moving, update the symlink: `ln -sf AGENTS.md CLAUDE.md`.
>
> **Any `AGENTS.md` in this repo must also have a `CLAUDE.md` symlink alongside it** (e.g. `app/src/main/java/app/quranhub/ui/mushaf/AGENTS.md` → `CLAUDE.md`) so Claude Code picks up nested intent-layer docs too.

## Git workflow

**Never commit directly to `master`.** `master` is branch-protected. Always create a new branch off `master` (e.g. `<area>/<short-description>`), commit changes there, push it, and open a PR targeting `master` (PRs must pass the required `build` status check before merge).

## Intent Layer

**Before modifying code in a subdirectory, read its AGENTS.md first** to understand local patterns and invariants.

- **Mushaf UI**: `app/src/main/java/app/quranhub/ui/mushaf/AGENTS.md` — Quran page reader, navigation indexes, search, tafsir, bookmarks/notes, per-aya audio. Largest and most complex UI area (~99k tokens).

### Measurements

| Directory | Tokens | Needs node? |
|-----------|--------|-------------|
| `ui/` (total) | ~136k | covered by `ui/mushaf` child node |
| `ui/mushaf` | ~99k | YES (child node above) |
| `ui/downloads_manager` | ~17.6k | NO |
| `data/` (local+remote+repository) | ~19k | NO — see Room gotchas below |
| `ui/settings`, `ui/main`, `ui/first_wizard`, `ui/base`, `ui/common` | <6k each | NO |
| `:prdownloader-service` | ~5.7k | NO |

### Global Invariants

- XML Views only (ViewBinding/DataBinding) — no Jetpack Compose; RxJava2 (not 3) + EventBus, no DI framework.
- Asset-backed DBs (`MushafDatabase`, `TranslationDatabase`) keep `@Database(version) = 2`; migrate via the `RoomAsset.databaseBuilder` version argument only.
- `UserDatabase` schema is exported to committed `app/schemas/` — bump version + export schema on any change.
- App locale is applied in three places in `QuranhubApplication` — keep all three in sync.

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

<!-- code-review-graph MCP tools -->
## MCP Tools: code-review-graph

**This project has a knowledge graph. Start with the code-review-graph
MCP tools to narrow scope, then read the source.** The graph is cheaper than scanning files and
gives you structural context (callers, dependents, test coverage) that file search cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes_tool` or `query_graph_tool` instead of Grep
- **Understanding impact**: `get_impact_radius_tool` instead of manually tracing imports
- **Code review**: `detect_changes_tool` + `get_review_context_tool` instead of reading entire files
- **Finding relationships**: `query_graph_tool` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview_tool` + `list_communities_tool`

### Verify in the source

- Narrow scope with the graph, then read the source. Do not change code from graph output alone.
- For any non-trivial change, read the implementation and the relevant tests before concluding.
- Verify the exact source when touching behavior, database logic, migrations, retries, fallbacks,
  recovery, or compatibility code.
- When the graph and the source disagree, the source wins. The graph may be stale or may not
  model that relationship.
- An empty graph result can mean "not indexed" or "not statically visible", not "does not exist".

### Key Tools

| Tool | Use when |
| ------ | ---------- |
| `detect_changes_tool` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context_tool` | Need source snippets for review — token-efficient |
| `get_impact_radius_tool` | Understanding blast radius of a change |
| `get_affected_flows_tool` | Finding which execution paths are impacted |
| `query_graph_tool` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes_tool` | Finding functions/classes by name or keyword |
| `get_architecture_overview_tool` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes_tool` for code review.
3. Use `get_affected_flows_tool` to understand impact.
4. Use `query_graph_tool` pattern="tests_for" to check coverage.
<!-- /code-review-graph MCP tools -->
