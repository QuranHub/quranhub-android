# Main shell (host)

App entry + navigation hub: `MainActivity` (drawer, fragment swaps to mushaf/index/topics/library/bookmarks/notes/search/tafseer), `MainViewModel`, notification-permission delegate.

- This is the only package allowed to reference other features: it hosts their fragments and routes navigation via explicit `Intent`s/fragment transactions. There is no navigation graph.
- Fragments hosted here call back through `Mus7afDrawerItemClickListener` / `QuranNavigationCallbacks` (fragment→host casts are a known edge; a navigation contract in `core/ui` is future work).
- Never add data access, download logic, or reader UI here — those belong to `core/data` and the owning feature.
