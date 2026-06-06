# シネコ Privacy Policy

**Version**: 1.1.0

**Effective Date**: 2026/06/06

**Last Updated**: 2026/06/06

Cineko Operations Team ("we" or "us") regards the protection of your personal information as one of our core responsibilities. This Privacy Policy explains, when you use シネコ (the "Service"), **what information we collect, why we collect it, how we use, share, and retain it, and the rights you can exercise**.

Please read this Privacy Policy carefully before using the Service. By using the Service you agree that we may process your personal information in accordance with this Policy.

---

## 1. Who We Are

- Personal information controller: Cineko Operations Team
- Registered Address: Tokyo, Japan (to be updated after confirmation)
- Privacy contact: privacy@cineko.app

## 2. Information We Collect

We only collect and process information necessary to provide the Service.

### 2.1 Information You Provide

| Scenario | Items | Required |
|---|---|---|
| Email registration | Email address, password (stored encrypted), nickname | Yes |
| Google / Apple Sign-In | OpenID (`sub`), email, nickname, avatar URL | Yes |
| Profile editing | Avatar, nickname, gender, date of birth | Optional |
| Order / Ticketing | Contact details, movie, showtime, seat, order amount | Yes |
| Reviews / Support | Text, images, videos | Optional |

### 2.2 Information Collected Automatically

| Type | Items | Purpose |
|---|---|---|
| Device | Device model, OS and version, `deviceId` (generated locally), app version | Anti-fraud, refresh-token binding, compatibility |
| Network | IP address, connection type | Anti-fraud, API routing |
| Logs | API access timestamps, key operation logs | Troubleshooting and product improvement |
| Location (**only with your permission**) | GPS / network coordinates from `geolocator`; reverse geocoding via `geocoding` | Nearby cinema recommendations |
| Photos / Camera (**only when you actively choose**) | Images you select or capture | Avatar, support attachments, review images |
| Local storage | `accessToken` / `refreshToken` / `deviceId` (encrypted via `flutter_secure_storage`); preferences via `shared_preferences` | Stays on the device only |
| **Firebase Analytics** (Google LLC) | Screen-view events, custom event names and parameters (movie ID, purchase flow ID, etc.), Firebase instance ID, device OS / model / app version | Service improvement, usage statistics, purchase funnel optimisation |
| **Firebase Crashlytics** (Google LLC) | Crash stack traces, device OS / model / app version, Firebase instance ID | Crash detection, analysis, and quality improvement |

> The current build **does not integrate** Firebase Cloud Messaging / APNs push SDKs. We will update this Policy and the Third-party SDK List, and request your renewed consent, when push SDKs are introduced.

### 2.3 Information We Do Not Collect

- Full bank card numbers, CVV, or expiry are **not transmitted to our servers**.
- We **do not** scan your photo library, contacts, SMS, or clipboard.
- Firebase Analytics does **not intentionally transmit** your name, email, or phone number (we may link a user ID to Firebase after login; see Section 4).

## 3. How We Use Your Information

1. Account registration, login, and identity verification.
2. Order fulfilment: order placement, payment relay, ticket issuance, entry verification, refunds / changes, and customer support.
3. Security: account anti-fraud, anti-scalping, risk audit.
4. Service improvement: statistics and feature optimisation using **anonymised or pseudonymised** data (including Firebase Analytics).
5. Quality improvement: crash report analysis and fixes (Firebase Crashlytics).
6. Marketing notifications (only if you have not turned off push notifications).
7. Performance of legal obligations.

## 4. Sharing, Processors, and Disclosure

We **do not sell** your personal information to unrelated third parties.

### 4.1 Sharing with Cinemas / Distributors

For order fulfilment and entry verification, we share order number, showtime, seat, electronic pickup code, and where necessary the last four digits of name / phone with the relevant cinema.

### 4.2 Sharing with Payment Providers (**not yet active**)

When online payment is introduced, we will share necessary information with the third-party payment provider.

### 4.3 Processors (acting on our instructions)

- Cloud services: AWS (Tokyo Region)
- Customer support / Email / SMS: SendGrid / Twilio
- **Firebase / Google LLC (United States)**: usage statistics via Firebase Analytics, crash report collection and analysis via Firebase Crashlytics. Google processes data in accordance with the [Google Privacy Policy](https://policies.google.com/privacy). Firebase instance IDs and (after login) user IDs may be sent to Google servers (primarily in the United States).

### 4.4 Third-Party Login

When you use Google / Apple Sign-In, the relevant platform returns the necessary account identifier, email, nickname, and avatar to us.

### 4.5 Disclosure Required by Law

We may disclose information when required by laws, courts, or administrative authorities.

## 5. Cross-Border Transfers

Your personal information is stored on servers in Japan (Tokyo Region). Firebase Analytics / Crashlytics data may be sent to and stored on **Google LLC (United States)** servers. We adopt necessary protection measures (e.g. Standard Contractual Clauses) under applicable laws.

## 6. Storage and Security

We employ TLS encryption, salted hashing, role-based access control, operation logging, and vulnerability monitoring. Security incidents will be reported as required by law.

## 7. Retention Periods

| Category | Retention period |
|---|---|
| Account information | Deleted or anonymised within 30 days after account closure. |
| Order and transaction records | Retained for **at least 7 years** under Japan's Electronic Books Preservation Act. |
| Login and operation logs | 180 days. |
| Firebase Analytics events | Up to 14 months per our Google data retention settings. |
| Firebase Crashlytics reports | 90 days (Google default). |
| Local Tokens | Cleared on logout or app uninstallation. |

## 8. Your Rights

You may exercise access, correction, deletion, and consent withdrawal rights. Contact our privacy team at privacy@cineko.app (response within 15 business days).

## 9. Third-Party SDKs

See the **Third-party SDK List** (code: `THIRD_PARTY_SDK`) for details.

## 10. Protection of Minors

The Service is intended for users aged **13 or above**.

## 11. Changes to This Policy

Material changes will be notified prominently. Changes involving sensitive personal information processing, sharing, or cross-border transfers will only take effect after we obtain your **renewed explicit consent**.

## 12. Contact Us

- Operator: Cineko Operations Team
- Privacy contact: privacy@cineko.app
- Customer Support: support@cineko.app

---

## Changelog

### 1.1.0 (2026/06/06)

- Integrated Firebase Analytics (Google LLC) for usage statistics
- Integrated Firebase Crashlytics (Google LLC) for crash reporting
- Updated Privacy Policy and Third-party SDK List accordingly

### 1.0.0

- Initial release
