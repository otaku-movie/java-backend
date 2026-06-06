# シネコ Third-party SDK List

**Version**: 1.1.0

**Effective Date**: 2026/06/06

**Last Updated**: 2026/06/06

To support account registration, login, location-based cinema discovery, avatar / review uploads, usage statistics, and crash reporting, the App integrates the third-party SDKs and system services listed below.

> This list is kept consistent with the actual app dependencies declared in `pubspec.yaml` and is updated alongside any addition or removal in subsequent releases.

---

## 1. Login and Authentication

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `google_sign_in` | Google LLC | Google account sign-in / sign-up | Google `sub`, email, nickname, avatar URL | Tap "Sign in with Google" | https://policies.google.com/privacy |
| `sign_in_with_apple` | Apple Inc. | Apple ID sign-in (iOS only) | Apple `sub`, email, nickname | Tap "Sign in with Apple" on iOS | https://www.apple.com/legal/privacy/ |
| `flutter_appauth` | Maks O. (OSS) | OAuth 2.0 PKCE login via X | Temporary parameters for authorization code exchange | Tap "Sign in with X" | https://x.com/privacy |

---

## 2. Analytics and Crash Monitoring

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `firebase_analytics` | Google LLC | Screen-view and custom event statistics, purchase funnel optimisation | Event names and parameters, Firebase instance ID, device OS / model / app version; user ID linked after login | App launch, navigation, key actions (automatic) | https://policies.google.com/privacy |
| `firebase_crashlytics` | Google LLC | Crash detection, analysis, quality improvement | Crash stack traces, device OS / model / app version, Firebase instance ID | App crash (automatic) | https://policies.google.com/privacy |
| `firebase_core` | Google LLC | Firebase SDK initialisation foundation | Initialisation data shared with the above Firebase services | App launch | https://policies.google.com/privacy |

> Firebase data is processed on Google LLC (United States) servers. See Privacy Policy Sections 4.3 and 5 for details.

---

## 3. Device Capabilities and Location

| SDK / Service | Provider | Purpose | Information collected / used | Trigger | Privacy Policy |
|---|---|---|---|---|---|
| `geolocator` | Baseflow | Approximate device location | GPS / network coordinates (after user authorisation) | Nearby cinema recommendations | https://baseflow.com/privacy-statement/ |
| `geocoding` | Baseflow (OS-level) | Reverse geocoding | Coordinates only | City name display | Same as above |
| `image_picker` | Flutter official plugin | Pick / capture images | Image selected by you | Avatar change, review images | System privacy permissions |
| `image_editor` | Flutter community | Crop / rotate images | Selected image (local processing only) | Same as `image_picker` | Local processing only |

---

## 4. Local-Only Storage (does not leave your device)

| SDK / Service | Provider | Purpose | Stored information |
|---|---|---|---|
| `flutter_secure_storage` | Flutter community | Encrypted credential storage | `accessToken` / `refreshToken` / `deviceId` |
| `shared_preferences` | Flutter official plugin | Non-sensitive preferences | Language, home tab, onboarding flags, etc. |

---

## 5. Networking and Content Display

| SDK / Service | Provider | Purpose | Information involved |
|---|---|---|---|
| `dio` | Flutter community | HTTPS communication with our backend (`api.cineko.app`) | Request payload, Bearer token, `deviceId` |
| `extended_image` | Flutter community | Image loading and disk caching | Local cache only |
| `flutter_markdown` | Flutter official plugin | Rendering Markdown agreement bodies | Local rendering only |
| `url_launcher` | Flutter official plugin | Opening external links in browser / mail app | Triggered only when you tap |
| `share_plus` | Flutter official plugin | System share sheet | Only when you choose to share |

---

## 6. Helper Tools (no personal data collected)

| SDK / Service | Purpose |
|---|---|
| `package_info_plus` | Read the app's own version number |
| `uuid` | Generate device-local `deviceId` |
| `crypto` | Local hash computations |
| `logger` / `pretty_dio_logger` | Debug logging only; release builds emit nothing |
| UI plugin suite | Rendering, animation, formatting. No network or personal data collection. |

---

## 7. Third-Party Services Not Currently Integrated

The following are **not currently integrated**. We will update this list and the relevant Privacy Policy when they are added.

- Push messaging (Firebase Cloud Messaging / APNs)
- Online payment (Stripe / PayPay / Apple Pay / Google Pay, etc.)
- Third-party map components (Google Maps / Apple MapKit JS)

---

## 8. Maintenance Notes

1. This list is curated based on the actual `pubspec.yaml` dependencies and kept in sync with app releases.
2. When adding any SDK that involves personal information collection, we will update this list and the relevant Privacy Policy, and request your renewed consent where required.
3. Questions: privacy@cineko.app.

---

## Changelog

### 1.1.0 (2026/06/06)

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
