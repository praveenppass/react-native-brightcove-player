package com.brightcoveplayer;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.view.ViewGroup;
import android.graphics.Color;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventType;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.text.SubtitleView;

public class BrightcovePlayerView extends FrameLayout implements TextOutput {
    private BrightcoveExoPlayerVideoView brightcoveVideoView;
    private Catalog catalog;
    private String accountId;
    private String videoId;
    private String policyKey;
    private ReactApplicationContext reactContext;
    private ProgressBar loadingIndicator;
    private SeekBar progressBar;
    private ProgressBar bufferingProgressBar;
    private TextView currentTimeText;
    private TextView durationText;
    private LinearLayout controlsContainer;
    private boolean isControlsVisible = true;
    private ExoPlayer exoPlayer;
    private DefaultTrackSelector trackSelector;
    private String initialQuality;
    private boolean autoQuality = true;
    private boolean captionsEnabled = true;
    private String captionsLanguage = "en";
    private SubtitleView subtitleView;
    private Handler mainHandler;
    private ConnectivityManager connectivityManager;
    private NetworkCallback networkCallback;

    public BrightcovePlayerView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mainHandler = new Handler(Looper.getMainLooper());
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new NetworkCallback();

        // Initialize ExoPlayer with HD support
        trackSelector = new DefaultTrackSelector(context);
        trackSelector.setParameters(
            trackSelector.buildUponParameters()
                .setMaxVideoSize(1920, 1080)  // Support up to 1080p
                .setMaxVideoBitrate(8000000)  // 8Mbps for high quality
                .setPreferredTextLanguage(captionsLanguage)
                .setPreferredAudioLanguage(captionsLanguage)
        );

        exoPlayer = new ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build();

