package br.com.a01optimizer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import java.util.concurrent.Executors

class DashboardActivity : Activity() {
    private enum class Tab { APPS, GUIDE, TOOLS, SETTINGS }

    private val prefs by lazy { getSharedPreferences("a01", Context.MODE_PRIVATE) }
    private val worker = Executors.newSingleThreadExecutor()
    private val selected = linkedSetOf<String>()
    private var lang = "en"
    private var tab = Tab.APPS
    private var category = 0
    private var riskFilter = 0
    private var guideCategory = 0
    private var guideRisk = 0
    private var sort = 0
    private var query = ""
    private var connected = false
    private var busy = false
    private var model = ""
    private var statusText = ""
    private var systemApps = emptySet<String>()
    private var userApps = emptySet<String>()
    private var removedApps = emptySet<String>()
    private var disabled = emptySet<String>()
    private var labels = emptyMap<String, String>()
    private var ram = emptyMap<String, Long>()
    private var sizes = emptyMap<String, Long>()
    private var sizeRequested = false
    private var adapter: AppsAdapter? = null
    private var uninstallButton: Button? = null
    private var restoreButton: Button? = null
    private var appList: ListView? = null
    private var statusLabel: TextView? = null
    private var guideList: ListView? = null
    private var guideSelectionLabel: TextView? = null
    private var reviewButton: Button? = null
    private var discovery: AdbDiscovery? = null

