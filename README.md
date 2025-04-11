# React Native Brightcove Player

A React Native plugin for the Brightcove Player SDK with ExoPlayer integration and HD quality support.

[![npm version](https://badge.fury.io/js/react-native-brightcove-player.svg)](https://badge.fury.io/js/react-native-brightcove-player)
[![GitHub license](https://img.shields.io/github/license/praveenppass/react-native-brightcove-player)](https://github.com/praveenppass/react-native-brightcove-player/blob/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/praveenppass/react-native-brightcove-player)](https://github.com/praveenppass/react-native-brightcove-player/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/praveenppass/react-native-brightcove-player)](https://github.com/praveenppass/react-native-brightcove-player/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/praveenppass/react-native-brightcove-player)](https://github.com/praveenppass/react-native-brightcove-player/pulls)

## Features

- 🎥 Core video playback functionality with native implementation for both iOS and Android
- 🎮 ExoPlayer integration for enhanced playback experience
- 🎯 HD quality support with multiple resolutions (1080p, 720p, 480p, 360p)
- 🔄 Automatic quality switching based on network conditions
- 🎛️ Manual quality selection with user preference
- 📊 Quality change event notifications and monitoring
- 🔒 Secure credential management system
- 🌐 Network state monitoring and optimization
- ⚡ Promise-based method calls for better async handling
- 🎯 Comprehensive error handling and reporting
- 📱 Support for multiple video formats and codecs
- 🎨 Customizable player controls and UI
- 🔄 Background playback support
- 📺 Picture-in-Picture mode support
- 💾 Offline playback capabilities
- 📈 Analytics and tracking integration
- 🔄 Adaptive bitrate streaming
- 📝 Subtitle and caption support
- 🔊 Multiple audio track support
- ⏳ Native buffering loader display during video loading
- ⏮️⏭️ Next/Previous video navigation with playlist support

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
# Using npm
npm install react-native-brightcove-player

# Using yarn
yarn add react-native-brightcove-player
```

### React Native Compatibility
- React Native >= 0.70.0
- React >= 18.0.0

### Important Note
All Brightcove SDK dependencies are included directly in the plugin. You do not need to declare any Brightcove dependencies in your parent project. The plugin handles all necessary configurations automatically.

### Secure Credential Management

The plugin uses environment variables for secure credential management. To set up your Brightcove credentials:

1. For iOS, set the following environment variables:
```bash
export BRIGHTCOVE_USERNAME="your-email@example.com"
export BRIGHTCOVE_PASSWORD="your-password"
```

2. For Android, create or edit the `~/.gradle/gradle.properties` file:
```properties
# Brightcove credentials
brightcove.user=your-email@example.com
brightcove.password=your-password

# GitHub credentials (if needed)
github.user=your-github-username
github.password=your-github-token
```

3. For local development, you can create a `.env` file in your project root:
```bash
BRIGHTCOVE_USERNAME=your-email@example.com
BRIGHTCOVE_PASSWORD=your-password
GITHUB_USERNAME=your-github-username
GITHUB_TOKEN=your-github-token
```

This approach keeps sensitive information out of your source code and build files, following security best practices.

## Usage

```jsx
import React, { useRef, useState } from 'react';
import { View, StyleSheet } from 'react-native';
import BrightcovePlayer from 'react-native-brightcove-player';

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

```jsx
// Play video
playerRef.current?.play();

// Pause video
playerRef.current?.pause();

// Get current time
playerRef.current?.getCurrentTime();

// Seek to specific time
playerRef.current?.seekTo(seconds);

// Seek forward by seconds
playerRef.current?.seekForward(seconds);

// Seek backward by seconds
playerRef.current?.seekBackward(seconds);

// Get available qualities
playerRef.current?.getAvailableQualities();

// Get current quality
playerRef.current?.getCurrentQuality();

// Set quality
playerRef.current?.setQuality('720p');

// Play next video in playlist
playerRef.current?.playNext();

// Play previous video in playlist
playerRef.current?.playPrevious();

// Get current playlist
playerRef.current?.getPlaylist();

// Set playlist
playerRef.current?.setPlaylist(videos);
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
| playlist | array | No | Array of video IDs for playlist support |
| onPlay | function | No | Called when video starts playing |
| onPause | function | No | Called when video is paused |
| onProgress | function | No | Called with progress data |
| onBuffering | function | No | Called when buffering state changes |
| onQualityChange | function | No | Called when video quality changes |
| onAvailableQualities | function | No | Called with available quality options |
| onPlaylistChange | function | No | Called when playlist changes |
| onVideoChange | function | No | Called when current video changes |
| onSeek | function | No | Called when seeking starts/ends |
| onSeekForward | function | No | Called when seeking forward |
| onSeekBackward | function | No | Called when seeking backward |
| seekInterval | number | No | Default seek interval in seconds (default: 10) |
| initialQuality | string | No | Initial video quality ('auto', '1080p', '720p', '480p', '360p') |

## Usage Example with Seek Controls

```jsx
import React, { useRef, useState } from 'react';
import { View, StyleSheet, TouchableOpacity, Text } from 'react-native';
import BrightcovePlayer from 'react-native-brightcove-player';

const App = () => {
  const playerRef = useRef(null);
  const [currentQuality, setCurrentQuality] = useState('auto');
  const [availableQualities, setAvailableQualities] = useState([]);
  const seekInterval = 10; // 10 seconds

  const handleQualityChange = (quality) => {
    setCurrentQuality(quality);
  };

  const handleAvailableQualities = (qualities) => {
    setAvailableQualities(qualities);
  };

  const handleSeekForward = () => {
    playerRef.current?.seekForward(seekInterval);
  };

  const handleSeekBackward = () => {
    playerRef.current?.seekBackward(seekInterval);
  };

  return (
    <View style={styles.container}>
      <BrightcovePlayer
        ref={playerRef}
        accountId="your_account_id"
        policyKey="your_policy_key"
        videoId="your_video_id"
        seekInterval={seekInterval}
        onPlay={() => console.log('Video started playing')}
        onPause={() => console.log('Video paused')}
        onProgress={(data) => console.log('Progress:', data)}
        onBuffering={(isBuffering) => console.log('Buffering:', isBuffering)}
        onQualityChange={handleQualityChange}
        onAvailableQualities={handleAvailableQualities}
        onSeek={(data) => console.log('Seeking:', data)}
        onSeekForward={(data) => console.log('Seeking forward:', data)}
        onSeekBackward={(data) => console.log('Seeking backward:', data)}
        initialQuality="auto"
      />
      <View style={styles.controls}>
        <TouchableOpacity onPress={handleSeekBackward} style={styles.button}>
          <Text>⏪ {seekInterval}s</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={handleSeekForward} style={styles.button}>
          <Text>{seekInterval}s ⏩</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  controls: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 10,
  },
  button: {
    padding: 10,
    backgroundColor: '#eee',
    borderRadius: 5,
  },
});

export default App;
```

## License

MIT

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

Praveen Kumar Tripathi

## Support

If you encounter any issues or have questions, please file an issue on the [GitHub repository](https://github.com/praveenppass/react-native-brightcove-player).

## GitHub Repository

Visit our [GitHub repository](https://github.com/praveenppass/react-native-brightcove-player) for more information, to report issues, or to contribute to the project. 