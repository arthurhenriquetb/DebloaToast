package br.com.a01optimizer

import java.util.Locale

object AppGuide {
    const val ALL = 0
    const val SAMSUNG = 1
    const val PHONE = 2
    const val MAPS = 3
    const val GOOGLE = 4
    const val NETWORK = 5
    const val MEDIA = 6
    const val PERSONAL = 7
    const val SECURITY = 8
    const val TOOLS = 9
    const val OTHER = 10
    const val INSTALLED = 11

    val entries: Map<String, OptionalPackage> =
        (SafeCatalog.entries + FactoryExtras.entries + AdvancedCatalog.entries + UniversalCatalog.entries)
            .associateBy { it.name }

    private val englishNames = mapOf(
        "android" to "Android OS",
        "com.android.systemui" to "Android System UI",
        "com.android.settings" to "Android Settings",
        "com.android.shell" to "ADB command service",
        "com.android.phone" to "Android Phone Services",
        "com.android.vending" to "Google Play Store",
        "com.android.keychain" to "Certificates and credentials",
        "com.android.se" to "Android Secure Element",
        "com.android.printspooler" to "Android Print Spooler",
        "com.android.bookmarkprovider" to "Browser bookmarks provider",
        "com.android.wallpaper.livepicker" to "Live wallpaper picker",
        "com.android.wallpaperbackup" to "Wallpaper backup",
        "com.android.providers.downloads" to "Android Downloads Manager",
        "com.android.providers.media" to "Media database",
        "com.android.providers.telephony" to "SMS and phone database",
        "com.google.android.gms" to "Google Play Services",
        "com.google.android.gsf" to "Google Services Framework",
        "com.google.android.webview" to "Android System WebView",
        "com.google.android.packageinstaller" to "Android App Installer",
        "com.google.android.permissioncontroller" to "Permission Manager",
        "com.google.android.syncadapters.contacts" to "Google Contacts Sync",
        "com.google.android.gms.location.history" to "Google Location History",
        "com.google.mainline.telemetry" to "Google Module Telemetry",
        "com.google.android.apps.restore" to "Google Data Restore",
        "com.google.android.apps.turbo" to "Google Device Health",
        "com.sec.android.app.launcher" to "Samsung One UI Home",
        "com.samsung.android.honeyboard" to "Samsung Keyboard",
        "com.sec.android.app.camera" to "Samsung Camera",
        "com.sec.android.app.clockpackage" to "Samsung Clock and Alarms",
        "com.sec.android.app.myfiles" to "Samsung My Files",
        "com.sec.android.app.samsungapps" to "Galaxy Store",
        "com.samsung.android.app.updatecenter" to "Samsung Update Center",
        "com.sec.android.emergencylauncher" to "Samsung Emergency Mode",
        "com.sec.imsservice" to "Samsung IMS Calling",
        "com.sec.epdg" to "Carrier Wi-Fi Calling",
        "com.samsung.android.kgclient" to "Samsung Knox Guard",
        "com.samsung.android.fmm" to "Samsung Find My Mobile",
        "com.samsung.android.biometrics.app.setting" to "Biometric Settings",
        "com.samsung.android.lool" to "Samsung Device Care",
        "com.samsung.android.callbgprovider" to "Samsung Call Backgrounds",
        "com.samsung.android.app.soundpicker" to "Samsung Sound Picker",
        "com.samsung.android.secsoundpicker" to "Samsung Ringtone Picker",
        "com.samsung.android.mapsagent" to "Samsung Maps Integration",
        "com.samsung.android.networkdiagnostic" to "Samsung Network Diagnostics",
        "com.sec.android.app.billing" to "Galaxy Store Billing",
        "com.sec.android.app.quicktool" to "Samsung Quick Tools",
        "com.samsung.android.bluelightfilter" to "Samsung Blue Light Filter",
        "com.samsung.android.beaconmanager" to "Samsung Nearby Devices",
        "com.samsung.android.calendar" to "Samsung Calendar",
        "com.samsung.android.faceservice" to "Samsung Face Recognition",
        "com.samsung.android.sm.devicesecurity" to "Samsung Device Security",
        "com.samsung.android.scloud" to "Samsung Cloud",
        "com.samsung.android.dynamiclock" to "Samsung Dynamic Lock Screen",
        "com.samsung.android.game.gos" to "Samsung Game Optimizer",
        "com.dti.samsung" to "Carrier App Installer",
        "com.android.dreams.basic" to "Basic Screensaver",
        "com.android.dreams.phototable" to "Photo Screensaver",
        "com.android.egg" to "Android Easter Egg",
        "com.android.traceur" to "Android System Tracing",
        "com.google.android.feedback" to "Google Feedback",
        "com.google.android.tts" to "Google Text-to-Speech",
        "com.samsung.android.app.settings.bixby" to "Bixby Settings",
        "com.samsung.android.app.watchmanagerstub" to "Galaxy Watch Shortcut",
        "com.samsung.android.stickercenter" to "Samsung Stickers",
        "com.samsung.storyservice" to "Samsung Gallery Stories",
        "com.samsung.android.app.appsedge" to "Edge Apps",
        "com.samsung.android.app.clipboardedge" to "Edge Clipboard",
        "com.samsung.android.app.taskedge" to "Edge Tasks",
        "com.samsung.android.service.peoplestripe" to "Edge Contacts",
        "com.samsung.android.app.clockpack" to "Lock Screen Clock Styles",
        "com.sec.android.app.fm" to "FM Radio",
        "com.google.android.printservice.recommendation" to "Printer Suggestions",
        "com.android.bips" to "Android Print Service",
        "com.google.android.projection.gearhead" to "Android Auto",
        "com.samsung.android.app.galaxyfinder" to "Samsung Finder",
        "com.samsung.android.app.cocktailbarservice" to "Samsung Edge Panels",
        "com.samsung.android.forest" to "Digital Wellbeing",
        "com.samsung.android.themestore" to "Galaxy Themes Store",
        "com.samsung.android.themecenter" to "Samsung Themes Service",
        "com.samsung.android.app.dressroom" to "Wallpapers and Styles",
        "com.samsung.android.rubin.app" to "Samsung Personalization",
        "com.samsung.android.mdx.kit" to "Link to Windows",
        "com.samsung.android.mdx.quickboard" to "Windows Clipboard Sync",
        "com.samsung.android.app.sharelive" to "Quick Share",
        "com.samsung.android.allshare.service.fileshare" to "Samsung File Sharing",
        "com.samsung.android.aware.service" to "Nearby Sharing",
        "com.samsung.android.easysetup" to "Samsung Device Setup",
        "com.sec.android.daemonapp" to "Samsung Weather",
        "com.sec.android.easyMover.Agent" to "Smart Switch Agent",
        "com.samsung.android.smartswitchassistant" to "Smart Switch Assistant",
        "com.samsung.android.uds" to "Ultra Data Saving",
        "com.facebook.appmanager" to "Facebook App Manager",
        "com.facebook.katana" to "Facebook",
        "com.facebook.services" to "Facebook Services",
        "com.facebook.system" to "Facebook System",
        "com.google.android.apps.tachyon" to "Google Meet / Duo",
        "com.microsoft.skydrive" to "Microsoft OneDrive",
        "com.opera.max.oem" to "Opera Max Data Saving",
        "com.samsung.android.kidsinstaller" to "Samsung Kids Installer",
        "com.sec.android.usermanual" to "Samsung User Manual",
        "com.google.android.youtube" to "YouTube",
        "com.google.android.gm" to "Gmail",
        "com.google.android.apps.maps" to "Google Maps",
        "com.google.android.googlequicksearchbox" to "Google Search and Assistant",
        "com.samsung.android.video" to "Samsung Video Player",
        "com.hiya.star" to "Hiya Caller ID",
        "com.sec.android.app.soundalive" to "Samsung Audio Effects",
        "com.google.android.syncadapters.calendar" to "Google Calendar Sync",
        "com.motorola.help" to "Motorola Device Help",
        "com.motorola.moto" to "Moto Features",
        "com.motorola.fmplayer" to "Motorola FM Radio",
        "com.motorola.timeweatherwidget" to "Motorola Time and Weather",
        "com.miui.weather2" to "Xiaomi Weather",
        "com.miui.player" to "Xiaomi Music",
        "com.miui.videoplayer" to "Xiaomi Video",
        "com.miui.bugreport" to "Xiaomi Bug Reports"
    )

