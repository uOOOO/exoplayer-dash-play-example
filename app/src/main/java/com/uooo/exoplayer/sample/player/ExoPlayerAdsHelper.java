package com.uooo.exoplayer.sample.player;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import kotlin.Pair;

import java.lang.reflect.Constructor;

final class ExoPlayerAdsHelper {
    private ExoPlayerAdsHelper() {
    }

    @Nullable
    static AdsLoader createImaAdsLoader(@NonNull Context context, @NonNull Player player, @NonNull Uri adTagUri) {
        // Load the extension source using reflection so the demo app doesn't have to depend on it.
        // The ads loader is reused for multiple playbacks, so that ad playback can resume.
        try {
            Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");
            // Full class names used so the LINT.IfChange rule triggers should any of the classes move.
            // LINT.IfChange
            Constructor<? extends AdsLoader> loaderConstructor =
                    loaderClass
                            .asSubclass(AdsLoader.class)
                            .getConstructor(android.content.Context.class, android.net.Uri.class);
            // LINT.ThenChange(../../../../../../../../proguard-rules.txt)
            AdsLoader adsLoader = loaderConstructor.newInstance(context, adTagUri);
            adsLoader.setPlayer(player);
            return adsLoader;
        } catch (ClassNotFoundException e) {
            // IMA extension not loaded.
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an ads media source, reusing the ads loader if one exists.
     */
    static MediaSource createAdsMediaSource(
            @NonNull AdsLoader adsLoader, @NonNull PlayerView playerView, @NonNull MediaSource mediaSource,
            @NonNull DataSource.Factory dataSourceFactory
    ) {
        AdsMediaSource.MediaSourceFactory adMediaSourceFactory =
                new AdsMediaSource.MediaSourceFactory() {
                    @Override
                    public MediaSource createMediaSource(Uri uri) {
                        return ExoPlayerBuildHelper.buildMediaSource(uri, dataSourceFactory);
                    }

                    @Override
                    public int[] getSupportedTypes() {
                        return new int[]{C.TYPE_DASH, C.TYPE_SS, C.TYPE_HLS, C.TYPE_OTHER};
                    }
                };
        return new AdsMediaSource(mediaSource, adMediaSourceFactory, adsLoader, playerView);
    }
}
