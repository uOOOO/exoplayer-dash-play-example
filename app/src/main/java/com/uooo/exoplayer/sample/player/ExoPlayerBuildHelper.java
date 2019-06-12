package com.uooo.exoplayer.sample.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.drm.*;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;
import java.util.UUID;

final class ExoPlayerBuildHelper {
    private ExoPlayerBuildHelper() {
    }

    private static UdpDataSource.Factory buildUdpDataSourceFactory() {
        return UdpDataSource::new;
    }

    static HttpDataSource.Factory buildHttpDataSourceFactory(
            @NonNull Context context, @Nullable String userAgent) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context, userAgent));
    }

    static DefaultDataSource.Factory buildDataSourceFactory(
            @NonNull Context context, @NonNull @SuppressWarnings("SameParameterValue") String userAgent, boolean isUdp) {
        return new DefaultDataSourceFactory(context,
                isUdp ? buildUdpDataSourceFactory() : buildHttpDataSourceFactory(context, userAgent));
    }

    @SuppressLint("SwitchIntDef")
    static MediaSource buildMediaSource(@NonNull Uri uri, @NonNull DataSource.Factory dataSourceFactory) {
        @C.ContentType int type = Util.inferContentType(uri, null);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory)
                        .setManifestParser(
                                new FilteringManifestParser<>(new DashManifestParser(), null))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory)
                        .setManifestParser(
                                new FilteringManifestParser<>(new SsManifestParser(), null))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory)
                        .setPlaylistParserFactory(
                                new DefaultHlsPlaylistParserFactory())
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private static FrameworkMediaDrm buildFrameworkMediaDrm(
            @NonNull @SuppressWarnings("SameParameterValue") UUID uuid) throws UnsupportedDrmException {
        return FrameworkMediaDrm.newInstance(uuid);
    }

    @SuppressLint("ObsoleteSdkInt")
    static DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
            @NonNull @SuppressWarnings("SameParameterValue") UUID uuid,
            @NonNull MediaDrmCallback mediaDrmCallback) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            throw new RuntimeException("Protected content not supported on API levels below 18");
        }
        return new DefaultDrmSessionManager<>(
                uuid, buildFrameworkMediaDrm(uuid), mediaDrmCallback, null, false);
    }

    private static DefaultTrackSelector.Parameters buildTrackSelectorParameter() {
        return new DefaultTrackSelector
                .ParametersBuilder()
                .setPreferredTextLanguage(Locale.getDefault().getLanguage())
                .build();
    }

    static DefaultTrackSelector buildTrackSelector() {
        TrackSelection.Factory trackSelectionFactory =
                new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        trackSelector.setParameters(buildTrackSelectorParameter());
        return trackSelector;
    }

    static DefaultRenderersFactory buildRenderersFactory(@NonNull Context context) {
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(context);
        defaultRenderersFactory.setExtensionRendererMode(extensionRendererMode);
        return defaultRenderersFactory;
    }

    static SimpleExoPlayer buildPlayer(@NonNull Context context,
                                       @NonNull DefaultRenderersFactory defaultRenderersFactory,
                                       @NonNull DefaultTrackSelector trackSelector,
                                       @Nullable DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        return ExoPlayerFactory.newSimpleInstance(context, defaultRenderersFactory,
                trackSelector, drmSessionManager);
    }

//    public static Triple<SimpleExoPlayer, MediaSource, DefaultTrackSelector> newPlayerInstance(
//            @NonNull Context context, @NonNull Uri uri, @Nullable String licenseUrl, boolean isUdp
//    ) throws UnsupportedDrmException {
//        final String userAgent = String.valueOf(context.getApplicationInfo().loadLabel(context.getPackageManager()));
//        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
//        if (licenseUrl != null) {
//            MediaDrmCallback drmCallback =
//                    ExoPlayerDrmHelper.buildMediaDrmCallback(licenseUrl,
//                            buildHttpDataSourceFactory(context, userAgent));
//            drmSessionManager = buildDrmSessionManagerV18(C.WIDEVINE_UUID, drmCallback);
//        }
//        DefaultTrackSelector trackSelector =
//                buildTrackSelector();
//        DefaultRenderersFactory renderersFactory =
//                buildRenderersFactory(context);
//        DataSource.Factory dataSourceFactory =
//                buildDataSourceFactory(context, userAgent, isUdp);
//        MediaSource mediaSource =
//                buildMediaSource(uri, dataSourceFactory);
//        SimpleExoPlayer player =
//                buildPlayer(context, renderersFactory, trackSelector, drmSessionManager);
//        return new Triple<>(player, mediaSource, trackSelector);
//    }
}