    private val englishNotes = mapOf(
        "android" to "KEEP. Core Android framework; removal can prevent the phone from booting.",
        "com.android.systemui" to "KEEP. Status bar, notifications and system controls.",
        "com.android.settings" to "KEEP. Android settings and device management.",
        "com.android.shell" to "KEEP. ADB commands; removal can block recovery.",
        "com.android.phone" to "KEEP. Calls, SIM and cellular service.",
        "com.android.keychain" to "KEEP. Certificates and secure authentication.",
        "com.android.vending" to "KEEP. Play Store and app updates; banking checks may rely on it.",
        "com.google.android.gms" to "KEEP. Google Play Services; many banking apps rely on it.",
        "com.google.android.gsf" to "KEEP. Google Services Framework and account services.",
        "com.google.android.webview" to "KEEP. Web content and authentication inside apps.",
        "com.google.android.permissioncontroller" to "KEEP. Permission and privacy controls.",
        "com.sec.android.app.launcher" to "KEEP. Samsung home screen and navigation.",
        "com.samsung.android.honeyboard" to "KEEP. Samsung keyboard and text input.",
        "com.sec.imsservice" to "KEEP. Calls and mobile network features.",
        "com.samsung.android.biometrics.app.setting" to "KEEP. Biometric sign-in settings.",
        "com.samsung.android.kgclient" to "KEEP. Samsung Knox device security.",
        "com.android.dreams.basic" to "Basic idle screensaver; optional if unused.",
        "com.android.dreams.phototable" to "Photo screensaver when the phone is idle.",
        "com.android.egg" to "Hidden Android version Easter egg.",
        "com.samsung.android.app.settings.bixby" to "Bixby settings; optional when Bixby is unused.",
        "com.samsung.android.app.watchmanagerstub" to "Galaxy Watch shortcut; optional without a Samsung watch.",
        "com.samsung.android.dynamiclock" to "Dynamic lock-screen wallpapers.",
        "com.samsung.android.stickercenter" to "Samsung stickers and decorative effects.",
        "com.samsung.storyservice" to "Automatically generated Gallery stories.",
        "com.samsung.android.app.appsedge" to "Edge apps panel; removes a multitasking feature.",
        "com.samsung.android.app.clipboardedge" to "Edge clipboard panel; optional if unused.",
        "com.samsung.android.app.taskedge" to "Edge task shortcuts; optional if unused.",
        "com.samsung.android.service.peoplestripe" to "Edge contacts panel; optional if unused.",
        "com.samsung.android.app.clockpack" to "Alternative lock-screen clock designs.",
        "com.sec.android.app.fm" to "FM radio; remove only if you do not use it.",
        "com.google.android.printservice.recommendation" to "Automatic printer recommendations.",
        "com.android.bips" to "Built-in printing; removal can disable printing.",
        "com.google.android.projection.gearhead" to "Android Auto car connection.",
        "com.samsung.android.app.galaxyfinder" to "Samsung home-screen app search.",
        "com.samsung.android.app.cocktailbarservice" to "Samsung Edge panels and their shortcuts.",
        "com.samsung.android.forest" to "Digital wellbeing and screen-time tools.",
        "com.samsung.android.themestore" to "Downloading new Samsung themes.",
        "com.samsung.android.themecenter" to "Applying Samsung themes and personalization.",
        "com.samsung.android.app.dressroom" to "Wallpapers and appearance settings.",
        "com.samsung.android.scloud" to "Samsung Cloud backup and sync.",
        "com.samsung.android.rubin.app" to "Samsung personalization and recommendations.",
        "com.samsung.android.mdx.kit" to "Link to Windows integration.",
        "com.samsung.android.mdx.quickboard" to "Clipboard sharing with Windows PCs.",
        "com.samsung.android.app.sharelive" to "Quick Share file transfers.",
        "com.samsung.android.allshare.service.fileshare" to "Samsung file-sharing features.",
        "com.samsung.android.aware.service" to "Nearby device discovery and sharing.",
        "com.samsung.android.easysetup" to "Samsung accessory setup.",
        "com.sec.android.daemonapp" to "Samsung weather and weather widget.",
        "com.sec.android.easyMover.Agent" to "Smart Switch device migration.",
        "com.samsung.android.smartswitchassistant" to "Smart Switch migration assistant.",
        "com.samsung.android.uds" to "Samsung ultra data saving.",
        "com.facebook.appmanager" to "Preinstalled Facebook update manager.",
        "com.facebook.katana" to "Facebook app, if unused.",
        "com.facebook.services" to "Preinstalled background Facebook services.",
        "com.facebook.system" to "Preinstalled Facebook integration.",
        "com.google.android.apps.tachyon" to "Legacy Google Meet / Duo calling app.",
        "com.microsoft.skydrive" to "OneDrive and optional photo backup.",
        "com.opera.max.oem" to "Opera data-saving integration.",
        "com.samsung.android.kidsinstaller" to "Samsung Kids setup package.",
        "com.sec.android.usermanual" to "Built-in Samsung user manual.",
        "com.google.android.youtube" to "YouTube app; optional if unused.",
        "com.google.android.gm" to "Gmail app; removing may stop mail notifications.",
        "com.google.android.apps.maps" to "Google Maps and navigation.",
        "com.google.android.googlequicksearchbox" to "Google Search, voice and Assistant features.",
        "com.samsung.android.video" to "Samsung local video playback.",
        "com.hiya.star" to "Caller identification and spam detection.",
        "com.samsung.android.game.gos" to "Game performance, battery and temperature management.",
        "com.android.traceur" to "Android performance trace collection.",
        "com.google.android.feedback" to "Send feedback reports to Google.",
        "com.google.android.tts" to "Speech output; needed by some accessibility tools.",
        "com.samsung.android.calendar" to "Samsung calendar and events.",
        "com.samsung.android.bluelightfilter" to "Samsung blue-light comfort filter.",
        "com.sec.android.app.myfiles" to "Samsung file manager and local files.",
        "com.sec.android.app.clockpackage" to "Alarms, stopwatch and clock. Check alarms first.",
        "com.sec.android.app.soundalive" to "Samsung audio equalizer and effects.",
        "com.google.android.syncadapters.calendar" to "Google Calendar event synchronization.",
        "com.samsung.android.beaconmanager" to "Nearby Samsung accessory detection.",
        "com.android.wallpaper.livepicker" to "Live wallpaper selection; effects vary by device.",
        "com.android.wallpaperbackup" to "Wallpaper restoration from backups.",
        "com.android.bookmarkprovider" to "Browser bookmarks integration; dependencies uncertain.",
        "com.android.printspooler" to "System print queue; may disable printing.",
        "com.google.android.gms.location.history" to "Location History; not core GPS services.",
        "com.samsung.android.callbgprovider" to "Samsung call background customization.",
        "com.samsung.android.app.soundpicker" to "Samsung sound selection; may affect ringtones.",
        "com.samsung.android.secsoundpicker" to "Samsung ringtone and sound selection.",
        "com.samsung.android.mapsagent" to "Samsung Maps integration; exact effect is unverified.",
        "com.samsung.android.networkdiagnostic" to "Samsung network diagnostics; effect unverified.",
        "com.sec.android.app.billing" to "Galaxy Store payments and purchases.",
        "com.sec.android.app.quicktool" to "Samsung quick tools; dependencies are unverified."
    )

