package com.example.dbvideomarker.player.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.MarkAdapter;
import com.example.dbvideomarker.player.PlayerActivity;
import com.example.dbvideomarker.player.gesture.OnVideoGestureChangeListener;
import com.example.dbvideomarker.player.gesture.VideoGesture;
import com.example.dbvideomarker.player.media.ExoMediaSource;
import com.example.dbvideomarker.player.orientation.OnOrientationChangedListener;
import com.example.dbvideomarker.player.orientation.SensorOrientation;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.google.android.exoplayer2.util.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import static com.example.dbvideomarker.player.orientation.OnOrientationChangedListener.SENSOR_LANDSCAPE;
import static com.example.dbvideomarker.player.orientation.OnOrientationChangedListener.SENSOR_PORTRAIT;
import static com.example.dbvideomarker.player.orientation.OnOrientationChangedListener.SENSOR_UNKNOWN;

public class ExoVideoPlaybackControlView extends FrameLayout {

    public interface VideoViewAccessor {
        View attachVideoView();
    }

    public interface PlayerAccessor {
        Player attachPlayer();
    }

    public interface VisibilityListener {
        void onVisibilityChange(int visibility);
    }

    public interface ExoClickListener {
        boolean onClick(@Nullable View view, boolean isPortrait);

    }

    public interface OrientationListener {
        void onOrientationChanged(@OnOrientationChangedListener.SensorOrientationType int orientation);
    }

    public static final int DEFAULT_FAST_FORWARD_MS = 5000;
    public static final int DEFAULT_REWIND_MS = 5000;
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;
    public static final @RepeatModeUtil.RepeatToggleModes
    int DEFAULT_REPEAT_TOGGLE_MODES =
            RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE;

    public static final int MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR = 100;
    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;
    public static final int CONTROLLER_MODE_ALL = 0b1111;

