package com.simplstudios.simplstream.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watchlist",
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
data class WatchlistEntity(
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
    
    @ColumnInfo(name = "overview")
    val overview: String? = null,
    
    @ColumnInfo(name = "poster_path")
    val posterPath: String? = null,
    
    @ColumnInfo(name = "backdrop_path")
    val backdropPath: String? = null,
    
    @ColumnInfo(name = "vote_average")
    val voteAverage: Float = 0f,
    
    @ColumnInfo(name = "release_date")
    val releaseDate: String? = null,
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)