    private val criticalGuide = setOf(
        "android", "com.android.systemui", "com.android.settings", "com.android.shell",
        "com.android.phone", "com.android.keychain", "com.android.vending",
        "com.google.android.gms", "com.google.android.gsf", "com.google.android.webview",
        "com.google.android.permissioncontroller", "com.sec.android.app.launcher",
        "com.samsung.android.honeyboard", "com.sec.imsservice",
        "com.samsung.android.biometrics.app.setting", "com.samsung.android.kgclient",
        "com.motorola.ccc.ota", "com.motorola.launcher3", "com.motorola.camera3",
        "com.miui.home", "com.miui.securitycenter", "com.xiaomi.finddevice"
    )

    fun adviseKeep(id: String) = id in criticalGuide || ProtectedInfo.isCritical(id)

    fun name(id: String, pt: Boolean, fallback: String): String =
        if (fallback.isNotBlank() && fallback != id) fallback
        else if (pt) KnownNames.label(id) ?: entries[id]?.label ?: fallback
        else englishNames[id] ?: entries[id]?.label ?: fallback

    fun categoryNames(pt: Boolean) = if (pt) arrayOf(
        "Todos", "Fabricante", "Telefone e SMS", "Mapas e localização", "Google",
        "Wi-Fi e conexões", "Câmera e mídia", "Tela e aparência", "Segurança e bancos",
        "Ferramentas", "Outros", "Instalados"
    ) else arrayOf(
        "All apps", "Manufacturer", "Phone & SMS", "Maps & location", "Google",
        "Wi-Fi & connections", "Camera & media", "Home & appearance", "Security & banking",
        "Tools & diagnostics", "Other", "Installed apps"
    )

