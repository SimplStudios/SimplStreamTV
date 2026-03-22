package com.simplstudios.simplstream.presentation.player

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.data.remote.dto.VideoStream
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Native video player using ExoPlayer with direct streams from SimplStream API
 * NO WEBVIEW = NO ADS! 🎉
 */
@AndroidEntryPoint
class NativePlayerFragment : Fragment(R.layout.fragment_native_player) {
    
    private val viewModel: NativePlayerViewModel by viewModels()
    private val args: NativePlayerFragmentArgs by navArgs()
    
    // Views
    private lateinit var playerView: PlayerView
    private lateinit var subtitleView: androidx.media3.ui.SubtitleView
    private lateinit var loadingView: View
    private lateinit var loadingText: TextView
    private lateinit var loadingSubtext: TextView
    private lateinit var errorView: View
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button
    private lateinit var tryNextButton: Button
    private lateinit var whyButton: Button
    private lateinit var controlsOverlay: View
    private lateinit var titleRow: View
    private lateinit var controlsRow: View
    private lateinit var progressRow: View
    private lateinit var titleText: TextView
    private lateinit var qualityText: TextView
    private lateinit var sourceButton: TextView
    private lateinit var backButton: ImageButton
    private lateinit var progressSeekbar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var totalTimeText: TextView
    private lateinit var playPauseIndicator: ImageView

    // Playback controls
    private lateinit var playPauseButton: ImageButton
    private lateinit var rewindButton: ImageButton
    private lateinit var forwardButton: ImageButton
    
    // Volume controls
    private lateinit var volumeButton: ImageButton
    private lateinit var volumeSeekbar: SeekBar
    private var isMuted = false
    private var lastVolume = 0.8f
    
    // Aspect ratio control
    private lateinit var aspectButton: TextView
    private var currentAspectMode = 0  // Start with FIT - shows ENTIRE video (0=FIT, 1=FILL, 2=STRETCH, 3=ZOOM)
    private val aspectModes = listOf("FIT", "FILL", "STRETCH", "ZOOM")
    
    // Stream sidebar / Player Settings
    private lateinit var streamSidebar: View
    private lateinit var streamList: RecyclerView
    private lateinit var closeSidebarButton: ImageButton
    private lateinit var qualitySelectorButton: TextView
    private lateinit var settingQuality: View
    private lateinit var settingAspect: View
    private lateinit var settingSubtitles: View
    private lateinit var aspectValue: TextView
    private lateinit var subtitlesValue: TextView
    private lateinit var settingsHint: View

    // Next Episode UI
    private lateinit var nextEpisodeOverlay: View
    private lateinit var nextEpisodeButton: Button
    private lateinit var nextEpisodeText: TextView
    private lateinit var nextEpisodeTitle: TextView
    private lateinit var nextEpisodeCountdown: TextView
    private lateinit var nextEpisodeThumbnail: android.widget.ImageView
    private lateinit var replayButton: Button
    private var nextEpisodeCountdownJob: Job? = null
    private var nextEpisodeShown = false  // Track if we've shown the card this playback

    // Skip indicators
    private lateinit var skipBackwardIndicator: View
    private lateinit var skipForwardIndicator: View
    private var skipIndicatorJob: Job? = null
    
    // Subtitles
    private var subtitlesEnabled = false
    private var availableSubtitles: List<Pair<Int, String>> = emptyList() // trackIndex -> language label
    private var currentSubtitleIndex = -1 // -1 = Off
    
    // ExoPlayer
    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var hideControlsJob: Job? = null
    private var progressUpdateJob: Job? = null
    private var currentPlayingStreamId: String? = null  // Track which stream we're playing
    private var userHidControls = false  // Track if user manually hid controls with UP button
    private var isPaused = false  // Track if player is paused to keep controls visible
    private var controlsAnimating = false  // Prevent animation conflicts
    private var loadingMessageJob: Job? = null  // Rotating loading messages

    // Funny loading messages
    private val funnyLoadingMessages = listOf(
        "Bribing the streaming gods...",
        "Convincing pixels to appear...",
        "Stealing Wi-Fi from the neighbors...",
        "Asking the movie nicely to load...",
        "Poking the server with a stick...",
        "Blowing on the cartridge...",
        "Downloading more RAM...",
        "Sending a search party for your movie...",
        "Waking up the intern...",
        "Buffering... just kidding. Maybe."
    )
    
    // Quality selection
    private var availableQualities: List<Pair<Int, String>> = emptyList() // height -> label
    private var currentQualityLabel = "Auto"
    
    // Back button state: press twice to exit
    private var backPressCount = 0
    private var lastBackPressTime = 0L
    
    // Stream adapter
    private val streamAdapter = StreamAdapter { stream ->
        viewModel.switchToStream(stream)
        hideSidebar()
    }
    
    // Prevent double initialization on config changes
    private var isInitialized = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        bindViews(view)
        setupControls()
        setupStreamSidebar()
        setupKeyListener()
        setupBackPressHandler()
        observeState()
        observeEvents()

