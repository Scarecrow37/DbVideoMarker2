package com.example.dbvideomarker.player.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.player.media.ExoMediaSource;
import com.example.dbvideomarker.player.media.MediaSourceCreator;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.util.List;

import static android.content.Context.AUDIO_SERVICE;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class ExoVideoView extends FrameLayout implements ExoVideoPlaybackControlView.VideoViewAccessor {


    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    private final AspectRatioFrameLayout contentFrame;
    private final View shutterView;
    private final View surfaceView;
    private final ExoVideoPlaybackControlView controller;
    private final ComponentListener componentListener;
    private final FrameLayout overlayFrameLayout;

    private SimpleExoPlayer player;
    private boolean useController;
    private int controllerShowTimeoutMs;
    private boolean controllerAutoShow;
    private boolean controllerHideOnTouch;
    private boolean pausedFromPlayer = false;
    private boolean enableMultiQuality = true;

    private final AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
                resume();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // audioManager.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                audioManager.abandonAudioFocus(afChangeListener);
                // Stop playback
                stop();
            }
        }
    };

    private long lastPlayedPosition = 0L;
    private int controllerBackgroundId = 0;

    public ExoVideoView(Context context) {
        this(context, null);
    }

    public ExoVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        audioManager = (AudioManager) context.getApplicationContext().getSystemService(AUDIO_SERVICE);

        if (isInEditMode()) {
            contentFrame = null;
            shutterView = null;
            surfaceView = null;
            controller = null;
            componentListener = null;
            overlayFrameLayout = null;
            return;
        }

        boolean shutterColorSet = false;
        int shutterColor = 0;
        int playerLayoutId = R.layout.exo_video_view;
        boolean useController = true;
        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        int controllerShowTimeoutMs = PlaybackControlView.DEFAULT_SHOW_TIMEOUT_MS;
        boolean controllerHideOnTouch = true;
        boolean controllerAutoShow = true;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.ExoVideoView, 0, 0);
            try {
                shutterColorSet = a.hasValue(R.styleable.ExoVideoView_shutter_background_color);
                shutterColor = a.getColor(R.styleable.ExoVideoView_shutter_background_color,
                        shutterColor);
                playerLayoutId = a.getResourceId(R.styleable.ExoVideoView_player_layout_id,
                        playerLayoutId);
                useController = a.getBoolean(R.styleable.ExoVideoView_use_controller, useController);
                surfaceType = a.getInt(R.styleable.ExoVideoView_surface_type, surfaceType);
                resizeMode = a.getInt(R.styleable.ExoVideoView_resize_mode, resizeMode);
                controllerShowTimeoutMs = a.getInt(R.styleable.ExoVideoView_show_timeout,
                        controllerShowTimeoutMs);
                controllerHideOnTouch = a.getBoolean(R.styleable.ExoVideoView_hide_on_touch,
                        controllerHideOnTouch);
                controllerAutoShow = a.getBoolean(R.styleable.ExoVideoView_auto_show,
                        controllerAutoShow);
                controllerBackgroundId = a.getResourceId(R.styleable.ExoVideoView_controller_background, 0);
            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(playerLayoutId, this);
        componentListener = new ComponentListener();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        // Content frame.
        contentFrame = findViewById(R.id.exo_player_content_frame);
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame, resizeMode);
        }

        // Shutter view.
        shutterView = findViewById(R.id.exo_player_shutter);
        if (shutterView != null && shutterColorSet) {
            shutterView.setBackgroundColor(shutterColor);
        }

        // Create a surface view and insert it into the content frame, if there is one.
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView = surfaceType == SURFACE_TYPE_TEXTURE_VIEW ? new TextureView(context)
                    : new SurfaceView(context);
            surfaceView.setLayoutParams(params);
            contentFrame.addView(surfaceView, 0);
        } else {
            surfaceView = null;
        }

        // Overlay frame layout.
        overlayFrameLayout = findViewById(R.id.exo_player_overlay);


        // Playback control view.
        ExoVideoPlaybackControlView customController = findViewById(R.id.exo_player_controller);
        View controllerPlaceholder = findViewById(R.id.exo_player_controller_placeholder);
        if (customController != null) {
            this.controller = customController;
        } else if (controllerPlaceholder != null) {
            // Propagate attrs as playbackAttrs so that PlaybackControlView's custom attributes are
            // transferred, but standard FrameLayout attributes (e.g. background) are not.
            this.controller = new ExoVideoPlaybackControlView(context, null, 0, attrs);
            controller.setLayoutParams(controllerPlaceholder.getLayoutParams());
            ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
            int controllerIndex = parent.indexOfChild(controllerPlaceholder);
            parent.removeView(controllerPlaceholder);
            parent.addView(controller, controllerIndex);
        } else {
            this.controller = null;
        }
        this.controllerShowTimeoutMs = controller != null ? controllerShowTimeoutMs : 0;
        this.controllerHideOnTouch = controllerHideOnTouch;
        this.controllerAutoShow = controllerAutoShow;
        this.useController = useController && controller != null;


        if (useController && controller != null) {
            controller.show();
            controller.setVideoViewAccessor(this);
        }
        setKeepScreenOn(true);
    }

    public void setPlayer(SimpleExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
            this.player.removeTextOutput(componentListener);
            this.player.removeVideoListener(componentListener);
            if (surfaceView instanceof TextureView) {
                this.player.clearVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                this.player.clearVideoSurfaceView((SurfaceView) surfaceView);
            }
        }
        this.player = player;
        if (useController) {
            controller.setPlayer(player);
        }
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.addVideoListener(componentListener);
            player.addTextOutput(componentListener);
            player.addListener(componentListener);
            maybeShowController(false);
        } else {
            hideController();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (surfaceView instanceof SurfaceView) {
            // Work around https://github.com/google/ExoPlayer/issues/3160.
            surfaceView.setVisibility(visibility);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (audioManager != null) {
            requestAudioFocus();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (audioManager != null) {
            audioManager.abandonAudioFocus(afChangeListener);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (player != null && player.isPlayingAd()) {
            // Focus any overlay UI now, in case it's provided by a WebView whose contents may update
            // dynamically. This is needed to make the "Skip ad" button focused on Android TV when using
            // IMA [Internal: b/62371030].
            overlayFrameLayout.requestFocus();
            return super.dispatchKeyEvent(event);
        }
        boolean isDpadWhenControlHidden = isDpadKey(event.getKeyCode()) && useController
                && !controller.isVisible();
        maybeShowController(true);
        return isDpadWhenControlHidden || dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return useController && controller.dispatchMediaKeyEvent(event);
    }


    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    public void hideController() {
        if (controller != null) {
            controller.hide();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (!useController || player == null || ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (enableMultiQuality) {
            View v = overlayFrameLayout.findViewById(R.id.exo_player_quality_container);
            if (v != null && overlayFrameLayout.getVisibility() == VISIBLE) {
                return true;
            }

        }

        if (!controller.isVisible()) {
            maybeShowController(true);
        } else if (controllerHideOnTouch) {
            controller.hide();
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null) {
            return false;
        }
        maybeShowController(true);
        return true;
    }

    /**
     * Shows the playback controls, but only if forced or shown indefinitely.
     */
    private void maybeShowController(boolean isForced) {
        if (isPlayingAd()) {
            // Never show the controller if an ad is currently playing.
            return;
        }
        if (useController) {
            boolean wasShowingIndefinitely = controller.isVisible() && controller.getShowTimeoutMs() <= 0;
            boolean shouldShowIndefinitely = shouldShowControllerIndefinitely();
            if (isForced || wasShowingIndefinitely || shouldShowIndefinitely) {
                showController(shouldShowIndefinitely);
            }
        }
    }

    private boolean shouldShowControllerIndefinitely() {
        if (player == null) {
            return true;
        }
        int playbackState = player.getPlaybackState();
        return controllerAutoShow && (playbackState == Player.STATE_IDLE
                || playbackState == Player.STATE_ENDED || !player.getPlayWhenReady());
    }

    private void showController(boolean showIndefinitely) {
        if (!useController) {
            return;
        }
        controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        controller.show();
    }

    private boolean isPlayingAd() {
        return player != null && player.isPlayingAd() && player.getPlayWhenReady();
    }

    @SuppressWarnings("ResourceType")
    private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
        aspectRatioFrame.setResizeMode(resizeMode);
    }

    @SuppressLint("InlinedApi")
    private boolean isDpadKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_UP_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
    }

    public void play(ExoMediaSource mediaSource, boolean playWhenReady) {
        play(mediaSource, playWhenReady, C.TIME_UNSET);
    }

    public void play(ExoMediaSource mediaSource, boolean playWhenReady, long where) {
        releasePlayer();
        MediaSourceCreator creator = new MediaSourceCreator(getContext().getApplicationContext());
        createExoPlayer(creator);
        if (controller != null) {
            controller.setMediaSource(mediaSource);
        }

        playInternal(mediaSource, playWhenReady, where, creator);
    }

    public void pause() {
        if (player == null) {
            return;
        }

        if (player.getPlayWhenReady()) {
            lastPlayedPosition = player.getCurrentPosition();
            player.setPlayWhenReady(false);
            pausedFromPlayer = false;
        } else {
            pausedFromPlayer = true;
        }
    }

    public void resume() {
        if (player == null) {
            return;
        }

        if (player.getPlayWhenReady()) {
            return;
        }

        if (!pausedFromPlayer) {
            player.seekTo(lastPlayedPosition - 500 < 0 ? 0 : lastPlayedPosition - 500);
            player.setPlayWhenReady(true);
        }

    }

    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    private void playInternal(ExoMediaSource mediaSource, boolean playWhenReady, long where, MediaSourceCreator creator) {
        MediaSource tmp = creator.buildMediaSource(mediaSource.uri(), mediaSource.extension());
        player.prepare(tmp);
        if (where == C.TIME_UNSET) {
            player.seekTo(0L);
        } else {
            player.seekTo(where);
        }

        player.setPlayWhenReady(requestAudioFocus() && playWhenReady);
    }

    private void createExoPlayer(MediaSourceCreator creator) {
        if (player != null) {
            releasePlayer();
        }

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getContext());
        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(getContext(), renderersFactory);
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(getContext(),new AdaptiveTrackSelection.Factory());
        builder.setTrackSelector(new DefaultTrackSelector(getContext(),adaptiveTrackSelectionFactory));
        builder.setTrackSelector(trackSelector);
        builder.setBandwidthMeter(new DefaultBandwidthMeter.Builder(getContext()).build());
        SimpleExoPlayer internalPlayer = builder.build();
        internalPlayer.addListener(componentListener);
        internalPlayer.addListener(creator.getEventLogger());
        internalPlayer.addMetadataOutput(creator.getEventLogger());
        setPlayer(internalPlayer);
    }


    public void releasePlayer() {
        if (player != null) {
            player.release();
        }
        player = null;
    }


    private boolean requestAudioFocus() {

        if (audioManager == null) {
            return true;
        }
        // Request audio focus for playback
        int result = audioManager.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    public void setBackListener(ExoVideoPlaybackControlView.ExoClickListener backListener) {
        if (controller != null) {
            controller.setBackListener(backListener);
        }
    }


    public void setOrientationListener(ExoVideoPlaybackControlView.OrientationListener orientationListener) {
        if (controller != null) {
            controller.setOrientationListener(orientationListener);
        }
    }

    public void setPortrait(boolean portrait) {
        if (controller != null) {
            controller.setPortrait(portrait);
        }

    }


    public void setControllerDisplayMode(int displayMode) {
        if (controller != null) {
            controller.setControllerDisplayMode(ExoVideoPlaybackControlView.CONTROLLER_MODE_ALL);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (controller != null) {
            return controller.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public View attachVideoView() {
        return this;
    }


    private final class ComponentListener implements TextOutput, Player.EventListener,
            com.google.android.exoplayer2.video.VideoListener {

        @Override
        public void onCues(List<Cue> cues) {}

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            if (contentFrame != null) {
                float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
                contentFrame.setAspectRatio(aspectRatio);
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (isPlayingAd()) {
                hideController();
            } else {
                maybeShowController(false);
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (isPlayingAd()) {
                hideController();
            }
        }
    }
}
