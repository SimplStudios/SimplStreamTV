package com.simplstudios.simplstream.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "avatar_index")
    val avatarIndex: Int = 0,
    
    @ColumnInfo(name = "pin_hash")
    val pinHash: String? = null, // Null means no PIN required
    
    @ColumnInfo(name = "is_kids_profile")
    val isKidsProfile: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
