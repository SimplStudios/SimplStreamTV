package com.simplstudios.simplstream.data.extractor

import com.simplstudios.simplstream.domain.model.VideoServerId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts direct stream URLs from embed pages for ExoPlayer playback
 */
@Singleton
class StreamExtractor @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    
    data class StreamResult(
        val streamUrl: String,
        val headers: Map<String, String> = emptyMap(),
        val isHls: Boolean = false,
        val subtitles: List<Subtitle> = emptyList()
    )
    
    data class Subtitle(
        val url: String,
        val language: String,
        val label: String
    )
    
    /**
     * Extract direct stream URL from embed page
     * Returns null if extraction fails - caller should fall back to WebView
     */
    suspend fun extractStream(embedUrl: String, serverId: VideoServerId): StreamResult? {
        return withContext(Dispatchers.IO) {
            try {
                when (serverId) {
                    VideoServerId.MOVIES111 -> extract111Movies(embedUrl)
                    VideoServerId.VIDNEST -> extractVidNest(embedUrl)
                    VideoServerId.VIDSRC -> extractVidSrc(embedUrl)
                    VideoServerId.VIDLINK -> extractVidLink(embedUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    private fun fetchPage(url: String, headers: Map<String, String> = emptyMap()): String? {
        return try {
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            
            headers.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }
            
            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun extract111Movies(embedUrl: String): StreamResult? {
        val html = fetchPage(embedUrl, mapOf("Referer" to "https://111movies.com/")) ?: return null
        
        // Look for m3u8 or mp4 URLs in the page
        val hlsPattern = Pattern.compile("""(?:file|source|src)["']?\s*[:=]\s*["']?(https?://[^"'\s]+\.m3u8[^"'\s]*)""", Pattern.CASE_INSENSITIVE)
        val mp4Pattern = Pattern.compile("""(?:file|source|src)["']?\s*[:=]\s*["']?(https?://[^"'\s]+\.mp4[^"'\s]*)""", Pattern.CASE_INSENSITIVE)
        
        // Try HLS first
        var matcher = hlsPattern.matcher(html)
        if (matcher.find()) {
            val streamUrl = matcher.group(1)?.replace("\\", "") ?: return null
            return StreamResult(
                streamUrl = streamUrl,
                headers = mapOf("Referer" to embedUrl),
                isHls = true
            )
        }
        
        // Try MP4
        matcher = mp4Pattern.matcher(html)
        if (matcher.find()) {
            val streamUrl = matcher.group(1)?.replace("\\", "") ?: return null
            return StreamResult(
                streamUrl = streamUrl,
                headers = mapOf("Referer" to embedUrl),
                isHls = false
            )
        }
        
        // Look for encoded/obfuscated sources
        val encodedPattern = Pattern.compile("""atob\(["']([A-Za-z0-9+/=]+)["']\)""")
        matcher = encodedPattern.matcher(html)
        if (matcher.find()) {
            try {
                val decoded = String(android.util.Base64.decode(matcher.group(1), android.util.Base64.DEFAULT))
                if (decoded.contains(".m3u8")) {
                    val urlMatch = Pattern.compile("""(https?://[^\s"']+\.m3u8[^\s"']*)""").matcher(decoded)
                    if (urlMatch.find()) {
                        return StreamResult(
                            streamUrl = urlMatch.group(1) ?: return null,
                            headers = mapOf("Referer" to embedUrl),
                            isHls = true
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore decoding errors
            }
        }
        
        return null
    }
    
    private fun extractVidNest(embedUrl: String): StreamResult? {
        val html = fetchPage(embedUrl, mapOf("Referer" to "https://vidnest.fun/")) ?: return null
        
        // VidNest typically uses JWPlayer or similar with file: "url"
        val patterns = listOf(
            Pattern.compile("""["']?file["']?\s*:\s*["'](https?://[^"']+\.m3u8[^"']*)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""sources\s*:\s*\[\s*\{\s*["']?file["']?\s*:\s*["'](https?://[^"']+)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""source\s*=\s*["'](https?://[^"']+\.m3u8[^"']*)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(https?://[^"'\s]+/master\.m3u8[^"'\s]*)""", Pattern.CASE_INSENSITIVE)
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                val streamUrl = matcher.group(1)?.replace("\\", "") ?: continue
                if (streamUrl.isNotEmpty()) {
                    return StreamResult(
                        streamUrl = streamUrl,
                        headers = mapOf("Referer" to embedUrl),
                        isHls = streamUrl.contains(".m3u8")
                    )
                }
            }
        }
        
        return null
    }
    
    private fun extractVidSrc(embedUrl: String): StreamResult? {
        val html = fetchPage(embedUrl, mapOf("Referer" to "https://vidsrc.me/")) ?: return null
        
        // VidSrc uses various player implementations
        val patterns = listOf(
            Pattern.compile("""["']?(?:file|src|source)["']?\s*[:=]\s*["'](https?://[^"']+\.m3u8[^"']*)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""data-src=["'](https?://[^"']+)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""Playerjs\(\{[^}]*file\s*:\s*["'](https?://[^"']+)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(https?://[^"'\s<>]+\.m3u8(?:\?[^"'\s<>]*)?)""", Pattern.CASE_INSENSITIVE)
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                val streamUrl = matcher.group(1)?.replace("\\", "") ?: continue
                if (streamUrl.isNotEmpty() && !streamUrl.contains("advertisement")) {
                    return StreamResult(
                        streamUrl = streamUrl,
                        headers = mapOf(
                            "Referer" to embedUrl,
                            "Origin" to "https://vidsrc.me"
                        ),
                        isHls = streamUrl.contains(".m3u8")
                    )
                }
            }
        }
        
        return null
    }
    
    private fun extractVidLink(embedUrl: String): StreamResult? {
        val html = fetchPage(embedUrl, mapOf("Referer" to "https://vidlink.pro/")) ?: return null
        
        // VidLink patterns
        val patterns = listOf(
            Pattern.compile("""["']?file["']?\s*:\s*["'](https?://[^"']+)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""sources\s*=\s*\[["'](https?://[^"']+)["']\]""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""src\s*=\s*["'](https?://[^"']+\.m3u8[^"']*)["']""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(https?://[^"'\s]+hls[^"'\s]*\.m3u8[^"'\s]*)""", Pattern.CASE_INSENSITIVE)
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                val streamUrl = matcher.group(1)?.replace("\\", "") ?: continue
                if (streamUrl.isNotEmpty()) {
                    return StreamResult(
                        streamUrl = streamUrl,
                        headers = mapOf("Referer" to embedUrl),
                        isHls = streamUrl.contains(".m3u8")
                    )
                }
            }
        }
        
        // Try to find iframe and recurse
        val iframePattern = Pattern.compile("""<iframe[^>]+src=["'](https?://[^"']+)["']""", Pattern.CASE_INSENSITIVE)
        val iframeMatcher = iframePattern.matcher(html)
        if (iframeMatcher.find()) {
            val iframeUrl = iframeMatcher.group(1)
            if (iframeUrl != null && iframeUrl != embedUrl) {
                val iframeHtml = fetchPage(iframeUrl, mapOf("Referer" to embedUrl)) ?: return null
                for (pattern in patterns) {
                    val matcher = pattern.matcher(iframeHtml)
                    if (matcher.find()) {
                        val streamUrl = matcher.group(1)?.replace("\\", "") ?: continue
                        if (streamUrl.isNotEmpty()) {
                            return StreamResult(
                                streamUrl = streamUrl,
                                headers = mapOf("Referer" to iframeUrl),
                                isHls = streamUrl.contains(".m3u8")
                            )
                        }
                    }
                }
            }
        }
        
        return null
    }
    
    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
}
