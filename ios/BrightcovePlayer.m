#import "BrightcovePlayer.h"
#import <BrightcovePlayerSDK/BrightcovePlayerSDK.h>

@interface BrightcovePlayerView : UIView <BCOVPlaybackControllerDelegate>

@property (nonatomic, strong) BCOVPlaybackService *playbackService;
@property (nonatomic, strong) id<BCOVPlaybackController> playbackController;
@property (nonatomic, strong) BCOVPlayerUIView *playerView;
@property (nonatomic, strong) NSString *accountId;
@property (nonatomic, strong) NSString *videoId;
@property (nonatomic, strong) NSString *policyKey;
@property (nonatomic, strong) RCTEventEmitter *eventEmitter;
@property (nonatomic, strong) UIActivityIndicatorView *loadingIndicator;
@property (nonatomic, strong) UIProgressView *bufferingProgressView;
@property (nonatomic, strong) UISlider *progressSlider;
@property (nonatomic, strong) UILabel *currentTimeLabel;
@property (nonatomic, strong) UILabel *durationLabel;
@property (nonatomic, strong) UIView *controlsContainer;
@property (nonatomic, assign) BOOL isControlsVisible;

@end

@implementation BrightcovePlayerView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setupPlayer];
        [self setupLoadingIndicator];
        [self setupControls];
    }
    return self;
}

- (void)setupLoadingIndicator {
    self.loadingIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    self.loadingIndicator.center = self.center;
    self.loadingIndicator.hidesWhenStopped = YES;
    [self addSubview:self.loadingIndicator];
}

- (void)setupControls {
    // Create controls container
    self.controlsContainer = [[UIView alloc] init];
    self.controlsContainer.backgroundColor = [UIColor colorWithWhite:0 alpha:0.7];
    [self addSubview:self.controlsContainer];
    
    // Create buffering progress view
    self.bufferingProgressView = [[UIProgressView alloc] initWithProgressViewStyle:UIProgressViewStyleDefault];
    self.bufferingProgressView.progressTintColor = [UIColor whiteColor];
    self.bufferingProgressView.trackTintColor = [UIColor grayColor];
    [self.controlsContainer addSubview:self.bufferingProgressView];
    
    // Create progress slider
    self.progressSlider = [[UISlider alloc] init];
    [self.progressSlider addTarget:self action:@selector(progressSliderValueChanged:) forControlEvents:UIControlEventValueChanged];
    [self.progressSlider addTarget:self action:@selector(progressSliderTouchBegan:) forControlEvents:UIControlEventTouchDown];
    [self.progressSlider addTarget:self action:@selector(progressSliderTouchEnded:) forControlEvents:UIControlEventTouchUpInside];
    [self.progressSlider addTarget:self action:@selector(progressSliderTouchEnded:) forControlEvents:UIControlEventTouchUpOutside];
    [self.controlsContainer addSubview:self.progressSlider];
    
    // Create time labels
    self.currentTimeLabel = [[UILabel alloc] init];
    self.currentTimeLabel.textColor = [UIColor whiteColor];
    self.currentTimeLabel.text = @"0:00";
    self.currentTimeLabel.font = [UIFont systemFontOfSize:12];
    [self.controlsContainer addSubview:self.currentTimeLabel];
    
    self.durationLabel = [[UILabel alloc] init];
    self.durationLabel.textColor = [UIColor whiteColor];
    self.durationLabel.text = @"0:00";
    self.durationLabel.font = [UIFont systemFontOfSize:12];
    self.durationLabel.textAlignment = NSTextAlignmentRight;
    [self.controlsContainer addSubview:self.durationLabel];
    
    // Add tap gesture to show/hide controls
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(toggleControls)];
    [self addGestureRecognizer:tapGesture];
    
    self.isControlsVisible = YES;
    
    // Layout controls
    [self layoutControls];
}

- (void)layoutControls {
    CGFloat controlsHeight = 60;
    self.controlsContainer.frame = CGRectMake(0, self.bounds.size.height - controlsHeight, self.bounds.size.width, controlsHeight);
    
    CGFloat progressBarHeight = 4;
    self.bufferingProgressView.frame = CGRectMake(0, 0, self.bounds.size.width, progressBarHeight);
    
    CGFloat sliderHeight = 30;
    self.progressSlider.frame = CGRectMake(10, progressBarHeight, self.bounds.size.width - 20, sliderHeight);
    
    CGFloat labelHeight = 20;
    CGFloat labelWidth = 50;
    self.currentTimeLabel.frame = CGRectMake(10, progressBarHeight + sliderHeight, labelWidth, labelHeight);
    self.durationLabel.frame = CGRectMake(self.bounds.size.width - labelWidth - 10, progressBarHeight + sliderHeight, labelWidth, labelHeight);
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [self layoutControls];
}

- (void)toggleControls {
    self.isControlsVisible = !self.isControlsVisible;
    [UIView animateWithDuration:0.3 animations:^{
        self.controlsContainer.alpha = self.isControlsVisible ? 1.0 : 0.0;
    }];
}