    fun category(id: String): Int {
        val v = id.lowercase(Locale.ROOT)
        fun any(vararg tokens: String) = tokens.any(v::contains)
        return when {
            v in setOf("android", "com.android.vending", "com.google.android.gms",
                "com.google.android.gsf", "com.google.android.webview",
                "com.google.android.packageinstaller") -> SECURITY
            v in setOf("com.android.systemui", "com.sec.android.app.launcher",
                "com.samsung.android.honeyboard") -> PERSONAL
            v in setOf("com.android.settings", "com.android.shell") -> TOOLS
            any("maps", "location", "geotz", "geofence", ".gps", "fused", "nfwlocation") -> MAPS
            any("phone", "telephony", "telecom", "dialer", "incall", "callbg", "calllog",
                "messaging", ".mms", ".sms", "ims", "ril", ".stk", "carrierconfig",
                "rcs", "hi ya", "hiya", "contacts", "epdg", "unifiedwfc") -> PHONE
            any("wifi", "wlan", "bluetooth", "network", "tether", "hotspot", "connectivity",
                "modem", "uwb", "wcmurls") -> NETWORK
            any("camera", "gallery", "media", "video", "audio", "sound", "fm", "photo",
                "music", "voice", "tts", "sticker", "image", "mic") -> MEDIA
            any("wallpaper", "theme", "launcher", "dream", "font", "edge", "lock",
                "cocktail", "dressroom", "bluelight", "display", "clock", "onehanded",
                "navbar", "gestural", "personalization") -> PERSONAL
            any("knox", "security", "biometric", "keychain", "permission", "cert",
                "trustzone", "integrity", "kgclient", "fmm", "safe", "secure", "safety",
                "auth", "bank", "nubank", "picpay", "pagbank", "banestes", "app.inter") -> SECURITY
            any("backup", "update", "feedback", "trace", "diagnos", "factory", "setup",
                "smart", "debug", "print", "docs", "cloud", "installer", "restore",
                "manager", "manual", "logging", "telemetry") -> TOOLS
            else -> OTHER
        }
    }

