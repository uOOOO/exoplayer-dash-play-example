package com.uooo.exoplayer.sample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.EventLogger
import com.uooo.exoplayer.sample.player.ExoPlayerPlayManager

class MainActivity : AppCompatActivity() {
    private lateinit var playManager: ExoPlayerPlayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playManager = ExoPlayerPlayManager()
        val playerView: PlayerView = findViewById(R.id.playerView)

        val contentUri =
//            Uri.parse("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd")
//            Uri.parse("https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd")
            Uri.parse("https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv")
        val adTagUri =
            Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=")
        val licenseUrl = "https://proxy.uat.widevine.com/proxy?provider=widevine_test"

        playManager.prepare(this, playerView, licenseUrl, contentUri, adTagUri)
        playManager.addAnalyticsListener(EventLogger(playManager.trackSelector))
        playManager.start()
    }

    override fun onStop() {
        playManager.pause()
        super.onStop()
    }

    override fun onDestroy() {
        playManager.release()
        super.onDestroy()
    }
}
