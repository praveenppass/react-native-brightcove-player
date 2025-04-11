import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Platform, NativeEventEmitter, NativeModules, findNodeHandle } from 'react-native';

interface Quality {
  width: number;
  height: number;
  bitrate: number;
}

interface Caption {
  language: string;
  label: string;
}

interface BrightcovePlayerProps {
  accountId: string;
  videoId: string;
  policyKey: string;
  onError?: (error: Error | unknown) => void;
  onPlay?: () => void;
  onPause?: () => void;
  onEnd?: () => void;
  onBuffering?: (isBuffering: boolean) => void;
  onProgress?: (currentTime: number, duration: number) => void;
  onDurationChange?: (duration: number) => void;
  onQualityChange?: (quality: Quality) => void;
  onAvailableQualities?: (qualities: Quality[]) => void;
  onAvailableCaptions?: (captions: Caption[]) => void;
  onLanguageChange?: (language: string) => void;
  autoPlay?: boolean;
  initialLanguage?: string;
  initialQuality?: string;
  autoQuality?: boolean;
  captionsEnabled?: boolean;
  captionsLanguage?: string;
  showControls?: boolean;
}

interface BufferingEvent {
  isBuffering: boolean;
}

const BrightcovePlayer: React.FC<BrightcovePlayerProps> = ({
  accountId,
  videoId,
  policyKey,
  onError,
  onPlay,
  onPause,
  onEnd,
  onBuffering,
  onProgress,
  onDurationChange,
  onQualityChange,
  onAvailableQualities,
  onAvailableCaptions,
  onLanguageChange,
  autoPlay = false,
  initialLanguage = 'en',
  initialQuality,
  autoQuality = true,
  captionsEnabled = true,
  captionsLanguage = 'en',
  showControls = true,
}) => {
  const playerRef = useRef<any>(null);
  const eventEmitterRef = useRef<NativeEventEmitter | null>(null);
  const viewRef = useRef<number>(0);

  useEffect(() => {
    initializePlayer();
    setupEventListeners();
    return () => {
      cleanup();
    };
  }, []);

  const initializePlayer = async () => {
    try {
      if (Platform.OS === 'ios') {
        playerRef.current = NativeModules.BrightcovePlayer;
      } else {
        playerRef.current = NativeModules.BrightcovePlayer;
      }
      
      // Initialize player with credentials
      await playerRef.current.initialize(accountId, videoId, policyKey, autoPlay, initialLanguage);
    } catch (error) {
      onError?.(error);
    }
  };

  const setupEventListeners = () => {
    if (Platform.OS === 'ios') {
      eventEmitterRef.current = new NativeEventEmitter(NativeModules.BrightcovePlayer);
    } else {
      eventEmitterRef.current = new NativeEventEmitter();
    }

    // Buffering events
    eventEmitterRef.current.addListener('onBuffering', (event: BufferingEvent) => {
      onBuffering?.(event.isBuffering);
    });

    // Playback events
    eventEmitterRef.current.addListener('onPlay', () => {
      onPlay?.();
    });

    eventEmitterRef.current.addListener('onPause', () => {
      onPause?.();
    });

    eventEmitterRef.current.addListener('onEnd', () => {
      onEnd?.();
    });

    // Progress events
    eventEmitterRef.current.addListener('onProgress', (event: { currentTime: number; duration: number }) => {
      onProgress?.(event.currentTime, event.duration);
    });

    // Duration change events
    eventEmitterRef.current.addListener('onDurationChange', (event: { duration: number }) => {
      onDurationChange?.(event.duration);
    });

    // Quality change events
    eventEmitterRef.current.addListener('onQualityChange', (event: { quality: Quality }) => {
      onQualityChange?.(event.quality);
    });

    // Available qualities events
    eventEmitterRef.current.addListener('onAvailableQualities', (event: { qualities: Quality[] }) => {
      onAvailableQualities?.(event.qualities);
    });

    // Available captions events
    eventEmitterRef.current.addListener('onAvailableCaptions', (event: { captions: Caption[] }) => {
      onAvailableCaptions?.(event.captions);
    });

    // Language change events
    eventEmitterRef.current.addListener('onLanguageChange', (event: { language: string }) => {
      onLanguageChange?.(event.language);
    });
  };

  const cleanup = () => {
    if (playerRef.current) {
      playerRef.current.cleanup();
    }
  };

  // Expose player methods through ref
  React.useImperativeHandle(playerRef, () => ({
    play: () => playerRef.current?.play(viewRef.current),
    pause: () => playerRef.current?.pause(viewRef.current),
    seekTo: (time: number) => playerRef.current?.seekTo(viewRef.current, time),
    setVolume: (volume: number) => playerRef.current?.setVolume(viewRef.current, volume),
    getAvailableQualities: () => playerRef.current?.getAvailableQualities(viewRef.current),
    getCurrentQuality: () => playerRef.current?.getCurrentQuality(viewRef.current),
    getAvailableCaptions: () => playerRef.current?.getAvailableCaptions(viewRef.current),
    getCurrentTime: () => playerRef.current?.getCurrentTime(viewRef.current),
    getDuration: () => playerRef.current?.getDuration(viewRef.current),
  }));

  return (
    <View 
      style={styles.container}
      onLayout={(event) => {
        const nodeHandle = findNodeHandle(event.target);
        if (nodeHandle !== null) {
          viewRef.current = nodeHandle;
        }
      }}
    />
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
    aspectRatio: 16 / 9,
    backgroundColor: '#000',
  },
});

export default BrightcovePlayer; 