# React Native Brightcove Player

A React Native plugin for the Brightcove Player SDK with ExoPlayer integration and HD quality support.

## Features

- 🎥 Core video playback functionality
- 📱 Native implementation for both iOS and Android
- 🎮 ExoPlayer integration for enhanced playback
- 🎯 HD quality support (1080p, 720p, 480p, 360p)
- 🔄 Automatic quality switching based on network conditions
- 🎛️ Manual quality selection
- 📊 Quality change event notifications
- 🔒 Secure credential management
- 🌐 Network state monitoring
- ⚡ Promise-based method calls
- 🎯 Better error handling

## Dependencies

This plugin includes all necessary Brightcove SDK dependencies:

### Android
- Brightcove Player SDK (v10.0.1)
- ExoPlayer (v2.18.7)
- All required Brightcove dependencies

### iOS
- Brightcove Player SDK (v7.0.2)
- All required Brightcove dependencies

## Installation

```bash
npm install @upgrad/react-native-brightcove-player --registry https://verdaccio.upgrad.dev/
```

### React Native Compatibility
- React Native >= 0.63.0
- React >= 17.0.0

### Important Note
All Brightcove SDK dependencies are included directly in the plugin. You do not need to declare any Brightcove dependencies in your parent project. The plugin handles all necessary configurations automatically.

### Secure Credential Management

The plugin uses Gradle properties for secure credential management. To set up your Brightcove credentials:

1. Create or edit the `~/.gradle/gradle.properties` file:
```properties
# Brightcove credentials
brightcove.user=your-email@example.com
brightcove.password=your-password
```

2. The plugin will automatically use these credentials when accessing the Brightcove repositories.

This approach keeps sensitive information out of your source code and build files, following security best practices.

## Usage

```jsx
import React, { useRef, useState } from 'react';
import { View, StyleSheet } from 'react-native';
import BrightcovePlayer from '@upgrad/react-native-brightcove-player';

const App = () => {
  const playerRef = useRef(null);
  const [currentQuality, setCurrentQuality] = useState('auto');
  const [availableQualities, setAvailableQualities] = useState([]);

  const handleQualityChange = (quality) => {
    setCurrentQuality(quality);
  };

  const handleAvailableQualities = (qualities) => {
    setAvailableQualities(qualities);
  };

  return (
    <View style={styles.container}>
      <BrightcovePlayer
        ref={playerRef}
        accountId="your_account_id"
        policyKey="your_policy_key"
        videoId="your_video_id"
        onPlay={() => console.log('Video started playing')}
        onPause={() => console.log('Video paused')}
        onProgress={(data) => console.log('Progress:', data)}
        onBuffering={(isBuffering) => console.log('Buffering:', isBuffering)}
        onQualityChange={handleQualityChange}
        onAvailableQualities={handleAvailableQualities}
        initialQuality="auto"
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export default App;
```

## Player Methods

```javascript
// Play video
playerRef.current?.play();

// Pause video
playerRef.current?.pause();

// Get current time
playerRef.current?.getCurrentTime();

// Seek to specific time
playerRef.current?.seekTo(seconds);

// Get available qualities
playerRef.current?.getAvailableQualities();

// Get current quality
playerRef.current?.getCurrentQuality();

// Set quality
playerRef.current?.setQuality('720p');
```

## HD Quality Features

### Automatic Quality Selection
- Automatically selects the best quality based on network conditions
- Smooth transitions between quality levels
- Network bandwidth monitoring

### Manual Quality Selection
- User can manually select preferred quality
- Available qualities are provided through `onAvailableQualities` callback
- Quality changes are notified through `onQualityChange` callback

### Quality Change Events
- Real-time notifications of quality changes
- Information about available qualities
- Current quality status

### Quality Persistence
- Remembers user's quality preference
- Applies preferred quality on subsequent video loads

### Quality Indicators
- Visual feedback for current quality
- Quality selection UI components
- Network status indicators

## Props

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| accountId | string | Yes | Brightcove account ID |
| policyKey | string | Yes | Brightcove policy key |
| videoId | string | Yes | Brightcove video ID |
| onPlay | function | No | Called when video starts playing |
| onPause | function | No | Called when video is paused |
| onProgress | function | No | Called with progress data |
| onBuffering | function | No | Called when buffering state changes |
| onQualityChange | function | No | Called when video quality changes |
| onAvailableQualities | function | No | Called with available quality options |
| initialQuality | string | No | Initial video quality ('auto', '1080p', '720p', '480p', '360p') |

## License

MIT 