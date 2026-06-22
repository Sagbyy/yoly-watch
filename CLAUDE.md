# yoly-watch

Wear OS companion app for Yoly, built with Jetpack Compose (Wear Material).

## Architecture

Clean Architecture with a strict one-way dependency rule: `presentation → domain ← data`.
The `domain` layer is pure Kotlin and never depends on Android, Compose, or any framework.
`presentation` and `data` depend only on `domain` abstractions, never on each other.

```
com.yoly.watch/
  domain/          pure business core (no Android imports)
    model/         entities; validate invariants in init {}
    repository/    interfaces (contracts only)
    usecase/       one class per intent, single operator fun invoke()
  data/            framework details, implements domain interfaces
    remote/        data sources (API interface + impl), dto/ for raw payloads
    mapper/        dto → domain extension functions
    repository/    domain interface implementations
  di/              ServiceLocator — manual DI, swap impls in one place
  presentation/    Compose UI (Wear Material)
    <feature>/     UiState (sealed) + ViewModel + Screen per feature
    theme/
```

### Layer rules
- A feature owns a folder under `presentation/<feature>/` containing its `UiState`, `ViewModel`, and `Screen`.
- UI state is a `sealed interface` with explicit `Loading` / `Success` / `Error` cases. No nullable spaghetti.
- ViewModels expose a single `StateFlow<UiState>`, mutate it via a private `MutableStateFlow`, and run work in `viewModelScope`. They depend on use cases only.
- Use cases are the only entry point the presentation calls into the domain.
- DTOs never leak past the `data` layer; map them to domain models in `mapper/`.
- To swap the mock for a real network client, change only the binding in `di/ServiceLocator`.

## Conventions

- **No useless comments.** Code is self-documenting through naming. Add a comment only to explain a non-obvious *why*, never to restate *what* the code does. No KDoc boilerplate on obvious members.
- **Always provide a `@Preview` for UI components.** Every Composable screen/component has at least one `@Preview` (use `WearDevices.SMALL_ROUND`). Cover each meaningful state (Loading, Success, Error) with its own preview, driven by a stateless Composable that takes `UiState` as a parameter.
- Keep Composables stateless where possible: a `Route` Composable wires the ViewModel and passes plain state + callbacks down to a stateless `Screen`.
- Use `collectAsStateWithLifecycle()` to observe state in Compose.
- User-facing strings live in `res/values/strings.xml`; never hardcode them in Composables.
- Suspend functions for I/O; no blocking calls on the main thread.

## Build

```
./gradlew :app:compileDebugKotlin   # fast compile check
./gradlew :app:assembleDebug        # build APK
```

Dependencies are declared via the version catalog in `gradle/libs.versions.toml`.
