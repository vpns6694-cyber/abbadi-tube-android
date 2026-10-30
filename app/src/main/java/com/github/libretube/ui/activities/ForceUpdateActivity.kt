package com.github.libretube.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.github.libretube.databinding.ActivityForceUpdateBinding
import com.github.libretube.ui.base.BaseActivity
import com.github.libretube.util.AbbadiUpdate
import com.github.libretube.util.UpdateChecker

/** Blocking screen shown when this version is below minSupportedVersionCode. No dismiss. */
class ForceUpdateActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityForceUpdateBinding.inflate(layoutInflater)
        setContentView(b.root)
        val info = AbbadiUpdate(0, intent.getStringExtra(N) ?: "", intent.getStringExtra(U) ?: "", 0, "",
            intent.getStringExtra(M) ?: "")
        if (info.forceMessage.isNotBlank()) b.message.text = info.forceMessage
        b.update.setOnClickListener { UpdateChecker.startDownload(this, info) }
        b.developer.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:connect@3bbadi.com")))
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { moveTaskToBack(true) }
        })
    }

    companion object {
        private const val N = "n"; private const val U = "u"; private const val M = "m"
        fun intent(ctx: Context, i: AbbadiUpdate) = Intent(ctx, ForceUpdateActivity::class.java)
            .putExtra(N, i.latestName).putExtra(U, i.apkUrl).putExtra(M, i.forceMessage)
    }
}
