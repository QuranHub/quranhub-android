# Mushaf UI

Quran page reader — the app's core feature. Owned by this area: page rendering, page/sura/guz2 navigation indexes, search, tafsir, bookmarks & notes, per-aya audio (playback, recording, repeat). Not owned: downloads of tafsir/translation content (see `feature/downloads`), app-wide settings, DB creation/migration (see `core/data/local/db`). Shared UI models consumed by DAOs live in `core/data/model`; playback/download signals live in `core/common/flowholder`; shared picker dialogs live in `core/ui`.

## Entry Points

- `fragments/MushafFragment.kt` — hosts the `QuranViewPagerAdapter` of `QuranPageFragment`s; wires `MushafViewModel` and the top/bottom bar fragments. Start here when tracing anything.
- `fragments/QuranPageFragment.kt` — one mushaf page.
- `audio_manager/AyaAudioService.kt` — per-aya audio playback service; publishes playback state through `core/common/flowholder/AudioPlaybackStateHolder`.

## Architecture: MVVM (migration complete)

The MVP layer (presenters, view interfaces, RxJava2, EventBus) has been fully removed. Every screen follows one pattern:

- **Screen → ViewModel**: Fragment/Activity → `viewmodel/*` (AndroidViewModel + StateFlow state, Channel one-time events, coroutines) → `interactor/*` interface + `*Imp` → DAOs.
- **When adding a feature**: create a ViewModel in `viewmodel/`, reuse/extend an `interactor/` interface + `*Imp`. Do not create presenters or MVP view interfaces.
- ViewModels must NOT touch DAOs/databases directly — data access goes through `interactor/`.

## Subpackages

| Package | Role |
|---------|------|
| `fragments/` | All screens (16): page reader, indexes (`SuraIndexFragment`, `Guz2IndexFragment`, `SuraGuz2IndexFragment`), search, tafsir, topics, bookmarks/notes lists, translations library |
| `adapter/` | RecyclerView adapters incl. `QuranViewPagerAdapter` |
| `viewmodel/` | MVVM layer (13 ViewModels, StateFlow-based) |
| `interactor/` | Shared data-access interfaces + `*Imp` implementations |
| `model/` | UI models (mappers from Room entities live near their consumer) |
| `dialogs/` | Aya actions/bookmark/note/recorder/tafsir dialogs |
| `audio_manager/` | `AyaAudioService`, repeat config |
| `listener/`, `flowholder/`, `util/` | Callback interfaces, page-click flow holder, mushaf-only helpers (`RecorderMediaHelper`, `FragmentUtils`, `ImageUtil`, `IntentUtils`) |

## Contracts & Invariants

- Data access always goes through `interactor/*` interfaces; never import `core.data.local.db` DAOs into fragments/adapters.
- Cross-fragment/service communication: cross-component signaling flows through typed flow holders — `flowholder/QuranPageClickHolder` (page taps, owned here), `core/common/flowholder/AudioPlaybackStateHolder` (playback state), and `core/common/flowholder/DownloadFinishedHolder` (downloader-service completion; collected with repeatOnLifecycle, no register/unregister pairs). There is no greenrobot EventBus usage in this area.
- Async: coroutines + Flow only. RxJava and EventBus are removed from the codebase — do not reintroduce them.
- Search/navigation depends on mushaf topology constants in `core/data/Constants` (aya counts, page mapping) — don't duplicate these.
- Views are XML + ViewBinding only.
- Never import from another `feature/*` package except `Activity` classes for navigation `Intent`s; the host (`feature/main`) alone may reference feature fragments. Shared UI goes in `core/ui`.

## Anti-patterns

- Don't add `*Presenter`/`*PresenterImp` classes, MVP view interfaces, RxJava (`io.reactivex.*`), or greenrobot EventBus — use ViewModels, coroutines/Flows, and typed flow holders.
- Don't put DB queries in Fragments or Adapters.
- Don't bypass `LocaleUtils`/app preferences for language or recitation settings — read via `AppPreferencesManager`.

## Related Context

- Root: `AGENTS.md` (Room gotchas, global invariants)
- Data layer: `app/src/main/java/app/quranhub/core/data/` (no AGENTS.md — see root "Room gotchas")
