package com.uooo.exoplayer.sample.player;

import androidx.annotation.NonNull;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.upstream.HttpDataSource;

final class ExoPlayerDrmHelper {
    private ExoPlayerDrmHelper() {
    }

    static MediaDrmCallback buildMediaDrmCallback(
            @NonNull String licenseUrl, @NonNull HttpDataSource.Factory licenseDataSourceFactory) {
        return new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
    }
}
