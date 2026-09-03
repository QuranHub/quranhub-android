# Mushaf UI

Quran page reader — the app's core feature. Owned by this area: page rendering, page/sura/guz2 navigation indexes, search, tafsir, bookmarks & notes, per-aya audio (playback, recording, repeat). Not owned: downloads of tafsir/translation content (see `ui/downloads_manager`), app-wide settings, DB creation/migration (see `data/local/db`).

## Entry Points

- `fragments/MushafFragment.kt` — hosts the `QuranViewPagerAdapter` of `QuranPageFragment`s; implements `MushafView` (MVP) and wires top/bottom bar fragments. Start here when tracing anything.
- `fragments/QuranPageFragment.kt` — one mushaf page.
- `audio_manager/AyaAudioService.kt` — per-aya audio playback service; publishes playback state through `flowholder/AudioPlaybackStateHolder`.

## Architecture: MVP and MVVM coexist (migration in progress)

This area is mid-migration from MVP to MVVM (see repo `.scratch/mvvm-migration/`):

- **Legacy MVP path**: Fragment → `presenter/*` → `interactor/*` → DAOs (RxJava2/EventBus era). Only `Mus7fPresenter` and `QuranFooterPresenter` remain.
- **New MVVM path**: Fragment → `viewmodel/*` (AndroidViewModel + StateFlow/Channel events, coroutines) → same `interactor/*` implementations → DAOs.
- **When adding a feature, use MVVM**: create a ViewModel in `viewmodel/`, reuse/extend an `interactor/` interface + `*Imp`. Do not create new presenters.
- Both paths share `interactor/` for data access — viewmodels and presenters must NOT touch DAOs/databases directly.

## Subpackages

| Package | Role |
|---------|------|
| `fragments/` | All screens (16): page reader, indexes (`SuraIndexFragment`, `Guz2IndexFragment`, `SuraGuz2IndexFragment`), search, tafsir, topics, bookmarks/notes lists, translations library |
| `adapter/` | RecyclerView adapters incl. `QuranViewPagerAdapter` |
| `viewmodel/` | MVVM layer (11 ViewModels, StateFlow-based) |
| `presenter/` + `interactor/` | Legacy MVP presenters + shared data-access interactors |
| `model/` | UI models (mappers from Room entities live near their consumer) |
| `dialogs/` | Aya actions/bookmark/note/recorder/tafsir dialogs |
| `audio_manager/` | `AyaAudioService`, repeat config |
| `listener/`, `view/`, `flowholder/` | Callback interfaces, custom views, flow holders (page clicks, audio playback state) |

## Contracts & Invariants

- Data access always goes through `interactor/*` interfaces; never import `data.local.db` DAOs into fragments/adapters.
- Cross-fragment/service communication: audio playback state flows through the typed `flowholder/AudioPlaybackStateHolder` StateFlow (never via EventBus); the remaining greenrobot EventBus stream (download-finished) still requires matching `onStart`/`onStop` register/unregister pairs.
- Async: legacy MVP code uses RxJava2; new MVVM code uses coroutines + Flow. Do not mix RxJava into new code.
- Search/navigation depends on mushaf topology constants in `data/Constants` (aya counts, page mapping) — don't duplicate these.
- Views are XML + ViewBinding only.

## Anti-patterns

- Don't add `*Presenter`/`*PresenterImp` classes — new screens use ViewModels.
- Don't put DB queries in Fragments or Adapters.
- Don't bypass `LocaleUtils`/app preferences for language or recitation settings — read via `AppPreferencesManager`.

## Related Context

- Root: `AGENTS.md` (Room gotchas, global invariants)
- Data layer: `app/src/main/java/app/quranhub/data/` (no AGENTS.md — see root "Room gotchas")