- (void)progressSliderValueChanged:(UISlider *)slider {
    CMTime duration = [self.playbackController.currentItem duration];
    float durationSeconds = CMTimeGetSeconds(duration);
    float seekTime = durationSeconds * slider.value;
    [self.playbackController seekTo:CMTimeMakeWithSeconds(seekTime, NSEC_PER_SEC)];
    [self updateCurrentTimeLabel:seekTime];
}

- (void)progressSliderTouchBegan:(UISlider *)slider {
    [self.playbackController pause];
}

- (void)progressSliderTouchEnded:(UISlider *)slider {
    [self.playbackController play];
}

- (void)updateCurrentTimeLabel:(float)seconds {
    int minutes = (int)seconds / 60;
    int remainingSeconds = (int)seconds % 60;
    self.currentTimeLabel.text = [NSString stringWithFormat:@"%d:%02d", minutes, remainingSeconds];
}

- (void)updateDurationLabel:(float)seconds {
    int minutes = (int)seconds / 60;
    int remainingSeconds = (int)seconds % 60;
    self.durationLabel.text = [NSString stringWithFormat:@"%d:%02d", minutes, remainingSeconds];
}

- (void)showLoadingIndicator {
    [self.loadingIndicator startAnimating];
}

- (void)hideLoadingIndicator {
    [self.loadingIndicator stopAnimating];
}

- (void)setupPlayer {
    // Initialize the playback service
    self.playbackService = [[BCOVPlaybackService alloc] initWithAccountId:self.accountId
                                                                policyKey:self.policyKey];
    
    // Create the playback controller
    BCOVPlayerSDKManager *manager = [BCOVPlayerSDKManager sharedManager];
    self.playbackController = [manager createPlaybackController];
    self.playbackController.delegate = self;
    
    // Create the player view
    self.playerView = [[BCOVPlayerUIView alloc] initWithFrame:self.bounds];
    self.playerView.playbackController = self.playbackController;
    [self addSubview:self.playerView];
}

- (void)setAccountId:(NSString *)accountId {
    _accountId = accountId;
    [self setupPlayer];
}

- (void)setVideoId:(NSString *)videoId {
    _videoId = videoId;
    [self loadVideo];
}

- (void)loadVideo {
    [self.playbackService findVideoWithVideoID:self.videoId
                                 parameters:nil
                                 completion:^(BCOVVideo *video, NSDictionary *jsonResponse, NSError *error) {
        if (error) {
            NSLog(@"Error loading video: %@", error);
            return;
        }
        
        [self.playbackController setVideos:@[video]];
    }];
}

#pragma mark - BCOVPlaybackControllerDelegate

- (void)playbackController:(id<BCOVPlaybackController>)controller didAdvanceToPlaybackSession:(id<BCOVPlaybackSession>)session {
    // Handle session advancement
}

- (void)playbackController:(id<BCOVPlaybackController>)controller playbackSession:(id<BCOVPlaybackSession>)session didReceiveLifecycleEvent:(BCOVPlaybackSessionLifecycleEvent *)lifecycleEvent {
    // Handle lifecycle events
    if ([lifecycleEvent.eventType isEqualToString:BCOVPlaybackSessionLifecycleEventReady]) {
        [self hideLoadingIndicator];
        [self.eventEmitter sendEventWithName:@"onBuffering" body:@{@"isBuffering": @NO}];
    } else if ([lifecycleEvent.eventType isEqualToString:BCOVPlaybackSessionLifecycleEventBufferStart]) {
        [self showLoadingIndicator];
        [self.eventEmitter sendEventWithName:@"onBuffering" body:@{@"isBuffering": @YES}];
    } else if ([lifecycleEvent.eventType isEqualToString:BCOVPlaybackSessionLifecycleEventBufferEnd]) {
        [self hideLoadingIndicator];
        [self.eventEmitter sendEventWithName:@"onBuffering" body:@{@"isBuffering": @NO}];
    } else if ([lifecycleEvent.eventType isEqualToString:BCOVPlaybackSessionLifecycleEventProgress]) {
        float currentTime = [lifecycleEvent.properties[@"currentTime"] floatValue];
        float duration = [lifecycleEvent.properties[@"duration"] floatValue];
        if (duration > 0) {
            self.progressSlider.value = currentTime / duration;
            [self updateCurrentTimeLabel:currentTime];
        }
    } else if ([lifecycleEvent.eventType isEqualToString:BCOVPlaybackSessionLifecycleEventDurationChange]) {
        float duration = [lifecycleEvent.properties[@"duration"] floatValue];
        [self updateDurationLabel:duration];
    } else if ([lifecycleEvent.eventType isEqualToString:BCOVPlaybackSessionLifecycleEventBufferProgress]) {
        float progress = [lifecycleEvent.properties[@"progress"] floatValue];
        self.bufferingProgressView.progress = progress;
    }
}

@end

@implementation BrightcovePlayer

RCT_EXPORT_MODULE()

- (UIView *)view {
    return [[BrightcovePlayerView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(accountId, NSString)
RCT_EXPORT_VIEW_PROPERTY(videoId, NSString)
RCT_EXPORT_VIEW_PROPERTY(policyKey, NSString)

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onBuffering"];
}

@end 