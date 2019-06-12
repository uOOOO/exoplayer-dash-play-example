package com.uooo.exoplayer.sample.player;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.view.accessibility.CaptioningManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.Util;

public final class ExoPlayerCaptionHelper {
    private ExoPlayerCaptionHelper() {
    }

    @SuppressLint("ObsoleteSdkInt")
    public static void setCustomCaptionStyle(@NonNull Context context, @Nullable SubtitleView subtitleView) {
        if (subtitleView == null) {
            return;
        }
        CaptionStyleCompat style = Util.SDK_INT >= 19 ?
                getUserCaptionStyleV19(context) : CaptionStyleCompat.DEFAULT;
        if (style != null) {
            style = new CaptionStyleCompat(style.foregroundColor, Color.TRANSPARENT,
                    style.windowColor, CaptionStyleCompat.EDGE_TYPE_OUTLINE, Color.BLACK,
                    style.typeface);
        }
        subtitleView.setStyle(style);
        float fontScale = Util.SDK_INT >= 19 ? getUserCaptionFontScaleV19(context) : 1f;
        subtitleView.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * fontScale);
    }

    @Nullable
    @TargetApi(19)
    private static CaptionStyleCompat getUserCaptionStyleV19(@NonNull Context context) {
        CaptioningManager captioningManager =
                (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        return captioningManager == null ?
                null : CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
    }

    @TargetApi(19)
    private static float getUserCaptionFontScaleV19(@NonNull Context context) {
        CaptioningManager captioningManager =
                (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        return captioningManager == null ? 1f : captioningManager.getFontScale();
    }
}