    fun matches(group: Int, id: String, isSystem: Boolean): Boolean = when (group) {
        ALL -> true
        SAMSUNG -> KnownNames.isManufacturer(id)
        GOOGLE -> id.startsWith("com.google.") || id.startsWith("com.android.") || id == "android"
        INSTALLED -> !isSystem
        else -> category(id) == group
    }

    fun level(id: String): Int = when {
        id == "com.samsung.android.app.clockpack" -> 2 // Removing breaks Samsung lock-screen clock styles.
        adviseKeep(id) -> 3
        entries[id]?.risk == Risk.LOWER -> 0
        entries[id]?.risk == Risk.CONDITIONAL -> 1
        else -> 2
    }

    fun color(level: Int): Int = when (level) {
        0 -> 0xFF26834A.toInt()
        1 -> 0xFFA36E00.toInt()
        2 -> 0xFFC03F3A.toInt()
        else -> 0xFF212121.toInt()
    }
    fun levelName(level: Int, pt: Boolean): String = if (pt)
        arrayOf("Verde", "Amarelo", "Vermelho", "Preto · Crítico")[level]
    else arrayOf("Green", "Yellow", "Red", "Black · Critical")[level]

    fun description(id: String, pt: Boolean): String {
        if (adviseKeep(id)) {
            val details = if (pt) ProtectedInfo.reason(id)
                else englishNotes[id] ?: "Core device or operating-system component."
            val warning = if (pt)
                "CRÍTICO: a remoção pode impedir o Android de iniciar ou comprometer chamadas, bancos e a recuperação por ADB."
            else "CRITICAL: removal may prevent Android from starting or disable calls, banking, or ADB recovery."
            return "$warning\n$details"
        }
        if (pt) return entries[id]?.explanation ?: RiskSuggestions.notes[id]
            ?: ProtectedInfo.reason(id)
        return englishNotes[id] ?: if (entries[id] == null)
            "Unverified system component. Removing it may disable other apps or system features."
        else "Function or dependencies are not fully verified on this model."
    }

    fun reviewedSuggestions(ids: Set<String>): List<String> =
        (entries.keys + RiskSuggestions.notes.keys + criticalGuide + ProtectedInfo.criticalIds())
            .distinct().filter { it in ids }
            .sortedWith(compareBy<String> { level(it) }.thenBy { englishNames[it] ?: it })
}
