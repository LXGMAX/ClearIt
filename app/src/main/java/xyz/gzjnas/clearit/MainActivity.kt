package xyz.gzjnas.clearit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat


@SuppressLint("RestrictedApi")
class MainActivity : ComponentActivity() {
    private lateinit var clearButton: Button
    private lateinit var switchAutoClear: Switch
    private lateinit var switchGoHome: Switch
    private lateinit var switchTheme: Switch
    private lateinit var textGoHome: TextView
    private lateinit var sharedPreferences: SharedPreferences

    // 主题相关视图
    private lateinit var rootLayout: LinearLayout
    private lateinit var textTitle: TextView
    private lateinit var textTheme: TextView
    private lateinit var textAutoClear: TextView
    private lateinit var containerTheme: LinearLayout
    private lateinit var containerAutoClear: LinearLayout

    private companion object {
        const val PREFS_NAME = "ClipboardSettings"
        const val KEY_AUTO_CLEAR = "auto_clear"
        const val KEY_GO_HOME = "go_home"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_USE_SYSTEM_THEME = "use_system_theme"
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initViews()
        loadSettings()
        applyTheme()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()

        // 如果"应用打开时清除"开关打开，则自动清空剪贴板
        if (switchAutoClear.isChecked) {
            clearClipboard()
        }
    }

    private fun initViews() {
        clearButton = findViewById(R.id.btn_clear)
        switchAutoClear = findViewById(R.id.switch_auto_clear)
        switchGoHome = findViewById(R.id.switch_go_home)
        switchTheme = findViewById(R.id.switch_theme)
        textGoHome = findViewById(R.id.text_go_home)

        // 主题相关视图
        rootLayout = findViewById(R.id.root_layout)
        textTitle = findViewById(R.id.text_title)
        textTheme = findViewById(R.id.text_theme)
        textAutoClear = findViewById(R.id.text_auto_clear)
        containerTheme = findViewById(R.id.container_theme)
        containerAutoClear = findViewById(R.id.container_auto_clear)

        switchGoHome.visibility = Switch.GONE
        textGoHome.visibility = TextView.GONE
    }

    private fun loadSettings() {
        // 加载保存的设置，默认值：自动清除开启，回到桌面关闭，使用系统主题
        val autoClear = sharedPreferences.getBoolean(KEY_AUTO_CLEAR, true)
        val goHome = sharedPreferences.getBoolean(KEY_GO_HOME, false)
        val useSystemTheme = sharedPreferences.getBoolean(KEY_USE_SYSTEM_THEME, true)
        val darkMode = if (useSystemTheme) {
            isSystemDarkMode()
        } else {
            sharedPreferences.getBoolean(KEY_DARK_MODE, true)
        }

        switchAutoClear.isChecked = autoClear
        switchGoHome.isChecked = goHome
        switchTheme.isChecked = darkMode
    }

    private fun isSystemDarkMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private fun saveSettings() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_AUTO_CLEAR, switchAutoClear.isChecked)
        editor.putBoolean(KEY_GO_HOME, switchGoHome.isChecked)
        editor.putBoolean(KEY_DARK_MODE, switchTheme.isChecked)
        editor.putBoolean(KEY_USE_SYSTEM_THEME, false) // 手动切换后不再跟随系统
        editor.apply()
    }

    private fun applyTheme() {
        val isDark = switchTheme.isChecked

        // 更新背景色
        rootLayout.setBackgroundColor(
            ContextCompat.getColor(this, if (isDark) R.color.dark_black else R.color.light_background)
        )

        // 更新文字颜色
        val textColor = ContextCompat.getColor(this, if (isDark) R.color.white else R.color.light_text)
        textTitle.setTextColor(textColor)
        textTheme.setTextColor(textColor)
        textAutoClear.setTextColor(textColor)

        // 更新卡片背景
        val cardBg = if (isDark) R.drawable.bg_switch_container else R.drawable.bg_switch_container_light
        containerTheme.setBackgroundResource(cardBg)
        containerAutoClear.setBackgroundResource(cardBg)

        // 更新状态栏和导航栏
        updateSystemBars(isDark)
    }

    private fun updateSystemBars(isDark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 及以上
            window.insetsController?.apply {
                setSystemBarsAppearance(
                    if (isDark) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
                setSystemBarsAppearance(
                    if (isDark) 0 else WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            }
            window.statusBarColor = ContextCompat.getColor(this, if (isDark) R.color.dark_black else R.color.light_background)
            window.navigationBarColor = ContextCompat.getColor(this, if (isDark) R.color.dark_black else R.color.light_background)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 及以上
            window.statusBarColor = ContextCompat.getColor(this, if (isDark) R.color.dark_black else R.color.light_background)
            window.navigationBarColor = ContextCompat.getColor(this, if (isDark) R.color.dark_black else R.color.light_background)

            if (!isDark) {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = 0
            }
        }
    }

    private fun setupClickListeners() {
        // 清空按钮点击事件
        clearButton.setOnClickListener {
            clearClipboard()
        }

        // 主题开关
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            saveSettings()
            applyTheme()
            val mode = if (isChecked) "暗黑" else "明亮"
            Toast.makeText(this, "已切换到${mode}模式", Toast.LENGTH_SHORT).show()
        }

        // 开关状态改变事件
        switchAutoClear.setOnCheckedChangeListener { _, isChecked ->
            saveSettings()
            if (isChecked) {
                Toast.makeText(this, "已开启应用打开时自动清除", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "已关闭应用打开时自动清除", Toast.LENGTH_SHORT).show()
            }
        }

        switchGoHome.setOnCheckedChangeListener { _, isChecked ->
            saveSettings()
            if (isChecked) {
                Toast.makeText(this, "已开启清除后回到桌面", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "已关闭清除后回到桌面", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearClipboard() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", "")
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "剪贴板已清空", Toast.LENGTH_SHORT).show()

            // 如果"清除后回到桌面"开关打开，则返回桌面
            /*if (switchGoHome.isChecked) {
                goToHomeScreen()
            }*/

        } catch (e: Exception) {
            Toast.makeText(this, "清空剪贴板时出错", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        homeIntent.setClassName("com.android.launcher", "com.miui.home")
        startActivity(homeIntent)
        Toast.makeText(this, "已清除, 回到桌面", Toast.LENGTH_SHORT).show()
    }
}
