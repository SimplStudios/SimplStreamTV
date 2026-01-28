package com.simplstudios.simplstream.presentation.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplstudios.simplstream.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Video player fragment using WebView for embed sources
 * SimplStream v1.0
 */
@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {
    
    private val viewModel: PlayerViewModel by viewModels()
    private val args: PlayerFragmentArgs by navArgs()
    
    // Views
    private lateinit var webView: WebView
    private lateinit var loadingView: View
    private lateinit var loadingText: TextView
    private lateinit var errorView: View
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button
    private lateinit var controlsOverlay: View
    private lateinit var titleText: TextView
    private lateinit var sourceButton: TextView
    private lateinit var backButton: ImageButton
    private lateinit var progressBar: ProgressBar
    
    // Server sidebar views
    private lateinit var serverSidebar: View
    private lateinit var serverList: RecyclerView
    private lateinit var closeSidebarButton: ImageButton
    private lateinit var showNotFoundButton: Button
    
    private var hideControlsJob: Job? = null
    private var loadedUrl: String? = null
    
    // Server adapter
    private val serverAdapter = ServerAdapter(
        onServerSelected = { source ->
            viewModel.switchToServer(source)
            hideSidebar()
        },
        onSetDefaultServer = { source ->
            viewModel.setDefaultServer(source.id)
        }
    )
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            // Keep screen on
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
            bindViews(view)
            setupWebView()
            setupControls()
            setupServerSidebar()
            setupBackHandler()
            observeState()
            observeEvents()
            
            // Initialize with playback args
            viewModel.initialize(args.playbackArgs)
        } catch (e: Exception) {
            e.printStackTrace()
            findNavController().popBackStack()
        }
    }
    
    private fun bindViews(view: View) {
        webView = view.findViewById(R.id.web_view)
        loadingView = view.findViewById(R.id.loading_view)
        loadingText = view.findViewById(R.id.loading_text)
        errorView = view.findViewById(R.id.error_view)
        errorText = view.findViewById(R.id.error_text)
        retryButton = view.findViewById(R.id.retry_button)
        controlsOverlay = view.findViewById(R.id.controls_overlay)
        titleText = view.findViewById(R.id.title_text)
        sourceButton = view.findViewById(R.id.source_button)
        backButton = view.findViewById(R.id.back_button)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Server sidebar
        serverSidebar = view.findViewById(R.id.server_sidebar)
        serverList = view.findViewById(R.id.server_list)
        closeSidebarButton = view.findViewById(R.id.close_sidebar_button)
        showNotFoundButton = view.findViewById(R.id.show_not_found_button)
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(false)
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(false)
                allowFileAccess = true
                databaseEnabled = true
                setGeolocationEnabled(false)
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewModel.onSourceLoaded()
                }
                
                @Deprecated("Deprecated in Java")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    if (failingUrl == loadedUrl) {
                        viewModel.onSourceError(description ?: "Failed to load")
                    }
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progressBar.progress = newProgress
                }
            }
            
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
    }
    
    private fun setupControls() {
        backButton.setOnClickListener { showSidebar() }
        sourceButton.setOnClickListener { toggleSidebar() }
        retryButton.setOnClickListener {
            loadSource(viewModel.uiState.value.currentUrl)
        }
        
        webView.setOnClickListener {
            if (serverSidebar.isVisible) {
                hideSidebar()
            } else {
                viewModel.toggleControls()
            }
        }
        
        view?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        if (!serverSidebar.isVisible) {
                            viewModel.toggleControls()
                            true
                        } else false
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        if (serverSidebar.isVisible) {
                            viewModel.exit()
                        } else {
                            showSidebar()
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (!serverSidebar.isVisible && controlsOverlay.isVisible) {
                            showSidebar()
                            true
                        } else false
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (serverSidebar.isVisible) {
                            hideSidebar()
                            true
                        } else false
                    }
                    else -> false
                }
            } else false
        }
    }
    
    private fun setupServerSidebar() {
        serverList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serverAdapter
        }
        closeSidebarButton.setOnClickListener { hideSidebar() }
        showNotFoundButton.setOnClickListener { showNotFoundDialog() }
    }
    
    private fun setupBackHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (serverSidebar.isVisible) {
                        viewModel.exit()
                    } else {
                        showSidebar()
                    }
                }
            }
        )
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingView.isVisible = state.isLoading
                    loadingText.text = "Loading ${state.currentSource?.displayName ?: "video"}..."
                    
                    errorView.isVisible = state.error != null && !state.isLoading
                    errorText.text = state.error ?: ""
                    
                    titleText.text = state.displayTitle
                    sourceButton.text = state.currentSource?.displayName ?: "Server"
                    sourceButton.isVisible = state.hasMultipleSources
                    
                    controlsOverlay.isVisible = state.showControls && !serverSidebar.isVisible
                    
                    if (state.showControls && state.isPlaying) {
                        scheduleHideControls()
                    }
                    
                    updateServerList(state)
                    
                    if (state.currentUrl.isNotEmpty() && loadedUrl != state.currentUrl) {
                        loadedUrl = state.currentUrl
                        loadSource(state.currentUrl)
                    }
                }
            }
        }
    }
    
    private fun updateServerList(state: PlayerUiState) {
        val items = state.videoSources.map { source ->
            ServerAdapter.ServerItem(
                source = source,
                isCurrentServer = source.id == state.currentSource?.id,
                isDefaultServer = source.id == state.defaultServerId
            )
        }
        serverAdapter.submitList(items)
    }
    
    private fun loadSource(url: String) {
        viewModel.onSourceLoadStarted()
        webView.loadUrl(url)
    }
    
    private fun toggleSidebar() {
        if (serverSidebar.isVisible) hideSidebar() else showSidebar()
    }
    
    private fun showSidebar() {
        serverSidebar.isVisible = true
        controlsOverlay.isVisible = false
        hideControlsJob?.cancel()
        serverList.post {
            serverList.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
        }
    }
    
    private fun hideSidebar() {
        serverSidebar.isVisible = false
        controlsOverlay.isVisible = viewModel.uiState.value.showControls
        webView.requestFocus()
    }
    
    private fun showNotFoundDialog() {
        NotFoundInfoDialog.newInstance().show(childFragmentManager, NotFoundInfoDialog.TAG)
    }
    
    private fun scheduleHideControls() {
        hideControlsJob?.cancel()
        hideControlsJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)
            if (!serverSidebar.isVisible) {
                viewModel.hideControls()
            }
        }
    }
    
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is PlayerEvent.Exit -> findNavController().navigateUp()
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        webView.onPause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        webView.destroy()
    }
}
