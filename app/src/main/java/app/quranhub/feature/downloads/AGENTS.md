# Downloads feature

Tafsir/translation/audio download lists + manager activity (`DownloadsManagerActivity` with recitations/reciters/suras tabs + Quran-images download).

- Owned: download list screens, `viewmodel/*` list logic, `model/DisplayableDownload`, `adapters/DownloadsAdapter`.
- Not owned: the shared picker dialogs (`QuranRecitersDialogFragment`, `AudioDownloadAmountDialogFragment`, `DeleteConfirmationDialogFragment` + picker ViewModels) — those live in `core/ui` and are reused by mushaf/settings. Download execution + completion signal live in `core/data/service` + `core/common/flowholder/DownloadFinishedHolder`.
- Never import from another `feature/*` package except `Activity` classes for navigation `Intent`s.
