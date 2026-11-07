package com.agentai.voice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var heard: TextView
    private lateinit var mic: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 41)
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 30, 28, 30)
            setBackgroundColor(Color.rgb(5, 8, 13))
        }
        val title = TextView(this).apply {
            text = "AGENT AI"
            textSize = 32f
            letterSpacing = .16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(0, 217, 255))
        }
        status = TextView(this).apply {
            text = "ГОЛОСОВОЙ ПОМОЩНИК"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 18, 0, 24)
        }
        mic = Button(this).apply {
            text = "🎙  СЛУШАТЬ"
            textSize = 19f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(0, 115, 155))
            setOnClickListener { listen() }
        }
        heard = TextView(this).apply {
            text = "Скажи: «Открой YouTube» или «Открой HAPP»"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 30, 0, 20)
        }
        val hint = TextView(this).apply {
            text = "Работает по нажатию на микрофон • Русский язык"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(125, 145, 160))
        }
        root.addView(title)
        root.addView(status)
        root.addView(mic)
        root.addView(heard)
        root.addView(hint)
        setContentView(root)
    }

    private fun listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Распознавание речи недоступно на устройстве", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите команду")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try { startActivityForResult(intent, 42) }
        catch (_: Exception) { Toast.makeText(this, "Не удалось запустить распознавание", Toast.LENGTH_SHORT).show() }
    }

    @Deprecated("Activity result API kept for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 42 || resultCode != RESULT_OK) return
        val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: return
        heard.text = "Распознано: «$result»"
        status.text = "ВЫПОЛНЯЮ КОМАНДУ…"
        launchByVoice(result)
    }

    private fun launchByVoice(raw: String) {
        val command = raw.lowercase(Locale("ru", "RU"))
            .replace("открой", "").replace("запусти", "").replace("включи", "")
            .replace("пожалуйста", "").trim()
        val aliases = mapOf(
            "youtube" to listOf("youtube", "ютуб", "ю туб"),
            "happ" to listOf("happ", "хапп", "хэпп"),
            "chrome" to listOf("chrome", "хром"),
            "telegram" to listOf("telegram", "телеграм", "телеграмм"),
            "settings" to listOf("настройки", "settings")
        )
        if (command.contains("настройки") || command.contains("settings")) {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)); status.text = "ГОТОВО"; return
        }
        val target = aliases.entries.firstOrNull { (_, words) -> words.any { command.contains(it) } }
        if (target == null) {
            status.text = "КОМАНДА НЕ РАСПОЗНАНА"
            Toast.makeText(this, "Попробуй: открой YouTube или HAPP", Toast.LENGTH_LONG).show()
            return
        }
        val words = target.value
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val found = apps.firstOrNull { app ->
            val label = packageManager.getApplicationLabel(app).toString().lowercase(Locale.ROOT)
            words.any { label.contains(it.lowercase(Locale.ROOT)) } ||
                (target.key == "youtube" && app.packageName == "com.google.android.youtube") ||
                (target.key == "happ" && app.packageName.contains("happ", true))
        }
        if (found != null) {
            val launch = packageManager.getLaunchIntentForPackage(found.packageName)
            if (launch != null) { startActivity(launch); status.text = "ЗАПУЩЕНО: ${packageManager.getApplicationLabel(found)}"; return }
        }
        if (target.key == "youtube") {
            try { startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://youtube.com"))); status.text = "ОТКРЫВАЮ YOUTUBE"; return }
            catch (_: Exception) {}
        }
        status.text = "ПРИЛОЖЕНИЕ НЕ НАЙДЕНО"
        Toast.makeText(this, "Установи приложение ${target.key.uppercase(Locale.ROOT)}", Toast.LENGTH_LONG).show()
    }
}
