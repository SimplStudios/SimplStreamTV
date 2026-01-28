package com.simplstudios.simplstream.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watch_history",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["content_id", "media_type", "profile_id"], unique = true)
    ]
)
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    
    @ColumnInfo(name = "content_id")
    val contentId: Int, // TMDB ID
    
    @ColumnInfo(name = "media_type")
    val mediaType: String, // "movie" or "tv"
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "poster_path")
    val posterPath: String? = null,
    
    @ColumnInfo(name = "backdrop_path")
    val backdropPath: String? = null,
    
    // For TV shows
    @ColumnInfo(name = "season_number")
    val seasonNumber: Int? = null,
    
    @ColumnInfo(name = "episode_number")
    val episodeNumber: Int? = null,
    
    @ColumnInfo(name = "episode_title")
    val episodeTitle: String? = null,
    
    // Watch progress
    @ColumnInfo(name = "watch_position")
    val watchPosition: Long = 0, // Position in milliseconds
    
    @ColumnInfo(name = "total_duration")
    val totalDuration: Long = 0, // Total duration in milliseconds
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "last_watched_at")
    val lastWatchedAt: Long = System.currentTimeMillis()
) {
    val watchProgress: Float
        get() = if (totalDuration > 0) watchPosition.toFloat() / totalDuration else 0f
}