    private fun readyToConnect(): Boolean = connected ||
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
            prefs.getString("connection_port", "") == "5555") ||
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            prefs.getBoolean("paired", false) &&
            (prefs.getString("connection_port", "")?.toIntOrNull()?.let { it in 1..65535 } == true))

    private fun openConnectTab() {
        tab = Tab.TOOLS
        buildUi()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra("open_connect", false) == true) openConnectTab()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = prefs.getString("language", "en") ?: "en"
        loadLocalApps()
        if (intent?.getBooleanExtra("open_connect", false) == true) tab = Tab.TOOLS
        buildUi()
        if (savedInstanceState == null && tab != Tab.TOOLS) window.decorView.post {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (prefs.getString("connection_port", "") == "5555") {
                    scan(5555, automatic = true)
                } else if (!prefs.getBoolean("setup_intro_v010", false)) {
                    prefs.edit().putBoolean("setup_intro_v010", true).apply()
                    AlertDialog.Builder(this)
                        .setTitle(tr("Ready to use", "Pronto para usar"))
                        .setMessage(tr(
                            "Your installed apps are available. Want to unlock factory-app management? A guided setup handles the rest.",
                            "Seus aplicativos já estão disponíveis. Quer habilitar o gerenciamento dos apps de fábrica? Uma configuração guiada cuida do restante."
                        ))
                        .setNegativeButton(tr("Later", "Depois"), null)
                        .setPositiveButton(tr("Set up", "Configurar")) { _, _ -> openGuidedSetup() }
                        .show()
                }
            }
        }
    }

    @Deprecated("Result callback for guided initial setup")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 410 && resultCode == RESULT_OK) scan(5555)
    }

    private fun openGuidedSetup() {
        startActivityForResult(Intent(this, SetupActivity::class.java), 410)
    }

    override fun onResume() {
        super.onResume()
        if (connected && sort == 1 && !busy) maybeLoadSizes()
        if (!connected) loadLocalApps()
    }

    private fun loadLocalApps() {
        try {
            val items = LocalInventory.read(this)
            if (connected) return
            model = items.device
            systemApps = items.system
            userApps = items.installed
            disabled = items.disabled
            labels = items.names
            removedApps = emptySet()
            selected.retainAll(items.system + items.installed)
            updateList()
            updateGuide()
            updateActions()
        } catch (e: Exception) {
            statusText = tr("Could not read local apps", "Não foi possível ler os apps locais")
            updateActions()
        }
    }

    private fun tr(en: String, pt: String): String = if (lang == "pt") pt else en
    private fun Int.dp() = (this * resources.displayMetrics.density + 0.5f).toInt()
    private val ink get() = Color.rgb(34, 48, 59)
    private val accent get() = Color.rgb(25, 106, 120)

    private fun label(value: String, size: Float = 14f, strong: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(ink)
            if (strong) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4.dp(), 5.dp(), 4.dp(), 5.dp())
        }

    private fun button(title: String, onTap: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 13f
        setOnClickListener { onTap() }
    }

    private fun buildUi() {
        adapter = null
        appList = null
        uninstallButton = null
        restoreButton = null
        guideList = null
        guideSelectionLabel = null
        reviewButton = null
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(250, 251, 253))
            setPadding(10.dp(), 6.dp(), 10.dp(), 8.dp())
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = label("DebloaToast", 22f, true)
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(button("ⓘ " + tr("Help", "Ajuda")) {
            startActivity(Intent(this, HelpActivity::class.java))
        })
        root.addView(header)
        statusLabel = label(deviceStatus(), 12f)
        root.addView(statusLabel)

        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        when (tab) {
            Tab.APPS -> showApps(content, root)
            Tab.GUIDE -> showGuide(content)
            Tab.TOOLS -> showTools(content)
            Tab.SETTINGS -> showSettings(content)
        }

        val credit = label(tr("Developed by Arthur Henrique Tostes Batista Aranha · 2026",
            "Desenvolvido por Arthur Henrique Tostes Batista Aranha · 2026"), 10f)
        credit.gravity = Gravity.CENTER
        root.addView(credit)

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val tabs = listOf(
            Tab.APPS to tr("Apps", "Apps"),
            Tab.GUIDE to tr("Guide", "Sugestões"),
            Tab.TOOLS to tr("Connect", "Conectar"),
            Tab.SETTINGS to tr("Settings", "Ajustes")
        )
        tabs.forEach { (destination, caption) ->
            val b = button(caption) { tab = destination; buildUi() }
            b.textSize = 11f
            b.isAllCaps = false
            b.setPadding(1.dp(), 0, 1.dp(), 0)
            b.minWidth = 0
            b.minimumWidth = 0
            b.maxLines = 1
            b.ellipsize = android.text.TextUtils.TruncateAt.END
            if (destination == tab) {
                b.backgroundTintList = ColorStateList.valueOf(accent)
                b.setTextColor(Color.WHITE)
            }
            nav.addView(b, LinearLayout.LayoutParams(0, 46.dp(), 1f))
        }
        root.addView(nav)
        setContentView(root)
        updateActions()
    }

    private fun showApps(content: LinearLayout, root: LinearLayout) {
        val search = EditText(this).apply {
            hint = tr("Search apps", "Buscar aplicativos")
            textSize = 15f
            setSingleLine(true)
            setText(query)
        }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString() ?: ""
                updateList()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        content.addView(search)

        val filters = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val categoryPicker = picker(AppGuide.categoryNames(lang == "pt") + tr("Removed", "Removidos"), category) {
            category = it; updateList()
        }
        filters.addView(categoryPicker, LinearLayout.LayoutParams(0, 44.dp(), 2f))
        val riskPicker = picker(arrayOf(tr("Any risk", "Todos os riscos"),
            tr("🟢 Green", "🟢 Verde"), tr("🟡 Yellow", "🟡 Amarelo"),
            tr("🔴 Red", "🔴 Vermelho"), tr("⚫ Black", "⚫ Preto")), riskFilter) {
            riskFilter = it; updateList()
        }
        filters.addView(riskPicker, LinearLayout.LayoutParams(0, 44.dp(), 1.35f))
        content.addView(filters)

        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val sorter = picker(arrayOf(tr("Name A–Z", "Nome A–Z"),
            tr("Size ↓", "Tamanho ↓"), "RAM ↓"), sort) {
            sort = it
            updateList()
            if (sort == 1) maybeLoadSizes()
        }
        controls.addView(sorter, LinearLayout.LayoutParams(0, 42.dp(), 1f))
        controls.addView(button(tr("Refresh", "Atualizar")) { refresh() },
            LinearLayout.LayoutParams(-2, 42.dp()))
        content.addView(controls)

        val list = ListView(this).apply {
            dividerHeight = 1.dp()
            cacheColorHint = Color.TRANSPARENT
        }
        adapter = AppsAdapter()
        list.adapter = adapter
        appList = list
        content.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        updateList()

        val action = button(tr("Uninstall", "Desinstalar")) { confirmUninstall() }
        action.backgroundTintList = ColorStateList.valueOf(Color.rgb(175, 56, 49))
        action.setTextColor(Color.WHITE)
        uninstallButton = action
        root.addView(action, LinearLayout.LayoutParams(-1, 45.dp()))
        val recovery = button(tr("Reinstall", "Reinstalar")) { confirmSelectedRestore() }
        recovery.backgroundTintList = ColorStateList.valueOf(accent)
        recovery.setTextColor(Color.WHITE)
        restoreButton = recovery
        root.addView(recovery, LinearLayout.LayoutParams(-1, 45.dp()))
    }

    private fun picker(values: Array<String>, initial: Int, onSelect: (Int) -> Unit): Spinner =
        Spinner(this).apply {
            val a = ArrayAdapter(this@DashboardActivity,
                android.R.layout.simple_spinner_item, values)
            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter = a
            setSelection(initial)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                    onSelect(position)
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

    private fun showGuide(content: LinearLayout) {
        content.addView(label(tr("Suggestions", "Sugestões"), 18f, true))
        content.addView(label(tr("Select several suggestions without leaving this tab. Black means a critical system component.",
            "Marque várias sugestões sem sair desta tela. Preto identifica um componente crítico do sistema."), 12f))
        guideSelectionLabel = label("", 13f, true)
        content.addView(guideSelectionLabel)
        reviewButton = button(tr("Review selected apps", "Revisar apps selecionados")) {
            tab = Tab.APPS
            query = ""
            category = AppGuide.ALL
            riskFilter = 0
            buildUi()
        }
        content.addView(reviewButton)
        val filters = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        filters.addView(picker(AppGuide.categoryNames(lang == "pt"), guideCategory) {
            guideCategory = it; updateGuide()
        }, LinearLayout.LayoutParams(0, 44.dp(), 2f))
        filters.addView(picker(arrayOf(tr("All levels", "Todos"),
            tr("🟢 Green", "🟢 Verde"), tr("🟡 Yellow", "🟡 Amarelo"),
            tr("🔴 Red", "🔴 Vermelho"), tr("⚫ Black", "⚫ Preto")), guideRisk) {
            guideRisk = it; updateGuide()
        }, LinearLayout.LayoutParams(0, 44.dp(), 1.35f))
        content.addView(filters)
        val list = ListView(this).apply { dividerHeight = 1.dp() }
        guideList = list
        content.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        updateGuide()
    }

    private fun updateGuide() {
        val list = guideList ?: return
        val available = systemApps + userApps
        val suggestions = AppGuide.reviewedSuggestions(available).filter { id ->
            AppGuide.matches(guideCategory, id, id in systemApps) &&
                (guideRisk == 0 || AppGuide.level(id) == guideRisk - 1)
        }
        list.adapter = object : BaseAdapter() {
            override fun getCount(): Int = if (suggestions.isEmpty()) 1 else suggestions.size
            override fun getItem(position: Int): Any =
                if (suggestions.isEmpty()) "" else suggestions[position]
            override fun getItemId(position: Int): Long = position.toLong()
            override fun isEnabled(position: Int): Boolean = suggestions.isNotEmpty()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                if (suggestions.isEmpty()) return label(tr("No suggestions for installed apps.",
                    "Nenhuma sugestão para os apps encontrados."), 14f)
                val id = suggestions[position]
                val level = AppGuide.level(id)
                val title = AppGuide.name(id, lang == "pt", labels[id] ?: id)
                val flag = arrayOf("🟢", "🟡", "🔴", "⚫")[level]
                val check = (convertView as? CheckBox) ?: CheckBox(this@DashboardActivity).apply {
                    textSize = 14f
                    setTextColor(ink)
                    setPadding(9.dp(), 8.dp(), 8.dp(), 8.dp())
                    minHeight = 70.dp()
                }
                check.setOnCheckedChangeListener(null)
                check.text = "$flag ${AppGuide.levelName(level, lang == "pt").uppercase(Locale.ROOT)}" +
                    " · $title\n${AppGuide.description(id, lang == "pt")}\n$id"
                check.isChecked = id in selected
                check.isEnabled = true
                check.setOnCheckedChangeListener { _, checked ->
                    if (checked) selected.add(id) else selected.remove(id)
                    updateActions()
                }
                check.setOnLongClickListener {
                    AlertDialog.Builder(this@DashboardActivity).setTitle(title)
                        .setMessage("$id\n\n" + AppGuide.description(id, lang == "pt"))
                        .setPositiveButton("OK", null).show()
                    true
                }
                return check
            }
        }
        updateActions()
    }

    private fun showTools(content: LinearLayout) {
        content.addView(label(tr("ADB connection", "Conexão ADB"), 18f, true))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pair = button(tr("Pair", "Parear")) {
                try {
                    PairingReceiver.prepare(this)
                    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                } catch (e: Exception) {
                    toast(e.message ?: tr("Settings unavailable", "Configurações indisponíveis"))
                }
            }
            content.addView(pair)
        } else {
            content.addView(button(tr("Set up with Windows", "Configurar com Windows")) {
                openGuidedSetup()
            })
        }
        val connect = button(tr("CONNECT", "CONECTAR")) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                scan(5555)
            } else {
                val saved = prefs.getString("connection_port", "")?.toIntOrNull()
                if (saved != null && saved in 1..65535) scan(saved)
                else toast(tr("Enter the connection port in the pairing notification first.", "Informe a porta de conexão na notificação de pareamento."))
            }
        }
        connect.textSize = 19f
        connect.minHeight = 64.dp()
        connect.backgroundTintList = ColorStateList.valueOf(Color.rgb(184, 45, 43))
        connect.setTextColor(Color.WHITE)
        connect.isEnabled = readyToConnect() && !busy
        content.addView(connect)

    }

    private fun showSettings(content: LinearLayout) {
        content.addView(label(tr("Language", "Idioma"), 18f, true))
        content.addView(button("English" + if (lang == "en") " ✓" else "") {
            setLanguage("en")
        })
        content.addView(button("Português" + if (lang == "pt") " ✓" else "") {
            setLanguage("pt")
        })
        content.addView(label(tr("Storage access", "Acesso ao armazenamento"), 18f, true))
        val granted = PackageMetrics.hasUsageAccess(this)
        content.addView(button(if (granted) tr("Access granted", "Acesso autorizado")
            else tr("Allow usage access", "Permitir acesso a dados de uso")) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        })
        content.addView(label(tr("Required to sort by full app size.",
            "Necessário para ordenar pelo tamanho completo."), 12f))
        content.addView(label("DebloaToast Expert v0.12", 15f, true))
        content.addView(button(tr("Help and PDF guide", "Ajuda e tutorial em PDF")) {
            startActivity(Intent(this, HelpActivity::class.java))
        })
    }

    private fun setLanguage(code: String) {
        lang = code
        prefs.edit().putString("language", code).apply()
        buildUi()
    }

    private fun deviceStatus(): String = when {
        busy -> statusText.ifBlank { tr("Working…", "Processando…") }
        systemApps.isNotEmpty() || userApps.isNotEmpty() ->
            "$model · ${systemApps.size + userApps.size} ${tr("apps", "apps")}" +
                if (connected) tr(" · ADB connected", " · ADB conectado")
                else tr(" · local scan (no ADB)", " · leitura local (sem ADB)")
        else -> statusText.ifBlank { tr("No apps found", "Nenhum app encontrado") }
    }

    private fun updateActions() {
        uninstallButton?.apply {
            text = tr("Uninstall", "Desinstalar") + " (${selected.count { it !in removedApps }})"
            isEnabled = !busy && selected.any { it !in removedApps }
        }
        restoreButton?.apply {
            val count = selected.count { it in removedApps }
            text = tr("Reinstall", "Reinstalar") + " ($count)"
            isEnabled = connected && !busy && count > 0
        }
        guideSelectionLabel?.text = tr("Selected", "Selecionados") + ": ${selected.size}"
        reviewButton?.isEnabled = selected.isNotEmpty()
        statusLabel?.text = deviceStatus()
    }

    private fun updateList() {
        val all = systemApps + userApps + removedApps
        val term = query.trim().lowercase(Locale.ROOT)
        val matches = all.filter { name ->
            (if (category == AppGuide.categoryNames(lang == "pt").size) name in removedApps
                else AppGuide.matches(category, name, name in systemApps || name in removedApps)) &&
                (riskFilter == 0 || AppGuide.level(name) == riskFilter - 1) &&
                (term.isEmpty() || name.lowercase(Locale.ROOT).contains(term) ||
                    AppGuide.name(name, lang == "pt", labels[name] ?: name)
                        .lowercase(Locale.ROOT).contains(term))
        }
        val ordered = when (sort) {
            1 -> matches.sortedWith(compareByDescending<String> { sizes[it] ?: -1L }
                .thenBy { AppGuide.name(it, lang == "pt", labels[it] ?: it).lowercase(Locale.ROOT) })
            2 -> matches.sortedWith(compareByDescending<String> { ram[it] ?: -1L }
                .thenBy { AppGuide.name(it, lang == "pt", labels[it] ?: it).lowercase(Locale.ROOT) })
            else -> matches.sortedBy { AppGuide.name(it, lang == "pt", labels[it] ?: it).lowercase(Locale.ROOT) }
        }
        adapter?.items = ordered
        adapter?.notifyDataSetChanged()
        updateActions()
    }

    private inner class AppsAdapter : BaseAdapter() {
        var items = emptyList<String>()
        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Any = items[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val id = items[position]
            val checkbox = convertView as? CheckBox ?: CheckBox(this@DashboardActivity).apply {
                textSize = 14f
                setTextColor(ink)
                setPadding(9.dp(), 5.dp(), 8.dp(), 5.dp())
                minHeight = 58.dp()
            }
            val isRemoved = id in removedApps
            val kind = when { isRemoved -> tr("REMOVED", "REMOVIDO"); id in systemApps -> tr("system", "sistema"); else -> tr("installed", "instalado") }
            val size = PackageMetrics.bytesLabel(sizes[id])
            val memory = PackageMetrics.ramLabel(ram[id])
            val risk = AppGuide.level(id)
            val flag = arrayOf("🟢", "🟡", "🔴", "⚫")[risk]
            val group = AppGuide.categoryNames(lang == "pt")[AppGuide.category(id)]
            checkbox.setOnCheckedChangeListener(null)
            checkbox.text = "${AppGuide.name(id, lang == "pt", labels[id] ?: id)}  $flag\n" +
                "$group · $kind · $size · RAM $memory\n$id"
            checkbox.isChecked = id in selected
            checkbox.alpha = if (isRemoved) 0.58f else 1f
            checkbox.setOnLongClickListener {
                AlertDialog.Builder(this@DashboardActivity)
                    .setTitle(AppGuide.name(id, lang == "pt", labels[id] ?: id))
                    .setMessage("$id\n\n" + AppGuide.description(id, lang == "pt") +
                        "\n\n" + tr("Risk", "Risco") + ": " + AppGuide.levelName(risk, lang == "pt"))
                    .setPositiveButton("OK", null).show()
                true
            }
            checkbox.setOnCheckedChangeListener { _, checked ->
                if (checked) selected.add(id) else selected.remove(id)
                updateActions()
            }
            return checkbox
        }
    }

    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_LONG).show()

    private fun isValidId(id: String): Boolean = id == "android" ||
        id.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))

    private fun parsePackages(text: String): Set<String> = text.lineSequence()
        .map { it.trim() }.filter { it.startsWith("package:") }
        .map { it.removePrefix("package:") }.filter(::isValidId).toSet()

    private fun port(): Int = prefs.getString("connection_port", "")?.toIntOrNull()
        ?.takeIf { it in 1..65535 } ?: error(tr("Connect in Tools first", "Conecte em Ferramentas primeiro"))

    private fun task(message: String, automatic: Boolean = false, work: (Int) -> Unit) {
        if (busy) return
        val p = try { port() } catch (e: Exception) { toast(e.message ?: message); return }
        busy = true
        statusText = message
        updateActions()
        worker.execute {
            try { work(p) }
            catch (e: Exception) {
                runOnUiThread {
                    if (automatic) {
                        connected = false
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                            statusText = tr("Basic mode · tap Connect to set up factory apps",
                                "Modo básico · toque em Conectar para configurar apps de fábrica")
                            updateActions()
                        } else showSetupDialog(tr(
                            "Could not reconnect. Turn on Wireless debugging and pair this app once if needed.",
                            "Não foi possível reconectar. Ative a depuração sem fio e pareie este app se necessário."
                        ))
                    } else {
                        AlertDialog.Builder(this).setTitle(tr("Operation failed", "Operação não concluída"))
                            .setMessage((e.message ?: "ADB disconnected").take(400))
                            .setPositiveButton("OK", null).show()
                    }
                }
            } finally {
                runOnUiThread {
                    busy = false
                    updateActions()
                    if (tab == Tab.APPS && sort == 1 && connected) maybeLoadSizes()
                }
            }
        }
    }

    private fun scan(p: Int, automatic: Boolean = false) {
        prefs.edit().putString("connection_port", p.toString()).apply()
        task(tr("Connecting…", "Conectando…"), automatic) { connection ->
            val currentModel = AdbCore.command(this, connection, "getprop ro.product.model").trim()
            val systems = parsePackages(AdbCore.command(this, connection, "pm list packages -s --user 0"))
            val users = parsePackages(AdbCore.command(this, connection, "pm list packages -3 --user 0"))
            val disabledNow = parsePackages(AdbCore.command(this, connection, "pm list packages -d --user 0"))
            val names = (systems + users).associateWith { id ->
                try {
                    val info = packageManager.getApplicationInfo(id, 0)
                    packageManager.getApplicationLabel(info).toString().ifBlank { id }
                } catch (_: Exception) { id }
            }
            val currentRam = try {
                PackageMetrics.ramPssKb(AdbCore.command(this, connection, "dumpsys meminfo"), systems + users)
            } catch (_: Exception) { emptyMap() }
            val missing = missingSystemPackages(connection, systems)
            runOnUiThread {
                model = currentModel
                connected = true
                systemApps = systems
                userApps = users
                disabled = disabledNow
                labels = names
                ram = currentRam
                sizes = emptyMap()
                sizeRequested = false
                removedApps = missing
                selected.retainAll(systems + users + removedApps)
                tab = Tab.APPS
                buildUi()
            }
        }
    }

    private fun refresh() {
        if (!connected) { loadLocalApps(); buildUi(); return }
        scan(port())
    }

    private fun maybeLoadSizes() {
        if (sort != 1 || !connected || sizeRequested || !PackageMetrics.hasUsageAccess(this)) return
        sizeRequested = true
        task(tr("Reading storage…", "Lendo armazenamento…")) { _ ->
            val result = PackageMetrics.storageBytes(this, systemApps + userApps)
            runOnUiThread { sizes = result; updateList() }
        }
    }

    private fun removed(): MutableSet<String> = prefs.getStringSet("factory_removed", emptySet())
        ?.toMutableSet() ?: mutableSetOf()
    private fun wasDisabled(): MutableSet<String> = prefs.getStringSet("factory_was_disabled", emptySet())
        ?.toMutableSet() ?: mutableSetOf()
    private fun saveHistory(items: Set<String>, originalDisabled: Set<String>) {
        prefs.edit().putStringSet("factory_removed", items.toSet())
            .putStringSet("factory_was_disabled", originalDisabled.toSet()).commit()
    }

    private fun confirmUninstall() {
        val selectedOnDevice = selected.filter { it in systemApps || it in userApps }.filter(::isValidId)
        if (selectedOnDevice.isEmpty() || busy) return
        // All user-selected packages remain eligible; severity is explained in confirmation.
        val targets = selectedOnDevice
        if (!connected) {
            val first = targets.firstOrNull { it in userApps }
            if (first == null) {
                openGuidedSetup()
                return
            }
            AlertDialog.Builder(this)
                .setTitle(tr("Uninstall with Android", "Desinstalar pelo Android"))
                .setMessage(tr(
                    "Without ADB, Android will ask permission to remove each regular app. " +
                        "Starting with: ${labels[first] ?: first}. Other selections stay saved.",
                    "Sem ADB, o Android solicitará confirmação para cada app comum. " +
                        "Começando por: ${labels[first] ?: first}. As outras seleções continuam marcadas."))
                .setNegativeButton(tr("Cancel", "Cancelar"), null)
                .setPositiveButton(tr("Continue", "Continuar")) { _, _ ->
                    try {
                        startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$first")))
                    } catch (e: Exception) {
                        toast(e.message ?: tr("Unable to open Android uninstall", "Não foi possível abrir a desinstalação"))
                    }
                }.show()
            return
        }
        val systemCount = targets.count { it in systemApps }
        val preview = targets.take(7).joinToString("\n") { labels[it] ?: it }
        val more = if (targets.size > 7) "\n+${targets.size - 7}" else ""
        val criticalTargets = targets.filter { it in systemApps && AppGuide.level(it) == 3 }
        val criticalCount = criticalTargets.size
        val unknownCount = targets.count { it in systemApps && AppGuide.level(it) == 2 }
        val criticalList = criticalTargets.take(12).joinToString("\n") { "• $it" } +
            if (criticalCount > 12) "\n+${criticalCount - 12}" else ""
        val warning = when {
            criticalCount > 0 -> tr(
                "\n\n⚫ BLACK — CRITICAL ($criticalCount)\n$criticalList\n\nThese changes can make the device unusable, prevent Android from starting, disable calls, banking or recovery through ADB, and cause permanent data loss. Restoring the original firmware may be necessary or may not work. You are responsible for choosing which packages to remove; reversal is not guaranteed.",
                "\n\n⚫ PRETO — CRÍTICO ($criticalCount)\n$criticalList\n\nEssas alterações podem inutilizar o aparelho, impedir o Android de iniciar, desativar chamadas, bancos ou a recuperação por ADB e causar perda permanente de dados. Reinstalar o firmware pode ser necessário ou não resolver. Você é responsável por escolher quais pacotes remover; a reversão não é garantida."
            )
            systemCount > 0 -> tr(
                "\n\nWarning: $unknownCount red system packages have unverified dependencies. Removal may break Android, banking or ADB, and recovery is not guaranteed.",
                "\n\nAtenção: $unknownCount pacotes vermelhos têm dependências não verificadas. A remoção pode afetar Android, bancos ou ADB; a recuperação não é garantida."
            )
            else -> tr("\n\nInstalled apps may lose their data.", "\n\nApps instalados podem perder dados.")
        }
        AlertDialog.Builder(this).setTitle(tr("Uninstall ${targets.size} apps?", "Desinstalar ${targets.size} apps?"))
            .setMessage(preview + more + warning)
            .setNegativeButton(tr("Cancel", "Cancelar"), null)
            .setPositiveButton(tr("Uninstall", "Desinstalar")) { _, _ -> uninstall(targets) }.show()
    }

    private fun uninstall(targets: List<String>) {
        task(tr("Uninstalling…", "Desinstalando…")) { p ->
            var successes = 0
            var failures = 0
            for (id in targets) {
                if (!isValidId(id)) continue
                val system = id in systemApps
                if (!system && id !in userApps) continue
                if (system) {
                    val history = removed()
                    val previous = wasDisabled()
                    history.add(id)
                    if (id in disabled) previous.add(id) else previous.remove(id)
                    saveHistory(history, previous)
                }
                try {
                    val command = if (system) "pm uninstall -k --user 0 $id"
                        else "pm uninstall --user 0 $id"
                    val response = AdbCore.command(this, p, command)
                    if (!response.trim().startsWith("Success", ignoreCase = true)) error(response.take(160))
                    successes++
                } catch (_: Exception) {
                    if (system) {
                        val stillInstalled = try {
                            id in parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
                        } catch (_: Exception) { false }
                        if (stillInstalled) {
                            val history = removed(); val previous = wasDisabled()
                            history.remove(id); previous.remove(id)
                            saveHistory(history, previous)
                        }
                    }
                    failures++
                }
            }
            runOnUiThread {
                selected.clear()
                toast(tr("Removed: $successes · Failed: $failures", "Removidos: $successes · Falhas: $failures"))
            }
            val systems = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
            val users = parsePackages(AdbCore.command(this, p, "pm list packages -3 --user 0"))
            val missing = missingSystemPackages(p, systems)
            runOnUiThread {
                removedApps = missing
                systemApps = systems
                userApps = users
                buildUi()
            }
        }
    }

    private fun missingSystemPackages(p: Int, installed: Set<String>): Set<String> {
        val available = parsePackages(AdbCore.command(this, p, "pm list packages -u -s --user 0"))
        return (available - installed).filter(::isValidId).toSet()
    }

    private fun confirmSelectedRestore() {
        val targets = selected.filter { it in removedApps }.filter(::isValidId)
        if (!connected || busy || targets.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle(tr("Reinstall ${targets.size} selected apps?", "Reinstalar ${targets.size} apps selecionados?"))
            .setMessage(targets.joinToString("\n") + "\n\n" +
                tr("Only the selected removed packages will be reinstalled.",
                   "Somente os pacotes removidos selecionados serão reinstalados."))
            .setNegativeButton(tr("Cancel", "Cancelar"), null)
            .setPositiveButton(tr("Reinstall", "Reinstalar")) { _, _ -> restore(targets) }.show()
    }

    private fun restore(targets: List<String>) {
        task(tr("Restoring…", "Restaurando…")) { p ->
            var successes = 0
            for (id in targets) {
                try {
                    AdbCore.command(this, p, "cmd package install-existing --user 0 $id")
                    if (id in wasDisabled()) AdbCore.command(this, p, "pm disable-user --user 0 $id")
                    val present = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
                    if (id !in present) error("Not installed")
                    val history = removed(); val previous = wasDisabled()
                    history.remove(id); previous.remove(id)
                    saveHistory(history, previous)
                    successes++
                } catch (_: Exception) { }
            }
            runOnUiThread { toast(tr("Restored: $successes", "Restaurados: $successes")) }
            scanAfterRestore(p)
        }
    }

    private fun scanAfterRestore(p: Int) {
        val systems = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
        val users = parsePackages(AdbCore.command(this, p, "pm list packages -3 --user 0"))
        val missing = missingSystemPackages(p, systems)
        runOnUiThread {
            removedApps = missing
            selected.removeAll(removedApps.intersect(selected)) // selections are reset after recovery
            selected.clear()
            systemApps = systems
            userApps = users
            buildUi()
        }
    }

    override fun onDestroy() {
        discovery?.cancel()
        discovery = null
        worker.shutdown()
        super.onDestroy()
    }
}
