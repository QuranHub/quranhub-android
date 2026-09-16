# Settings feature

App settings (`SettingsActivity` + `SettingsFragment`, `SettingsViewModel`, `custom/` toggle views).

- Owned: settings screens and their `viewmodel/`; note the `view_mushaf_*` layouts and `custom/MushafSetting*` views belong here, not to mushaf.
- Not owned: locale/prefs storage (`core/data`), shared option-picker dialog (`core/ui`), app-wide utils (`core/common`).
- Settings opens the downloads manager and the reciter picker via navigation `Intent` / `core/ui` dialog — the only cross-feature edges allowed here.
- Never import from another `feature/*` package except `Activity` classes for navigation `Intent`s.