        // Only initialize ViewModel once to prevent duplicate initialization issues
        if (!isInitialized) {
            isInitialized = true
            viewModel.initialize(args.playbackArgs)
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (streamSidebar.isVisible) {
                    // Sidebar is open - close it and go back to detail page
                    hideSidebar()
                    viewModel.exit()
                } else {
                    // Sidebar is closed - open it
                    showSidebar()
                }
            }
        })
    }
    
    private fun bindViews(view: View) {
        playerView = view.findViewById(R.id.player_view)
        subtitleView = view.findViewById(R.id.subtitle_view)
        loadingView = view.findViewById(R.id.loading_view)
        loadingText = view.findViewById(R.id.loading_text)
        loadingSubtext = view.findViewById(R.id.loading_subtext)
        errorView = view.findViewById(R.id.error_view)
        errorText = view.findViewById(R.id.error_text)
        retryButton = view.findViewById(R.id.retry_button)
        tryNextButton = view.findViewById(R.id.try_next_button)
        whyButton = view.findViewById(R.id.why_button)
        controlsOverlay = view.findViewById(R.id.controls_overlay)
        titleRow = view.findViewById(R.id.title_row)
        controlsRow = view.findViewById(R.id.controls_row)
        progressRow = view.findViewById(R.id.progress_row)
        playPauseIndicator = view.findViewById(R.id.play_pause_indicator)
        titleText = view.findViewById(R.id.title_text)
        qualityText = view.findViewById(R.id.quality_text)
        sourceButton = view.findViewById(R.id.source_button)
        backButton = view.findViewById(R.id.back_button)
        progressSeekbar = view.findViewById(R.id.progress_seekbar)
        currentTimeText = view.findViewById(R.id.current_time_text)
        totalTimeText = view.findViewById(R.id.total_time_text)
        
        // Playback controls
        playPauseButton = view.findViewById(R.id.play_pause_button)
        rewindButton = view.findViewById(R.id.rewind_button)
        forwardButton = view.findViewById(R.id.forward_button)
        
        // Volume controls
        volumeButton = view.findViewById(R.id.volume_button)
        volumeSeekbar = view.findViewById(R.id.volume_seekbar)
        
        // Aspect ratio control
        aspectButton = view.findViewById(R.id.aspect_button)
        
        // Sidebar / Player Settings
        streamSidebar = view.findViewById(R.id.stream_sidebar)
        streamList = view.findViewById(R.id.stream_list)
        closeSidebarButton = view.findViewById(R.id.close_sidebar_button)
        qualitySelectorButton = view.findViewById(R.id.quality_selector_button)
        settingQuality = view.findViewById(R.id.setting_quality)
        settingAspect = view.findViewById(R.id.setting_aspect)
        settingSubtitles = view.findViewById(R.id.setting_subtitles)
        aspectValue = view.findViewById(R.id.aspect_value)
        subtitlesValue = view.findViewById(R.id.subtitles_value)
        settingsHint = view.findViewById(R.id.settings_hint)
        
        // Skip indicators
        skipBackwardIndicator = view.findViewById(R.id.skip_backward_indicator)
        skipForwardIndicator = view.findViewById(R.id.skip_forward_indicator)

        // Next Episode UI
        nextEpisodeOverlay = view.findViewById(R.id.next_episode_overlay)
        nextEpisodeButton = view.findViewById(R.id.next_episode_button)
        nextEpisodeText = view.findViewById(R.id.next_episode_text)
        nextEpisodeTitle = view.findViewById(R.id.next_episode_title)
        nextEpisodeCountdown = view.findViewById(R.id.next_episode_countdown)
        nextEpisodeThumbnail = view.findViewById(R.id.next_episode_thumbnail)
        replayButton = view.findViewById(R.id.replay_button)

        // Disable PlayerView's internal subtitle rendering — we use our own SubtitleView
        playerView.subtitleView?.visibility = View.GONE

        // Configure subtitle view appearance
        subtitleView.setStyle(
            androidx.media3.ui.CaptionStyleCompat(
                android.graphics.Color.WHITE,           // foregroundColor
                android.graphics.Color.parseColor("#CC000000"),  // backgroundColor (semi-transparent black)
                android.graphics.Color.TRANSPARENT,     // windowColor
                androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_OUTLINE,  // edgeType
                android.graphics.Color.BLACK,           // edgeColor
                null                                    // typeface
            )
        )
        subtitleView.setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18f)
    }
    
    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        if (player != null) return
        
        // Create track selector for quality control
        trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(buildUponParameters()
                .setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
                .setForceHighestSupportedBitrate(true)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setPreferredTextLanguage("en"))
        }
        
        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        player = ExoPlayer.Builder(requireContext())
            .setTrackSelector(trackSelector!!)
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                playerView.player = this
                playWhenReady = true
                
                // Force FIT resize mode on PlayerView
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                viewModel.onStreamReady()
                                startProgressUpdates()
                                // Ensure resize mode is maintained after stream is ready
                                playerView.resizeMode = when (currentAspectMode) {
                                    0 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    1 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    2 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            }
                            Player.STATE_ENDED -> {
                                viewModel.onPlaybackEnded()
                                // Auto-return to detail page when episode ACTUALLY ends (0 seconds left)
                                if (viewModel.uiState.value.mediaType == com.simplstudios.simplstream.domain.model.MediaType.TV) {
                                    nextEpisodeCountdownJob?.cancel()
                                    nextEpisodeOverlay.isVisible = false
                                    viewModel.exit()
                                }
                            }
                            Player.STATE_BUFFERING -> {
                                // Show buffering indicator if needed
                            }
                            Player.STATE_IDLE -> {
                                // Idle state
                            }
                        }
                    }
                    
                    override fun onTracksChanged(tracks: Tracks) {
                        // Extract available video qualities from tracks
                        updateAvailableQualities(tracks)
                        // Extract available subtitle tracks
                        updateAvailableSubtitles(tracks)
                    }
                    
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        // Maintain current aspect mode when video size changes (happens after seek)
                        playerView.resizeMode = when (currentAspectMode) {
                            0 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            1 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            2 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
                    
                    @Suppress("DEPRECATION")
                    override fun onCues(cues: List<androidx.media3.common.text.Cue>) {
                        // Update subtitle view with current cues
                        subtitleView.setCues(cues)
                    }
                    
                    override fun onCues(cueGroup: androidx.media3.common.text.CueGroup) {
                        // Update subtitle view with current cues (newer API)
                        subtitleView.setCues(cueGroup.cues)
                    }
                    
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        viewModel.setPlaying(isPlaying)
                        isPaused = !isPlaying

                        // Update play/pause button icon
                        playPauseButton.setImageResource(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        )

                        // Show center play/pause bounce animation
                        showPlayPauseIndicator(isPlaying)

                        // When paused: show controls and keep them visible
                        // When playing: schedule auto-hide
                        if (!isPlaying) {
                            // Paused - show controls and cancel auto-hide
                            hideControlsJob?.cancel()
                            if (!userHidControls) {
                                showControlsAnimated(minimal = true)
                            }
                        } else {
                            // Playing - schedule auto-hide (if controls are visible)
                            if (controlsOverlay.isVisible && !userHidControls) {
                                scheduleHideControls()
                            }
                        }
                    }
                    
                    override fun onPlayerError(error: PlaybackException) {
                        val errorMessage = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                                "Your network connection dropped. Check your internet and try again."
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                                "The stream took too long to respond. Trying another source..."
                            PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                                "This stream format isn't supported. Trying another source..."
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                                "This stream isn't responding. Trying another source..."
                            else -> error.message ?: "Playback failed unexpectedly. Try another source."
                        }
                        viewModel.onStreamError(errorMessage)
                    }
                })
            }
    }
    
    // Store external subtitles for the current stream
    private var externalSubtitles: List<com.simplstudios.simplstream.data.remote.dto.SubtitleTrack> = emptyList()

    @OptIn(UnstableApi::class)
    private fun playStream(stream: VideoStream) {
        val player = player ?: run {
            initializePlayer()
            player
        } ?: return

        android.util.Log.d("NativePlayer", "Playing stream: ${stream.url.take(80)}...")
        android.util.Log.d("NativePlayer", "Headers: ${stream.headers}")
        android.util.Log.d("NativePlayer", "Referer: ${stream.referer}")
        android.util.Log.d("NativePlayer", "IsHLS: ${stream.isHls}")
        android.util.Log.d("NativePlayer", "Subtitles: ${stream.subtitles.size}")

        // Store external subtitles for subtitle selection dialog
        externalSubtitles = stream.subtitles

        // Build headers map including referer
        val headersMap = mutableMapOf<String, String>()
        headersMap.putAll(stream.headers)

        // Always add Referer if available (critical for 403 errors)
        stream.referer?.let { referer ->
            headersMap["Referer"] = referer
            // Also add Origin header (some servers need this)
            try {
                val uri = android.net.Uri.parse(referer)
                headersMap["Origin"] = "${uri.scheme}://${uri.host}"
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }

        android.util.Log.d("NativePlayer", "Final headers: $headersMap")

        // Create data source with custom headers
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)

        // Add headers
        if (headersMap.isNotEmpty()) {
            dataSourceFactory.setDefaultRequestProperties(headersMap)
        }

        // Build MediaItem for video with embedded subtitle configs
        val videoMediaItemBuilder = MediaItem.Builder()
            .setUri(stream.url)

        // Add subtitle configurations to the MediaItem
        if (stream.subtitles.isNotEmpty()) {
            val subtitleConfigs = stream.subtitles.map { sub ->
                MediaItem.SubtitleConfiguration.Builder(android.net.Uri.parse(sub.url))
                    .setMimeType(getMimeTypeForSubtitle(sub.url))
                    .setLanguage(sub.language)
                    .setLabel(sub.label)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
            }
            videoMediaItemBuilder.setSubtitleConfigurations(subtitleConfigs)
            android.util.Log.d("NativePlayer", "Added ${subtitleConfigs.size} subtitle configurations")

            // Log each subtitle for debugging
            stream.subtitles.forEachIndexed { i, sub ->
                android.util.Log.d("NativePlayer", "  Subtitle $i: ${sub.label} (${sub.language}) - ${sub.url.take(60)}...")
            }
        }

        val videoMediaItem = videoMediaItemBuilder.build()

        // Create appropriate media source based on stream type
        val mediaSource = if (stream.isHls) {
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videoMediaItem)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videoMediaItem)
        }

        // Get position before switching media (for seamless stream switching)
        val seekPosition = viewModel.uiState.value.resumePosition

        player.setMediaSource(mediaSource)
        player.prepare()

        // Resume from saved position or current position (for stream switching)
        if (seekPosition > 0) {
            player.seekTo(seekPosition)
        }
    }

    /**
     * Get MIME type for subtitle file based on extension
     */
    private fun getMimeTypeForSubtitle(url: String): String {
        return when {
            url.contains(".vtt", ignoreCase = true) -> "text/vtt"
            url.contains(".srt", ignoreCase = true) -> "application/x-subrip"
            url.contains(".ass", ignoreCase = true) || url.contains(".ssa", ignoreCase = true) -> "text/x-ssa"
            url.contains(".ttml", ignoreCase = true) -> "application/ttml+xml"
            else -> "text/vtt" // Default to VTT
        }
    }
    
    // Subtitle and settings buttons (from control bar)
    private lateinit var subtitlesButton: ImageButton
    private lateinit var settingsButton: ImageButton

    private fun setupControls() {
        // Wire up subtitle button to show subtitle dialog directly
        subtitlesButton = view?.findViewById(R.id.subtitles_button) ?: return
        subtitlesButton.setOnClickListener { showSubtitlesDialog() }

        // Wire up settings button to open sidebar
        settingsButton = view?.findViewById(R.id.settings_button) ?: return
        settingsButton.setOnClickListener { showSidebar() }

        backButton.setOnClickListener {
            if (streamSidebar.isVisible) {
                hideSidebar()
            } else {
                showSidebar()
            }
        }

        sourceButton.setOnClickListener { toggleSidebar() }
        
        retryButton.setOnClickListener {
            viewModel.retryFetchStreams()
        }
        
        tryNextButton.setOnClickListener {
            viewModel.tryNextStream()
        }

        whyButton.setOnClickListener {
            showWhyNoStreamsDialog()
        }

        // Next Episode controls
        nextEpisodeButton.setOnClickListener {
            nextEpisodeCountdownJob?.cancel()
            nextEpisodeOverlay.isVisible = false
            viewModel.playNextEpisode()
        }

        replayButton.setOnClickListener {
            // Close the next episode card - user wants to wait
            nextEpisodeCountdownJob?.cancel()
            nextEpisodeOverlay.isVisible = false
            nextEpisodeShown = false  // Allow showing again if they seek back
        }

        playerView.setOnClickListener {
            if (streamSidebar.isVisible) {
                hideSidebar()
            } else if (controlsOverlay.isVisible) {
                hideControlsAnimated()
            } else {
                showControlsAnimated(minimal = true)
                if (player?.isPlaying == true) {
                    scheduleHideControls()
                }
            }
        }
        
        progressSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = player?.duration ?: 0
                    if (duration > 0) {
                        val seekPosition = (progress.toLong() * duration) / 1000
                        player?.seekTo(seekPosition)
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Playback controls
        playPauseButton.setOnClickListener {
            player?.let { p ->
                if (p.isPlaying) {
                    p.pause()
                    playPauseButton.setImageResource(R.drawable.ic_play)
                } else {
                    p.play()
                    playPauseButton.setImageResource(R.drawable.ic_pause)
                }
            }
        }
        
        rewindButton.setOnClickListener {
            player?.let { p ->
                val savedResizeMode = playerView.resizeMode
                val newPosition = (p.currentPosition - 10000).coerceAtLeast(0)
                p.seekTo(newPosition)
                playerView.resizeMode = savedResizeMode
            }
        }
        
        forwardButton.setOnClickListener {
            player?.let { p ->
                val savedResizeMode = playerView.resizeMode
                val newPosition = (p.currentPosition + 10000).coerceAtMost(p.duration)
                p.seekTo(newPosition)
                playerView.resizeMode = savedResizeMode
            }
        }
        
        // Volume controls
        volumeSeekbar.progress = 80
        volumeButton.setOnClickListener {
            player?.let { p ->
                if (isMuted) {
                    // Unmute
                    p.volume = lastVolume
                    volumeSeekbar.progress = (lastVolume * 100).toInt()
                    volumeButton.setImageResource(
                        if (lastVolume > 0.5f) R.drawable.ic_volume_up else R.drawable.ic_volume_down
                    )
                    isMuted = false
                } else {
                    // Mute
                    lastVolume = p.volume
                    p.volume = 0f
                    volumeSeekbar.progress = 0
                    volumeButton.setImageResource(R.drawable.ic_volume_mute)
                    isMuted = true
                }
            }
        }
        
        volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100f
                    player?.volume = volume
                    lastVolume = volume
                    isMuted = volume == 0f
                    
                    // Update icon
                    volumeButton.setImageResource(when {
                        volume == 0f -> R.drawable.ic_volume_mute
                        volume < 0.5f -> R.drawable.ic_volume_down
                        else -> R.drawable.ic_volume_up
                    })
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Aspect ratio control - set initial state to FILL
        aspectButton.text = aspectModes[currentAspectMode]  // Shows "FILL"
        aspectButton.setOnClickListener {
            cycleAspectRatio()
        }
    }
    
    @OptIn(UnstableApi::class)
    private fun cycleAspectRatio() {
        currentAspectMode = (currentAspectMode + 1) % aspectModes.size
        aspectButton.text = aspectModes[currentAspectMode]
        aspectValue.text = aspectModes[currentAspectMode]
        applyAspectRatio()
    }
    
    private fun setupStreamSidebar() {
        streamList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = streamAdapter
        }
        closeSidebarButton.setOnClickListener { hideSidebar() }

        // Quality setting click - show quality dialog
        settingQuality.setOnClickListener { showQualityDialog() }

        // Aspect ratio setting click - show aspect dialog
        settingAspect.setOnClickListener { showAspectDialog() }

        // Subtitles setting click - toggle or show options
        settingSubtitles.setOnClickListener { showSubtitlesDialog() }

        // Set up D-pad navigation for settings (BACK handled by OnBackPressedCallback)
        settingQuality.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        showQualityDialog()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        settingSubtitles.requestFocus()
                        true
                    }
                    else -> false
                }
            } else false
        }

        settingSubtitles.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        showSubtitlesDialog()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        settingQuality.requestFocus()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        settingAspect.requestFocus()
                        true
                    }
                    else -> false
                }
            } else false
        }

        settingAspect.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        showAspectDialog()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        settingSubtitles.requestFocus()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // Move focus to stream list if available
                        if (streamAdapter.itemCount > 0) {
                            streamList.getChildAt(0)?.requestFocus() ?: streamList.requestFocus()
                        }
                        true
                    }
                    else -> false
                }
            } else false
        }
    }
    
    private fun showAspectDialog() {
        val aspectLabels = arrayOf("FIT (Letterbox)", "FILL (Stretch)", "STRETCH (Fill Screen)", "ZOOM (Crop)")
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Aspect Ratio")
            .setSingleChoiceItems(aspectLabels, currentAspectMode) { dlg, which ->
                currentAspectMode = which
                applyAspectRatio()
                aspectButton.text = aspectModes[currentAspectMode]
                aspectValue.text = aspectModes[currentAspectMode]
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            dialog.listView?.let { listView ->
                listView.isFocusable = true
                listView.isFocusableInTouchMode = true
                listView.choiceMode = android.widget.ListView.CHOICE_MODE_SINGLE
                listView.post {
                    listView.setSelection(currentAspectMode)
                    listView.requestFocus()
                }
                listView.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        when (keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                val position = listView.selectedItemPosition
                                if (position >= 0) {
                                    listView.performItemClick(
                                        listView.getChildAt(position - listView.firstVisiblePosition),
                                        position,
                                        listView.getItemIdAtPosition(position)
                                    )
                                }
                                true
                            }
                            KeyEvent.KEYCODE_BACK -> {
                                dialog.dismiss()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
            }
        }
        dialog.show()
    }
    
    @OptIn(UnstableApi::class)
    private fun showSubtitlesDialog() {
        // Check if any subtitles are available (embedded or external)
        if (availableSubtitles.isEmpty() && externalSubtitles.isEmpty()) {
            AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
                .setTitle("Subtitles")
                .setMessage("No subtitles available for this content.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        // Build subtitle options: "Off" + available tracks
        val subtitleLabels = mutableListOf("Off")
        subtitleLabels.addAll(availableSubtitles.map { it.second })

        val subtitleOptions = subtitleLabels.toTypedArray()
        // currentSubtitleIndex is -1 for Off, otherwise index into availableSubtitles
        val currentSelection = if (currentSubtitleIndex < 0) 0 else currentSubtitleIndex + 1

        val dialog = AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Subtitles")
            .setSingleChoiceItems(subtitleOptions, currentSelection) { dlg, which ->
                if (which == 0) {
                    // Off selected
                    currentSubtitleIndex = -1
                    subtitlesEnabled = false
                    subtitlesValue.text = "Off"
                    setSubtitleTrack(-1)
                } else {
                    // A subtitle track selected
                    currentSubtitleIndex = which - 1
                    subtitlesEnabled = true
                    subtitlesValue.text = availableSubtitles[currentSubtitleIndex].second
                    setSubtitleTrack(currentSubtitleIndex)
                }
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            dialog.listView?.let { listView ->
                listView.isFocusable = true
                listView.isFocusableInTouchMode = true
                listView.choiceMode = android.widget.ListView.CHOICE_MODE_SINGLE
                listView.post {
                    listView.setSelection(currentSelection)
                    listView.requestFocus()
                }
                listView.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        when (keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                val position = listView.selectedItemPosition
                                if (position >= 0) {
                                    listView.performItemClick(
                                        listView.getChildAt(position - listView.firstVisiblePosition),
                                        position,
                                        listView.getItemIdAtPosition(position)
                                    )
                                }
                                true
                            }
                            KeyEvent.KEYCODE_BACK -> {
                                dialog.dismiss()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
            }
        }
        dialog.show()
    }
    
    @OptIn(UnstableApi::class)
    private fun setSubtitleTrack(trackIndex: Int) {
        val selector = trackSelector ?: return
        val currentPlayer = player ?: return
        
        if (trackIndex < 0) {
            // Disable all text tracks
            selector.setParameters(
                selector.buildUponParameters()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            )
            android.util.Log.d("NativePlayer", "Subtitles disabled")
        } else {
            // Enable text tracks and select specific track
            val tracks = currentPlayer.currentTracks
            var textGroupIndex = 0
            var targetGroupIndex = -1
            var targetTrackIndex = -1
            var currentTrackCounter = 0
            
            for (groupIndex in 0 until tracks.groups.size) {
                val group = tracks.groups[groupIndex]
                if (group.type == C.TRACK_TYPE_TEXT) {
                    for (i in 0 until group.length) {
                        if (currentTrackCounter == trackIndex) {
                            targetGroupIndex = groupIndex
                            targetTrackIndex = i
                            break
                        }
                        currentTrackCounter++
                    }
                    if (targetGroupIndex >= 0) break
                    textGroupIndex++
                }
            }
            
            if (targetGroupIndex >= 0 && targetTrackIndex >= 0) {
                val group = tracks.groups[targetGroupIndex]
                val override = TrackSelectionOverride(group.mediaTrackGroup, targetTrackIndex)
                
                selector.setParameters(
                    selector.buildUponParameters()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                        .addOverride(override)
                )
                android.util.Log.d("NativePlayer", "Subtitle track set to: ${availableSubtitles.getOrNull(trackIndex)?.second}")
            }
        }
    }
    
    @OptIn(UnstableApi::class)
    private fun applyAspectRatio() {
        playerView.resizeMode = when (currentAspectMode) {
            0 -> AspectRatioFrameLayout.RESIZE_MODE_FIT      // FIT - letterbox
            1 -> AspectRatioFrameLayout.RESIZE_MODE_FILL     // FILL - may crop
            2 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH  // STRETCH
            3 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM     // ZOOM - crops
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }
    
    @OptIn(UnstableApi::class)
    private fun updateAvailableQualities(tracks: Tracks) {
        val qualities = mutableListOf<Pair<Int, String>>()
        qualities.add(0 to "Auto (Best)")  // Always have Auto option
        
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_VIDEO) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    val height = format.height
                    if (height > 0) {
                        val label = when {
                            height >= 2160 -> "4K (${height}p)"
                            height >= 1080 -> "1080p"
                            height >= 720 -> "720p"
                            height >= 480 -> "480p"
                            height >= 360 -> "360p"
                            else -> "${height}p"
                        }
                        if (qualities.none { it.first == height }) {
                            qualities.add(height to label)
                        }
                    }
                }
            }
        }
        
        // Sort by quality (highest first, Auto stays at 0)
        availableQualities = qualities.sortedByDescending { if (it.first == 0) Int.MAX_VALUE else it.first }
        
        android.util.Log.d("NativePlayer", "Available qualities: $availableQualities")
        
        // Update button text
        activity?.runOnUiThread {
            qualitySelectorButton.text = currentQualityLabel
        }
    }
    
    @OptIn(UnstableApi::class)
    private fun updateAvailableSubtitles(tracks: Tracks) {
        val subtitles = mutableListOf<Pair<Int, String>>()
        var trackIndexGlobal = 0
        
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    val language = format.language
                    val formatId = format.id

                    android.util.Log.d("NativePlayer", "Subtitle track $trackIndexGlobal: label=${format.label}, lang=$language, id=$formatId, mime=${format.sampleMimeType}")

                    // Build a good label from available info
                    val label = when {
                        // Use explicit label if available
                        !format.label.isNullOrBlank() -> format.label!!
                        // Try to get display language from language code
                        !language.isNullOrBlank() && language != "und" -> {
                            try {
                                val locale = java.util.Locale.forLanguageTag(language)
                                val displayName = locale.displayLanguage
                                if (displayName.isNotBlank() && displayName != language) {
                                    displayName
                                } else {
                                    getLanguageDisplayName(language)
                                }
                            } catch (e: Exception) {
                                getLanguageDisplayName(language)
                            }
                        }
                        // Try to extract language from format ID (e.g. "subs:0/en" or "English")
                        !formatId.isNullOrBlank() -> {
                            val langFromId = extractLanguageFromId(formatId)
                            langFromId ?: "English" // Default to English for single unnamed track
                        }
                        // Single subtitle track with no info — likely English
                        else -> "English"
                    }

                    subtitles.add(trackIndexGlobal to label)
                    trackIndexGlobal++
                }
            }
        }
        
        // If no embedded subtitles found, use external subtitles from stream
        if (subtitles.isEmpty() && externalSubtitles.isNotEmpty()) {
            android.util.Log.d("NativePlayer", "No embedded subtitles, using ${externalSubtitles.size} external subtitles")
            externalSubtitles.forEachIndexed { index, sub ->
                subtitles.add(index to sub.label)
            }
        }

        availableSubtitles = subtitles

        android.util.Log.d("NativePlayer", "Available subtitles: $availableSubtitles")

        // Auto-enable English subtitles if available and no subtitle currently selected
        if (currentSubtitleIndex < 0 && availableSubtitles.isNotEmpty()) {
            val englishIndex = availableSubtitles.indexOfFirst { (_, label) ->
                label.equals("English", ignoreCase = true) ||
                label.startsWith("English", ignoreCase = true)
            }
            if (englishIndex >= 0) {
                currentSubtitleIndex = englishIndex
                subtitlesEnabled = true
                setSubtitleTrack(availableSubtitles[englishIndex].first)
                android.util.Log.d("NativePlayer", "Auto-enabled English subtitles (index $englishIndex)")
            }
        }

        // Update UI with current subtitle state
        activity?.runOnUiThread {
            if (currentSubtitleIndex >= 0 && currentSubtitleIndex < availableSubtitles.size) {
                subtitlesValue.text = availableSubtitles[currentSubtitleIndex].second
            } else {
                subtitlesValue.text = if (availableSubtitles.isNotEmpty()) "Off" else "None"
            }
        }
    }
    
    private fun extractLanguageFromId(id: String): String? {
        // Try to find a language code in the ID string
        // Common formats: "subs:0/en", "cc/eng", "English", "en-US"
        val lowerid = id.lowercase()
        // Check if the ID itself is a known language name
        val knownNames = mapOf(
            "english" to "English", "spanish" to "Spanish", "french" to "French",
            "german" to "German", "italian" to "Italian", "portuguese" to "Portuguese",
            "russian" to "Russian", "japanese" to "Japanese", "korean" to "Korean",
            "chinese" to "Chinese", "arabic" to "Arabic", "hindi" to "Hindi"
        )
        for ((key, value) in knownNames) {
            if (lowerid.contains(key)) return value
        }
        // Try to extract 2-3 letter language code from the ID
        val codeMatch = Regex("\\b([a-z]{2,3})\\b").findAll(lowerid)
        for (match in codeMatch) {
            val lang = getLanguageDisplayName(match.value)
            if (lang != match.value.uppercase()) return lang
        }
        return null
    }

    private fun getLanguageDisplayName(code: String): String {
        // Common language codes to display names
        return when (code.lowercase()) {
            "en", "eng" -> "English"
            "es", "spa" -> "Spanish"
            "fr", "fra", "fre" -> "French"
            "de", "deu", "ger" -> "German"
            "it", "ita" -> "Italian"
            "pt", "por" -> "Portuguese"
            "ru", "rus" -> "Russian"
            "ja", "jpn" -> "Japanese"
            "ko", "kor" -> "Korean"
            "zh", "zho", "chi" -> "Chinese"
            "ar", "ara" -> "Arabic"
            "hi", "hin" -> "Hindi"
            "nl", "nld", "dut" -> "Dutch"
            "pl", "pol" -> "Polish"
            "tr", "tur" -> "Turkish"
            "vi", "vie" -> "Vietnamese"
            "th", "tha" -> "Thai"
            "id", "ind" -> "Indonesian"
            "ms", "msa", "may" -> "Malay"
            "sv", "swe" -> "Swedish"
            "da", "dan" -> "Danish"
            "no", "nor" -> "Norwegian"
            "fi", "fin" -> "Finnish"
            "cs", "ces", "cze" -> "Czech"
            "el", "ell", "gre" -> "Greek"
            "he", "heb" -> "Hebrew"
            "hu", "hun" -> "Hungarian"
            "ro", "ron", "rum" -> "Romanian"
            "uk", "ukr" -> "Ukrainian"
            "bg", "bul" -> "Bulgarian"
            "hr", "hrv" -> "Croatian"
            "sk", "slk", "slo" -> "Slovak"
            "sr", "srp" -> "Serbian"
            "und", "unknown" -> "Unknown"
            else -> code.uppercase()
        }
    }
    
    @OptIn(UnstableApi::class)
    private fun showQualityDialog() {
        if (availableQualities.isEmpty()) {
            availableQualities = listOf(0 to "Auto")
        }
        
        val qualityLabels = availableQualities.map { it.second }.toTypedArray()
        val currentIndex = availableQualities.indexOfFirst { it.second == currentQualityLabel }.coerceAtLeast(0)
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Video Quality")
            .setSingleChoiceItems(qualityLabels, currentIndex) { dlg, which ->
                val selected = availableQualities[which]
                currentQualityLabel = selected.second
                qualitySelectorButton.text = currentQualityLabel
                
                // Apply quality selection
                setVideoQuality(selected.first)
                
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            // Make the dialog focusable and handle D-pad
            dialog.window?.let { window ->
                window.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                )
            }
            
            // Find the ListView inside the dialog and make items focusable
            dialog.listView?.let { listView ->
                listView.isFocusable = true
                listView.isFocusableInTouchMode = true
                listView.itemsCanFocus = true
                listView.choiceMode = android.widget.ListView.CHOICE_MODE_SINGLE
                
                // Request focus on current selection
                listView.post {
                    listView.setSelection(currentIndex)
                    listView.requestFocus()
                }
                
                // Handle ENTER/DPAD_CENTER to select
                listView.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        when (keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                val position = listView.selectedItemPosition
                                if (position >= 0) {
                                    listView.performItemClick(
                                        listView.getChildAt(position - listView.firstVisiblePosition),
                                        position,
                                        listView.getItemIdAtPosition(position)
                                    )
                                }
                                true
                            }
                            KeyEvent.KEYCODE_BACK -> {
                                dialog.dismiss()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
            }
        }
        
        dialog.show()
    }
    
    @OptIn(UnstableApi::class)
    private fun setVideoQuality(height: Int) {
        val selector = trackSelector ?: return
        
        if (height == 0) {
            // Auto (Best) - no constraints, prefer highest bitrate
            selector.setParameters(
                selector.buildUponParameters()
                    .clearVideoSizeConstraints()
                    .setForceHighestSupportedBitrate(true)
            )
            android.util.Log.d("NativePlayer", "Quality set to Auto (Best)")
        } else {
            // Set specific quality
            selector.setParameters(
                selector.buildUponParameters()
                    .setMaxVideoSize(Int.MAX_VALUE, height)
                    .setMinVideoSize(0, height)
            )
            android.util.Log.d("NativePlayer", "Quality set to ${height}p")
        }
    }
    
    private fun setupKeyListener() {
        // Make playerView focusable and request focus
        playerView.isFocusable = true
        playerView.isFocusableInTouchMode = true
        playerView.requestFocus()
        
        // Single key listener on playerView handles everything
        playerView.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
            
            when (keyCode) {
                // ENTER / OK = ALWAYS Play/Pause
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    player?.let {
                        if (it.isPlaying) it.pause() else it.play()
                    }
                    true
                }

                // BACK is handled by OnBackPressedCallback in setupBackPressHandler()

                // DOWN = Show minimal controls bar (just progress + timestamps + hints)
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (!controlsOverlay.isVisible) {
                        userHidControls = false
                        showControlsAnimated(minimal = true)
                        if (player?.isPlaying == true) {
                            scheduleHideControls()
                        }
                    }
                    true
                }

                // UP = Hide controls bar
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (controlsOverlay.isVisible) {
                        userHidControls = true
                        hideControlsAnimated()
                    }
                    true
                }
                
                // RIGHT = Forward 10 seconds
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    player?.let { p ->
                        p.seekTo(p.currentPosition + 10_000)
                        showSkipIndicator(forward = true)
                    }
                    true
                }
                
                // LEFT = Rewind 10 seconds
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    player?.let { p ->
                        p.seekTo(maxOf(0, p.currentPosition - 10_000))
                        showSkipIndicator(forward = false)
                    }
                    true
                }
                
                // Media keys
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    player?.let { if (it.isPlaying) it.pause() else it.play() }
                    true
                }
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                    player?.let { 
                        it.seekTo(it.currentPosition + 30_000)
                        showSkipIndicator(forward = true)
                    }
                    true
                }
                KeyEvent.KEYCODE_MEDIA_REWIND -> {
                    player?.let { 
                        it.seekTo(maxOf(0, it.currentPosition - 30_000))
                        showSkipIndicator(forward = false)
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showSkipIndicator(forward: Boolean) {
        skipIndicatorJob?.cancel()
        
        // Show the appropriate indicator
        if (forward) {
            skipForwardIndicator.isVisible = true
            skipBackwardIndicator.isVisible = false
        } else {
            skipBackwardIndicator.isVisible = true
            skipForwardIndicator.isVisible = false
        }
        
        // Hide after 600ms
        skipIndicatorJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(600)
            skipForwardIndicator.isVisible = false
            skipBackwardIndicator.isVisible = false
        }
    }
    
    private fun showExitConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Exit Player")
            .setMessage("Do you want to exit the player?")
            .setPositiveButton("Exit") { _, _ ->
                viewModel.exit()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            // Focus on cancel button by default
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.requestFocus()
        }

        dialog.show()
    }

    private fun showWhyNoStreamsDialog() {
        val message = """
Why can't I watch this?

1. Too New
The movie or TV show you're trying to watch may be too new and not on our SimplStream database yet. Try waiting a little longer for it to be added.

2. Too Old
Some older classics are sometimes not available in the SimplStream database. Try contacting us or finding another streaming service.

3. Not Popular Enough
Some movies or TV shows aren't in our database because they're not popular. Try finding another streaming service that has it.

4. Database Error
There may be a temporary issue with SimplStream. Try:
• Retry the stream
• Contact: simplstudios@protonmail.com
• Check updates: simplstudios.vercel.app
• Try another streaming service
        """.trimIndent()

        val dialog = AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Stream Unavailable")
            .setMessage(message)
            .setPositiveButton("Got it", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.requestFocus()
        }

        dialog.show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Loading state
                    loadingView.isVisible = state.isLoading
                    if (state.isLoading) {
                        if (state.streams.isEmpty()) {
                            startRotatingLoadingMessages()
                        } else {
                            stopRotatingLoadingMessages()
                            loadingText.text = "Loading ${state.currentStream?.provider ?: "stream"}..."
                            loadingSubtext.text = state.currentStream?.quality ?: ""
                        }
                    } else {
                        stopRotatingLoadingMessages()
                    }
                    
                    // Error state
                    errorView.isVisible = state.error != null && !state.isLoading
                    errorText.text = state.error ?: ""
                    tryNextButton.isVisible = state.currentStreamIndex < state.streams.size - 1
                    
                    // Request focus on retry button when error is shown (for D-pad)
                    if (state.error != null && !state.isLoading) {
                        retryButton.requestFocus()
                    }
                    
                    // Title
                    titleText.text = state.displayTitle
                    
                    // Quality info
                    state.currentStream?.let { stream ->
                        qualityText.text = "${stream.quality} • ${stream.provider}"
                    }
                    
                    // Source button
                    sourceButton.text = "Streams (${state.streams.size})"
                    sourceButton.isVisible = state.hasMultipleStreams
                    
                    // Controls visibility - driven by animations, don't fight them
                    // Only act on explicit showControls=false from ViewModel (e.g. toggleControls)
                    // Don't re-schedule hide on every state update — that causes the 1-second close bug
                    
                    // Update stream list
                    updateStreamList(state)
                    
                    // Play stream when available - ONLY if it's a new stream
                    state.currentStream?.let { stream ->
                        if (!state.isLoading && state.error == null && stream.id != currentPlayingStreamId) {
                            currentPlayingStreamId = stream.id
                            playStream(stream)
                        }
                    }
                    
                    // Progress
                    currentTimeText.text = state.formattedPosition
                    totalTimeText.text = state.formattedDuration

                    // Next Episode card - show 30 seconds before end for TV shows
                    val timeRemaining = state.totalDuration - state.currentPosition
                    val thirtySecondsMs = 30_000L

                    if (state.mediaType == com.simplstudios.simplstream.domain.model.MediaType.TV
                        && viewModel.hasNextEpisode()
                        && state.totalDuration > 0
                        && timeRemaining in 1..thirtySecondsMs
                        && !nextEpisodeShown) {
                        // Show next episode card
                        showNextEpisodeCard(state)
                    }
                }
            }
        }
    }
    
    private fun showNextEpisodeCard(state: NativePlayerUiState) {
        nextEpisodeShown = true
        nextEpisodeOverlay.isVisible = true

        // Set episode info
        val nextEpNum = (state.episodeNumber ?: 0) + 1
        nextEpisodeText.text = "Episode $nextEpNum"
        nextEpisodeTitle.text = state.title.substringBefore(" - ")

        // Load thumbnail if available
        state.posterUrl?.let { url ->
            coil.ImageLoader(requireContext()).enqueue(
                coil.request.ImageRequest.Builder(requireContext())
                    .data(url)
                    .target(nextEpisodeThumbnail)
                    .build()
            )
        }

        // Start 10 second countdown
        nextEpisodeCountdownJob?.cancel()
        nextEpisodeCountdownJob = viewLifecycleOwner.lifecycleScope.launch {
            for (i in 10 downTo 1) {
                nextEpisodeCountdown.text = "$i"
                delay(1000)
            }
            // Auto-dismiss after countdown (don't auto-play, just hide)
            // Keep nextEpisodeShown = true so it doesn't show again
            nextEpisodeOverlay.isVisible = false
        }

        // Focus on the Play Next button for D-pad
        nextEpisodeButton.requestFocus()
    }

    private fun updateStreamList(state: NativePlayerUiState) {
        val items = state.streams.map { stream ->
            StreamAdapter.StreamItem(
                stream = stream,
                isCurrentStream = stream.id == state.currentStream?.id
            )
        }
        streamAdapter.submitList(items)
    }
    
    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                player?.let { exoPlayer ->
                    if (exoPlayer.duration > 0) {
                        val position = exoPlayer.currentPosition
                        val duration = exoPlayer.duration
                        
                        // Update seekbar (0-1000 range)
                        progressSeekbar.progress = ((position * 1000) / duration).toInt()
                        
                        // Update ViewModel
                        viewModel.updateProgress(position, duration)
                    }
                }
                delay(1000)
            }
        }
    }
    
    private fun toggleSidebar() {
        if (streamSidebar.isVisible) hideSidebar() else showSidebar()
    }
    
    private fun showSidebar() {
        controlsOverlay.visibility = View.GONE
        controlsOverlay.translationY = 0f
        settingsHint.visibility = View.GONE
        settingsHint.translationY = 0f
        hideControlsJob?.cancel()
        userHidControls = false

        // Slide in from the right
        streamSidebar.translationX = streamSidebar.width.toFloat().takeIf { it > 0f } ?: 400f
        streamSidebar.isVisible = true
        streamSidebar.animate()
            .translationX(0f)
            .setDuration(250)
            .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
            .start()

        // Clear focus from playerView first, then transfer to sidebar
        playerView.clearFocus()
        streamSidebar.postDelayed({
            settingQuality.isFocusable = true
            settingQuality.isFocusableInTouchMode = true
            settingQuality.requestFocus()
        }, 50)
    }

    private fun hideSidebar() {
        userHidControls = false
        backPressCount = 0

        // Slide out to the right
        streamSidebar.animate()
            .translationX(streamSidebar.width.toFloat().takeIf { it > 0f } ?: 400f)
            .setDuration(200)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                streamSidebar.isVisible = false
                streamSidebar.translationX = 0f
            }
            .start()

        // Return focus to playerView for D-pad controls
        playerView.requestFocus()
    }
    
    private fun scheduleHideControls() {
        hideControlsJob?.cancel()

        // Don't auto-hide if paused
        if (isPaused) return

        hideControlsJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(5000)  // 5 seconds of inactivity while playing
            // Only hide if still playing and not user-hidden
            if (!streamSidebar.isVisible && !userHidControls && player?.isPlaying == true) {
                hideControlsAnimated()
                viewModel.hideControls()
            }
        }
    }

    private fun showControlsAnimated(minimal: Boolean = false) {
        if (controlsAnimating) return

        // Set minimal mode: hide title and buttons rows, show only progress + hints
        titleRow.isVisible = !minimal
        controlsRow.isVisible = !minimal

        if (!controlsOverlay.isVisible) {
            controlsOverlay.visibility = View.VISIBLE
            settingsHint.visibility = View.VISIBLE
            controlsOverlay.translationY = controlsOverlay.height.toFloat().takeIf { it > 0f } ?: 200f
            settingsHint.translationY = 50f

            controlsAnimating = true
            val slideUp = ObjectAnimator.ofFloat(controlsOverlay, "translationY", 0f).apply {
                duration = 300
                interpolator = android.view.animation.DecelerateInterpolator()
            }
            val hintSlide = ObjectAnimator.ofFloat(settingsHint, "translationY", 0f).apply {
                duration = 300
                interpolator = android.view.animation.DecelerateInterpolator()
            }
            AnimatorSet().apply {
                playTogether(slideUp, hintSlide)
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        controlsAnimating = false
                    }
                })
                start()
            }
        } else {
            // Already visible, just update minimal mode
            settingsHint.isVisible = true
        }
    }

    private fun hideControlsAnimated() {
        if (!controlsOverlay.isVisible || controlsAnimating) return

        controlsAnimating = true
        hideControlsJob?.cancel()

        val slideDown = ObjectAnimator.ofFloat(controlsOverlay, "translationY", controlsOverlay.height.toFloat()).apply {
            duration = 250
            interpolator = android.view.animation.AccelerateInterpolator()
        }
        val hintSlide = ObjectAnimator.ofFloat(settingsHint, "translationY", 50f).apply {
            duration = 250
            interpolator = android.view.animation.AccelerateInterpolator()
        }
        AnimatorSet().apply {
            playTogether(slideDown, hintSlide)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    controlsOverlay.isVisible = false
                    settingsHint.isVisible = false
                    controlsOverlay.translationY = 0f
                    settingsHint.translationY = 0f
                    controlsAnimating = false
                    playerView.requestFocus()
                }
            })
            start()
        }
    }

    private fun showPlayPauseIndicator(isPlaying: Boolean) {
        playPauseIndicator.setImageResource(if (isPlaying) R.drawable.ic_play else R.drawable.ic_pause)
        playPauseIndicator.visibility = View.VISIBLE
        playPauseIndicator.alpha = 1f
        playPauseIndicator.scaleX = 0.3f
        playPauseIndicator.scaleY = 0.3f

        // Bounce in
        val scaleX = ObjectAnimator.ofFloat(playPauseIndicator, "scaleX", 0.3f, 1.2f, 1f).apply {
            duration = 400
            interpolator = OvershootInterpolator(2f)
        }
        val scaleY = ObjectAnimator.ofFloat(playPauseIndicator, "scaleY", 0.3f, 1.2f, 1f).apply {
            duration = 400
            interpolator = OvershootInterpolator(2f)
        }
        // Fade out after bounce
        val fadeOut = ObjectAnimator.ofFloat(playPauseIndicator, "alpha", 1f, 0f).apply {
            startDelay = 500
            duration = 500
        }

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeOut)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    playPauseIndicator.visibility = View.GONE
                }
            })
            start()
        }
    }
    
    private fun startRotatingLoadingMessages() {
        if (loadingMessageJob?.isActive == true) return
        var index = 0
        loadingMessageJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                loadingText.text = funnyLoadingMessages[index % funnyLoadingMessages.size]
                loadingSubtext.text = "Hold tight..."
                index++
                delay(3000)
            }
        }
    }

    private fun stopRotatingLoadingMessages() {
        loadingMessageJob?.cancel()
        loadingMessageJob = null
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is NativePlayerEvent.Exit -> findNavController().navigateUp()
                        is NativePlayerEvent.PlayNextEpisode -> {
                            // Create new args for next episode and reinitialize
                            val nextArgs = NativePlaybackArgs(
                                contentId = event.contentId,
                                title = event.title,
                                mediaType = com.simplstudios.simplstream.domain.model.MediaType.TV,
                                imdbId = event.imdbId,
                                seasonNumber = event.seasonNumber,
                                episodeNumber = event.episodeNumber,
                                episodeName = "Episode ${event.episodeNumber}",
                                resumePosition = 0,
                                posterUrl = event.posterUrl
                            )
                            // Reset state and play next
                            currentPlayingStreamId = null
                            nextEpisodeOverlay.isVisible = false
                            nextEpisodeShown = false  // Reset for new episode
                            nextEpisodeCountdownJob?.cancel()
                            viewModel.initialize(nextArgs)
                        }
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Only resume playback if we were playing before and player is ready
        player?.let { p ->
            if (p.playbackState == Player.STATE_READY && !isPaused) {
                p.play()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Remember if we were playing before pause
        player?.let { p ->
            if (p.isPlaying) {
                p.pause()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideControlsJob?.cancel()
        progressUpdateJob?.cancel()
        skipIndicatorJob?.cancel()
        nextEpisodeCountdownJob?.cancel()
        loadingMessageJob?.cancel()
        try {
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
            // Ignore if activity is not available
        }
        player?.release()
        player = null
        trackSelector = null
        isInitialized = false
    }
}
