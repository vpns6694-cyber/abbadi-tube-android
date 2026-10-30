package com.github.libretube.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.github.libretube.BuildConfig
import com.github.libretube.R
import com.github.libretube.ui.activities.ForceUpdateActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * ABBADI Tube self-update via remote update.json on GitHub (replaces LibreTube's upstream checker).
 * Developer: Abdalla Abbadi · connect@3bbadi.com
 */
data class AbbadiUpdate(
    val latestCode: Int, val latestName: String, val apkUrl: String, val minCode: Int,
    val notes: String, val forceMessage: String
)

class UpdateChecker(private val context: Context) {

    suspend fun checkUpdate(isManualCheck: Boolean = false) {
        val info = try { fetch() } catch (e: Exception) {
            e.printStackTrace()
            if (isManualCheck) toast(R.string.abbadi_update_failed)
            return // fail open
        }
        withContext(Dispatchers.Main) {
            when {
                BuildConfig.VERSION_CODE < info.minCode -> {
                    context.startActivity(ForceUpdateActivity.intent(context, info).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    (context as? Activity)?.finish()
                }
                BuildConfig.VERSION_CODE < info.latestCode -> showDialog(info)
                isManualCheck -> Toast.makeText(context, R.string.app_uptodate, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun toast(res: Int) = withContext(Dispatchers.Main) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    private fun showDialog(info: AbbadiUpdate) {
        val act = context as? FragmentActivity ?: return
        if (act.isFinishing) return
        MaterialAlertDialogBuilder(act)
            .setTitle(act.getString(R.string.abbadi_update_title, info.latestName))
            .setMessage(info.notes)
            .setNegativeButton(R.string.abbadi_later, null)
            .setPositiveButton(R.string.abbadi_update_now) { _, _ -> startDownload(act, info) }
            .show()
    }

    companion object {
        const val UPDATE_JSON = "https://raw.githubusercontent.com/vpns6694-cyber/abbadi-tube-android/main/update.json"

        fun isArabic() = Locale.getDefault().language == "ar"

        fun fetch(): AbbadiUpdate {
            val c = URL("$UPDATE_JSON?t=${System.currentTimeMillis()}").openConnection() as HttpURLConnection
            c.connectTimeout = 8000; c.readTimeout = 8000; c.useCaches = false
            c.setRequestProperty("Cache-Control", "no-cache")
            val j = c.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            val ar = isArabic()
            return AbbadiUpdate(
                j.optInt("latestVersionCode", 0), j.optString("latestVersionName"), j.optString("apkUrl"),
                j.optInt("minSupportedVersionCode", 0),
                j.optString(if (ar) "notesAr" else "notesEn"),
                j.optString(if (ar) "forceMessageAr" else "forceMessageEn")
            )
        }

        /** Downloads the APK with a progress dialog, then hands it to the system installer. */
        fun startDownload(act: FragmentActivity, info: AbbadiUpdate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !act.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(act, R.string.abbadi_allow_install, Toast.LENGTH_LONG).show()
                act.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${act.packageName}")))
                return
            }
            val bar = LinearProgressIndicator(act).apply { max = 100; isIndeterminate = true }
            val label = TextView(act).apply { text = "0%" }
            val box = LinearLayout(act).apply {
                orientation = LinearLayout.VERTICAL; val p = (24 * resources.displayMetrics.density).toInt()
                setPadding(p, p / 2, p, 0); addView(bar); addView(label)
            }
            val dlg = MaterialAlertDialogBuilder(act).setTitle(R.string.abbadi_downloading)
                .setView(box).setCancelable(false).show()
            act.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dir = File(act.cacheDir, "updates").apply { mkdirs() }
                    val out = File(dir, "abbadi-update.apk")
                    val c = URL(info.apkUrl).openConnection() as HttpURLConnection
                    c.instanceFollowRedirects = true
                    val total = c.contentLengthLong
                    c.inputStream.use { inp -> out.outputStream().use { o ->
                        val buf = ByteArray(64 * 1024); var done = 0L; var last = -1
                        while (true) {
                            val n = inp.read(buf); if (n < 0) break
                            o.write(buf, 0, n); done += n
                            if (total > 0) { val pct = (done * 100 / total).toInt()
                                if (pct != last) { last = pct; withContext(Dispatchers.Main) {
                                    bar.isIndeterminate = false; bar.progress = pct; label.text = "$pct%" } } }
                        }
                    } }
                    withContext(Dispatchers.Main) { dlg.dismiss(); install(act, out) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) { dlg.dismiss(); Toast.makeText(act, R.string.abbadi_update_failed, Toast.LENGTH_LONG).show() }
                }
            }
        }

        private fun install(act: Activity, file: File) {
            val uri = FileProvider.getUriForFile(act, "${act.packageName}.fileprovider", file)
            act.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
