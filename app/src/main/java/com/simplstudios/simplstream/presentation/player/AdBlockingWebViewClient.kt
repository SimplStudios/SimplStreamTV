package com.simplstudios.simplstream.presentation.player

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * Custom WebViewClient that blocks ads and prevents popup/redirect hijacking
 */
class AdBlockingWebViewClient(
    private val onPageStarted: (String?) -> Unit = {},
    private val onPageFinished: (String?) -> Unit = {},
    private val onError: (String) -> Unit = {},
    private val onAdPageDetected: () -> Unit = {},
    private val validDomains: List<String> = emptyList()
) : WebViewClient() {

    companion object {
        // Known ad and tracking domains to block
        private val BLOCKED_DOMAINS = listOf(
            // Ad networks
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "google-analytics.com",
            "googletagmanager.com",
            "facebook.com/tr",
            "connect.facebook.net",
            "adservice.google",
            "pagead2.googlesyndication.com",
            "adnxs.com",
            "adsrvr.org",
            "advertising.com",
            "pubmatic.com",
            "openx.net",
            "rubiconproject.com",
            "criteo.com",
            "outbrain.com",
            "taboola.com",
            "mgid.com",
            "revcontent.com",
            
            // Tracking
            "scorecardresearch.com",
            "quantserve.com",
            "hotjar.com",
            "crazyegg.com",
            "mouseflow.com",
            "fullstory.com",
            "mixpanel.com",
            "amplitude.com",
            "segment.io",
            "optimizely.com",
            
            // Popup/overlay ad networks commonly used by streaming sites
            "popads.net",
            "popcash.net",
            "propellerads.com",
            "exoclick.com",
            "trafficjunky.com",
            "juicyads.com",
            "adsterra.com",
            "a]dspyglass.com",
            "hilltopads.net",
            "clickadu.com",
            "evadav.com",
            "pushground.com",
            "richpush.co",
            "megapush.io",
            "pushwoosh.com",
            "onesignal.com",
            "pushengage.com",
            
            // Common embed ad overlays
            "streamtape.com/get_video",
            "filemoon.sx/download",
            "dood.watch/e",
            
            // Malware/scam domains
            "bit.ly",
            "tinyurl.com",
            "adf.ly",
            "bc.vc",
            "exe.io",
            "za.gl",
            "uiz.io",
            
            // Cryptocurrency miners
            "coinhive.com",
            "coin-hive.com",
            "cryptoloot.pro",
            "crypto-loot.com",
            "webminerpool.com"
        )

        // Patterns that indicate ad/popup content
        private val BLOCKED_URL_PATTERNS = listOf(
            "/ads/",
            "/ad/",
            "/advertisement",
            "/pop.js",
            "/popup",
            "/popunder",
            "clickunder",
            "/banner",
            "/sponsor",
            "vast.js",
            "vpaid",
            "/track",
            "/pixel",
            "/beacon",
            "prebid",
            "/adserver",
            "adtech",
            "/pops/",
            "interstitial"
        )

        // File types to block (usually ads or tracking)
        private val BLOCKED_FILE_EXTENSIONS = listOf(
            ".gif?",  // tracking pixels often use gif
            "pixel.",
            "tracking.",
            "beacon."
        )
    }

    private var originalUrl: String? = null
    private var lastLoadedUrl: String? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        
        if (originalUrl == null) {
            originalUrl = url
        }
        lastLoadedUrl = url
        onPageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        
        // Check if we've been redirected to an ad page
        if (url != null && originalUrl != null && isAdRedirect(url)) {
            onAdPageDetected()
        }
        
        // Inject ad-blocking CSS and JS
        injectAdBlockingCode(view)
        
        onPageFinished(url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
        
        if (shouldBlockUrl(url)) {
            // Return empty response to block the request
            return WebResourceResponse(
                "text/plain",
                "utf-8",
                ByteArrayInputStream("".toByteArray())
            )
        }
        
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        
        // Block navigation to ad pages
        if (shouldBlockUrl(url)) {
            return true // Block the navigation
        }
        
        // Check if this is a popup/redirect attempt
        if (!request.isForMainFrame) {
            // This is likely an iframe or popup - check if it's valid
            if (!isValidEmbedDomain(url)) {
                return true // Block
            }
        }
        
        // Allow navigation to valid video source domains
        if (isValidEmbedDomain(url)) {
            return false // Allow
        }
        
        // Block external redirects (likely ads)
        if (isExternalRedirect(url)) {
            onAdPageDetected()
            return true // Block
        }
        
        return false
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        onError(description ?: "Failed to load video")
    }

    private fun shouldBlockUrl(url: String): Boolean {
        val lowercaseUrl = url.lowercase()
        
        // Check against blocked domains
        for (domain in BLOCKED_DOMAINS) {
            if (lowercaseUrl.contains(domain)) {
                return true
            }
        }
        
        // Check against blocked URL patterns
        for (pattern in BLOCKED_URL_PATTERNS) {
            if (lowercaseUrl.contains(pattern)) {
                return true
            }
        }
        
        // Check file extensions
        for (ext in BLOCKED_FILE_EXTENSIONS) {
            if (lowercaseUrl.contains(ext)) {
                return true
            }
        }
        
        return false
    }

    private fun isValidEmbedDomain(url: String): Boolean {
        val lowercaseUrl = url.lowercase()
        
        // Always allow our configured video source domains
        val validSources = listOf(
            "111movies.com",
            "vidnest.fun",
            "vidsrc.cc",
            "vidsrc.to",
            "vidsrc.me",
            "vidlink.pro",
            "vidplay.site",
            "vidplay.online",
            "vidstream.pro",
            "filemoon.sx",
            "streamtape.com",
            "dood.watch",
            "mixdrop.co",
            "upstream.to"
        )
        
        for (domain in validSources + validDomains) {
            if (lowercaseUrl.contains(domain)) {
                return true
            }
        }
        
        return false
    }

    private fun isExternalRedirect(url: String): Boolean {
        val original = originalUrl ?: return false
        
        // Get base domains
        val originalDomain = extractDomain(original)
        val newDomain = extractDomain(url)
        
        // If navigating to a completely different domain, it might be an ad redirect
        return originalDomain != newDomain && !isValidEmbedDomain(url)
    }

    private fun isAdRedirect(url: String): Boolean {
        return shouldBlockUrl(url) || 
               (!isValidEmbedDomain(url) && url != originalUrl)
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.host?.lowercase()?.removePrefix("www.") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun injectAdBlockingCode(view: WebView?) {
        view ?: return
        
        // CSS to hide common ad elements
        val css = """
            /* Hide common ad containers */
            .ad, .ads, .advertisement, .ad-container, .ad-wrapper,
            [class*="ad-"], [id*="ad-"], [class*="ads-"], [id*="ads-"],
            .banner, .popup, .overlay:not(.player-overlay), .modal-backdrop,
            .interstitial, .sponsored, .promotion,
            [class*="popup"], [id*="popup"],
            iframe[src*="ad"], iframe[src*="banner"],
            div[data-ad], div[data-ads],
            .close-button-overlay, .skip-ad,
            /* VidSrc specific overlays */
            div[style*="z-index: 2147483647"],
            div[style*="position: fixed"][style*="inset: 0"],
            div[style*="position: fixed"][style*="top: 0"][style*="left: 0"][style*="right: 0"][style*="bottom: 0"],
            div[style*="background"][style*="fixed"][style*="100%"],
            .vjs-overlay, .overlay-container,
            div[class*="blocker"], div[id*="blocker"],
            div[class*="preroll"], div[id*="preroll"] {
                display: none !important;
                visibility: hidden !important;
                opacity: 0 !important;
                pointer-events: none !important;
                height: 0 !important;
                width: 0 !important;
                overflow: hidden !important;
            }
            
            /* Ensure video player is always visible and clickable */
            video, .video-player, .player, .jw-video, #player, .vjs-tech,
            [class*="player"]:not([class*="popup"]) {
                z-index: 9999 !important;
                pointer-events: auto !important;
            }
            
            /* Make sure the video container is on top */
            .video-container, #video-container, .player-container {
                z-index: 9998 !important;
            }
        """.trimIndent()
        
        // JavaScript to remove ad elements and prevent popups
        val js = """
            (function() {
                // Override window.open to block popups
                window.open = function() { return null; };
                
                // Block alert/confirm/prompt
                window.alert = function() { return true; };
                window.confirm = function() { return true; };
                window.prompt = function() { return ''; };
                
                // Function to remove overlays
                function removeOverlays() {
                    // Remove elements with very high z-index (ad overlays)
                    document.querySelectorAll('*').forEach(function(el) {
                        var style = window.getComputedStyle(el);
                        var zIndex = parseInt(style.zIndex) || 0;
                        var position = style.position;
                        var bgColor = style.backgroundColor;
                        
                        // Detect fullscreen overlays
                        if (position === 'fixed' || position === 'absolute') {
                            var rect = el.getBoundingClientRect();
                            var isFullscreen = rect.width >= window.innerWidth * 0.8 && 
                                              rect.height >= window.innerHeight * 0.8;
                            var isWhiteOverlay = bgColor.includes('255, 255, 255') || 
                                                bgColor === 'white' || 
                                                bgColor === 'rgb(255, 255, 255)';
                            var isBlackOverlay = bgColor.includes('0, 0, 0') || 
                                                bgColor === 'black';
                            
                            // Remove if it's a fullscreen overlay that's not a video
                            if (isFullscreen && (isWhiteOverlay || zIndex > 9000)) {
                                if (!el.querySelector('video') && el.tagName !== 'VIDEO') {
                                    el.style.display = 'none';
                                    el.style.pointerEvents = 'none';
                                    el.remove();
                                }
                            }
                        }
                    });
                    
                    // Remove onclick handlers that open popups
                    document.querySelectorAll('[onclick]').forEach(function(el) {
                        var onclick = el.getAttribute('onclick') || '';
                        if (onclick.includes('window.open') || onclick.includes('popup')) {
                            el.removeAttribute('onclick');
                        }
                    });
                    
                    // Remove ad iframes
                    document.querySelectorAll('iframe').forEach(function(iframe) {
                        var src = iframe.src || '';
                        if (src.includes('ad') || src.includes('banner') || src.includes('pop') || 
                            src.includes('tracking') || src === '' || src === 'about:blank') {
                            // Don't remove video player iframes
                            if (!src.includes('vidplay') && !src.includes('vidsrc') && 
                                !src.includes('filemoon') && !src.includes('streamtape')) {
                                iframe.remove();
                            }
                        }
                    });
                    
                    // Remove elements by common ad selectors
                    var selectors = [
                        '.ad', '.ads', '.popup', '.overlay:not(.player-overlay)', 
                        '.modal', '.interstitial', '[class*="ad-"]', '[id*="ad-"]',
                        '[class*="overlay"]:not(.player)', '[class*="blocker"]',
                        'div[style*="z-index: 999"]', 'div[style*="z-index: 9999"]'
                    ];
                    selectors.forEach(function(selector) {
                        try {
                            document.querySelectorAll(selector).forEach(function(el) {
                                if (!el.closest('video') && !el.closest('.player') && 
                                    !el.querySelector('video') && el.tagName !== 'VIDEO') {
                                    el.style.display = 'none';
                                    el.style.pointerEvents = 'none';
                                }
                            });
                        } catch(e) {}
                    });
                }
                
                // Run immediately
                removeOverlays();
                
                // Run after a delay (for dynamically loaded ads)
                setTimeout(removeOverlays, 500);
                setTimeout(removeOverlays, 1000);
                setTimeout(removeOverlays, 2000);
                setTimeout(removeOverlays, 3000);
                setTimeout(removeOverlays, 5000);
                
                // Observe DOM for dynamically added ads
                var observer = new MutationObserver(function(mutations) {
                    removeOverlays();
                });
                
                if (document.body) {
                    observer.observe(document.body, { 
                        childList: true, 
                        subtree: true,
                        attributes: true,
                        attributeFilter: ['style', 'class']
                    });
                }
                
                // Intercept addEventListener for click hijacking
                var originalAddEventListener = EventTarget.prototype.addEventListener;
                EventTarget.prototype.addEventListener = function(type, listener, options) {
                    if (type === 'click' && this === document) {
                        // Block document-level click handlers (often used for ad popups)
                        return;
                    }
                    return originalAddEventListener.call(this, type, listener, options);
                };
            })();
        """.trimIndent()
        
        // Inject CSS
        val cssInjection = "javascript:(function() { " +
                "var style = document.createElement('style'); " +
                "style.type = 'text/css'; " +
                "style.innerHTML = '${css.replace("\n", " ").replace("'", "\\'")}'; " +
                "document.head.appendChild(style); " +
                "})();"
        
        view.evaluateJavascript(cssInjection, null)
        view.evaluateJavascript(js, null)
    }

    /**
     * Call this to go back to the original video URL if hijacked
     */
    fun getOriginalUrl(): String? = originalUrl

    /**
     * Check if we can go back within the WebView
     */
    fun canGoBackToVideo(currentUrl: String?): Boolean {
        return originalUrl != null && currentUrl != originalUrl
    }
}
