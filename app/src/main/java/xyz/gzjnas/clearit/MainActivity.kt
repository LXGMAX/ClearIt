package xyz.gzjnas.clearit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ComponentActivity
import org.w3c.dom.Text


@SuppressLint("RestrictedApi")
class MainActivity : ComponentActivity() {
    private lateinit var clearButton: Button
    private lateinit var switchAutoClear: Switch
    private lateinit var switchGoHome: Switch
    private lateinit var textGoHome: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private companion object {
        const val PREFS_NAME = "ClipboardSettings"
        const val KEY_AUTO_CLEAR = "auto_clear"
        const val KEY_GO_HOME = "go_home"
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initViews()
        loadSettings()
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
        textGoHome = findViewById(R.id.text_go_home)

        switchGoHome.visibility = Switch.GONE
        textGoHome.visibility = TextView.GONE
    }

    private fun loadSettings() {
        // 加载保存的设置，默认值：自动清除开启，回到桌面关闭
        val autoClear = sharedPreferences.getBoolean(KEY_AUTO_CLEAR, true)
        val goHome = sharedPreferences.getBoolean(KEY_GO_HOME, false)

        switchAutoClear.isChecked = autoClear
        switchGoHome.isChecked = goHome
    }

    private fun saveSettings() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_AUTO_CLEAR, switchAutoClear.isChecked)
        editor.putBoolean(KEY_GO_HOME, switchGoHome.isChecked)
        editor.apply()
    }

    private fun setupClickListeners() {
        // 清空按钮点击事件
        clearButton.setOnClickListener {
            clearClipboard()
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