        brightcoveVideoView = new BrightcoveExoPlayerVideoView(context);
        brightcoveVideoView.setPlayer(exoPlayer);
        addView(brightcoveVideoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        // Initialize subtitle view
        subtitleView = new SubtitleView(context);
        subtitleView.setVisibility(captionsEnabled ? VISIBLE : GONE);
        addView(subtitleView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        // Initialize loading indicator
        loadingIndicator = new ProgressBar(context);
        loadingIndicator.setIndeterminate(true);
        FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        loadingParams.gravity = Gravity.CENTER;
        addView(loadingIndicator, loadingParams);
        loadingIndicator.setVisibility(GONE);
        
        // Initialize controls container
        controlsContainer = new LinearLayout(context);
        controlsContainer.setOrientation(LinearLayout.VERTICAL);
        controlsContainer.setBackgroundColor(Color.argb(180, 0, 0, 0));
        FrameLayout.LayoutParams controlsParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        controlsParams.gravity = Gravity.BOTTOM;
        addView(controlsContainer, controlsParams);
        
        // Initialize progress bars
        bufferingProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bufferingProgressBar.setProgressTintList(ColorStateList.valueOf(Color.WHITE));
        bufferingProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        LinearLayout.LayoutParams bufferingParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            4
        );
        controlsContainer.addView(bufferingProgressBar, bufferingParams);

        progressBar = new SeekBar(context);
        progressBar.setMax(100);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.setMargins(0, 0, 0, 10);
        controlsContainer.addView(progressBar, progressParams);
        
        // Initialize time texts
        LinearLayout timeContainer = new LinearLayout(context);
        timeContainer.setOrientation(LinearLayout.HORIZONTAL);
        timeContainer.setPadding(10, 0, 10, 10);
        
        currentTimeText = new TextView(context);
        currentTimeText.setTextColor(Color.WHITE);
        currentTimeText.setText("0:00");
        
        durationText = new TextView(context);
        durationText.setTextColor(Color.WHITE);
        durationText.setText("0:00");
        durationText.setGravity(Gravity.RIGHT);
        
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        timeContainer.addView(currentTimeText, timeParams);
        timeContainer.addView(durationText, timeParams);
        
        controlsContainer.addView(timeContainer);
        
        // Add event listeners
        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();
        eventEmitter.on(EventType.BUFFER_START, event -> {
            showLoadingIndicator();
            sendEvent("onBuffering", true);
        });
        eventEmitter.on(EventType.BUFFER_END, event -> {
            hideLoadingIndicator();
            sendEvent("onBuffering", false);
        });
        eventEmitter.on(EventType.PROGRESS, event -> {
            updateProgress(event);
        });
        eventEmitter.on(EventType.DURATION_CHANGE, event -> {
            updateDuration(event);
        });

        // Add ExoPlayer listeners with quality tracking
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        showLoadingIndicator();
                        sendEvent("onBuffering", true);
                        break;
                    case Player.STATE_READY:
                        hideLoadingIndicator();
                        sendEvent("onBuffering", false);
                        // Send available qualities when ready
                        sendAvailableQualities();
                        break;
                }
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                sendAvailableQualities();
                sendCurrentQuality();
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                WritableMap params = Arguments.createMap();
                params.putString("error", error.getMessage());
                reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("onError", params);
            }
        });
        
        // Set up progress bar listener
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    long duration = exoPlayer.getDuration();
                    long newPosition = (duration * progress) / 100;
                    exoPlayer.seekTo(newPosition);
                    updateCurrentTime(newPosition);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                exoPlayer.pause();
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                exoPlayer.play();
            }
        });
        
        // Set up touch listener to show/hide controls
        setOnClickListener(v -> {
            isControlsVisible = !isControlsVisible;
            controlsContainer.setVisibility(isControlsVisible ? VISIBLE : GONE);
        });

        // Register network callback
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            if (autoQuality) {
                adjustQualityForNetwork();
            }
        }
    }

    private void adjustQualityForNetwork() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            DefaultTrackSelector.Parameters.Builder parametersBuilder = trackSelector.getParameters().buildUpon();
            
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    parametersBuilder.setMaxVideoSize(1920, 1080);
                    parametersBuilder.setMaxVideoBitrate(8000000);
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    parametersBuilder.setMaxVideoSize(854, 480);
                    parametersBuilder.setMaxVideoBitrate(2000000);
                    break;
                default:
                    parametersBuilder.setMaxVideoSize(640, 360);
                    parametersBuilder.setMaxVideoBitrate(1000000);
                    break;
            }
            
            trackSelector.setParameters(parametersBuilder);
        }
    }

    @Override
    public void onCues(List<Cue> cues) {
        if (captionsEnabled && subtitleView != null) {
            subtitleView.setCues(cues);
        }
    }

    public void setCaptionsEnabled(boolean enabled) {
        this.captionsEnabled = enabled;
        if (subtitleView != null) {
            subtitleView.setVisibility(enabled ? VISIBLE : GONE);
        }
    }

    public void setCaptionsLanguage(String language) {
        this.captionsLanguage = language;
        DefaultTrackSelector.Parameters.Builder parametersBuilder = trackSelector.getParameters().buildUpon();
        parametersBuilder.setPreferredTextLanguage(language);
        trackSelector.setParameters(parametersBuilder);
    }

    public void setAutoQuality(boolean auto) {
        this.autoQuality = auto;
        if (auto) {
            adjustQualityForNetwork();
        }
    }

    public void setShowControls(boolean show) {
        this.isControlsVisible = show;
        if (controlsContainer != null) {
            controlsContainer.setVisibility(show ? VISIBLE : GONE);
        }
    }

    public void getAvailableQualities(Promise promise) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            WritableArray qualities = Arguments.createArray();
            
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
                    TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
                    
                    for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
                        TrackGroup group = trackGroups.get(groupIndex);
                        for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                            Format format = group.getFormat(trackIndex);
                            WritableMap quality = Arguments.createMap();
                            quality.putInt("width", format.width);
                            quality.putInt("height", format.height);
                            quality.putInt("bitrate", format.bitrate);
                            qualities.pushMap(quality);
                        }
                    }
                }
            }
            
            promise.resolve(qualities);
        } else {
            promise.reject("NO_QUALITIES", "No qualities available");
        }
    }

    public void getCurrentQuality(Promise promise) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
                    TrackSelectionArray selections = exoPlayer.getCurrentTrackSelections();
                    TrackSelection selection = selections.get(rendererIndex);
                    
                    if (selection != null) {
                        Format format = selection.getSelectedFormat();
                        WritableMap quality = Arguments.createMap();
                        quality.putInt("width", format.width);
                        quality.putInt("height", format.height);
                        quality.putInt("bitrate", format.bitrate);
                        promise.resolve(quality);
                        return;
                    }
                }
            }
        }
        promise.reject("NO_QUALITY", "No quality information available");
    }

    public void getAvailableCaptions(Promise promise) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            WritableArray captions = Arguments.createArray();
            
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
                    TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
                    
                    for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
                        TrackGroup group = trackGroups.get(groupIndex);
                        for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                            Format format = group.getFormat(trackIndex);
                            WritableMap caption = Arguments.createMap();
                            caption.putString("language", format.language);
                            caption.putString("label", format.label);
                            captions.pushMap(caption);
                        }
                    }
                }
            }
            
            promise.resolve(captions);
        } else {
            promise.reject("NO_CAPTIONS", "No captions available");
        }
    }

    public void getCurrentTime(Promise promise) {
        long currentTime = exoPlayer.getCurrentPosition();
        promise.resolve(currentTime);
    }

    public void getDuration(Promise promise) {
        long duration = exoPlayer.getDuration();
        promise.resolve(duration);
    }

    private void sendAvailableQualities() {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            WritableArray qualities = Arguments.createArray();
            
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
                    TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
                    
                    for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
                        TrackGroup group = trackGroups.get(groupIndex);
                        for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                            Format format = group.getFormat(trackIndex);
                            WritableMap quality = Arguments.createMap();
                            quality.putInt("width", format.width);
                            quality.putInt("height", format.height);
                            quality.putInt("bitrate", format.bitrate);
                            qualities.pushMap(quality);
                        }
                    }
                }
            }
            
            WritableMap params = Arguments.createMap();
            params.putArray("qualities", qualities);
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onAvailableQualities", params);
        }
    }

    private void sendCurrentQuality() {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
                    TrackSelectionArray selections = exoPlayer.getCurrentTrackSelections();
                    TrackSelection selection = selections.get(rendererIndex);
                    
                    if (selection != null) {
                        Format format = selection.getSelectedFormat();
                        WritableMap params = Arguments.createMap();
                        params.putInt("width", format.width);
                        params.putInt("height", format.height);
                        params.putInt("bitrate", format.bitrate);
                        reactContext
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onQualityChanged", params);
                    }
                }
            }
        }
    }

    public void setInitialQuality(String quality) {
        this.initialQuality = quality;
        applyQualityConstraints();
    }

    private void applyQualityConstraints() {
        if (initialQuality != null) {
            DefaultTrackSelector.Parameters.Builder parametersBuilder = trackSelector.getParameters().buildUpon();
            
            switch (initialQuality) {
                case "1080p":
                    parametersBuilder.setMaxVideoSize(1920, 1080);
                    parametersBuilder.setMaxVideoBitrate(8000000);
                    break;
                case "720p":
                    parametersBuilder.setMaxVideoSize(1280, 720);
                    parametersBuilder.setMaxVideoBitrate(4000000);
                    break;
                case "480p":
                    parametersBuilder.setMaxVideoSize(854, 480);
                    parametersBuilder.setMaxVideoBitrate(2000000);
                    break;
                case "360p":
                    parametersBuilder.setMaxVideoSize(640, 360);
                    parametersBuilder.setMaxVideoBitrate(1000000);
                    break;
            }
            
            trackSelector.setParameters(parametersBuilder);
        }
    }
    
    private void updateProgress(Event event) {
        long currentTime = event.getIntegerProperty("currentTime");
        long duration = exoPlayer.getDuration();
        if (duration > 0) {
            int progress = (int) ((currentTime * 100) / duration);
            progressBar.setProgress(progress);
            updateCurrentTime(currentTime);
        }
    }
    
    private void updateDuration(Event event) {
        long duration = event.getIntegerProperty("duration");
        durationText.setText(formatTime(duration));
    }
    
    private void updateCurrentTime(long milliseconds) {
        currentTimeText.setText(formatTime(milliseconds));
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private void updateBufferingProgress(int progress) {
        bufferingProgressBar.setProgress(progress);
    }

    private void showLoadingIndicator() {
        loadingIndicator.setVisibility(VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingIndicator.setVisibility(GONE);
    }

    private void sendEvent(String eventName, boolean isBuffering) {
        WritableMap params = Arguments.createMap();
        params.putBoolean("isBuffering", isBuffering);
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
        setupCatalog();
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
        loadVideo();
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
        setupCatalog();
    }

    private void setupCatalog() {
        if (accountId != null && policyKey != null) {
            catalog = new Catalog.Builder(accountId, policyKey)
                    .build();
        }
    }

    private void loadVideo() {
        if (catalog != null && videoId != null) {
            catalog.findVideoByID(videoId, new VideoListener() {
                @Override
                public void onVideo(Video video) {
                    brightcoveVideoView.add(video);
                    applyQualityConstraints();
                }
            });
        }
    }

    public void play() {
        exoPlayer.play();
    }

    public void pause() {
        exoPlayer.pause();
    }

    public void seekTo(long milliseconds) {
        exoPlayer.seekTo(milliseconds);
    }

    public void setVolume(float volume) {
        exoPlayer.setVolume(volume);
    }

    public EventEmitter getEventEmitter() {
        return brightcoveVideoView.getEventEmitter();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
} 