    @IntDef({
            CONTROLLER_MODE_ALL,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ControllerModeType {

    }

    public static final int CUSTOM_VIEW_TOP = 1;
    public static final int CUSTOM_VIEW_TOP_LANDSCAPE = CUSTOM_VIEW_TOP + 1;
    public static final int CUSTOM_VIEW_BOTTOM_LANDSCAPE = CUSTOM_VIEW_TOP_LANDSCAPE + 1;

    @IntDef({CUSTOM_VIEW_TOP, CUSTOM_VIEW_TOP_LANDSCAPE, CUSTOM_VIEW_BOTTOM_LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CustomViewType {

    }

    private final ComponentListener componentListener;
    private final View previousButton;
    private final View nextButton;
    private final View playButton;
    private final View pauseButton;
    private final View fastForwardButton;
    private final View rewindButton;
    private final TextView durationView;
    private final TextView positionView;
    private final TimeBar timeBar;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;
    private final Timeline.Period period;
    private final Timeline.Window window;


    private final Drawable repeatOffButtonDrawable;
    private final Drawable repeatOneButtonDrawable;
    private final Drawable repeatAllButtonDrawable;
    private final String repeatOffButtonContentDescription;
    private final String repeatOneButtonContentDescription;
    private final String repeatAllButtonContentDescription;

    private Player player;
    private ExoVideoView videoView;
    private com.google.android.exoplayer2.ControlDispatcher controlDispatcher;
    private VisibilityListener visibilityListener;

    private boolean isAttachedToWindow;
    private boolean showMultiWindowTimeBar;
    private boolean multiWindowTimeBar;
    private boolean scrubbing;
    private int rewindMs;
    private int fastForwardMs;
    private int showTimeoutMs;
    private @RepeatModeUtil.RepeatToggleModes
    int repeatToggleModes;
    private boolean showShuffleButton;
    private long hideAtMs;
    private long[] adGroupTimesMs;
    private boolean[] playedAdGroups;
    private long[] extraAdGroupTimesMs;
    private boolean[] extraPlayedAdGroups;

    private final Runnable updateProgressAction = this::updateProgress;

    private final Runnable hideAction = this::hide;


    private final TimeBar timeBarLandscape;
    private final View playButtonLandScape;
    private final View pauseButtonLandScape;
    private final TextView durationViewLandscape;
    private final View enterFullscreen;
    private final View exitFullscreen;


    private final View exoPlayerControllerTop;
    private final View exoPlayerControllerTopLandscape;
    private final View exoPlayerControllerBottom;
    private final View exoPlayerControllerBottomLandscape;

    private final View centerInfoWrapper;
    private final TextView centerInfo;

    private final TextView exoPlayerVideoName;
    private final TextView exoPlayerVideoNameLandscape;

    private final TextView centerError;
    private final ProgressBar loadingBar;

    private final View back;
    private final View backLandscape;

    private final View exoPlayerMarkLandscape;

    private boolean portrait = true;

    private SensorOrientation sensorOrientation;
    private PlayerActivity playerActivity;
    private OrientationListener orientationListener;
    private ExoClickListener backListener;

    private boolean isHls;

    private int displayMode = CONTROLLER_MODE_ALL;


    private VideoViewAccessor videoViewAccessor;
    private VideoGesture videoGesture;

    public ExoVideoPlaybackControlView(Context context) {
        this(context, null);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, attrs);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr,
                                       AttributeSet playbackAttrs) {
        super(context, attrs, defStyleAttr);


        int controllerLayoutId = R.layout.exo_video_playback_control_view;
        rewindMs = DEFAULT_REWIND_MS;
        fastForwardMs = DEFAULT_FAST_FORWARD_MS;
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;
        repeatToggleModes = DEFAULT_REPEAT_TOGGLE_MODES;
        showShuffleButton = false;
        boolean enableGesture = true;

        int controllerBackgroundId = 0;

        if (playbackAttrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(playbackAttrs,
                    R.styleable.ExoVideoPlaybackControlView, 0, 0);
            try {
                rewindMs = a.getInt(R.styleable.ExoVideoPlaybackControlView_rewind_increment, rewindMs);
                fastForwardMs = a.getInt(R.styleable.ExoVideoPlaybackControlView_fastforward_increment,
                        fastForwardMs);
                showTimeoutMs = a.getInt(R.styleable.ExoVideoPlaybackControlView_show_timeout, showTimeoutMs);
                controllerLayoutId = a.getResourceId(R.styleable.ExoVideoPlaybackControlView_controller_layout_id,
                        controllerLayoutId);
                repeatToggleModes = getRepeatToggleModes(a, repeatToggleModes);
                showShuffleButton = a.getBoolean(R.styleable.ExoVideoPlaybackControlView_show_shuffle_button,
                        showShuffleButton);
                displayMode = a.getInt(R.styleable.ExoVideoPlaybackControlView_controller_display_mode, CONTROLLER_MODE_ALL);

                controllerBackgroundId = a.getResourceId(R.styleable.ExoVideoPlaybackControlView_controller_background, 0);
                enableGesture = a.getBoolean(R.styleable.ExoVideoPlaybackControlView_enableGesture, enableGesture);
            } finally {
                a.recycle();
            }
        }


        period = new Timeline.Period();
        window = new Timeline.Window();
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        adGroupTimesMs = new long[0];
        playedAdGroups = new boolean[0];
        extraAdGroupTimesMs = new long[0];
        extraPlayedAdGroups = new boolean[0];
        componentListener = new ComponentListener();
        controlDispatcher = new com.google.android.exoplayer2.DefaultControlDispatcher();

        LayoutInflater.from(context).inflate(controllerLayoutId, this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        durationView = findViewById(R.id.exo_player_duration);
        positionView = findViewById(R.id.exo_player_position);
        timeBar = findViewById(R.id.exo_player_progress);
        if (timeBar != null) {
            timeBar.addListener(componentListener);
        }


        playButton = findViewById(R.id.exo_player_play);
        if (playButton != null) {
            playButton.setOnClickListener(componentListener);
        }


        pauseButton = findViewById(R.id.exo_player_pause);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(componentListener);
        }


        previousButton = findViewById(R.id.exo_prev);
        if (previousButton != null) {
            previousButton.setOnClickListener(componentListener);
        }
        nextButton = findViewById(R.id.exo_next);
        if (nextButton != null) {
            nextButton.setOnClickListener(componentListener);
        }
        rewindButton = findViewById(R.id.exo_rew);
        if (rewindButton != null) {
            rewindButton.setOnClickListener(componentListener);
        }
        fastForwardButton = findViewById(R.id.exo_ffwd);
        if (fastForwardButton != null) {
            fastForwardButton.setOnClickListener(componentListener);
        }

        Resources resources = context.getResources();
        repeatOffButtonDrawable = resources.getDrawable(R.drawable.exo_controls_repeat_off);
        repeatOneButtonDrawable = resources.getDrawable(R.drawable.exo_controls_repeat_one);
        repeatAllButtonDrawable = resources.getDrawable(R.drawable.exo_controls_repeat_all);
        repeatOffButtonContentDescription = resources.getString(
                R.string.exo_controls_repeat_off_description);
        repeatOneButtonContentDescription = resources.getString(
                R.string.exo_controls_repeat_one_description);
        repeatAllButtonContentDescription = resources.getString(
                R.string.exo_controls_repeat_all_description);


        durationViewLandscape = findViewById(R.id.exo_player_position_duration_landscape);

        timeBarLandscape = findViewById(R.id.exo_player_progress_landscape);
        if (timeBarLandscape != null) {
            timeBarLandscape.addListener(componentListener);
        }

        playButtonLandScape = findViewById(R.id.exo_player_play_landscape);
        if (playButtonLandScape != null) {
            playButtonLandScape.setOnClickListener(componentListener);
        }

        pauseButtonLandScape = findViewById(R.id.exo_player_pause_landscape);
        if (pauseButtonLandScape != null) {
            pauseButtonLandScape.setOnClickListener(componentListener);
        }

        enterFullscreen = findViewById(R.id.exo_player_enter_fullscreen);
        if (enterFullscreen != null) {
            enterFullscreen.setOnClickListener(componentListener);
        }


        exitFullscreen = findViewById(R.id.exo_player_exit_fullscreen);
        if (exitFullscreen != null) {
            exitFullscreen.setOnClickListener(componentListener);
        }

        centerInfoWrapper = findViewById(R.id.exo_player_center_info_wrapper);
        centerInfo = findViewById(R.id.exo_player_center_text);


        exoPlayerControllerTop = findViewById(R.id.exo_player_controller_top);
        if (exoPlayerControllerTop != null && controllerBackgroundId != 0) {
            exoPlayerControllerTop.setBackgroundResource(controllerBackgroundId);
        }

        exoPlayerControllerTopLandscape = findViewById(R.id.exo_player_controller_top_landscape);
        if (exoPlayerControllerTopLandscape != null && controllerBackgroundId != 0) {
            exoPlayerControllerTopLandscape.setBackgroundResource(controllerBackgroundId);
        }

        exoPlayerControllerBottom = findViewById(R.id.exo_player_controller_bottom);
        if (exoPlayerControllerBottom != null && controllerBackgroundId != 0) {
            exoPlayerControllerBottom.setBackgroundResource(controllerBackgroundId);
        }

        exoPlayerControllerBottomLandscape = findViewById(R.id.exo_player_controller_bottom_landscape);
        if (exoPlayerControllerBottomLandscape != null && controllerBackgroundId != 0) {
            exoPlayerControllerBottomLandscape.setBackgroundResource(controllerBackgroundId);
        }


        exoPlayerVideoName = findViewById(R.id.exo_player_video_name);
        if (exoPlayerVideoName != null) {
            exoPlayerVideoName.setOnClickListener(componentListener);
        }

        exoPlayerVideoNameLandscape = findViewById(R.id.exo_player_video_name_landscape);
        if (exoPlayerVideoNameLandscape != null) {
            exoPlayerVideoNameLandscape.setOnClickListener(componentListener);
        }

        exoPlayerMarkLandscape = findViewById(R.id.exo_player_controller_mark_landscape);
        if(exoPlayerMarkLandscape != null) {
            exoPlayerMarkLandscape.setOnClickListener(componentListener);
        }

        back = findViewById(R.id.exo_player_controller_back);
        if (back != null) {
            back.setOnClickListener(componentListener);
        }

        backLandscape = findViewById(R.id.exo_player_controller_back_landscape);
        if (backLandscape != null) {
            backLandscape.setOnClickListener(componentListener);
        }

        if (centerInfoWrapper != null) {
            setupVideoGesture(enableGesture);
        }


        centerError = findViewById(R.id.exo_player_center_error);
        loadingBar = findViewById(R.id.exo_player_loading);
        sensorOrientation = new SensorOrientation(getContext(), this::changeOrientation);
        showControllerByDisplayMode();

        showUtilHideCalled();
    }


    private void setupVideoGesture(boolean enableGesture) {
        OnVideoGestureChangeListener onVideoGestureChangeListener = new OnVideoGestureChangeListener() {

            @Override
            public void onVolumeChanged(int range, int type) {
                show();
                setVolumeOrBrightnessInfo(getContext().getString(R.string.volume_changing, range));
            }

            @Override
            public void onBrightnessChanged(int brightnessPercent) {
                show();
                setVolumeOrBrightnessInfo(getContext().getString(R.string.brightness_changing, brightnessPercent));
            }

            @Override
            public void onNoGesture() {

                if (centerInfo == null) {
                    return;
                }
                centerInfo.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                centerInfo.setVisibility(GONE);
            }

            @Override
            public void onShowSeekSize(long seekSize, boolean fastForward) {
                if (isHls) {
                    return;
                }

                show();
                seekTo(seekSize);
                if (centerInfo == null) {
                    return;
                }

                if (centerError != null && centerError.getVisibility() == VISIBLE) {
                    centerError.setVisibility(GONE);
                }
                centerInfo.setVisibility(VISIBLE);
                centerInfo.setText(generateFastForwardOrRewindTxt(seekSize));
                int drawableId = fastForward ? R.drawable.ic_fast_forward_white_36dp : R.drawable.ic_fast_rewind_white_36dp;
                Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);
                centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            }
        };


        videoGesture = new VideoGesture(getContext(), onVideoGestureChangeListener, () -> player);
        if (!enableGesture) {
            videoGesture.disable();
        }
        centerInfoWrapper.setOnClickListener(componentListener);
        centerInfoWrapper.setOnTouchListener(videoGesture);

    }


    private CharSequence generateFastForwardOrRewindTxt(long changingTime) {

        long duration = player == null ? 0 : player.getDuration();
        String result = Util.getStringForTime(formatBuilder, formatter, changingTime);
        result = result + "/";
        result = result + Util.getStringForTime(formatBuilder, formatter, duration);

        int index = result.indexOf("/");

        SpannableString spannableString = new SpannableString(result);


        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        spannableString.setSpan(new ForegroundColorSpan(color), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), index, result.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


    private void setVolumeOrBrightnessInfo(String txt) {
        if (centerInfo == null) {
            return;
        }
        if (centerError != null && centerError.getVisibility() == VISIBLE) {
            centerError.setVisibility(GONE);
        }
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(txt);
        centerInfo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
    }


    @SuppressWarnings("ResourceType")
    private static @RepeatModeUtil.RepeatToggleModes
    int getRepeatToggleModes(TypedArray a,
                             @RepeatModeUtil.RepeatToggleModes int repeatToggleModes) {
        return a.getInt(R.styleable.ExoVideoPlaybackControlView_repeat_toggle_modes, repeatToggleModes);
    }

    public void setPlayer(Player player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        this.player = player;
        if (player != null) {
            player.addListener(componentListener);
        }
        updateAll();
    }


    public int getShowTimeoutMs() {
        return showTimeoutMs;
    }

    public void setShowTimeoutMs(int showTimeoutMs) {
        this.showTimeoutMs = showTimeoutMs;
    }

    public void show() {

        if (!isVisible()) {
            setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }

            if (portrait) {
                changeSystemUiVisibilityPortrait();
            }

            updateAll();
            requestPlayPauseFocus();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();

    }

    /**
     * Hides the controller.
     */
    public void hide() {
        if (isVisible()) {
            setVisibility(GONE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;

            if (!portrait) {
                changeSystemUiVisibilityLandscape();
            }
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            if (isAttachedToWindow) {
                postDelayed(hideAction, showTimeoutMs);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    private void updateNavigation() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        Timeline timeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        boolean isSeekable = false;
        boolean enablePrevious = false;
        boolean enableNext = false;
        if (haveNonEmptyTimeline && !player.isPlayingAd()) {
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
            isSeekable = window.isSeekable;
            enablePrevious = isSeekable || !window.isDynamic
                    || player.getPreviousWindowIndex() != C.INDEX_UNSET;
            enableNext = window.isDynamic || player.getNextWindowIndex() != C.INDEX_UNSET;
        }
        setButtonEnabled(enablePrevious, previousButton);
        setButtonEnabled(enableNext, nextButton);
        setButtonEnabled(fastForwardMs > 0 && isSeekable, fastForwardButton);
        setButtonEnabled(rewindMs > 0 && isSeekable, rewindButton);
        if (timeBar != null) {
            timeBar.setEnabled(isSeekable && !isHls);
        }
        if (timeBarLandscape != null) {
            timeBarLandscape.setEnabled(isSeekable && !isHls);
        }
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = player != null && player.getPlayWhenReady();
        if (playButton != null) {
            requestPlayPauseFocus |= playing && playButton.isFocused();
            playButton.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (pauseButton != null) {
            requestPlayPauseFocus |= !playing && pauseButton.isFocused();
            pauseButton.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }


        if (playButtonLandScape != null) {
            requestPlayPauseFocus |= playing && playButtonLandScape.isFocused();
            playButtonLandScape.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (pauseButtonLandScape != null) {
            requestPlayPauseFocus |= !playing && pauseButtonLandScape.isFocused();
            pauseButtonLandScape.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }


        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void updateTimeBarMode() {
        if (player == null) {
            return;
        }
        multiWindowTimeBar = showMultiWindowTimeBar
                && canShowMultiWindowTimeBar(player.getCurrentTimeline(), window);
    }

    private void updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }

        long position = 0;
        long bufferedPosition = 0;
        long duration = 0;
        if (player != null) {
            long currentWindowTimeBarOffsetUs = 0;
            long durationUs = 0;
            int adGroupCount = 0;
            Timeline timeline = player.getCurrentTimeline();
            if (!timeline.isEmpty()) {
                int currentWindowIndex = player.getCurrentWindowIndex();
                int firstWindowIndex = multiWindowTimeBar ? 0 : currentWindowIndex;
                int lastWindowIndex =
                        multiWindowTimeBar ? timeline.getWindowCount() - 1 : currentWindowIndex;
                for (int i = firstWindowIndex; i <= lastWindowIndex; i++) {
                    if (i == currentWindowIndex) {
                        currentWindowTimeBarOffsetUs = durationUs;
                    }
                    timeline.getWindow(i, window);
                    if (window.durationUs == C.TIME_UNSET) {
                        Assertions.checkState(!multiWindowTimeBar);
                        break;
                    }
                    for (int j = window.firstPeriodIndex; j <= window.lastPeriodIndex; j++) {
                        timeline.getPeriod(j, period);
                        int periodAdGroupCount = period.getAdGroupCount();
                        for (int adGroupIndex = 0; adGroupIndex < periodAdGroupCount; adGroupIndex++) {
                            long adGroupTimeInPeriodUs = period.getAdGroupTimeUs(adGroupIndex);
                            if (adGroupTimeInPeriodUs == C.TIME_END_OF_SOURCE) {
                                if (period.durationUs == C.TIME_UNSET) {
                                    // Don't show ad markers for postrolls in periods with unknown duration.
                                    continue;
                                }
                                adGroupTimeInPeriodUs = period.durationUs;
                            }
                            long adGroupTimeInWindowUs = adGroupTimeInPeriodUs + period.getPositionInWindowUs();
                            if (adGroupTimeInWindowUs >= 0 && adGroupTimeInWindowUs <= window.durationUs) {
                                if (adGroupCount == adGroupTimesMs.length) {
                                    int newLength = adGroupTimesMs.length == 0 ? 1 : adGroupTimesMs.length * 2;
                                    adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, newLength);
                                    playedAdGroups = Arrays.copyOf(playedAdGroups, newLength);
                                }
                                adGroupTimesMs[adGroupCount] = C.usToMs(durationUs + adGroupTimeInWindowUs);
                                playedAdGroups[adGroupCount] = period.hasPlayedAdGroup(adGroupIndex);
                                adGroupCount++;
                            }
                        }
                    }
                    durationUs += window.durationUs;
                }
            }
            duration = C.usToMs(durationUs);
            position = C.usToMs(currentWindowTimeBarOffsetUs);
            bufferedPosition = position;
            if (player.isPlayingAd()) {
                position += player.getContentPosition();
                bufferedPosition = position;
            } else {
                position += player.getCurrentPosition();
                bufferedPosition += player.getBufferedPosition();
            }
            if (timeBar != null) {
                int extraAdGroupCount = extraAdGroupTimesMs.length;
                int totalAdGroupCount = adGroupCount + extraAdGroupCount;
                if (totalAdGroupCount > adGroupTimesMs.length) {
                    adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, totalAdGroupCount);
                    playedAdGroups = Arrays.copyOf(playedAdGroups, totalAdGroupCount);
                }
                System.arraycopy(extraAdGroupTimesMs, 0, adGroupTimesMs, adGroupCount, extraAdGroupCount);
                System.arraycopy(extraPlayedAdGroups, 0, playedAdGroups, adGroupCount, extraAdGroupCount);
                timeBar.setAdGroupTimesMs(adGroupTimesMs, playedAdGroups, totalAdGroupCount);
            }
        }
        if (durationView != null && !isHls) {
            durationView.setText(Util.getStringForTime(formatBuilder, formatter, duration));
        }

        if (durationViewLandscape != null && !isHls) {
            String positionStr = Util.getStringForTime(formatBuilder, formatter, position);
            String durationStr = Util.getStringForTime(formatBuilder, formatter, duration);
            durationViewLandscape.setText(positionStr.concat("/").concat(durationStr));
        }

        if (positionView != null && !scrubbing && !isHls) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }


        if (timeBar != null && !isHls) {
            timeBar.setPosition(position);
            timeBar.setBufferedPosition(bufferedPosition);
            timeBar.setDuration(duration);
        }

        if (timeBarLandscape != null && !isHls) {
            timeBarLandscape.setPosition(position);
            timeBarLandscape.setBufferedPosition(bufferedPosition);
            timeBarLandscape.setDuration(duration);
        }


        // Cancel any pending updates and schedule a new one if necessary.
        removeCallbacks(updateProgressAction);
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                float playbackSpeed = player.getPlaybackParameters().speed;
                if (playbackSpeed <= 0.1f) {
                    delayMs = 1000;
                } else if (playbackSpeed <= 5f) {
                    long mediaTimeUpdatePeriodMs = 1000 / Math.max(1, Math.round(1 / playbackSpeed));
                    long mediaTimeDelayMs = mediaTimeUpdatePeriodMs - (position % mediaTimeUpdatePeriodMs);
                    if (mediaTimeDelayMs < (mediaTimeUpdatePeriodMs / 5)) {
                        mediaTimeDelayMs += mediaTimeUpdatePeriodMs;
                    }
                    delayMs = playbackSpeed == 1 ? mediaTimeDelayMs
                            : (long) (mediaTimeDelayMs / playbackSpeed);
                } else {
                    delayMs = 200;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private void requestPlayPauseFocus() {
        boolean playing = player != null && player.getPlayWhenReady();
        if (!playing && playButton != null) {
            playButton.requestFocus();
        } else if (playing && pauseButton != null) {
            pauseButton.requestFocus();

        }

        if (!playing && playButtonLandScape != null) {
            playButtonLandScape.requestFocus();
        } else if (playing && pauseButtonLandScape != null) {
            pauseButtonLandScape.requestFocus();

        }
    }

    private void setButtonEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.3f);
        view.setVisibility(VISIBLE);
    }

    private void previous() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = player.getCurrentWindowIndex();
        timeline.getWindow(windowIndex, window);
        int previousWindowIndex = player.getPreviousWindowIndex();
        if (previousWindowIndex != C.INDEX_UNSET
                && (player.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || (window.isDynamic && !window.isSeekable))) {
            seekTo(previousWindowIndex, C.TIME_UNSET);
        } else {
            seekTo(0);
        }
    }

    private void next() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = player.getCurrentWindowIndex();
        int nextWindowIndex = player.getNextWindowIndex();
        if (nextWindowIndex != C.INDEX_UNSET) {
            seekTo(nextWindowIndex, C.TIME_UNSET);
        } else if (timeline.getWindow(windowIndex, window, false).isDynamic) {
            seekTo(windowIndex, C.TIME_UNSET);
        }
    }

    private void rewind() {
        if (rewindMs <= 0) {
            return;
        }
        seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
    }

    private void fastForward() {
        if (fastForwardMs <= 0) {
            return;
        }
        long durationMs = player.getDuration();
        long seekPositionMs = player.getCurrentPosition() + fastForwardMs;
        if (durationMs != C.TIME_UNSET) {
            seekPositionMs = Math.min(seekPositionMs, durationMs);
        }
        seekTo(seekPositionMs);
    }

    public void seekTo(long positionMs) {
        seekTo(player.getCurrentWindowIndex(), positionMs);
    }

    private void seekTo(int windowIndex, long positionMs) {
        boolean dispatched = controlDispatcher.dispatchSeekTo(player, windowIndex, positionMs);
        if (!dispatched) {
            // The seek wasn't dispatched. If the progress bar was dragged by the user to perform the
            // seek then it'll now be in the wrong position. Trigger a progress update to snap it back.
            updateProgress();
        }
    }

    private void seekToTimeBarPosition(long positionMs) {
        int windowIndex;
        Timeline timeline = player.getCurrentTimeline();
        if (multiWindowTimeBar && !timeline.isEmpty()) {
            int windowCount = timeline.getWindowCount();
            windowIndex = 0;
            while (true) {
                long windowDurationMs = timeline.getWindow(windowIndex, window).getDurationMs();
                if (positionMs < windowDurationMs) {
                    break;
                } else if (windowIndex == windowCount - 1) {
                    // Seeking past the end of the last window should seek to the end of the timeline.
                    positionMs = windowDurationMs;
                    break;
                }
                positionMs -= windowDurationMs;
                windowIndex++;
            }
        } else {
            windowIndex = player.getCurrentWindowIndex();
        }
        seekTo(windowIndex, positionMs);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        sensorOrientation.enable();
        isAttachedToWindow = true;
        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hide();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
        updateAll();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sensorOrientation.disable();
        isAttachedToWindow = false;
        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (player == null || !isHandledMediaKey(keyCode)) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                fastForward();
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                rewind();
            } else if (event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, !player.getPlayWhenReady());
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        controlDispatcher.dispatchSetPlayWhenReady(player, true);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, false);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        next();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        previous();
                        break;
                    default:
                        break;
                }
            }
        }
        return true;
    }


    public void setBackListener(ExoClickListener backListener) {
        this.backListener = backListener;
    }


    public void setPortrait(boolean portrait) {
        this.portrait = portrait;
        showControllerByDisplayMode();
    }

    public void setOrientationListener(OrientationListener orientationListener) {
        this.orientationListener = orientationListener;
    }


    public void setMediaSource(ExoMediaSource exoMediaSource) {
        if (exoPlayerVideoName != null) {
            exoPlayerVideoName.setText(exoMediaSource.name());
        }

        if (exoPlayerVideoNameLandscape != null) {
            exoPlayerVideoNameLandscape.setText(exoMediaSource.name());
        }

        if (centerError != null) {
            centerError.setText(null);
            centerError.setVisibility(GONE);
        }
    }


    public void setControllerDisplayMode(int displayMode) {
        this.displayMode = displayMode;
        showControllerByDisplayMode();
    }

    private void showControllerByDisplayMode() {
        boolean showByMode = (displayMode) == displayMode;
        int visibility = showByMode ? VISIBLE : INVISIBLE;
        if (portrait) {
            exoPlayerControllerTop.setVisibility(visibility);
            exoPlayerControllerBottom.setVisibility(visibility);
            exoPlayerControllerTopLandscape.setVisibility(INVISIBLE);
            exoPlayerControllerBottomLandscape.setVisibility(INVISIBLE);
        } else {
            exoPlayerControllerTop.setVisibility(INVISIBLE);
            exoPlayerControllerBottom.setVisibility(INVISIBLE);
            exoPlayerControllerTopLandscape.setVisibility(visibility);
            exoPlayerControllerBottomLandscape.setVisibility(visibility);
        }


    }

    private synchronized void changeOrientation(@OnOrientationChangedListener.SensorOrientationType int orientation) {
        Context context = getContext();
        Activity activity;
        if (!(context instanceof Activity)) {
            return;
        }
        if (orientationListener == null) {
            return;
        }

        activity = (Activity) context;
        switch (orientation) {
            case SENSOR_PORTRAIT:
                setPortrait(true);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                changeSystemUiVisibilityPortrait();
                break;
            case SENSOR_LANDSCAPE:
                setPortrait(false);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                changeSystemUiVisibilityLandscape();
                break;
            case SENSOR_UNKNOWN:
            default:
                break;
        }
        orientationListener.onOrientationChanged(orientation);
    }


    private void changeSystemUiVisibilityPortrait() {
        videoViewAccessor.attachVideoView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void changeSystemUiVisibilityLandscape() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }

        int flag = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        videoViewAccessor.attachVideoView().setSystemUiVisibility(flag);
    }



    public void setVideoViewAccessor(VideoViewAccessor videoViewAccessor) {
        this.videoViewAccessor = videoViewAccessor;
    }

    private void showLoading(boolean isLoading) {
        if (loadingBar == null) {
            return;
        }
        if (isLoading) {
            loadingBar.setVisibility(View.VISIBLE);
        } else {
            loadingBar.setVisibility(GONE);
        }
    }

    public void showUtilHideCalled() {
        if (!isVisible()) {
            setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            updateAll();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (backListener != null) {
                if (!backListener.onClick(null, portrait)) {
                    if (portrait) {
                        return super.onKeyDown(keyCode, event);
                    } else {
                        changeOrientation(SENSOR_PORTRAIT);
                        return true;
                    }

                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("InlinedApi")
    private static boolean isHandledMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    }


    private static boolean canShowMultiWindowTimeBar(Timeline timeline, Timeline.Window window) {
        if (timeline.getWindowCount() > MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR) {
            return false;
        }
        int windowCount = timeline.getWindowCount();
        for (int i = 0; i < windowCount; i++) {
            if (timeline.getWindow(i, window).durationUs == C.TIME_UNSET) {
                return false;
            }
        }
        return true;
    }

    private final class ComponentListener implements
            TimeBar.OnScrubListener, OnClickListener, Player.EventListener {

        @Override
        public void onScrubStart(TimeBar timeBar, long position) {
            removeCallbacks(hideAction);
            scrubbing = true;
        }

        @Override
        public void onScrubMove(TimeBar timeBar, long position) {
            if (positionView != null) {
                positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
            }
        }

        @Override
        public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
            scrubbing = false;
            if (!canceled && player != null) {
                seekToTimeBarPosition(position);
            }
            hideAfterTimeout();
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            if (playbackState != Player.STATE_IDLE && centerError != null && centerError.getVisibility() == VISIBLE) {
                centerError.setVisibility(GONE);
            }

            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_BUFFERING) {
                removeCallbacks(hideAction);
                showUtilHideCalled();
                showLoading(true);
            } else if (playbackState == Player.STATE_READY && player.getPlayWhenReady() || playbackState == Player.STATE_ENDED) {
                showLoading(false);
                hide();
            }

            updatePlayPauseButton();
            updateProgress();
        }


        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            if (manifest instanceof HlsManifest) {
                HlsManifest hlsManifest = (HlsManifest) manifest;
                isHls = !hlsManifest.mediaPlaylist.hasEndTag && hlsManifest.mediaPlaylist.playlistType == HlsMediaPlaylist.PLAYLIST_TYPE_UNKNOWN;
            } else {
                isHls = false;
            }


            updateNavigation();
            updateTimeBarMode();
            updateProgress();
        }


        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (loadingBar != null) {
                loadingBar.setVisibility(GONE);
            }
            if (centerError != null) {
                String errorText = getResources().getString(R.string.player_error, error.type);
                centerError.setText(errorText);
                centerError.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            if (player != null) {
                if (nextButton == view) {
                    next();
                } else if (previousButton == view) {
                    previous();
                } else if (fastForwardButton == view) {
                    fastForward();
                } else if (rewindButton == view) {
                    rewind();
                } else if (playButton == view || playButtonLandScape == view) {
                    controlDispatcher.dispatchSetPlayWhenReady(player, true);
                } else if (pauseButton == view || pauseButtonLandScape == view) {
                    controlDispatcher.dispatchSetPlayWhenReady(player, false);
                } else if (enterFullscreen == view) {
                    changeOrientation(SENSOR_LANDSCAPE);
                } else if (exitFullscreen == view) {
                    changeOrientation(SENSOR_PORTRAIT);
                } else if (exoPlayerVideoName == view || back == view) {
                    if (backListener != null) {
                        if (!backListener.onClick(view, portrait)) {
                            changeOrientation(SENSOR_LANDSCAPE);
                        }
                    }
                } else if (exoPlayerVideoNameLandscape == view || backLandscape == view) {
                    if (backListener != null) {
                        if (!backListener.onClick(view, portrait)) {
                            changeOrientation(SENSOR_PORTRAIT);
                        }
                    }
                } else if (centerInfoWrapper == view) {
                    playOrPause();
                }
            }
            hideAfterTimeout();
        }

        long[] mHits = new long[2];

        private void playOrPause() {

            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();

            if (500 > (SystemClock.uptimeMillis() - mHits[0])) {
                controlDispatcher.dispatchSetPlayWhenReady(player, !player.getPlayWhenReady());
            }
        }
    }
}
