package com.ia.democast

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private var castContext: CastContext? = null
    private var castSession: CastSession? = null

    enum class PlaybackLocation {
        LOCAL,
        REMOTE
    }

    private var playbackLocation = PlaybackLocation.LOCAL

    private val sessionManagerListener = object : EstimateSessionManagerListener {

        override fun onSessionStarted(pCastSession: CastSession?, p1: String?) =
            this.run {
                onApplicationConnected(pCastSession)
                updatePlaybackLocation(PlaybackLocation.REMOTE)

            }

        override fun onSessionResumed(pCastSession: CastSession?, wasSuspended: Boolean) =
            this.run {
                onApplicationConnected(pCastSession)
                updatePlaybackLocation(PlaybackLocation.REMOTE)
            }

        override fun onSessionEnded(pCastSession: CastSession, error: Int) =
            this.run {
                if (pCastSession == castSession) {
                    //cleanup()
                }
                label_cast.text = "Desconectado"
                updatePlaybackLocation(PlaybackLocation.LOCAL)
                invalidateOptionsMenu()
            }

        fun onApplicationConnected(pCastSession: CastSession?) {
            castSession = pCastSession
            invalidateOptionsMenu()
            if (pCastSession?.isConnected == true) {
                label_cast.text = "Conectado"
            } else {
                label_cast.text = "Desconectado"
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        this.castContext = CastContext.getSharedInstance(this)
        this.castSession = castContext?.sessionManager?.currentCastSession
        this.play.setOnClickListener {
            if (this.playbackLocation == PlaybackLocation.REMOTE) {
                this.loadRemoteMedia("https://msscf.cinepolisklic.com/usp-s3-storage/clear/cada-dia/cada-dia.ism/Manifest?filter=(type==%22audio%22)%7C%7C(type==%22textstream%22)%7C%7C(type==%22video%22%26%26MaxHeight%3C=480)")
            } else {
                Toast.makeText(this, "No hay un chromecast conectado...", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        this.castContext?.sessionManager?.addSessionManagerListener(
            this.sessionManagerListener,
            CastSession::class.java
        )
    }

    override fun onPause() {
        super.onPause()
        this.castContext?.sessionManager?.removeSessionManagerListener(
            this.sessionManagerListener,
            CastSession::class.java
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.media_route_menu_item
        )
        return true
    }

    private fun updatePlaybackLocation(playbackLocation: PlaybackLocation) {
        this.playbackLocation = playbackLocation
    }

    private fun loadRemoteMedia(url: String) {
        if (this.castSession == null) return
        val remoteMediaClient = this.castSession!!.remoteMediaClient ?: return
        remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {

            }

            override fun onMetadataUpdated() {

            }
        })

        remoteMediaClient.load(getMediaInfo(url))

    }


    private fun getMediaInfo(url: String): MediaInfo {

        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC)
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Cada Día")
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, "Cada Día")

        mediaMetadata.addImage(WebImage(Uri.parse("https://assets.cinepolisklic.com/cmsklicia/movieimages/cada-dia/poster_resize_182X273.jpg")))
        mediaMetadata.addImage(WebImage(Uri.parse("https://assets.cinepolisklic.com/cmsklicia/movieimages/cada-dia/poster_resize_182X273.jpg")))

        var jsonObject = JSONObject()
        try {
            jsonObject.put("licenseServer", "")
            jsonObject.put("customDataPR", "")
        } catch (e: JSONException) {

        }

        val contentType = "application/vnd.ms-sstr+xml"
        return MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_NONE)
            .setContentType(contentType)
            .setMetadata(mediaMetadata)
            .setCustomData(jsonObject)
            .build()
    }
}
