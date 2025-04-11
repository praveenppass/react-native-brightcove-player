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

interface PlaylistItem {
  videoId: string;
  title?: string;
  thumbnail?: string;
}

interface BrightcovePlayerProps {
  accountId: string;
  videoId: string;
  policyKey: string;
  playlist?: PlaylistItem[];
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
  onPlaylistChange?: (playlist: PlaylistItem[]) => void;
  onVideoChange?: (videoId: string) => void;
  onSeek?: (time: number) => void;
  onSeekForward?: (time: number) => void;
  onSeekBackward?: (time: number) => void;
  autoPlay?: boolean;
  initialLanguage?: string;
  initialQuality?: string;
  autoQuality?: boolean;
  captionsEnabled?: boolean;
  captionsLanguage?: string;
  showControls?: boolean;
  seekInterval?: number;
  enableBackgroundPlayback?: boolean;
  enablePictureInPicture?: boolean;
  enableOfflinePlayback?: boolean;
}

interface BufferingEvent {
  isBuffering: boolean;
}

const BrightcovePlayer: React.FC<BrightcovePlayerProps> = ({
  accountId,
  videoId,
  policyKey,
  playlist,
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
  onPlaylistChange,
  onVideoChange,
  onSeek,
  onSeekForward,
  onSeekBackward,
  autoPlay = false,
  initialLanguage = 'en',
  initialQuality,
  autoQuality = true,
  captionsEnabled = true,
  captionsLanguage = 'en',
  showControls = true,
  seekInterval = 10,
  enableBackgroundPlayback = false,
  enablePictureInPicture = false,
  enableOfflinePlayback = false,
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
      
      // Initialize player with credentials and features
      await playerRef.current.initialize({
        accountId,
        videoId,
        policyKey,
        autoPlay,
        initialLanguage,
        initialQuality,
        autoQuality,
        captionsEnabled,
        captionsLanguage,
        showControls,
        seekInterval,
        enableBackgroundPlayback,
        enablePictureInPicture,
        enableOfflinePlayback,
        playlist,
      });
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

    // Playlist events
    eventEmitterRef.current.addListener('onPlaylistChange', (event: { playlist: PlaylistItem[] }) => {
      onPlaylistChange?.(event.playlist);
    });

    // Video change events
    eventEmitterRef.current.addListener('onVideoChange', (event: { videoId: string }) => {
      onVideoChange?.(event.videoId);
    });

    // Seek events
    eventEmitterRef.current.addListener('onSeek', (event: { time: number }) => {
      onSeek?.(event.time);
    });

    eventEmitterRef.current.addListener('onSeekForward', (event: { time: number }) => {
      onSeekForward?.(event.time);
    });

    eventEmitterRef.current.addListener('onSeekBackward', (event: { time: number }) => {
      onSeekBackward?.(event.time);
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
    seekForward: (seconds: number = seekInterval) => playerRef.current?.seekForward(viewRef.current, seconds),
    seekBackward: (seconds: number = seekInterval) => playerRef.current?.seekBackward(viewRef.current, seconds),
    setVolume: (volume: number) => playerRef.current?.setVolume(viewRef.current, volume),
    getAvailableQualities: () => playerRef.current?.getAvailableQualities(viewRef.current),
    getCurrentQuality: () => playerRef.current?.getCurrentQuality(viewRef.current),
    setQuality: (quality: string) => playerRef.current?.setQuality(viewRef.current, quality),
    getAvailableCaptions: () => playerRef.current?.getAvailableCaptions(viewRef.current),
    getCurrentTime: () => playerRef.current?.getCurrentTime(viewRef.current),
    getDuration: () => playerRef.current?.getDuration(viewRef.current),
    playNext: () => playerRef.current?.playNext(viewRef.current),
    playPrevious: () => playerRef.current?.playPrevious(viewRef.current),
    getPlaylist: () => playerRef.current?.getPlaylist(viewRef.current),
    setPlaylist: (playlist: PlaylistItem[]) => playerRef.current?.setPlaylist(viewRef.current, playlist),
    enterPictureInPicture: () => playerRef.current?.enterPictureInPicture(viewRef.current),
    exitPictureInPicture: () => playerRef.current?.exitPictureInPicture(viewRef.current),
    downloadVideo: () => playerRef.current?.downloadVideo(viewRef.current),
    getDownloadedVideos: () => playerRef.current?.getDownloadedVideos(viewRef.current),
    deleteDownloadedVideo: (videoId: string) => playerRef.current?.deleteDownloadedVideo(viewRef.current, videoId),
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