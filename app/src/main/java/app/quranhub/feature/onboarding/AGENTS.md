# Onboarding feature

First-run wizard (`FirstTimeWizardActivity` pager of `OptionsListFragment`s + `FirstTimeWizardViewModel`).

- Owned: wizard flow and its screens only.
- Not owned: the shared option-row adapter + option dialog (`core/ui`), language/recitation settings storage (`core/data`).
- Finishes by navigating to the main shell via `Intent` — the only cross-feature edge allowed here.
- Never import from another `feature/*` package except `Activity` classes for navigation `Intent`s.
