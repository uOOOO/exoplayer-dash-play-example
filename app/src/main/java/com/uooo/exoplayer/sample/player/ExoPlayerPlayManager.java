package com.uooo.exoplayer.sample.player;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.drm.*;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.EventLogger;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public final class ExoPlayerPlayManager {
    private static final String TAG = ExoPlayerAdsHelper.class.getSimpleName();

    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private SimpleExoPlayer player;
    private MediaSource mediaSource;
    private FrameworkMediaDrm mediaDrm;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private AdsLoader adsLoader;
    private PlayerView playerView;

    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private Uri adTagUri;

    public ExoPlayerPlayManager() {
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        clearStartPosition();
    }

    public void prepare(@NonNull Context context, @NonNull PlayerView playerView, @Nullable String licenseUrl,
                        @NonNull Uri uri, @Nullable Uri adTagUri
    ) throws UnsupportedDrmException {
        if (this.player == null) {
            final String userAgent = String.valueOf(context.getApplicationInfo().loadLabel(context.getPackageManager()));
            DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (licenseUrl != null) {
                MediaDrmCallback drmCallback =
                        ExoPlayerDrmHelper.buildMediaDrmCallback(licenseUrl,
                                ExoPlayerBuildHelper.buildHttpDataSourceFactory(context, userAgent));
                drmSessionManager = ExoPlayerBuildHelper.buildDrmSessionManagerV18(C.WIDEVINE_UUID, drmCallback);
            }
            DefaultTrackSelector trackSelector =
                    ExoPlayerBuildHelper.buildTrackSelector();
            DefaultRenderersFactory renderersFactory =
                    ExoPlayerBuildHelper.buildRenderersFactory(context);
            DataSource.Factory dataSourceFactory =
                    ExoPlayerBuildHelper.buildDataSourceFactory(context, userAgent, false);
            MediaSource mediaSource =
                    ExoPlayerBuildHelper.buildMediaSource(uri, dataSourceFactory);
            SimpleExoPlayer player =
                    ExoPlayerBuildHelper.buildPlayer(context, renderersFactory, trackSelector, drmSessionManager);

            if (adTagUri != null) {
                if (!adTagUri.equals(this.adTagUri)) {
                    releaseAdsLoader();
                    this.adTagUri = adTagUri;
                }
                adsLoader = ExoPlayerAdsHelper.createImaAdsLoader(context, player, adTagUri);
                if (adsLoader != null) {
                    mediaSource = ExoPlayerAdsHelper.createAdsMediaSource(adsLoader, playerView, mediaSource, dataSourceFactory);
                } else {
                    Log.w(TAG, "Playing sample without ads, as the IMA extension was not loaded");
                }
            } else {
                releaseAdsLoader();
            }

            this.playerView = playerView;
            this.playerView.setPlayer(player);
            this.player = player;
            this.mediaSource = mediaSource;
            this.trackSelector = trackSelector;
            this.trackSelectorParameters = trackSelector.getParameters();
        }

        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            player.seekTo(startWindow, startPosition);
        }
        this.player.prepare(mediaSource, !haveStartPosition, false);
    }

    public void addEventListener(Player.EventListener listener) {
        if (player != null) {
            player.addListener(listener);
        }
    }

    public void addAnalyticsListener(AnalyticsListener listener) {
        if (player != null) {
            player.addAnalyticsListener(listener);
        }
    }

    private void updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector.getParameters();
        }
    }

    public void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private void releaseMediaDrm() {
        if (mediaDrm != null) {
            mediaDrm.release();
            mediaDrm = null;
        }
    }

    private void releaseAdsLoader() {
        if (adsLoader != null) {
            adsLoader.release();
            adsLoader = null;
            adTagUri = null;
            FrameLayout overlayFrameLayout = playerView.getOverlayFrameLayout();
            if (overlayFrameLayout != null) {
                overlayFrameLayout.removeAllViews();
            }
        }
    }

    public void reset() {
        if (player != null) {
            updateStartPosition();
            player.release();
            player = null;
            adsLoader.setPlayer(null);
        }
    }

    public void release() {
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            player.release();
            player = null;
            trackSelector = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
        }
        releaseMediaDrm();
        releaseAdsLoader();
    }

    @Nullable
    public SimpleExoPlayer getPlayer() {
        return player;
    }

    @Nullable
    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public void start() {
        if (hasPlayer()) {
            player.setPlayWhenReady(true);
        }
    }

    public void pause() {
        if (hasPlayer()) {
            player.setPlayWhenReady(false);
        }
    }

    public void seekTo(long seekPosition) {
        if (hasPlayer()) {
            player.seekTo(seekPosition);
        }
    }

    public long getCurrentPosition() {
        return hasPlayer() ? player.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return hasPlayer() ? player.getDuration() : 0;
    }

    public boolean isPlaying() {
        return hasPlayer() && player.getPlayWhenReady();
    }

    public boolean hasPlayer() {
        return player != null && playerView != null;
    }
}
