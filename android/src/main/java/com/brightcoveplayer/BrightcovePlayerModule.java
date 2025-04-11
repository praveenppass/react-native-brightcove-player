package com.brightcoveplayer;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.UiThreadUtil;

public class BrightcovePlayerModule extends SimpleViewManager<BrightcovePlayerView> {
    private static final String REACT_CLASS = "BrightcovePlayer";
    private final ReactApplicationContext reactContext;

    public BrightcovePlayerModule(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected BrightcovePlayerView createViewInstance(ThemedReactContext reactContext) {
        return new BrightcovePlayerView(reactContext);
    }

    @ReactProp(name = "accountId")
    public void setAccountId(BrightcovePlayerView view, String accountId) {
        view.setAccountId(accountId);
    }

    @ReactProp(name = "videoId")
    public void setVideoId(BrightcovePlayerView view, String videoId) {
        view.setVideoId(videoId);
    }

    @ReactProp(name = "policyKey")
    public void setPolicyKey(BrightcovePlayerView view, String policyKey) {
        view.setPolicyKey(policyKey);
    }

    @ReactProp(name = "initialQuality")
    public void setInitialQuality(BrightcovePlayerView view, String quality) {
        view.setInitialQuality(quality);
    }

    @ReactMethod
    public void play(final int viewId, final Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            try {
                BrightcovePlayerView view = (BrightcovePlayerView) reactContext.getCurrentActivity()
                    .findViewById(viewId);
                if (view != null) {
                    view.play();
                    promise.resolve(null);
                } else {
                    promise.reject("VIEW_NOT_FOUND", "Could not find view with id " + viewId);
                }
            } catch (Exception e) {
                promise.reject("PLAY_ERROR", e);
            }
        });
    }

    @ReactMethod
    public void pause(final int viewId, final Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            try {
                BrightcovePlayerView view = (BrightcovePlayerView) reactContext.getCurrentActivity()
                    .findViewById(viewId);
                if (view != null) {
                    view.pause();
                    promise.resolve(null);
                } else {
                    promise.reject("VIEW_NOT_FOUND", "Could not find view with id " + viewId);
                }
            } catch (Exception e) {
                promise.reject("PAUSE_ERROR", e);
            }
        });
    }

    @ReactMethod
    public void seekTo(final int viewId, final long milliseconds, final Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            try {
                BrightcovePlayerView view = (BrightcovePlayerView) reactContext.getCurrentActivity()
                    .findViewById(viewId);
                if (view != null) {
                    view.seekTo(milliseconds);
                    promise.resolve(null);
                } else {
                    promise.reject("VIEW_NOT_FOUND", "Could not find view with id " + viewId);
                }
            } catch (Exception e) {
                promise.reject("SEEK_ERROR", e);
            }
        });
    }

    @ReactMethod
    public void setVolume(final int viewId, final float volume, final Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            try {
                BrightcovePlayerView view = (BrightcovePlayerView) reactContext.getCurrentActivity()
                    .findViewById(viewId);
                if (view != null) {
                    view.setVolume(volume);
                    promise.resolve(null);
                } else {
                    promise.reject("VIEW_NOT_FOUND", "Could not find view with id " + viewId);
                }
            } catch (Exception e) {
                promise.reject("VOLUME_ERROR", e);
            }
        });
    }
} 