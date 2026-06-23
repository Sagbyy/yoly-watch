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

- **No comments unless strictly necessary.** The codebase is comment-free by default. Remove template/boilerplate KDoc, section-header comments, and any comment that restates *what* the code does. Keep a comment only when it explains a non-obvious *why* that the code cannot express on its own (e.g. an environment-specific gotcha, the intent behind an intentionally-empty block). When in doubt, delete it. This rule applies to Kotlin, XML, and Gradle files alike.
- **Always provide a `@Preview` for UI components.** Every Composable screen/component has at least one `@Preview` (use `WearDevices.SMALL_ROUND`). Cover each meaningful state (Loading, Success, Error) with its own preview, driven by a stateless Composable that takes `UiState` as a parameter.
- Keep Composables stateless where possible: a `Route` Composable wires the ViewModel and passes plain state + callbacks down to a stateless `Screen`.
- Use `collectAsStateWithLifecycle()` to observe state in Compose.
- User-facing strings live in `res/values/strings.xml`; never hardcode them in Composables.
- Suspend functions for I/O; no blocking calls on the main thread.

## Networking

- HTTP via Retrofit + OkHttp; JSON via kotlinx.serialization (`@Serializable` DTOs).
- Real-time pairing confirmation uses **SSE** (`okhttp-sse` `EventSource`) wrapped in a `callbackFlow`, exposed to the domain as `Flow<PairingStatus>`. The transport (SSE / polling / push) stays an implementation detail of `data/remote` — the ViewModel only collects the Flow.
- Swap mock ↔ real network in `di/ServiceLocator` via the `USE_MOCK` flag (currently `false`); `BASE_URL` is `http://10.0.2.2:3000/` (host `localhost` as seen from the Android emulator — use the LAN IP on a real device, prod URL otherwise). `MockPairingCodeApi` simulates a confirmation so the flow is testable offline.
- The SSE client uses `readTimeout = 0` (no read timeout) so the stream stays open.
- `INTERNET` permission and `usesCleartextTraffic="true"` (dev only — remove for HTTPS prod) are declared in the manifest.

### Pairing API contract (NestJS, `http://localhost:3000`, docs at `/api/docs`)
- `POST /pairing/codes` body `{ deviceUuid }` → `{ pairingId, code, expiresInSeconds }`.
- `GET /pairing/{pairingId}/events` (SSE) → each event `data` is `{ status: PENDING|CONFIRMED|EXPIRED, deviceToken? }`. `deviceToken` is present only on `CONFIRMED`; the stream ends on `CONFIRMED`/`EXPIRED`.
- On `CONFIRMED` the repository persists `deviceToken` via `DeviceCredentialsStore` (the watch's `dvc_…` bearer credential for future authenticated calls).
- `POST /pairing/confirm` is the mobile-side call (Firebase-authed) — not implemented on the watch.

## Watch identity

- The watch's stable identifier is a UUID generated once and persisted in DataStore (`DataStoreWatchIdentityProvider`), exposed via the domain `WatchIdentityProvider`. It survives app restarts and is reset only on uninstall / clear-data.
- It is sent as `watchId` in the `POST /pairing/codes` body so the backend can durably associate the watch with the account. Distinct from the server-generated, ephemeral `pairingId`.
- `ServiceLocator.init(context)` is called from the `YolyWatchApp` Application class (registered via `android:name` in the manifest) to provide the Context that DataStore needs.

## Build

```
./gradlew :app:compileDebugKotlin   # fast compile check
./gradlew :app:assembleDebug        # build APK
```

Dependencies are declared via the version catalog in `gradle/libs.versions.toml`.
