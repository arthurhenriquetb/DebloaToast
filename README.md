# DebloaToast Expert v0.12

**Android debloating with guided ADB setup — including advanced removal of critical system packages.**

**Developer:** Arthur Henrique Tostes Batista Aranha  
**Development disclosure:** The application concept, feature decisions and testing were directed by the developer. Application code was produced with assistance from ChatGPT (AI-generated / AI-assisted). The project has not been independently security-audited.

**Download:** [DebloaToast Expert v0.12 for Android](../../releases/latest) · [Windows setup helper](../../releases/latest)

DebloaToast runs on **Android 10 or later**. It displays packages actually present on the device instead of depending on a fixed list for one phone model. Manufacturer compatibility and risk classifications vary with the Android version and firmware.

## ⚠ Expert version: potentially irreversible changes

**The Expert version allows you to select and attempt to uninstall ANY package displayed in the app, including Android system components normally considered essential.** The developer does not claim that these removals are safe. Android or ADB may still deny a command, but DebloaToast itself does not block critical packages once the ADB connection is established.

Possible consequences include:
- Android failing to boot, becoming unstable or unusable.
- Loss of calls, SMS, mobile data, Wi-Fi, notifications, accessibility or device security.
- Banking, payment and authentication apps stopping working.
- Loss of ADB access, preventing a straightforward restoration.
- Permanent data loss, the need to reinstall the original firmware, or damage that cannot be fixed by ordinary software recovery.

**Use at your own risk.** The user is responsible for choosing which packages to remove and for any resulting consequences. Back up important data and be prepared for full firmware recovery before modifying critical system packages. An uninstall via ADB typically removes a system package for Android user 0 without erasing its read-only system image, but **that does not mean the phone or its data can always be recovered.** Removing or disabling essential components can break the system before a restoration can be performed.

### Four risk levels

| Level | Meaning | Removal selection |
| --- | --- | --- |
| 🟢 Green | Lower-risk, apparently optional package | Available |
| 🟡 Yellow | Removal can affect features you may use | Available |
| 🔴 Red | Higher or unknown risk; dependencies not validated | Available |
| ⚫ Black — Critical | Known essential or highly sensitive Android/manufacturer components | **Available — severe warning** |

A **single explicit warning** appears before executing the uninstall operation, especially when black-level packages are selected. The tool does not remove anything without the user's selection and confirmation. This is not a safety guarantee; the four levels are estimates and cannot account for every Android firmware.

## Recovery in v0.12 (revised)

Connect to ADB, then open **Apps**. System packages removed for Android user 0 remain in the list, appear dimmed and marked **REMOVED**, and are available under the **Removed** category. Select only the packages you want and tap **Reinstall**. No packages are selected by default. The previous recovery dialog has been removed. Automatic ADB discovery prompts and the automatic connection button have been removed; pairing is initiated manually.

## Main features

- View installed and preinstalled packages from the actual phone, not a device-specific preset.
- Search and filter by function, manufacturer and risk level, including black (critical).
- Select multiple items from **Suggestions** without leaving that tab.
- Sort by name, estimated app size or measured RAM where available.
- Uninstall ordinary applications with Android's confirmation.
- With authorized ADB, attempt uninstall of selected factory and critical packages for Android user 0, without root.
- Scan Android for system packages uninstalled for user 0, even if the app history is empty; selectively reinstall available packages using ADB.
- English interface and optional Brazilian Portuguese translation.

## Guided connection — no separate ADB installation

**Basic app browsing** works without ADB; you can also uninstall ordinary user-installed applications using Android's own confirmation dialog.

**Android 10:** To manage system packages, open **Connect → Set up full access**. The built-in wizard helps open Developer options and USB debugging, and offers a **Windows helper ZIP download** directly from the phone. Transfer that ZIP to a Windows computer, extract it and double-click `Conectar-Windows.cmd`. The script downloads **Google's official Android Platform Tools** automatically, detects the USB-connected phone and prepares a network ADB connection. Approve the phone's USB debugging prompt. When ready, return to DebloaToast and choose **Check connection**. Once connected over a trusted network, the USB cable can be disconnected.

**Android 11+:** Use Android's built-in wireless debugging pairing code. DebloaToast can discover the current connection port and reuse saved pairing authorization. No computer is generally required on devices supporting Android's wireless debugging.

**More intuitive connection flow (v0.12, updated build):** After replying to the pairing notification with Android's six-digit pairing code, DebloaToast automatically shows a **second notification** asking for the **connection port** displayed under **Developer options → Wireless debugging → IP address & Port** (not the temporary pairing port). Reply to the second notification with the number after the colon, for example `37123` from `192.168.1.5:37123`. DebloaToast then tests the connection. Alternatively, the connection-port field is **always visible** in the app's **Connect** tab, with an explanation and a shortcut to Developer options. Automatic port discovery is still available and may avoid typing this number altogether. Android controls whether inline notification replies are expanded; the app cannot guarantee an always-open keyboard in the notification.

An ordinary third-party app cannot bypass the initial Android debugging approval. Some Windows computers may require the manufacturer's USB driver. On Android 10, a restart can require running the helper again. Avoid using network ADB on public Wi-Fi; the legacy TCP port 5555 is exposed to the local network and should be disabled after use.

## Files and build

- `DebloaToast-Expert-v0.12.apk` — public Android installer (debug-signed).
- `DebloaToast-Windows.zip` — optional Windows companion for Android 10.
- Minimum supported Android: 10 (API 29). Kotlin, Kadb and Gradle.
- The application ID `br.com.a01optimizer.v08` remains unchanged to preserve compatibility with earlier installations using the same signing key.

This is an **experimental Expert tool, not a Play Store application**. Testing on a limited number of phones does not establish safety or compatibility on all devices.

## In-app help and PDF guide

The main **Connect** screen is intentionally minimal: **Pair** and a large red **CONNECT** button. CONNECT becomes available when a connection port has been provided by the notification flow (or the Android 10 Windows setup has been configured). A successful pairing prompts for the connection port in a second notification. The completion notification opens the **Connect** tab directly; the user then presses **CONNECT**. Automatic discovery remains an optional fallback. The connection port entry and verbose setup instructions have been removed from the main screen.

A **Help** button at the top of the app opens a dedicated bilingual, step-by-step tutorial, from enabling Developer options through wireless pairing or Windows setup, troubleshooting and restoration. The tutorial can be **shared as text** or **saved as PDF** using Android's Print / Save as PDF option. Android's print support is required for PDF export. Navigation labels have been reduced to fit narrow screens in both languages.
