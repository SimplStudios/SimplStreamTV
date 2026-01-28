# 🎬 SimplStream — Complete Technical Blueprint
## By SimplStudios | Android TV Streaming Application

---

# TABLE OF CONTENTS

1. [Executive Summary](#1-executive-summary)
2. [System Architecture](#2-system-architecture)
3. [Project Structure](#3-project-structure)
4. [Technology Stack](#4-technology-stack)
5. [Design System (TV-Optimized)](#5-design-system-tv-optimized)
6. [Screen Specifications](#6-screen-specifications)
7. [Authentication System](#7-authentication-system)
8. [User Profiles System](#8-user-profiles-system)
9. [Data Layer](#9-data-layer)
10. [Navigation & Focus System](#10-navigation--focus-system)
11. [Video Player Integration](#11-video-player-integration)
12. [TMDB Integration](#12-tmdb-integration)
13. [Video Sources Integration](#13-video-sources-integration)
14. [Local Storage & Caching](#14-local-storage--caching)
15. [Settings & Preferences](#15-settings--preferences)
16. [Animations & Transitions](#16-animations--transitions)
17. [Performance Optimization](#17-performance-optimization)
18. [Testing Strategy](#18-testing-strategy)
19. [Deployment & Distribution](#19-deployment--distribution)
20. [Future Roadmap](#20-future-roadmap)

---

# 1. EXECUTIVE SUMMARY

## 1.1 Project Overview

**SimplStream** is a premium Android TV streaming application designed for a select group of users. It provides a Netflix-like experience with content sourced from TMDB metadata and video embed services (VidSrc, VidFast, 111Movies, Vidzee).

### Core Objectives
- ✅ **TV-Native Experience** — Built from ground-up for D-pad/remote navigation
- ✅ **Stunning UI** — SimplStudios design language adapted for 10-foot UI
- ✅ **User Profiles** — Multiple profiles per device with separate watchlists
- ✅ **Authentication** — Secure local authentication system
- ✅ **Zero Cost** — No paid services, sideload distribution

### Target Devices
- Android TV boxes (Nvidia Shield, Mi Box, etc.)
- Smart TVs with Android TV OS
- Fire TV devices (with minor modifications)
- Google TV devices

### Minimum Requirements
- Android TV 7.0 (API 24) or higher
- 2GB RAM minimum
- 100MB storage for app
- Internet connection for streaming

---

## 1.2 Feature Matrix

| Feature | Priority | Status | Version |
|---------|----------|--------|---------|
| TMDB Browse (Movies/Shows) | P0 | Planned | v1.0 |
| Search (Keyboard) | P0 | Planned | v1.0 |
| Video Playback (WebView) | P0 | Planned | v1.0 |
| D-pad Navigation | P0 | Planned | v1.0 |
| Multi-source Fallback | P0 | Planned | v1.0 |
| User Profiles | P0 | Planned | v1.0 |
| Local Authentication | P0 | Planned | v1.0 |
| Continue Watching | P1 | Planned | v1.0 |
| My List / Favorites | P1 | Planned | v1.0 |
| Hero Banner Carousel | P1 | Planned | v1.0 |
| Dynamic Backgrounds | P1 | Planned | v1.0 |
| Focus Animations | P1 | Planned | v1.0 |
| Settings Panel | P1 | Planned | v1.0 |
| Voice Search | P2 | Planned | v1.1 |
| Skip Intro | P2 | Planned | v1.1 |
| Next Episode Auto-play | P2 | Planned | v1.1 |
| Subtitle Customization | P2 | Planned | v1.1 |
| Self-Update System | P2 | Planned | v1.1 |
| Screensaver Mode | P3 | Planned | v1.2 |
| Kids Mode | P3 | Planned | v1.2 |

---

# 2. SYSTEM ARCHITECTURE

## 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SIMPLSTREAM APPLICATION                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      PRESENTATION LAYER                              │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │   │
│  │  │  Browse  │ │  Search  │ │ Details  │ │  Player  │ │ Settings │  │   │
│  │  │  Screen  │ │  Screen  │ │  Screen  │ │  Screen  │ │  Screen  │  │   │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │   │
│  │       │            │            │            │            │         │   │
│  │  ┌────┴────────────┴────────────┴────────────┴────────────┴────┐   │   │
│  │  │                    VIEWMODELS (MVVM)                        │   │   │
│  │  │  BrowseVM │ SearchVM │ DetailsVM │ PlayerVM │ SettingsVM   │   │   │
│  │  └────────────────────────────┬────────────────────────────────┘   │   │
│  └───────────────────────────────┼────────────────────────────────────┘   │
│                                  │                                         │
│  ┌───────────────────────────────┼────────────────────────────────────┐   │
│  │                      DOMAIN LAYER                                   │   │
│  │  ┌────────────────────────────┴────────────────────────────────┐   │   │
│  │  │                      USE CASES                               │   │   │
│  │  │  GetTrending │ SearchContent │ GetDetails │ ResolveVideo    │   │   │
│  │  │  ManageWatchlist │ GetProfiles │ Authenticate │ GetHistory  │   │   │
│  │  └────────────────────────────┬────────────────────────────────┘   │   │
│  └───────────────────────────────┼────────────────────────────────────┘   │
│                                  │                                         │
│  ┌───────────────────────────────┼────────────────────────────────────┐   │
│  │                       DATA LAYER                                    │   │
│  │  ┌────────────────────────────┴────────────────────────────────┐   │   │
│  │  │                     REPOSITORIES                             │   │   │
│  │  │  ContentRepository │ UserRepository │ VideoSourceRepository │   │   │
│  │  └───────┬─────────────────────┬─────────────────────┬─────────┘   │   │
│  │          │                     │                     │             │   │
│  │  ┌───────┴───────┐     ┌───────┴───────┐     ┌───────┴───────┐    │   │
│  │  │  Remote Data  │     │  Local Data   │     │  Preferences  │    │   │
│  │  │   Sources     │     │   Sources     │     │    Storage    │    │   │
│  │  └───────┬───────┘     └───────┬───────┘     └───────┬───────┘    │   │
│  └──────────┼─────────────────────┼─────────────────────┼────────────┘   │
│             │                     │                     │                 │
└─────────────┼─────────────────────┼─────────────────────┼─────────────────┘
              │                     │                     │
              ▼                     ▼                     ▼
┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│     TMDB API        │ │   Room Database     │ │  DataStore Prefs    │
│  (Content Metadata) │ │ (Profiles, History) │ │ (Settings, Auth)    │
└─────────────────────┘ └─────────────────────┘ └─────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         VIDEO EMBED SERVICES                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐                │
│  │  VidSrc  │  │ VidFast  │  │111Movies │  │  Vidzee  │                │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘                │
└─────────────────────────────────────────────────────────────────────────┘
```

## 2.2 Architecture Principles

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  • Activities, Fragments (Leanback)                         │
│  • ViewModels                                               │
│  • UI State classes                                         │
│  • Mappers (Domain → UI)                                    │
├─────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                           │
│  • Use Cases (Single responsibility)                        │
│  • Domain Models (Pure Kotlin data classes)                 │
│  • Repository Interfaces                                    │
│  • Business Logic                                           │
├─────────────────────────────────────────────────────────────┤
│                       DATA LAYER                            │
│  • Repository Implementations                               │
│  • Data Sources (Remote/Local)                              │
│  • DTOs (Data Transfer Objects)                             │
│  • Mappers (DTO → Domain)                                   │
│  • Database Entities                                        │
└─────────────────────────────────────────────────────────────┘
```

### Dependency Flow

```
Presentation → Domain ← Data
     │            │         │
     │            │         │
     ▼            ▼         ▼
  ViewModels   UseCases   Repositories
     │            │         │
     └────────────┼─────────┘
                  │
                  ▼
            Hilt (DI Container)
```

## 2.3 Data Flow

### Content Browsing Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          CONTENT BROWSING FLOW                            │
└──────────────────────────────────────────────────────────────────────────┘

User Opens App
      │
      ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Profile   │───▶│   Browse    │───▶│   Browse    │───▶│    TMDB     │
│  Selection  │    │  ViewModel  │    │ Repository  │    │     API     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                          │                  │                   │
                          │                  │                   │
                          ▼                  ▼                   ▼
                   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
                   │  UI State   │◀───│   Domain    │◀───│    DTO      │
                   │   Update    │    │   Models    │    │  Response   │
                   └─────────────┘    └─────────────┘    └─────────────┘
                          │
                          ▼
                   ┌─────────────┐
                   │   Browse    │
                   │   Screen    │
                   │  Renders    │
                   └─────────────┘
```

### Video Playback Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          VIDEO PLAYBACK FLOW                              │
└──────────────────────────────────────────────────────────────────────────┘

User Clicks Play
      │
      ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Player    │───▶│   Player    │───▶│   Video     │
│  Activity   │    │  ViewModel  │    │  Source     │
└─────────────┘    └─────────────┘    │  Resolver   │
                                      └─────────────┘
                                            │
                    ┌───────────────────────┼───────────────────────┐
                    │                       │                       │
                    ▼                       ▼                       ▼
             ┌─────────────┐        ┌─────────────┐        ┌─────────────┐
             │   VidSrc    │        │  VidFast    │        │  111Movies  │
             │  (Primary)  │        │ (Fallback1) │        │ (Fallback2) │
             └─────────────┘        └─────────────┘        └─────────────┘
                    │                       │                       │
                    │         (If fails)    │        (If fails)     │
                    └───────────────────────┴───────────────────────┘
                                            │
                                            ▼
                                     ┌─────────────┐
                                     │  WebView    │
                                     │   Player    │
                                     │   Loads     │
                                     └─────────────┘
                                            │
                                            ▼
                                     ┌─────────────┐
                                     │   Update    │
                                     │  Watch      │
                                     │  History    │
                                     └─────────────┘
```

---

# 3. PROJECT STRUCTURE

## 3.1 Complete Directory Structure

```
SimplStream/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/simplstudios/simplstream/
│   │   │   │   │
│   │   │   │   ├── SimplStreamApp.kt                 # Application class
│   │   │   │   │
│   │   │   │   ├── di/                               # Dependency Injection
│   │   │   │   │   ├── AppModule.kt                  # App-wide dependencies
│   │   │   │   │   ├── NetworkModule.kt              # Retrofit, OkHttp
│   │   │   │   │   ├── DatabaseModule.kt             # Room database
│   │   │   │   │   ├── RepositoryModule.kt           # Repository bindings
│   │   │   │   │   └── UseCaseModule.kt              # Use case bindings
│   │   │   │   │
│   │   │   │   ├── data/                             # DATA LAYER
│   │   │   │   │   ├── remote/                       # Remote data sources
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   │   ├── TmdbApi.kt            # TMDB API interface
│   │   │   │   │   │   │   └── TmdbInterceptor.kt    # Auth interceptor
│   │   │   │   │   │   ├── dto/                      # Data Transfer Objects
│   │   │   │   │   │   │   ├── MovieDto.kt
│   │   │   │   │   │   │   ├── TvShowDto.kt
│   │   │   │   │   │   │   ├── PersonDto.kt
│   │   │   │   │   │   │   ├── SeasonDto.kt
│   │   │   │   │   │   │   ├── EpisodeDto.kt
│   │   │   │   │   │   │   ├── GenreDto.kt
│   │   │   │   │   │   │   ├── VideoDto.kt
│   │   │   │   │   │   │   ├── ImageDto.kt
│   │   │   │   │   │   │   └── responses/
│   │   │   │   │   │   │       ├── TrendingResponse.kt
│   │   │   │   │   │   │       ├── SearchResponse.kt
│   │   │   │   │   │   │       ├── MovieDetailResponse.kt
│   │   │   │   │   │   │       └── TvDetailResponse.kt
│   │   │   │   │   │   └── source/
│   │   │   │   │   │       └── TmdbRemoteDataSource.kt
│   │   │   │   │   │
│   │   │   │   │   ├── local/                        # Local data sources
│   │   │   │   │   │   ├── db/
│   │   │   │   │   │   │   ├── SimplStreamDatabase.kt
│   │   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   │   ├── ProfileDao.kt
│   │   │   │   │   │   │   │   ├── WatchHistoryDao.kt
│   │   │   │   │   │   │   │   ├── WatchlistDao.kt
│   │   │   │   │   │   │   │   └── CacheDao.kt
│   │   │   │   │   │   │   └── entity/
│   │   │   │   │   │   │       ├── ProfileEntity.kt
│   │   │   │   │   │   │       ├── WatchHistoryEntity.kt
│   │   │   │   │   │   │       ├── WatchlistEntity.kt
│   │   │   │   │   │   │       └── ContentCacheEntity.kt
│   │   │   │   │   │   ├── preferences/
│   │   │   │   │   │   │   ├── PreferencesManager.kt
│   │   │   │   │   │   │   └── AuthPreferences.kt
│   │   │   │   │   │   └── source/
│   │   │   │   │   │       └── LocalDataSource.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/                   # Repository implementations
│   │   │   │   │   │   ├── ContentRepositoryImpl.kt
│   │   │   │   │   │   ├── ProfileRepositoryImpl.kt
│   │   │   │   │   │   ├── WatchHistoryRepositoryImpl.kt
│   │   │   │   │   │   ├── WatchlistRepositoryImpl.kt
│   │   │   │   │   │   ├── AuthRepositoryImpl.kt
│   │   │   │   │   │   └── VideoSourceRepositoryImpl.kt
│   │   │   │   │   │
│   │   │   │   │   ├── mapper/                       # DTO ↔ Domain mappers
│   │   │   │   │   │   ├── MovieMapper.kt
│   │   │   │   │   │   ├── TvShowMapper.kt
│   │   │   │   │   │   ├── ProfileMapper.kt
│   │   │   │   │   │   └── WatchHistoryMapper.kt
│   │   │   │   │   │
│   │   │   │   │   └── videosource/                  # Video source handling
│   │   │   │   │       ├── VideoSourceResolver.kt
│   │   │   │   │       ├── sources/
│   │   │   │   │       │   ├── VideoSource.kt        # Interface
│   │   │   │   │       │   ├── VidSrcSource.kt
│   │   │   │   │       │   ├── VidFastSource.kt
│   │   │   │   │       │   ├── Movies111Source.kt
│   │   │   │   │       │   └── VidzeeSource.kt
│   │   │   │   │       └── model/
│   │   │   │   │           └── VideoSourceResult.kt
│   │   │   │   │
│   │   │   │   ├── domain/                           # DOMAIN LAYER
│   │   │   │   │   ├── model/                        # Domain models
│   │   │   │   │   │   ├── Content.kt                # Sealed class Movie/TvShow
│   │   │   │   │   │   ├── Movie.kt
│   │   │   │   │   │   ├── TvShow.kt
│   │   │   │   │   │   ├── Season.kt
│   │   │   │   │   │   ├── Episode.kt
│   │   │   │   │   │   ├── Genre.kt
│   │   │   │   │   │   ├── Person.kt
│   │   │   │   │   │   ├── Profile.kt
│   │   │   │   │   │   ├── WatchHistory.kt
│   │   │   │   │   │   ├── WatchlistItem.kt
│   │   │   │   │   │   ├── ContentRow.kt
│   │   │   │   │   │   ├── VideoSource.kt
│   │   │   │   │   │   └── User.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/                   # Repository interfaces
│   │   │   │   │   │   ├── ContentRepository.kt
│   │   │   │   │   │   ├── ProfileRepository.kt
│   │   │   │   │   │   ├── WatchHistoryRepository.kt
│   │   │   │   │   │   ├── WatchlistRepository.kt
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   └── VideoSourceRepository.kt
│   │   │   │   │   │
│   │   │   │   │   └── usecase/                      # Use cases
│   │   │   │   │       ├── content/
│   │   │   │   │       │   ├── GetTrendingUseCase.kt
│   │   │   │   │       │   ├── GetPopularMoviesUseCase.kt
│   │   │   │   │       │   ├── GetPopularTvShowsUseCase.kt
│   │   │   │   │       │   ├── GetMovieDetailUseCase.kt
│   │   │   │   │       │   ├── GetTvShowDetailUseCase.kt
│   │   │   │   │       │   ├── GetSeasonDetailUseCase.kt
│   │   │   │   │       │   ├── SearchContentUseCase.kt
│   │   │   │   │       │   ├── GetGenresUseCase.kt
│   │   │   │   │       │   └── GetByGenreUseCase.kt
│   │   │   │   │       ├── profile/
│   │   │   │   │       │   ├── GetProfilesUseCase.kt
│   │   │   │   │       │   ├── CreateProfileUseCase.kt
│   │   │   │   │       │   ├── UpdateProfileUseCase.kt
│   │   │   │   │       │   ├── DeleteProfileUseCase.kt
│   │   │   │   │       │   └── SetActiveProfileUseCase.kt
│   │   │   │   │       ├── watchlist/
│   │   │   │   │       │   ├── GetWatchlistUseCase.kt
│   │   │   │   │       │   ├── AddToWatchlistUseCase.kt
│   │   │   │   │       │   ├── RemoveFromWatchlistUseCase.kt
│   │   │   │   │       │   └── IsInWatchlistUseCase.kt
│   │   │   │   │       ├── history/
│   │   │   │   │       │   ├── GetWatchHistoryUseCase.kt
│   │   │   │   │       │   ├── UpdateWatchProgressUseCase.kt
│   │   │   │   │       │   ├── GetContinueWatchingUseCase.kt
│   │   │   │   │       │   └── ClearHistoryUseCase.kt
│   │   │   │   │       ├── auth/
│   │   │   │   │       │   ├── AuthenticateUseCase.kt
│   │   │   │   │       │   ├── ValidatePinUseCase.kt
│   │   │   │   │       │   ├── SetPinUseCase.kt
│   │   │   │   │       │   └── IsAuthenticatedUseCase.kt
│   │   │   │   │       └── player/
│   │   │   │   │           ├── GetVideoSourceUseCase.kt
│   │   │   │   │           └── ResolveVideoUseCase.kt
│   │   │   │   │
│   │   │   │   ├── presentation/                     # PRESENTATION LAYER
│   │   │   │   │   ├── MainActivity.kt               # Single Activity
│   │   │   │   │   │
│   │   │   │   │   ├── common/                       # Shared UI components
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   │   ├── ContentCardPresenter.kt
│   │   │   │   │   │   │   ├── HeroCardPresenter.kt
│   │   │   │   │   │   │   ├── LoadingPresenter.kt
│   │   │   │   │   │   │   ├── ErrorPresenter.kt
│   │   │   │   │   │   │   ├── ProfileCardPresenter.kt
│   │   │   │   │   │   │   └── SettingsCardPresenter.kt
│   │   │   │   │   │   ├── extensions/
│   │   │   │   │   │   │   ├── ViewExtensions.kt
│   │   │   │   │   │   │   ├── ImageExtensions.kt
│   │   │   │   │   │   │   └── FlowExtensions.kt
│   │   │   │   │   │   └── base/
│   │   │   │   │   │       ├── BaseFragment.kt
│   │   │   │   │   │       └── BaseViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── splash/                       # Splash screen
│   │   │   │   │   │   ├── SplashFragment.kt
│   │   │   │   │   │   └── SplashViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── auth/                         # Authentication
│   │   │   │   │   │   ├── AuthFragment.kt
│   │   │   │   │   │   ├── AuthViewModel.kt
│   │   │   │   │   │   ├── PinInputFragment.kt
│   │   │   │   │   │   └── SetupPinFragment.kt
│   │   │   │   │   │
│   │   │   │   │   ├── profile/                      # Profile selection
│   │   │   │   │   │   ├── ProfileSelectionFragment.kt
│   │   │   │   │   │   ├── ProfileSelectionViewModel.kt
│   │   │   │   │   │   ├── ProfileEditorFragment.kt
│   │   │   │   │   │   └── ProfileEditorViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── browse/                       # Main browse screen
│   │   │   │   │   │   ├── BrowseFragment.kt
│   │   │   │   │   │   ├── BrowseViewModel.kt
│   │   │   │   │   │   ├── state/
│   │   │   │   │   │   │   └── BrowseUiState.kt
│   │   │   │   │   │   └── adapter/
│   │   │   │   │   │       ├── ContentRowAdapter.kt
│   │   │   │   │   │       └── HeroAdapter.kt
│   │   │   │   │   │
│   │   │   │   │   ├── search/                       # Search screen
│   │   │   │   │   │   ├── SearchFragment.kt
│   │   │   │   │   │   ├── SearchViewModel.kt
│   │   │   │   │   │   └── state/
│   │   │   │   │   │       └── SearchUiState.kt
│   │   │   │   │   │
│   │   │   │   │   ├── detail/                       # Detail screens
│   │   │   │   │   │   ├── MovieDetailFragment.kt
│   │   │   │   │   │   ├── MovieDetailViewModel.kt
│   │   │   │   │   │   ├── TvShowDetailFragment.kt
│   │   │   │   │   │   ├── TvShowDetailViewModel.kt
│   │   │   │   │   │   ├── SeasonDetailFragment.kt
│   │   │   │   │   │   └── state/
│   │   │   │   │   │       ├── MovieDetailUiState.kt
│   │   │   │   │   │       └── TvShowDetailUiState.kt
│   │   │   │   │   │
│   │   │   │   │   ├── player/                       # Video player
│   │   │   │   │   │   ├── PlayerActivity.kt
│   │   │   │   │   │   ├── PlayerViewModel.kt
│   │   │   │   │   │   ├── WebViewPlayerFragment.kt
│   │   │   │   │   │   ├── SourceSelectorDialog.kt
│   │   │   │   │   │   └── state/
│   │   │   │   │   │       └── PlayerUiState.kt
│   │   │   │   │   │
│   │   │   │   │   ├── watchlist/                    # My List
│   │   │   │   │   │   ├── WatchlistFragment.kt
│   │   │   │   │   │   └── WatchlistViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   └── settings/                     # Settings
│   │   │   │   │       ├── SettingsFragment.kt
│   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │       └── screens/
│   │   │   │   │           ├── AppearanceSettingsFragment.kt
│   │   │   │   │           ├── PlaybackSettingsFragment.kt
│   │   │   │   │           ├── AccountSettingsFragment.kt
│   │   │   │   │           └── AboutFragment.kt
│   │   │   │   │
│   │   │   │   └── util/                             # Utilities
│   │   │   │       ├── Constants.kt
│   │   │   │       ├── Resource.kt                   # Wrapper for API responses
│   │   │   │       ├── NetworkUtils.kt
│   │   │   │       ├── DateUtils.kt
│   │   │   │       ├── ImageUtils.kt
│   │   │   │       └── FocusUtils.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/                         # Drawables
│   │   │   │   │   ├── bg_card_focused.xml
│   │   │   │   │   ├── bg_card_unfocused.xml
│   │   │   │   │   ├── bg_hero_gradient.xml
│   │   │   │   │   ├── bg_button_primary.xml
│   │   │   │   │   ├── bg_button_secondary.xml
│   │   │   │   │   ├── ic_simplstream_logo.xml
│   │   │   │   │   ├── ic_profile_placeholder.xml
│   │   │   │   │   ├── ic_play.xml
│   │   │   │   │   ├── ic_add_list.xml
│   │   │   │   │   ├── ic_info.xml
│   │   │   │   │   ├── ic_search.xml
│   │   │   │   │   ├── ic_settings.xml
│   │   │   │   │   ├── ic_back.xml
│   │   │   │   │   ├── ic_star.xml
│   │   │   │   │   └── shimmer_placeholder.xml
│   │   │   │   │
│   │   │   │   ├── layout/                           # Layouts
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_player.xml
│   │   │   │   │   ├── fragment_splash.xml
│   │   │   │   │   ├── fragment_auth.xml
│   │   │   │   │   ├── fragment_pin_input.xml
│   │   │   │   │   ├── fragment_profile_selection.xml
│   │   │   │   │   ├── fragment_profile_editor.xml
│   │   │   │   │   ├── fragment_browse.xml
│   │   │   │   │   ├── fragment_search.xml
│   │   │   │   │   ├── fragment_movie_detail.xml
│   │   │   │   │   ├── fragment_tv_detail.xml
│   │   │   │   │   ├── fragment_season_detail.xml
│   │   │   │   │   ├── fragment_player_webview.xml
│   │   │   │   │   ├── fragment_watchlist.xml
│   │   │   │   │   ├── fragment_settings.xml
│   │   │   │   │   ├── item_content_card.xml
│   │   │   │   │   ├── item_hero_card.xml
│   │   │   │   │   ├── item_profile_card.xml
│   │   │   │   │   ├── item_episode_card.xml
│   │   │   │   │   ├── item_source_option.xml
│   │   │   │   │   ├── view_content_row.xml
│   │   │   │   │   ├── view_hero_banner.xml
│   │   │   │   │   ├── view_loading.xml
│   │   │   │   │   ├── view_error.xml
│   │   │   │   │   └── dialog_source_selector.xml
│   │   │   │   │
│   │   │   │   ├── values/                           # Values
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── dimens.xml
│   │   │   │   │   ├── themes.xml
│   │   │   │   │   ├── styles.xml
│   │   │   │   │   ├── attrs.xml
│   │   │   │   │   └── arrays.xml
│   │   │   │   │
│   │   │   │   ├── font/                             # Fonts
│   │   │   │   │   ├── outfit_regular.ttf
│   │   │   │   │   ├── outfit_medium.ttf
│   │   │   │   │   ├── outfit_semibold.ttf
│   │   │   │   │   ├── outfit_bold.ttf
│   │   │   │   │   ├── plus_jakarta_sans_regular.ttf
│   │   │   │   │   ├── plus_jakarta_sans_medium.ttf
│   │   │   │   │   ├── plus_jakarta_sans_semibold.ttf
│   │   │   │   │   ├── plus_jakarta_sans_bold.ttf
│   │   │   │   │   ├── inter_regular.ttf
│   │   │   │   │   ├── inter_medium.ttf
│   │   │   │   │   └── inter_semibold.ttf
│   │   │   │   │
│   │   │   │   ├── anim/                             # Animations
│   │   │   │   │   ├── fade_in.xml
│   │   │   │   │   ├── fade_out.xml
│   │   │   │   │   ├── slide_in_right.xml
│   │   │   │   │   ├── slide_out_left.xml
│   │   │   │   │   ├── scale_up.xml
│   │   │   │   │   ├── scale_down.xml
│   │   │   │   │   └── shimmer.xml
│   │   │   │   │
│   │   │   │   ├── raw/                              # Raw assets
│   │   │   │   │   ├── click_sound.mp3
│   │   │   │   │   └── whoosh_sound.mp3
│   │   │   │   │
│   │   │   │   └── navigation/                       # Navigation
│   │   │   │       └── nav_graph.xml
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/                                     # Unit tests
│   │       └── java/com/simplstudios/simplstream/
│   │           ├── data/
│   │           │   └── repository/
│   │           ├── domain/
│   │           │   └── usecase/
│   │           └── presentation/
│   │               └── viewmodel/
│   │
│   ├── build.gradle.kts                              # App-level build config
│   └── proguard-rules.pro
│
├── build.gradle.kts                                  # Project-level build config
├── settings.gradle.kts
├── gradle.properties
├── local.properties
├── README.md
├── BLUEPRINT.md                                      # This file
└── .gitignore
```

---

# 4. TECHNOLOGY STACK

## 4.1 Core Technologies

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| **Language** | Kotlin | 1.9.x | Primary language |
| **Min SDK** | API 24 | Android 7.0 | TV compatibility |
| **Target SDK** | API 34 | Android 14 | Latest features |
| **Build** | Gradle (KTS) | 8.x | Build system |

## 4.2 Android Jetpack

| Library | Version | Purpose |
|---------|---------|---------|
| **Leanback** | 1.2.0-alpha04 | TV UI components |
| **Navigation** | 2.7.x | Fragment navigation |
| **Room** | 2.6.x | Local database |
| **DataStore** | 1.1.x | Preferences storage |
| **Lifecycle** | 2.7.x | ViewModels, LiveData |
| **Hilt** | 2.50 | Dependency injection |

## 4.3 Networking

| Library | Version | Purpose |
|---------|---------|---------|
| **Retrofit** | 2.9.x | REST API client |
| **OkHttp** | 4.12.x | HTTP client |
| **Moshi** | 1.15.x | JSON parsing |

## 4.4 UI & Media

| Library | Version | Purpose |
|---------|---------|---------|
| **Coil** | 2.5.x | Image loading |
| **Palette** | 1.0.0 | Color extraction |
| **Shimmer** | 0.5.0 | Loading animations |

## 4.5 Async & Reactive

| Library | Version | Purpose |
|---------|---------|---------|
| **Coroutines** | 1.8.x | Async programming |
| **Flow** | (part of coroutines) | Reactive streams |

## 4.6 Dependencies Declaration

```kotlin
// build.gradle.kts (app module)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.simplstudios.simplstream"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.simplstudios.simplstream"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        // TMDB API Key (move to local.properties for security)
        buildConfigField("String", "TMDB_API_KEY", "\"335a2d8a6455213ca6201aba18056860\"")
        buildConfigField("String", "TMDB_ACCESS_TOKEN", "\"eyJhbGciOiJIUzI1NiJ9...\"")
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Leanback (TV)
    implementation("androidx.leanback:leanback:1.2.0-alpha04")
    implementation("androidx.leanback:leanback-preference:1.2.0-alpha04")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    
    // Image Loading
    implementation("io.coil-kt:coil:2.5.0")
    
    // Palette
    implementation("androidx.palette:palette-ktx:1.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    
    // WebView
    implementation("androidx.webkit:webkit:1.10.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

---

# 5. DESIGN SYSTEM (TV-OPTIMIZED)

## 5.1 SimplStudios Design Adaptation for TV

### Original SimplStudios Colors → TV Adaptation

```xml
<!-- res/values/colors.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Primary Brand Colors (SimplStudios) -->
    <color name="simpl_blue">#2563EB</color>
    <color name="simpl_blue_dark">#1D4ED8</color>
    <color name="simpl_blue_light">#3B82F6</color>
    <color name="simpl_blue_pale">#DBEAFE</color>
    
    <!-- Accent Colors -->
    <color name="simpl_purple">#7C3AED</color>
    <color name="simpl_pink">#EC4899</color>
    <color name="simpl_rose">#F43F5E</color>
    
    <!-- Background Colors (TV Dark Theme) -->
    <color name="background_primary">#0D0D0D</color>
    <color name="background_secondary">#1A1A1A</color>
    <color name="background_card">#1F1F1F</color>
    <color name="background_elevated">#2A2A2A</color>
    
    <!-- OLED Black -->
    <color name="background_oled">#000000</color>
    <color name="background_oled_card">#0A0A0A</color>
    
    <!-- Text Colors -->
    <color name="text_primary">#FFFFFF</color>
    <color name="text_secondary">#B3B3B3</color>
    <color name="text_muted">#737373</color>
    <color name="text_disabled">#4D4D4D</color>
    
    <!-- Focus States -->
    <color name="focus_ring">#2563EB</color>
    <color name="focus_glow">#3B82F6</color>
    <color name="focus_surface">#1E3A5F</color>
    
    <!-- Semantic Colors -->
    <color name="success">#22C55E</color>
    <color name="warning">#F59E0B</color>
    <color name="error">#EF4444</color>
    <color name="info">#3B82F6</color>
    
    <!-- Gradients (start/end) -->
    <color name="gradient_blue_start">#2563EB</color>
    <color name="gradient_blue_end">#1D4ED8</color>
    <color name="gradient_purple_start">#7C3AED</color>
    <color name="gradient_purple_end">#5B21B6</color>
    
    <!-- Scrim/Overlay -->
    <color name="scrim_dark">#CC000000</color>
    <color name="scrim_medium">#99000000</color>
    <color name="scrim_light">#66000000</color>
    
    <!-- Progress Bar -->
    <color name="progress_background">#3D3D3D</color>
    <color name="progress_fill">#2563EB</color>
    
    <!-- Rating -->
    <color name="rating_star">#F59E0B</color>
</resources>
```

## 5.2 Typography System

```xml
<!-- res/values/styles.xml (Typography) -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ========================================== -->
    <!-- TYPOGRAPHY - TV OPTIMIZED (10-foot UI)    -->
    <!-- ========================================== -->
    
    <!-- Hero Title - Largest, for splash/hero banners -->
    <style name="TextAppearance.SimplStream.Hero">
        <item name="android:fontFamily">@font/outfit_bold</item>
        <item name="android:textSize">72sp</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:letterSpacing">-0.02</item>
    </style>
    
    <!-- Display - Large headlines -->
    <style name="TextAppearance.SimplStream.Display">
        <item name="android:fontFamily">@font/outfit_bold</item>
        <item name="android:textSize">56sp</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:letterSpacing">-0.02</item>
    </style>
    
    <!-- Headline Large - Section titles -->
    <style name="TextAppearance.SimplStream.HeadlineLarge">
        <item name="android:fontFamily">@font/outfit_bold</item>
        <item name="android:textSize">40sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Headline Medium - Card titles -->
    <style name="TextAppearance.SimplStream.HeadlineMedium">
        <item name="android:fontFamily">@font/outfit_semibold</item>
        <item name="android:textSize">32sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Headline Small - Smaller headings -->
    <style name="TextAppearance.SimplStream.HeadlineSmall">
        <item name="android:fontFamily">@font/outfit_semibold</item>
        <item name="android:textSize">28sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Title Large - Row titles -->
    <style name="TextAppearance.SimplStream.TitleLarge">
        <item name="android:fontFamily">@font/plus_jakarta_sans_semibold</item>
        <item name="android:textSize">26sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Title Medium -->
    <style name="TextAppearance.SimplStream.TitleMedium">
        <item name="android:fontFamily">@font/plus_jakarta_sans_semibold</item>
        <item name="android:textSize">22sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Title Small -->
    <style name="TextAppearance.SimplStream.TitleSmall">
        <item name="android:fontFamily">@font/plus_jakarta_sans_medium</item>
        <item name="android:textSize">20sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Body Large - Main content text -->
    <style name="TextAppearance.SimplStream.BodyLarge">
        <item name="android:fontFamily">@font/plus_jakarta_sans_regular</item>
        <item name="android:textSize">24sp</item>
        <item name="android:textColor">@color/text_secondary</item>
        <item name="android:lineSpacingMultiplier">1.4</item>
    </style>
    
    <!-- Body Medium -->
    <style name="TextAppearance.SimplStream.BodyMedium">
        <item name="android:fontFamily">@font/plus_jakarta_sans_regular</item>
        <item name="android:textSize">20sp</item>
        <item name="android:textColor">@color/text_secondary</item>
        <item name="android:lineSpacingMultiplier">1.4</item>
    </style>
    
    <!-- Body Small -->
    <style name="TextAppearance.SimplStream.BodySmall">
        <item name="android:fontFamily">@font/plus_jakarta_sans_regular</item>
        <item name="android:textSize">18sp</item>
        <item name="android:textColor">@color/text_muted</item>
    </style>
    
    <!-- Label Large - Buttons, badges -->
    <style name="TextAppearance.SimplStream.LabelLarge">
        <item name="android:fontFamily">@font/inter_semibold</item>
        <item name="android:textSize">20sp</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textAllCaps">false</item>
    </style>
    
    <!-- Label Medium -->
    <style name="TextAppearance.SimplStream.LabelMedium">
        <item name="android:fontFamily">@font/inter_medium</item>
        <item name="android:textSize">18sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Label Small - Metadata, tags -->
    <style name="TextAppearance.SimplStream.LabelSmall">
        <item name="android:fontFamily">@font/inter_medium</item>
        <item name="android:textSize">16sp</item>
        <item name="android:textColor">@color/text_muted</item>
    </style>
    
    <!-- Brand Text - SimplStream logo -->
    <style name="TextAppearance.SimplStream.Brand">
        <item name="android:fontFamily">@font/outfit_bold</item>
        <item name="android:textSize">36sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Brand Accent - "Simpl" in brand -->
    <style name="TextAppearance.SimplStream.BrandAccent" parent="TextAppearance.SimplStream.Brand">
        <item name="android:textColor">@color/simpl_blue</item>
    </style>
</resources>
```

## 5.3 Dimensions (TV-Optimized)

```xml
<!-- res/values/dimens.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ========================================== -->
    <!-- SPACING SYSTEM (8dp base unit)            -->
    <!-- ========================================== -->
    <dimen name="spacing_xxs">4dp</dimen>
    <dimen name="spacing_xs">8dp</dimen>
    <dimen name="spacing_sm">12dp</dimen>
    <dimen name="spacing_md">16dp</dimen>
    <dimen name="spacing_lg">24dp</dimen>
    <dimen name="spacing_xl">32dp</dimen>
    <dimen name="spacing_xxl">48dp</dimen>
    <dimen name="spacing_xxxl">64dp</dimen>
    
    <!-- ========================================== -->
    <!-- SAFE ZONES (TV Overscan Protection)       -->
    <!-- ========================================== -->
    <!-- 5% of 1920x1080 = 96px horizontal, 54px vertical -->
    <dimen name="safe_zone_horizontal">48dp</dimen>
    <dimen name="safe_zone_vertical">27dp</dimen>
    
    <!-- ========================================== -->
    <!-- CONTENT CARDS                             -->
    <!-- ========================================== -->
    <!-- Poster Card (2:3 aspect ratio) -->
    <dimen name="card_poster_width">180dp</dimen>
    <dimen name="card_poster_height">270dp</dimen>
    
    <!-- Backdrop Card (16:9 aspect ratio) -->
    <dimen name="card_backdrop_width">320dp</dimen>
    <dimen name="card_backdrop_height">180dp</dimen>
    
    <!-- Episode Card (16:9 aspect ratio) -->
    <dimen name="card_episode_width">280dp</dimen>
    <dimen name="card_episode_height">157dp</dimen>
    
    <!-- Profile Card -->
    <dimen name="card_profile_size">160dp</dimen>
    
    <!-- Card corners -->
    <dimen name="card_corner_radius">12dp</dimen>
    <dimen name="card_corner_radius_large">16dp</dimen>
    
    <!-- Card spacing -->
    <dimen name="card_gap">16dp</dimen>
    <dimen name="card_gap_large">24dp</dimen>
    
    <!-- ========================================== -->
    <!-- HERO BANNER                               -->
    <!-- ========================================== -->
    <dimen name="hero_height">480dp</dimen>
    <dimen name="hero_poster_width">240dp</dimen>
    <dimen name="hero_poster_height">360dp</dimen>
    
    <!-- ========================================== -->
    <!-- CONTENT ROWS                              -->
    <!-- ========================================== -->
    <dimen name="row_height">320dp</dimen>
    <dimen name="row_title_margin_start">48dp</dimen>
    <dimen name="row_title_margin_bottom">16dp</dimen>
    <dimen name="row_content_margin_start">48dp</dimen>
    <dimen name="row_spacing">40dp</dimen>
    
    <!-- ========================================== -->
    <!-- BUTTONS                                   -->
    <!-- ========================================== -->
    <dimen name="button_height">56dp</dimen>
    <dimen name="button_height_large">64dp</dimen>
    <dimen name="button_min_width">120dp</dimen>
    <dimen name="button_corner_radius">8dp</dimen>
    <dimen name="button_padding_horizontal">24dp</dimen>
    <dimen name="button_padding_vertical">12dp</dimen>
    <dimen name="button_icon_size">24dp</dimen>
    <dimen name="button_icon_spacing">12dp</dimen>
    
    <!-- ========================================== -->
    <!-- ICONS                                     -->
    <!-- ========================================== -->
    <dimen name="icon_size_small">24dp</dimen>
    <dimen name="icon_size_medium">32dp</dimen>
    <dimen name="icon_size_large">48dp</dimen>
    <dimen name="icon_size_xlarge">64dp</dimen>
    
    <!-- ========================================== -->
    <!-- FOCUS STATES                              -->
    <!-- ========================================== -->
    <dimen name="focus_border_width">3dp</dimen>
    <dimen name="focus_scale_factor">1.08</dimen>
    <dimen name="focus_elevation">16dp</dimen>
    <dimen name="focus_glow_radius">8dp</dimen>
    
    <!-- ========================================== -->
    <!-- DETAIL SCREEN                             -->
    <!-- ========================================== -->
    <dimen name="detail_poster_width">300dp</dimen>
    <dimen name="detail_poster_height">450dp</dimen>
    <dimen name="detail_content_max_width">800dp</dimen>
    <dimen name="detail_backdrop_height">400dp</dimen>
    
    <!-- ========================================== -->
    <!-- PLAYER                                    -->
    <!-- ========================================== -->
    <dimen name="player_controls_height">80dp</dimen>
    <dimen name="player_seek_bar_height">8dp</dimen>
    <dimen name="player_button_size">56dp</dimen>
    
    <!-- ========================================== -->
    <!-- PROGRESS BARS                             -->
    <!-- ========================================== -->
    <dimen name="progress_bar_height">4dp</dimen>
    <dimen name="progress_bar_height_thick">6dp</dimen>
    <dimen name="progress_bar_corner_radius">2dp</dimen>
    
    <!-- ========================================== -->
    <!-- DIALOGS                                   -->
    <!-- ========================================== -->
    <dimen name="dialog_width">600dp</dimen>
    <dimen name="dialog_corner_radius">16dp</dimen>
    <dimen name="dialog_padding">32dp</dimen>
    
    <!-- ========================================== -->
    <!-- NAVIGATION                                -->
    <!-- ========================================== -->
    <dimen name="nav_drawer_width">300dp</dimen>
    <dimen name="nav_item_height">64dp</dimen>
    <dimen name="nav_icon_size">32dp</dimen>
</resources>
```

## 5.4 Theme Definition

```xml
<!-- res/values/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ========================================== -->
    <!-- BASE THEME - SIMPLSTREAM TV               -->
    <!-- ========================================== -->
    <style name="Theme.SimplStream" parent="Theme.Leanback">
        <!-- Status bar (hidden on TV, but just in case) -->
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowFullscreen">true</item>
        
        <!-- Background -->
        <item name="android:windowBackground">@color/background_primary</item>
        <item name="android:colorBackground">@color/background_primary</item>
        
        <!-- Primary colors -->
        <item name="colorPrimary">@color/simpl_blue</item>
        <item name="colorPrimaryDark">@color/simpl_blue_dark</item>
        <item name="colorAccent">@color/simpl_blue_light</item>
        
        <!-- Surface colors -->
        <item name="colorSurface">@color/background_card</item>
        <item name="colorOnSurface">@color/text_primary</item>
        
        <!-- Error colors -->
        <item name="colorError">@color/error</item>
        
        <!-- Default text appearances -->
        <item name="android:textColorPrimary">@color/text_primary</item>
        <item name="android:textColorSecondary">@color/text_secondary</item>
        
        <!-- Leanback specific -->
        <item name="browseTitleTextStyle">@style/TextAppearance.SimplStream.TitleLarge</item>
        <item name="browseRowsMarginStart">@dimen/row_content_margin_start</item>
        <item name="browseRowsMarginTop">@dimen/spacing_lg</item>
        
        <!-- Focus -->
        <item name="defaultBrandColor">@color/simpl_blue</item>
        <item name="defaultBrandColorDark">@color/simpl_blue_dark</item>
    </style>
    
    <!-- OLED Theme (Pure Black) -->
    <style name="Theme.SimplStream.OLED" parent="Theme.SimplStream">
        <item name="android:windowBackground">@color/background_oled</item>
        <item name="android:colorBackground">@color/background_oled</item>
        <item name="colorSurface">@color/background_oled_card</item>
    </style>
    
    <!-- Player Theme (Minimal UI) -->
    <style name="Theme.SimplStream.Player" parent="Theme.SimplStream">
        <item name="android:windowBackground">@color/background_oled</item>
        <item name="android:windowTranslucentStatus">true</item>
        <item name="android:windowTranslucentNavigation">true</item>
    </style>
    
    <!-- Splash Theme -->
    <style name="Theme.SimplStream.Splash" parent="Theme.SimplStream">
        <item name="android:windowBackground">@drawable/splash_background</item>
    </style>
</resources>
```

## 5.5 Component Styles

```xml
<!-- res/values/styles.xml (Components) -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ========================================== -->
    <!-- BUTTONS                                   -->
    <!-- ========================================== -->
    
    <!-- Primary Button (Filled) -->
    <style name="Widget.SimplStream.Button.Primary">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">@dimen/button_height</item>
        <item name="android:minWidth">@dimen/button_min_width</item>
        <item name="android:paddingStart">@dimen/button_padding_horizontal</item>
        <item name="android:paddingEnd">@dimen/button_padding_horizontal</item>
        <item name="android:background">@drawable/bg_button_primary</item>
        <item name="android:textAppearance">@style/TextAppearance.SimplStream.LabelLarge</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:gravity">center</item>
    </style>
    
    <!-- Secondary Button (Outline) -->
    <style name="Widget.SimplStream.Button.Secondary">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">@dimen/button_height</item>
        <item name="android:minWidth">@dimen/button_min_width</item>
        <item name="android:paddingStart">@dimen/button_padding_horizontal</item>
        <item name="android:paddingEnd">@dimen/button_padding_horizontal</item>
        <item name="android:background">@drawable/bg_button_secondary</item>
        <item name="android:textAppearance">@style/TextAppearance.SimplStream.LabelLarge</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:gravity">center</item>
    </style>
    
    <!-- Icon Button -->
    <style name="Widget.SimplStream.Button.Icon">
        <item name="android:layout_width">@dimen/button_height</item>
        <item name="android:layout_height">@dimen/button_height</item>
        <item name="android:background">@drawable/bg_button_icon</item>
        <item name="android:padding">@dimen/spacing_md</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:scaleType">centerInside</item>
    </style>
    
    <!-- ========================================== -->
    <!-- CARDS                                     -->
    <!-- ========================================== -->
    
    <!-- Content Card (Poster) -->
    <style name="Widget.SimplStream.Card.Poster">
        <item name="android:layout_width">@dimen/card_poster_width</item>
        <item name="android:layout_height">@dimen/card_poster_height</item>
        <item name="android:background">@drawable/bg_card</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:clickable">true</item>
    </style>
    
    <!-- Content Card (Backdrop) -->
    <style name="Widget.SimplStream.Card.Backdrop">
        <item name="android:layout_width">@dimen/card_backdrop_width</item>
        <item name="android:layout_height">@dimen/card_backdrop_height</item>
        <item name="android:background">@drawable/bg_card</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:clickable">true</item>
    </style>
    
    <!-- Episode Card -->
    <style name="Widget.SimplStream.Card.Episode">
        <item name="android:layout_width">@dimen/card_episode_width</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:background">@drawable/bg_card</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:clickable">true</item>
    </style>
    
    <!-- Profile Card -->
    <style name="Widget.SimplStream.Card.Profile">
        <item name="android:layout_width">@dimen/card_profile_size</item>
        <item name="android:layout_height">@dimen/card_profile_size</item>
        <item name="android:background">@drawable/bg_card_profile</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:clickable">true</item>
    </style>
    
    <!-- ========================================== -->
    <!-- BADGES & TAGS                             -->
    <!-- ========================================== -->
    
    <!-- Rating Badge -->
    <style name="Widget.SimplStream.Badge.Rating">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:background">@drawable/bg_badge_rating</item>
        <item name="android:paddingStart">@dimen/spacing_xs</item>
        <item name="android:paddingEnd">@dimen/spacing_xs</item>
        <item name="android:paddingTop">@dimen/spacing_xxs</item>
        <item name="android:paddingBottom">@dimen/spacing_xxs</item>
        <item name="android:textAppearance">@style/TextAppearance.SimplStream.LabelSmall</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    
    <!-- Genre Tag -->
    <style name="Widget.SimplStream.Tag.Genre">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:background">@drawable/bg_tag</item>
        <item name="android:paddingStart">@dimen/spacing_sm</item>
        <item name="android:paddingEnd">@dimen/spacing_sm</item>
        <item name="android:paddingTop">@dimen/spacing_xs</item>
        <item name="android:paddingBottom">@dimen/spacing_xs</item>
        <item name="android:textAppearance">@style/TextAppearance.SimplStream.LabelSmall</item>
        <item name="android:textColor">@color/simpl_blue</item>
    </style>
    
    <!-- ========================================== -->
    <!-- PROGRESS BARS                             -->
    <!-- ========================================== -->
    
    <!-- Watch Progress -->
    <style name="Widget.SimplStream.ProgressBar.Watch">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">@dimen/progress_bar_height</item>
        <item name="android:progressDrawable">@drawable/progress_watch</item>
        <item name="android:background">@color/progress_background</item>
        <item name="android:max">100</item>
    </style>
    
    <!-- ========================================== -->
    <!-- TEXT INPUTS (PIN, Search)                 -->
    <!-- ========================================== -->
    
    <!-- PIN Input -->
    <style name="Widget.SimplStream.PinInput">
        <item name="android:layout_width">80dp</item>
        <item name="android:layout_height">80dp</item>
        <item name="android:background">@drawable/bg_pin_input</item>
        <item name="android:textAppearance">@style/TextAppearance.SimplStream.HeadlineLarge</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:gravity">center</item>
        <item name="android:inputType">numberPassword</item>
        <item name="android:maxLength">1</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
    </style>
    
    <!-- Search Input -->
    <style name="Widget.SimplStream.SearchInput">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">64dp</item>
        <item name="android:background">@drawable/bg_search_input</item>
        <item name="android:textAppearance">@style/TextAppearance.SimplStream.BodyLarge</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:hint">Search movies, shows...</item>
        <item name="android:textColorHint">@color/text_muted</item>
        <item name="android:paddingStart">@dimen/spacing_lg</item>
        <item name="android:paddingEnd">@dimen/spacing_lg</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:imeOptions">actionSearch</item>
        <item name="android:inputType">text</item>
    </style>
</resources>
```

---

# 6. SCREEN SPECIFICATIONS

## 6.1 Screen Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                              SIMPLSTREAM SCREEN FLOW                                  │
└──────────────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────┐
                                    │   SPLASH    │
                                    │   SCREEN    │
                                    └──────┬──────┘
                                           │
                          ┌────────────────┼────────────────┐
                          │                │                │
                          ▼                ▼                ▼
                    ┌──────────┐    ┌──────────┐    ┌──────────┐
                    │  FIRST   │    │   PIN    │    │ PROFILE  │
                    │  SETUP   │    │  ENTRY   │    │ SELECT   │
                    │ (no auth)│    │ (has PIN)│    │(no PIN)  │
                    └────┬─────┘    └────┬─────┘    └────┬─────┘
                         │               │               │
                         └───────────────┼───────────────┘
                                         │
                                         ▼
                               ┌─────────────────────┐
                               │  PROFILE SELECTION  │
                               │     (Who's Watching)│
                               └──────────┬──────────┘
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    │                     │                     │
                    ▼                     ▼                     ▼
           ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
           │   PROFILE    │      │    SELECT    │      │    ADD       │
           │   EDITOR     │◀─────│   EXISTING   │─────▶│   PROFILE    │
           │              │      │   PROFILE    │      │              │
           └──────────────┘      └──────┬───────┘      └──────────────┘
                                        │
                                        ▼
                    ┌───────────────────────────────────────────────┐
                    │                BROWSE (HOME)                   │
                    │  ┌─────────────────────────────────────────┐  │
                    │  │ HERO BANNER (Featured Content)          │  │
                    │  ├─────────────────────────────────────────┤  │
                    │  │ Continue Watching ──────────────────►   │  │
                    │  │ Trending Now ───────────────────────►   │  │
                    │  │ Popular Movies ─────────────────────►   │  │
                    │  │ Popular TV Shows ───────────────────►   │  │
                    │  │ Top Rated ──────────────────────────►   │  │
                    │  └─────────────────────────────────────────┘  │
                    └───────────────────┬───────────────────────────┘
                                        │
        ┌───────────────────────────────┼───────────────────────────────┐
        │               │               │               │               │
        ▼               ▼               ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   SEARCH     │ │ MOVIE DETAIL │ │ TV DETAIL    │ │  MY LIST     │ │  SETTINGS    │
│              │ │              │ │              │ │              │ │              │
└──────────────┘ └──────┬───────┘ └──────┬───────┘ └──────────────┘ └──────────────┘
                        │                │
                        │         ┌──────┴──────┐
                        │         │   SEASON    │
                        │         │   DETAIL    │
                        │         └──────┬──────┘
                        │                │
                        └────────────────┼────────────────┐
                                         │                │
                                         ▼                ▼
                               ┌─────────────────┐ ┌─────────────────┐
                               │  SOURCE SELECT  │ │  SOURCE SELECT  │
                               │    (Movie)      │ │   (Episode)     │
                               └────────┬────────┘ └────────┬────────┘
                                        │                   │
                                        └─────────┬─────────┘
                                                  │
                                                  ▼
                                        ┌─────────────────┐
                                        │     PLAYER      │
                                        │   (WebView)     │
                                        └─────────────────┘
```

## 6.2 Splash Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│                                                                                     │
│                                                                                     │
│                                                                                     │
│                                                                                     │
│                              ┌────────────────────┐                                 │
│                              │    🎬              │                                 │
│                              │   SimplStream      │                                 │
│                              │   ━━━━━━━━━━━━━━   │                                 │
│                              │   Loading...       │                                 │
│                              └────────────────────┘                                 │
│                                                                                     │
│                                                                                     │
│                                                                                     │
│                              by SimplStudios                                        │
│                                                                                     │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Behavior
1. Display logo with fade-in animation (500ms)
2. Show loading indicator
3. Check authentication state
4. Navigate to appropriate screen:
   - No setup → First Setup
   - Has PIN → PIN Entry
   - No PIN, Has Profiles → Profile Selection
   - No PIN, No Profiles → Create First Profile

### Duration
- Minimum: 1.5 seconds (for branding)
- Maximum: 5 seconds (with timeout)

---

## 6.3 Authentication / PIN Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│                                                                                     │
│                                                                                     │
│                              Enter Your PIN                                         │
│                                                                                     │
│                         ┌────┐  ┌────┐  ┌────┐  ┌────┐                             │
│                         │ ●  │  │ ●  │  │ _  │  │    │                             │
│                         └────┘  └────┘  └────┘  └────┘                             │
│                                                                                     │
│                         ┌─────────────────────────────┐                            │
│                         │  1    2    3               │                            │
│                         │  4    5    6               │                            │
│                         │  7    8    9               │                            │
│                         │  ⌫   0    ✓               │                            │
│                         └─────────────────────────────┘                            │
│                                                                                     │
│                           Forgot PIN? Reset App                                     │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### PIN Input Behavior
- 4-digit PIN
- Auto-submit when 4 digits entered
- Show dots (●) for entered digits
- Show underscore (_) for current position
- D-pad navigates number pad
- Shake animation on wrong PIN
- 3 attempts before lockout (30 seconds)

### Focus Management
```
Default Focus: Number "5" (center of numpad)
Navigation:
  - UP/DOWN/LEFT/RIGHT moves between numbers
  - OK selects number
  - Backspace (⌫) deletes last digit
```

---

## 6.4 Profile Selection Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│                                                                                     │
│                              Who's Watching?                                        │
│                                                                                     │
│                                                                                     │
│      ┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐          │
│      │            │    │            │    │            │    │            │          │
│      │     😎     │    │     🎮     │    │     🌸     │    │     ➕     │          │
│      │            │    │            │    │            │    │            │          │
│      │   John     │    │   Gaming   │    │   Sarah    │    │    Add     │          │
│      │            │    │            │    │            │    │  Profile   │          │
│      └────────────┘    └────────────┘    └────────────┘    └────────────┘          │
│           ▲                                                                         │
│         Focus                                                                       │
│                                                                                     │
│                                                                                     │
│                    [ Manage Profiles ]        [ Settings ]                         │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Profile Card States

| State | Visual |
|-------|--------|
| Unfocused | Normal size, 80% opacity |
| Focused | Scale 1.1x, 100% opacity, blue border, glow |
| Selected | Brief scale down (0.95x), then navigate |

### Focus Management
```
Default Focus: Last used profile OR first profile
Navigation:
  - LEFT/RIGHT between profiles
  - DOWN to bottom buttons
  - OK selects profile
  - Long press for edit mode
```

### Maximum Profiles: 5

---

## 6.5 Browse (Home) Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ ◀ SimplStream                                    🔍 Search    👤 Profile   ⚙️     │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                                                                               │ │
│  │  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │ │
│  │  ░░░░░░░░░░░ BLURRED BACKDROP IMAGE ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │ │
│  │  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │ │
│  │                                                                               │ │
│  │  ┌─────────┐   DUNE: PART TWO                                                │ │
│  │  │         │   ★★★★☆ 8.5  •  2024  •  2h 46m  •  Sci-Fi, Adventure          │ │
│  │  │ POSTER  │                                                                  │ │
│  │  │         │   Follow the mythic journey of Paul Atreides as he unites       │ │
│  │  │         │   with Chani and the Fremen while on a warpath of revenge...    │ │
│  │  └─────────┘                                                                  │ │
│  │                [ ▶ Play ]    [ + My List ]    [ ℹ More Info ]                │ │
│  │                                                                               │ │
│  │                          ○  ○  ●  ○  ○  (pagination dots)                    │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  Continue Watching                                                                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ───────► │
│  │ Breaking│ │  Dune   │ │ Shogun  │ │  Silo   │ │ 3 Body  │ │ Fallout │          │
│  │   Bad   │ │         │ │         │ │         │ │ Problem │ │         │          │
│  │ ▓▓▓▓░░░ │ │ ▓▓░░░░░ │ │ ▓▓▓▓▓▓░ │ │ ▓░░░░░░ │ │ ▓▓▓░░░░ │ │ ▓▓▓▓░░░ │          │
│  │   67%   │ │   23%   │ │   89%   │ │   12%   │ │   45%   │ │   56%   │          │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│                                                                                     │
│  Trending Now                                                                       │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ───────► │
│  │         │ │         │ │         │ │         │ │         │ │         │          │
│  │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │          │
│  │         │ │         │ │         │ │         │ │         │ │         │          │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│                                                                                     │
│  Popular Movies                                                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ───────► │
│  ...                                                                                │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Content Rows

| Row | Content | Source |
|-----|---------|--------|
| Hero Banner | Featured content (rotating) | TMDB Trending + Editorial |
| Continue Watching | Partially watched content | Local DB (profile-specific) |
| Trending Now | Trending all (day/week) | TMDB `/trending/all/week` |
| Popular Movies | Popular movies | TMDB `/movie/popular` |
| Popular TV Shows | Popular TV | TMDB `/tv/popular` |
| Top Rated | Top rated movies | TMDB `/movie/top_rated` |
| New Releases | Recent movies | TMDB `/movie/now_playing` |
| My List | User's watchlist | Local DB (profile-specific) |
| Action & Adventure | Genre filter | TMDB `/discover/movie?with_genres=28` |
| Comedy | Genre filter | TMDB `/discover/movie?with_genres=35` |
| Drama | Genre filter | TMDB `/discover/movie?with_genres=18` |
| Sci-Fi | Genre filter | TMDB `/discover/movie?with_genres=878` |
| Horror | Genre filter | TMDB `/discover/movie?with_genres=27` |

### Hero Banner Behavior
- Auto-advances every 8 seconds
- Pauses on focus
- Smooth crossfade between items
- 5 featured items
- Dynamic background (blurred backdrop)

### Focus Management
```
Default Focus: First item in "Continue Watching" OR first item in "Trending Now"
Navigation:
  - LEFT/RIGHT within row
  - UP/DOWN between rows
  - UP from first row → Header (Search, Profile, Settings)
  - DOWN scrolls to next row
  - OK opens detail screen
```

---

## 6.6 Movie Detail Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │
│  ░░░░░░░░░░░░░░░░░░░░░░ BACKDROP IMAGE (Blurred Gradient) ░░░░░░░░░░░░░░░░░░░░░░░░ │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │
│                                                                                     │
│  ┌───────────────┐                                                                  │
│  │               │    DUNE: PART TWO                                               │
│  │               │                                                                  │
│  │   POSTER      │    ★ 8.5/10  •  2024  •  2h 46m  •  PG-13                       │
│  │   (High Res)  │                                                                  │
│  │               │    Sci-Fi  •  Adventure  •  Drama                               │
│  │               │                                                                  │
│  │               │    Follow the mythic journey of Paul Atreides as he unites      │
│  │               │    with Chani and the Fremen while on a warpath of revenge      │
│  └───────────────┘    against the conspirators who destroyed his family. Facing    │
│                       a choice between the love of his life and the fate of the    │
│                       known universe, Paul endeavors to prevent a terrible         │
│                       future only he can foresee.                                  │
│                                                                                     │
│    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│    │  ▶ PLAY     │  │  + MY LIST   │  │  ▼ TRAILER   │  │  ★ RATE      │          │
│    └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘          │
│         ▲ Focus                                                                     │
│                                                                                     │
│  ─────────────────────────────────────────────────────────────────────────────────  │
│                                                                                     │
│  Cast                                                                               │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ──────────────► │
│  │  Face  │ │  Face  │ │  Face  │ │  Face  │ │  Face  │ │  Face  │                 │
│  │Timothée│ │Zendaya │ │Rebecca │ │ Javier │ │  Josh  │ │ Austin │                 │
│  │Chalamet│ │        │ │Ferguson│ │ Bardem │ │ Brolin │ │ Butler │                 │
│  │  Paul  │ │ Chani  │ │ Jessica│ │Stilgar │ │Gurney  │ │ Feyd   │                 │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘                 │
│                                                                                     │
│  More Like This                                                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ────────────────────► │
│  │  Dune   │ │Blade    │ │  The    │ │ Avatar  │ │Interste-│                       │
│  │ Part 1  │ │Runner   │ │ Matrix  │ │         │ │  llar   │                       │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘                       │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Information Displayed
- Title
- Rating (TMDB score)
- Year
- Runtime
- Content rating (if available)
- Genres
- Overview/Description
- Cast & Crew
- Similar/Recommended content

### Actions
| Button | Action |
|--------|--------|
| Play | Opens source selector → Player |
| My List | Toggle watchlist (+ / ✓) |
| Trailer | Play YouTube trailer (if available) |
| Rate | Rate movie (stored locally) |

### Focus Management
```
Default Focus: "Play" button
Navigation:
  - LEFT/RIGHT between action buttons
  - DOWN to Cast row
  - DOWN from Cast to Recommendations
  - BACK returns to Browse
```

---

## 6.7 TV Show Detail Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░ BACKDROP ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │
│                                                                                     │
│  ┌───────────────┐                                                                  │
│  │               │    BREAKING BAD                                                  │
│  │   POSTER      │                                                                  │
│  │               │    ★ 9.5/10  •  2008-2013  •  5 Seasons  •  TV-MA               │
│  │               │                                                                  │
│  │               │    Drama  •  Crime  •  Thriller                                  │
│  └───────────────┘                                                                  │
│                       A chemistry teacher diagnosed with inoperable lung cancer    │
│                       turns to manufacturing and selling methamphetamine with      │
│                       a former student to secure his family's future.              │
│                                                                                     │
│    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                            │
│    │  ▶ PLAY S1E1 │  │  + MY LIST   │  │  ▼ TRAILER   │                            │
│    └──────────────┘  └──────────────┘  └──────────────┘                            │
│                                                                                     │
│  ─────────────────────────────────────────────────────────────────────────────────  │
│                                                                                     │
│  Seasons                                                                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
│  │             │ │             │ │             │ │             │ │             │   │
│  │  SEASON 1   │ │  SEASON 2   │ │  SEASON 3   │ │  SEASON 4   │ │  SEASON 5   │   │
│  │  7 Episodes │ │ 13 Episodes │ │ 13 Episodes │ │ 13 Episodes │ │ 16 Episodes │   │
│  │             │ │             │ │             │ │             │ │             │   │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │
│       ▲ Focus                                                                       │
│                                                                                     │
│  Cast                                                                               │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ────────────────────────►  │
│  │  Face  │ │  Face  │ │  Face  │ │  Face  │ │  Face  │                            │
│  │ Bryan  │ │ Aaron  │ │  Anna  │ │  Dean  │ │ Betsy  │                            │
│  │Cranston│ │  Paul  │ │  Gunn  │ │ Norris │ │ Brandt │                            │
│  │ Walter │ │ Jesse  │ │ Skyler │ │  Hank  │ │ Marie  │                            │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘                            │
│                                                                                     │
│  More Like This                                                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ────────────────────► │
│  │ Better  │ │  Ozark  │ │  Narcos │ │The Wire │ │ Fargo   │                       │
│  │  Call   │ │         │ │         │ │         │ │         │                       │
│  │  Saul   │ │         │ │         │ │         │ │         │                       │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘                       │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Play Button Logic
- If never watched: "▶ Play S1E1"
- If watching: "▶ Continue S2E5" (resume progress)
- Shows next unwatched episode

---

## 6.8 Season Detail / Episode List Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ◀ BACK    Breaking Bad                                                            │
│                                                                                     │
│  Season 1  ▼  (Dropdown to switch seasons)                                         │
│                                                                                     │
│  7 Episodes  •  2008                                                                │
│                                                                                     │
│  ─────────────────────────────────────────────────────────────────────────────────  │
│                                                                                     │
│  ┌──────────────────────────────────────────────────────────────────────────────┐  │
│  │ ┌─────────────────┐                                                          │  │
│  │ │                 │  E1 • Pilot                                   58 min    │  │
│  │ │   STILL IMAGE   │  Diagnosed with terminal lung cancer, a high school     │  │
│  │ │   (Episode)     │  chemistry teacher resorts to cooking meth to leave     │  │
│  │ │                 │  money for his family.                                   │  │
│  │ │ ▓▓▓▓▓▓▓▓▓▓░░░░░ │                                             ✓ Watched   │  │
│  │ └─────────────────┘                                                          │  │
│  └──────────────────────────────────────────────────────────────────────────────┘  │
│       ▲ Focus                                                                       │
│                                                                                     │
│  ┌──────────────────────────────────────────────────────────────────────────────┐  │
│  │ ┌─────────────────┐                                                          │  │
│  │ │                 │  E2 • Cat's in the Bag...                    48 min    │  │
│  │ │   STILL IMAGE   │  Walt and Jesse attempt to tie up loose ends.           │  │
│  │ │                 │                                                          │  │
│  │ │                 │                                             ✓ Watched   │  │
│  │ └─────────────────┘                                                          │  │
│  └──────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                     │
│  ┌──────────────────────────────────────────────────────────────────────────────┐  │
│  │ ┌─────────────────┐                                                          │  │
│  │ │                 │  E3 • ...And the Bag's in the River             45 min  │  │
│  │ │   STILL IMAGE   │  Walt wrestles with a difficult decision.               │  │
│  │ │                 │                                                          │  │
│  │ │ ▓▓▓▓░░░░░░░░░░░ │                                               30%       │  │
│  │ └─────────────────┘                                                          │  │
│  └──────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                     │
│  ... (scrollable list)                                                              │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Episode States
| State | Visual |
|-------|--------|
| Unwatched | No indicator |
| Watching | Progress bar + percentage |
| Watched | ✓ checkmark |

---

## 6.9 Source Selector Dialog

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│                         ░░░░░ DIM BACKGROUND ░░░░░                                  │
│                                                                                     │
│            ┌────────────────────────────────────────────────────────┐              │
│            │                                                        │              │
│            │   Select Video Source                                  │              │
│            │                                                        │              │
│            │   ┌──────────────────────────────────────────────┐    │              │
│            │   │  🎬  VidSrc                        Primary   │    │              │
│            │   │      Fast servers, multiple qualities        │    │              │
│            │   └──────────────────────────────────────────────┘    │              │
│            │        ▲ Focus                                        │              │
│            │                                                        │              │
│            │   ┌──────────────────────────────────────────────┐    │              │
│            │   │  ⚡  VidFast                       Backup    │    │              │
│            │   │      Quick loading, stable                   │    │              │
│            │   └──────────────────────────────────────────────┘    │              │
│            │                                                        │              │
│            │   ┌──────────────────────────────────────────────┐    │              │
│            │   │  🎥  111Movies                    Backup     │    │              │
│            │   │      Multiple servers available              │    │              │
│            │   └──────────────────────────────────────────────┘    │              │
│            │                                                        │              │
│            │   ┌──────────────────────────────────────────────┐    │              │
│            │   │  📺  Vidzee                       Backup     │    │              │
│            │   │      HD quality streams                      │    │              │
│            │   └──────────────────────────────────────────────┘    │              │
│            │                                                        │              │
│            │                        [ Cancel ]                      │              │
│            │                                                        │              │
│            └────────────────────────────────────────────────────────┘              │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Behavior
- Shows when user clicks "Play"
- Sources ordered by preference
- Can be skipped if "Auto-play best source" is enabled in settings
- If source fails, offers to try next source

---

## 6.10 Player Screen (WebView)

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                        WEBVIEW (FULLSCREEN)                                   │ │
│  │                      Embed Player from Source                                 │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  (Overlay on pause/activity - auto-hides after 5 seconds)                          │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │  ◀ Back         Dune: Part Two                              🔄 Switch Source │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                 (Video controls handled by embed source)                      │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │                                                                               │ │
│  │  ████████████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   1:23:45 / 2:46:12 │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Remote Control Mapping
| Button | Action |
|--------|--------|
| BACK | Exit player (confirm if playing) |
| D-PAD CENTER | Play/Pause (sent to WebView) |
| LEFT | Rewind (sent to WebView) |
| RIGHT | Fast Forward (sent to WebView) |
| UP | Show overlay / Volume up |
| DOWN | Show overlay / Volume down |
| MENU | Show source switcher |

### Progress Tracking
- Every 30 seconds, save current position to local DB
- On exit, save position
- Position synced with profile

---

## 6.11 Search Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ◀ BACK                          Search                                            │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │  🔍  breaking bad_                                                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                                                                             │   │
│  │   A  B  C  D  E  F  G  H  I  J  K  L  M                                    │   │
│  │   N  O  P  Q  R  S  T  U  V  W  X  Y  Z                                    │   │
│  │   1  2  3  4  5  6  7  8  9  0  ⌫  ␣  🔍                                   │   │
│  │                                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ─────────────────────────────────────────────────────────────────────────────────  │
│                                                                                     │
│  Results (8 found)                                                                  │
│                                                                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ───────► │
│  │Breaking │ │Better   │ │El Camino│ │Breaking │ │Metastasis│ │ ...    │          │
│  │   Bad   │ │ Call    │ │         │ │  Bad:   │ │(Remake) │ │        │          │
│  │   ★9.5  │ │  Saul   │ │         │ │ Movie   │ │         │ │        │          │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│                                                                                     │
│                                                                                     │
│  Suggested                                                                          │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ────────────────────► │
│  │  Ozark  │ │  Narcos │ │ The Wire│ │  Fargo  │ │Peaky    │                       │
│  │         │ │         │ │         │ │         │ │Blinders │                       │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘                       │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Search Behavior
- Debounced search (300ms after typing stops)
- Multi-search (movies + TV shows)
- Show keyboard by default
- Tab between keyboard and results
- Voice search support (if device supports)

### Focus Management
```
Default Focus: Search input / Keyboard
Navigation:
  - Navigate keyboard with D-pad
  - DOWN from keyboard → Results
  - UP from results → Keyboard
  - RIGHT from last result → scroll more
```

---

## 6.12 My List (Watchlist) Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ◀ BACK                         My List                                            │
│                                                                                     │
│  Movies (12)  |  TV Shows (8)                                                       │
│  ▔▔▔▔▔▔▔▔▔▔▔                                                                       │
│                                                                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │
│  │         │ │         │ │         │ │         │ │         │ │         │          │
│  │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │          │
│  │         │ │         │ │         │ │         │ │         │ │         │          │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│       ▲ Focus                                                                       │
│                                                                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │
│  │         │ │         │ │         │ │         │ │         │ │         │          │
│  │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │ │ Poster  │          │
│  │         │ │         │ │         │ │         │ │         │ │         │          │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│                                                                                     │
│                                                                                     │
│  (Grid continues...)                                                                │
│                                                                                     │
│                                                                                     │
│                                                                                     │
│                                                                                     │
│  Tip: Long-press on a title to remove from My List                                 │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Features
- Tab filtering: Movies / TV Shows / All
- Grid layout (6 columns)
- Long press to remove
- Profile-specific list
- Sort by: Date Added, Title, Rating

---

## 6.13 Settings Screen

### Layout Specification

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ◀ BACK                         Settings                                           │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                                                                             │   │
│  │   ┌──────┐   Account                                                        │   │
│  │   │  👤  │   Manage profiles, PIN settings                         ▶       │   │
│  │   └──────┘                                                                  │   │
│  │                                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│       ▲ Focus                                                                       │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                                                                             │   │
│  │   ┌──────┐   Playback                                                       │   │
│  │   │  ▶   │   Video quality, autoplay, sources                      ▶       │   │
│  │   └──────┘                                                                  │   │
│  │                                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                                                                             │   │
│  │   ┌──────┐   Appearance                                                     │   │
│  │   │  🎨  │   Theme, animations, UI sounds                          ▶       │   │
│  │   └──────┘                                                                  │   │
│  │                                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                                                                             │   │
│  │   ┌──────┐   Storage                                                        │   │
│  │   │  💾  │   Clear cache, clear history                            ▶       │   │
│  │   └──────┘                                                                  │   │
│  │                                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                                                                             │   │
│  │   ┌──────┐   About                                                          │   │
│  │   │  ℹ️  │   Version, updates, licenses                            ▶       │   │
│  │   └──────┘                                                                  │   │
│  │                                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Settings Categories

#### Account Settings
```
• Manage Profiles
  - Edit profiles
  - Delete profiles
  - Add profile
  
• Security
  - Enable/Disable PIN
  - Change PIN
  
• Profile
  - Switch profile
  - Current: [Profile Name]
```

#### Playback Settings
```
• Preferred Source
  - Auto (recommended)
  - VidSrc
  - VidFast
  - 111Movies
  - Vidzee

• Source Fallback
  - Enabled/Disabled
  - Fallback order

• Autoplay
  - Autoplay next episode: On/Off
  - Countdown duration: 15s / 10s / 5s / Off

• Continue Watching
  - Resume threshold: 5% / 10%
  - Mark complete threshold: 90% / 95%
```

#### Appearance Settings
```
• Theme
  - Dark (default)
  - OLED Black
  
• Animations
  - Focus animations: On/Off
  - Page transitions: On/Off
  
• UI Sounds
  - Navigation sounds: On/Off
  - Volume: Slider
  
• Language
  - English (default)
  - (future: more languages)
```

#### Storage Settings
```
• Cache
  - Image cache size: XXX MB
  - Clear image cache
  
• History
  - Clear watch history (current profile)
  - Clear all data (reset app)
  
• Debug
  - Enable debug mode
  - Export logs
```

#### About
```
• App Info
  - Version: 1.0.0
  - Build: 100
  
• Updates
  - Check for updates
  - Auto-update: On/Off
  
• Legal
  - Open source licenses
  - Privacy policy
  
• Developer
  - SimplStudios
  - Website
```

---

# 7. AUTHENTICATION SYSTEM

## 7.1 Overview

SimplStream uses a **local-only authentication** system. No server, no accounts — just device-level security with optional PIN protection.

### Security Model

```
┌─────────────────────────────────────────────────────────────────┐
│                    AUTHENTICATION FLOW                          │
└─────────────────────────────────────────────────────────────────┘

                         App Launch
                             │
                             ▼
                    ┌────────────────┐
                    │  Is PIN Set?   │
                    └───────┬────────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
        ┌──────────┐               ┌──────────┐
        │   YES    │               │    NO    │
        └────┬─────┘               └────┬─────┘
             │                          │
             ▼                          │
    ┌─────────────────┐                 │
    │  PIN Entry      │                 │
    │  Screen         │                 │
    └────────┬────────┘                 │
             │                          │
             ▼                          │
    ┌─────────────────┐                 │
    │  Validate PIN   │                 │
    └────────┬────────┘                 │
             │                          │
    ┌────────┴────────┐                 │
    │                 │                 │
    ▼                 ▼                 │
 ┌──────┐        ┌──────┐              │
 │Valid │        │Invalid│              │
 └──┬───┘        └──┬───┘              │
    │               │                   │
    │               ▼                   │
    │      ┌─────────────────┐         │
    │      │ Shake Animation │         │
    │      │ Try Again       │         │
    │      │ (3 attempts max)│         │
    │      └─────────────────┘         │
    │                                   │
    └───────────────┬───────────────────┘
                    │
                    ▼
           ┌─────────────────┐
           │ Profile Select  │
           └─────────────────┘
```

## 7.2 PIN Storage (Secure)

```kotlin
// data/local/preferences/AuthPreferences.kt

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Use EncryptedSharedPreferences for PIN storage
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "simplstream_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_TIME = "lockout_time"
    }
    
    // Store hashed PIN (never plain text)
    fun setPin(pin: String) {
        val hashedPin = hashPin(pin)
        securePrefs.edit()
            .putString(KEY_PIN_HASH, hashedPin)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }
    
    fun validatePin(pin: String): Boolean {
        val storedHash = securePrefs.getString(KEY_PIN_HASH, null) ?: return false
        val inputHash = hashPin(pin)
        return storedHash == inputHash
    }
    
    fun isPinEnabled(): Boolean = securePrefs.getBoolean(KEY_PIN_ENABLED, false)
    
    fun removePin() {
        securePrefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }
    
    // Lockout after failed attempts
    fun recordFailedAttempt(): Int {
        val attempts = securePrefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        securePrefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts).apply()
        
        if (attempts >= 3) {
            securePrefs.edit()
                .putLong(KEY_LOCKOUT_TIME, System.currentTimeMillis())
                .apply()
        }
        return attempts
    }
    
    fun isLockedOut(): Boolean {
        val lockoutTime = securePrefs.getLong(KEY_LOCKOUT_TIME, 0)
        if (lockoutTime == 0L) return false
        
        val elapsed = System.currentTimeMillis() - lockoutTime
        val lockoutDuration = 30_000L // 30 seconds
        
        if (elapsed > lockoutDuration) {
            clearLockout()
            return false
        }
        return true
    }
    
    fun getRemainingLockoutSeconds(): Int {
        val lockoutTime = securePrefs.getLong(KEY_LOCKOUT_TIME, 0)
        val elapsed = System.currentTimeMillis() - lockoutTime
        val remaining = 30_000L - elapsed
        return (remaining / 1000).toInt().coerceAtLeast(0)
    }
    
    fun clearLockout() {
        securePrefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LOCKOUT_TIME)
            .apply()
    }
    
    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
```

## 7.3 Authentication Use Cases

```kotlin
// domain/usecase/auth/AuthenticateUseCase.kt

class AuthenticateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    sealed class Result {
        object Success : Result()
        object InvalidPin : Result()
        data class LockedOut(val remainingSeconds: Int) : Result()
        object NoPinSet : Result()
    }
    
    suspend operator fun invoke(pin: String): Result {
        if (!authRepository.isPinEnabled()) {
            return Result.NoPinSet
        }
        
        if (authRepository.isLockedOut()) {
            return Result.LockedOut(authRepository.getRemainingLockoutSeconds())
        }
        
        return if (authRepository.validatePin(pin)) {
            authRepository.clearLockout()
            Result.Success
        } else {
            val attempts = authRepository.recordFailedAttempt()
            if (attempts >= 3) {
                Result.LockedOut(30)
            } else {
                Result.InvalidPin
            }
        }
    }
}
```

## 7.4 PIN Input UI Component

```kotlin
// presentation/auth/PinInputView.kt

class PinInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val pinDots = mutableListOf<View>()
    private var currentPin = StringBuilder()
    private var onPinComplete: ((String) -> Unit)? = null
    
    private val pinLength = 4
    
    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        
        // Create 4 PIN dot views
        repeat(pinLength) { index ->
            val dot = createPinDot()
            pinDots.add(dot)
            addView(dot)
            
            if (index < pinLength - 1) {
                addView(createSpacer())
            }
        }
    }
    
    private fun createPinDot(): View {
        return View(context).apply {
            layoutParams = LayoutParams(80.dp, 80.dp)
            background = ContextCompat.getDrawable(context, R.drawable.bg_pin_dot_empty)
        }
    }
    
    fun appendDigit(digit: Char) {
        if (currentPin.length >= pinLength) return
        
        currentPin.append(digit)
        updateDots()
        
        if (currentPin.length == pinLength) {
            onPinComplete?.invoke(currentPin.toString())
        }
    }
    
    fun deleteLastDigit() {
        if (currentPin.isEmpty()) return
        currentPin.deleteAt(currentPin.length - 1)
        updateDots()
    }
    
    fun clear() {
        currentPin.clear()
        updateDots()
    }
    
    fun shake() {
        val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake)
        startAnimation(shakeAnimation)
        clear()
    }
    
    private fun updateDots() {
        pinDots.forEachIndexed { index, dot ->
            val isFilled = index < currentPin.length
            dot.background = ContextCompat.getDrawable(
                context,
                if (isFilled) R.drawable.bg_pin_dot_filled else R.drawable.bg_pin_dot_empty
            )
        }
    }
    
    fun setOnPinCompleteListener(listener: (String) -> Unit) {
        onPinComplete = listener
    }
}
```

---

# 8. USER PROFILES SYSTEM

## 8.1 Profile Data Model

```kotlin
// domain/model/Profile.kt

data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarId: Int,  // Index into predefined avatars
    val isKidsMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
)

// Available Avatars (Predefined)
object ProfileAvatars {
    val avatars = listOf(
        R.drawable.avatar_1,  // 😎 Cool
        R.drawable.avatar_2,  // 🎮 Gamer
        R.drawable.avatar_3,  // 🌸 Flower
        R.drawable.avatar_4,  // 🎬 Movie
        R.drawable.avatar_5,  // 🌙 Night
        R.drawable.avatar_6,  // ⭐ Star
        R.drawable.avatar_7,  // 🎵 Music
        R.drawable.avatar_8,  // 📚 Book
        R.drawable.avatar_9,  // 🎨 Art
        R.drawable.avatar_10, // 🚀 Space
        R.drawable.avatar_11, // 🐱 Cat
        R.drawable.avatar_12  // 🐶 Dog
    )
}
```

## 8.2 Profile Database Entity

```kotlin
// data/local/db/entity/ProfileEntity.kt

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "avatar_id")
    val avatarId: Int,
    
    @ColumnInfo(name = "is_kids_mode")
    val isKidsMode: Boolean,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long
)
```

## 8.3 Profile DAO

```kotlin
// data/local/db/dao/ProfileDao.kt

@Dao
interface ProfileDao {
    
    @Query("SELECT * FROM profiles ORDER BY last_used_at DESC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>
    
    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: String): ProfileEntity?
    
    @Query("SELECT * FROM profiles ORDER BY last_used_at DESC LIMIT 1")
    suspend fun getLastUsedProfile(): ProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: ProfileEntity)
    
    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: String)
    
    @Query("UPDATE profiles SET last_used_at = :timestamp WHERE id = :profileId")
    suspend fun updateLastUsed(profileId: String, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
}
```

## 8.4 Profile Repository

```kotlin
// data/repository/ProfileRepositoryImpl.kt

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val profileMapper: ProfileMapper,
    private val preferencesManager: PreferencesManager
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { profileMapper.toDomain(it) }
        }
    }
    
    override suspend fun getProfileById(id: String): Profile? {
        return profileDao.getProfileById(id)?.let { profileMapper.toDomain(it) }
    }
    
    override suspend fun createProfile(name: String, avatarId: Int, isKidsMode: Boolean): Profile {
        val profile = Profile(
            name = name,
            avatarId = avatarId,
            isKidsMode = isKidsMode
        )
        profileDao.insertProfile(profileMapper.toEntity(profile))
        return profile
    }
    
    override suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profileMapper.toEntity(profile))
    }
    
    override suspend fun deleteProfile(profileId: String) {
        profileDao.deleteProfile(profileId)
        // Also clear this profile's watch history and watchlist
    }
    
    override suspend fun setActiveProfile(profileId: String) {
        profileDao.updateLastUsed(profileId, System.currentTimeMillis())
        preferencesManager.setActiveProfileId(profileId)
    }
    
    override suspend fun getActiveProfile(): Profile? {
        val activeId = preferencesManager.getActiveProfileId()
        return activeId?.let { getProfileById(it) }
            ?: profileDao.getLastUsedProfile()?.let { profileMapper.toDomain(it) }
    }
    
    override suspend fun canCreateMoreProfiles(): Boolean {
        return profileDao.getProfileCount() < MAX_PROFILES
    }
    
    companion object {
        const val MAX_PROFILES = 5
    }
}
```

## 8.5 Profile Selection ViewModel

```kotlin
// presentation/profile/ProfileSelectionViewModel.kt

@HiltViewModel
class ProfileSelectionViewModel @Inject constructor(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val setActiveProfileUseCase: SetActiveProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSelectionUiState())
    val uiState: StateFlow<ProfileSelectionUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<ProfileSelectionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    
    init {
        loadProfiles()
    }
    
    private fun loadProfiles() {
        viewModelScope.launch {
            getProfilesUseCase().collect { profiles ->
                _uiState.update { it.copy(
                    profiles = profiles,
                    isLoading = false,
                    canAddMore = profiles.size < 5
                )}
            }
        }
    }
    
    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            setActiveProfileUseCase(profile.id)
            _events.send(ProfileSelectionEvent.NavigateToBrowse)
        }
    }
    
    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            deleteProfileUseCase(profile.id)
        }
    }
    
    fun onAddProfileClicked() {
        viewModelScope.launch {
            _events.send(ProfileSelectionEvent.NavigateToProfileEditor(null))
        }
    }
    
    fun onEditProfileClicked(profile: Profile) {
        viewModelScope.launch {
            _events.send(ProfileSelectionEvent.NavigateToProfileEditor(profile.id))
        }
    }
}

data class ProfileSelectionUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = true,
    val canAddMore: Boolean = true,
    val isEditMode: Boolean = false
)

sealed class ProfileSelectionEvent {
    object NavigateToBrowse : ProfileSelectionEvent()
    data class NavigateToProfileEditor(val profileId: String?) : ProfileSelectionEvent()
}
```

---

# 9. DATA LAYER

## 9.1 Room Database

```kotlin
// data/local/db/SimplStreamDatabase.kt

@Database(
    entities = [
        ProfileEntity::class,
        WatchHistoryEntity::class,
        WatchlistEntity::class,
        ContentCacheEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SimplStreamDatabase : RoomDatabase() {
    
    abstract fun profileDao(): ProfileDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun cacheDao(): CacheDao
    
    companion object {
        const val DATABASE_NAME = "simplstream_db"
    }
}

// Type Converters
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")
    
    @TypeConverter
    fun toStringList(value: String): List<String> = 
        if (value.isEmpty()) emptyList() else value.split(",")
    
    @TypeConverter
    fun fromContentType(type: ContentType): String = type.name
    
    @TypeConverter
    fun toContentType(value: String): ContentType = ContentType.valueOf(value)
}
```

## 9.2 Watch History Entity

```kotlin
// data/local/db/entity/WatchHistoryEntity.kt

@Entity(
    tableName = "watch_history",
    primaryKeys = ["profile_id", "content_id", "content_type"],
    indices = [Index(value = ["profile_id", "last_watched"], orders = [Index.Order.DESC])]
)
data class WatchHistoryEntity(
    @ColumnInfo(name = "profile_id")
    val profileId: String,
    
    @ColumnInfo(name = "content_id")
    val contentId: Int,  // TMDB ID
    
    @ColumnInfo(name = "content_type")
    val contentType: ContentType,  // MOVIE or TV
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "poster_path")
    val posterPath: String?,
    
    @ColumnInfo(name = "backdrop_path")
    val backdropPath: String?,
    
    // For movies: position in the movie
    // For TV: position in current episode
    @ColumnInfo(name = "position_ms")
    val positionMs: Long,
    
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    
    // For TV shows only
    @ColumnInfo(name = "season_number")
    val seasonNumber: Int? = null,
    
    @ColumnInfo(name = "episode_number")
    val episodeNumber: Int? = null,
    
    @ColumnInfo(name = "episode_title")
    val episodeTitle: String? = null,
    
    @ColumnInfo(name = "last_watched")
    val lastWatched: Long,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)

enum class ContentType {
    MOVIE, TV
}
```

## 9.3 Watch History DAO

```kotlin
// data/local/db/dao/WatchHistoryDao.kt

@Dao
interface WatchHistoryDao {
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId 
        AND is_completed = 0
        ORDER BY last_watched DESC
        LIMIT :limit
    """)
    fun getContinueWatching(profileId: String, limit: Int = 20): Flow<List<WatchHistoryEntity>>
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId 
        ORDER BY last_watched DESC
    """)
    fun getAllHistory(profileId: String): Flow<List<WatchHistoryEntity>>
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND content_type = :contentType
    """)
    suspend fun getHistoryItem(
        profileId: String, 
        contentId: Int, 
        contentType: ContentType
    ): WatchHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: WatchHistoryEntity)
    
    @Query("""
        UPDATE watch_history 
        SET position_ms = :positionMs, 
            last_watched = :timestamp,
            is_completed = :isCompleted
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND content_type = :contentType
    """)
    suspend fun updateProgress(
        profileId: String,
        contentId: Int,
        contentType: ContentType,
        positionMs: Long,
        timestamp: Long,
        isCompleted: Boolean
    )
    
    @Query("DELETE FROM watch_history WHERE profile_id = :profileId")
    suspend fun clearHistoryForProfile(profileId: String)
    
    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
```

## 9.4 Watchlist Entity & DAO

```kotlin
// data/local/db/entity/WatchlistEntity.kt

@Entity(
    tableName = "watchlist",
    primaryKeys = ["profile_id", "content_id", "content_type"],
    indices = [Index(value = ["profile_id", "added_at"], orders = [Index.Order.DESC])]
)
data class WatchlistEntity(
    @ColumnInfo(name = "profile_id")
    val profileId: String,
    
    @ColumnInfo(name = "content_id")
    val contentId: Int,
    
    @ColumnInfo(name = "content_type")
    val contentType: ContentType,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "poster_path")
    val posterPath: String?,
    
    @ColumnInfo(name = "vote_average")
    val voteAverage: Float,
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long
)

// data/local/db/dao/WatchlistDao.kt

@Dao
interface WatchlistDao {
    
    @Query("""
        SELECT * FROM watchlist 
        WHERE profile_id = :profileId 
        ORDER BY added_at DESC
    """)
    fun getWatchlist(profileId: String): Flow<List<WatchlistEntity>>
    
    @Query("""
        SELECT * FROM watchlist 
        WHERE profile_id = :profileId 
        AND content_type = :contentType
        ORDER BY added_at DESC
    """)
    fun getWatchlistByType(
        profileId: String, 
        contentType: ContentType
    ): Flow<List<WatchlistEntity>>
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM watchlist 
            WHERE profile_id = :profileId 
            AND content_id = :contentId 
            AND content_type = :contentType
        )
    """)
    fun isInWatchlist(
        profileId: String, 
        contentId: Int, 
        contentType: ContentType
    ): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)
    
    @Query("""
        DELETE FROM watchlist 
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND content_type = :contentType
    """)
    suspend fun removeFromWatchlist(
        profileId: String, 
        contentId: Int, 
        contentType: ContentType
    )
    
    @Query("DELETE FROM watchlist WHERE profile_id = :profileId")
    suspend fun clearWatchlistForProfile(profileId: String)
}
```

## 9.5 DataStore Preferences

```kotlin
// data/local/preferences/PreferencesManager.kt

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        private val Context.dataStore by preferencesDataStore(name = "simplstream_prefs")
        
        // Keys
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        val THEME = stringPreferencesKey("theme")
        val PREFERRED_SOURCE = stringPreferencesKey("preferred_source")
        val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
        val AUTOPLAY_COUNTDOWN = intPreferencesKey("autoplay_countdown")
        val UI_SOUNDS = booleanPreferencesKey("ui_sounds")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val SOURCE_FALLBACK_ENABLED = booleanPreferencesKey("source_fallback_enabled")
        val RESUME_THRESHOLD = intPreferencesKey("resume_threshold")
        val COMPLETE_THRESHOLD = intPreferencesKey("complete_threshold")
    }
    
    // Active Profile
    suspend fun setActiveProfileId(profileId: String) {
        dataStore.edit { it[ACTIVE_PROFILE_ID] = profileId }
    }
    
    suspend fun getActiveProfileId(): String? {
        return dataStore.data.first()[ACTIVE_PROFILE_ID]
    }
    
    fun observeActiveProfileId(): Flow<String?> {
        return dataStore.data.map { it[ACTIVE_PROFILE_ID] }
    }
    
    // Theme
    val themeFlow: Flow<AppTheme> = dataStore.data.map { prefs ->
        AppTheme.valueOf(prefs[THEME] ?: AppTheme.DARK.name)
    }
    
    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[THEME] = theme.name }
    }
    
    // Preferred Video Source
    val preferredSourceFlow: Flow<VideoSourceType> = dataStore.data.map { prefs ->
        VideoSourceType.valueOf(prefs[PREFERRED_SOURCE] ?: VideoSourceType.AUTO.name)
    }
    
    suspend fun setPreferredSource(source: VideoSourceType) {
        dataStore.edit { it[PREFERRED_SOURCE] = source.name }
    }
    
    // Autoplay Settings
    val autoplayNextFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AUTOPLAY_NEXT] ?: true
    }
    
    val autoplayCountdownFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[AUTOPLAY_COUNTDOWN] ?: 15
    }
    
    // UI Settings
    val uiSoundsFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[UI_SOUNDS] ?: true
    }
    
    val animationsEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ANIMATIONS_ENABLED] ?: true
    }
    
    // Playback Settings
    val sourceFallbackEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SOURCE_FALLBACK_ENABLED] ?: true
    }
    
    val resumeThresholdFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[RESUME_THRESHOLD] ?: 5  // 5% of video
    }
    
    val completeThresholdFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[COMPLETE_THRESHOLD] ?: 90  // 90% of video
    }
}

enum class AppTheme {
    DARK, OLED
}

enum class VideoSourceType {
    AUTO, VIDSRC, VIDFAST, MOVIES111, VIDZEE
}
```

---

# 10. NAVIGATION & FOCUS SYSTEM

## 10.1 Focus Management Strategy

The focus system is **THE MOST CRITICAL** part of a TV app. Here's how we handle it:

### Focus Principles

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          TV FOCUS MANAGEMENT                                    │
└─────────────────────────────────────────────────────────────────────────────────┘

1. EVERY interactive element MUST be focusable
2. Focus MUST be visible (scale, border, glow)
3. Focus should NEVER get lost
4. Navigation should be predictable
5. Restore focus after screen changes
```

## 10.2 Focus Helper Utility

```kotlin
// util/FocusUtils.kt

object FocusUtils {
    
    /**
     * Request focus with a slight delay to ensure view is laid out
     */
    fun View.requestFocusDelayed(delayMs: Long = 100) {
        postDelayed({ requestFocus() }, delayMs)
    }
    
    /**
     * Setup focus change listener with scale animation
     */
    fun View.setupFocusAnimation(
        scaleFactor: Float = 1.08f,
        duration: Long = 200
    ) {
        setOnFocusChangeListener { view, hasFocus ->
            val scale = if (hasFocus) scaleFactor else 1f
            view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .start()
                
            // Elevation change for depth
            view.elevation = if (hasFocus) 16.dp.toFloat() else 4.dp.toFloat()
        }
    }
    
    /**
     * Setup explicit focus navigation
     */
    fun View.setFocusDirections(
        up: View? = null,
        down: View? = null,
        left: View? = null,
        right: View? = null
    ) {
        up?.let { nextFocusUpId = it.id }
        down?.let { nextFocusDownId = it.id }
        left?.let { nextFocusLeftId = it.id }
        right?.let { nextFocusRightId = it.id }
    }
    
    /**
     * Find first focusable child in a ViewGroup
     */
    fun ViewGroup.findFirstFocusable(): View? {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isFocusable) return child
            if (child is ViewGroup) {
                child.findFirstFocusable()?.let { return it }
            }
        }
        return null
    }
    
    /**
     * Ensure focus doesn't leave a container
     */
    fun ViewGroup.trapFocus() {
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isFocusable = true
        isFocusableInTouchMode = true
    }
}
```

## 10.3 Focus-Aware RecyclerView

```kotlin
// presentation/common/components/FocusAwareRecyclerView.kt

class FocusAwareRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var lastFocusedPosition = 0
    
    init {
        // Prevent focus from leaving the RecyclerView unexpectedly
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        
        // Ensure smooth scrolling to focused item
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_IDLE) {
                    restoreFocusIfNeeded()
                }
            }
        })
    }
    
    override fun focusSearch(focused: View?, direction: Int): View? {
        // Custom focus search to keep focus within row when appropriate
        val layoutManager = layoutManager as? LinearLayoutManager
        
        if (layoutManager?.orientation == LinearLayoutManager.HORIZONTAL) {
            when (direction) {
                FOCUS_LEFT -> {
                    val pos = getChildAdapterPosition(focused ?: return null)
                    if (pos == 0) {
                        // At start, let focus go to navigation
                        return super.focusSearch(focused, direction)
                    }
                }
                FOCUS_RIGHT -> {
                    val pos = getChildAdapterPosition(focused ?: return null)
                    val itemCount = adapter?.itemCount ?: 0
                    if (pos == itemCount - 1) {
                        // At end, stay on last item
                        return focused
                    }
                }
            }
        }
        
        return super.focusSearch(focused, direction)
    }
    
    fun saveFocusState() {
        findFocus()?.let { focused ->
            lastFocusedPosition = getChildAdapterPosition(focused)
        }
    }
    
    fun restoreFocusState() {
        post {
            layoutManager?.findViewByPosition(lastFocusedPosition)?.requestFocus()
                ?: findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
        }
    }
    
    private fun restoreFocusIfNeeded() {
        if (!hasFocus() && isAttachedToWindow) {
            restoreFocusState()
        }
    }
}
```

## 10.4 Focus States Drawable

```xml
<!-- res/drawable/bg_card_focused.xml -->
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Shadow/Glow -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/focus_glow"/>
            <corners android:radius="@dimen/card_corner_radius"/>
        </shape>
    </item>
    
    <!-- Main card background -->
    <item
        android:left="3dp"
        android:top="3dp"
        android:right="3dp"
        android:bottom="3dp">
        <shape android:shape="rectangle">
            <solid android:color="@color/background_card"/>
            <corners android:radius="@dimen/card_corner_radius"/>
            <stroke
                android:width="@dimen/focus_border_width"
                android:color="@color/simpl_blue"/>
        </shape>
    </item>
</layer-list>

<!-- res/drawable/selector_card_background.xml -->
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_focused="true" android:drawable="@drawable/bg_card_focused"/>
    <item android:drawable="@drawable/bg_card_unfocused"/>
</selector>
```

## 10.5 Navigation Graph

```xml
<!-- res/navigation/nav_graph.xml -->
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/splashFragment">

    <!-- Splash -->
    <fragment
        android:id="@+id/splashFragment"
        android:name="com.simplstudios.simplstream.presentation.splash.SplashFragment"
        android:label="Splash">
        <action
            android:id="@+id/action_splash_to_pin"
            app:destination="@id/pinInputFragment"
            app:popUpTo="@id/splashFragment"
            app:popUpToInclusive="true"/>
        <action
            android:id="@+id/action_splash_to_profiles"
            app:destination="@id/profileSelectionFragment"
            app:popUpTo="@id/splashFragment"
            app:popUpToInclusive="true"/>
        <action
            android:id="@+id/action_splash_to_setup"
            app:destination="@id/profileEditorFragment"
            app:popUpTo="@id/splashFragment"
            app:popUpToInclusive="true"/>
    </fragment>

    <!-- PIN Entry -->
    <fragment
        android:id="@+id/pinInputFragment"
        android:name="com.simplstudios.simplstream.presentation.auth.PinInputFragment"
        android:label="Enter PIN">
        <action
            android:id="@+id/action_pin_to_profiles"
            app:destination="@id/profileSelectionFragment"
            app:popUpTo="@id/pinInputFragment"
            app:popUpToInclusive="true"/>
    </fragment>

    <!-- Profile Selection -->
    <fragment
        android:id="@+id/profileSelectionFragment"
        android:name="com.simplstudios.simplstream.presentation.profile.ProfileSelectionFragment"
        android:label="Who's Watching?">
        <action
            android:id="@+id/action_profiles_to_browse"
            app:destination="@id/browseFragment"/>
        <action
            android:id="@+id/action_profiles_to_editor"
            app:destination="@id/profileEditorFragment">
            <argument
                android:name="profileId"
                app:argType="string"
                app:nullable="true"/>
        </action>
    </fragment>

    <!-- Profile Editor -->
    <fragment
        android:id="@+id/profileEditorFragment"
        android:name="com.simplstudios.simplstream.presentation.profile.ProfileEditorFragment"
        android:label="Profile">
        <argument
            android:name="profileId"
            app:argType="string"
            app:nullable="true"
            android:defaultValue="@null"/>
    </fragment>

    <!-- Browse (Home) -->
    <fragment
        android:id="@+id/browseFragment"
        android:name="com.simplstudios.simplstream.presentation.browse.BrowseFragment"
        android:label="SimplStream">
        <action
            android:id="@+id/action_browse_to_search"
            app:destination="@id/searchFragment"/>
        <action
            android:id="@+id/action_browse_to_movieDetail"
            app:destination="@id/movieDetailFragment">
            <argument
                android:name="movieId"
                app:argType="integer"/>
        </action>
        <action
            android:id="@+id/action_browse_to_tvDetail"
            app:destination="@id/tvShowDetailFragment">
            <argument
                android:name="tvShowId"
                app:argType="integer"/>
        </action>
        <action
            android:id="@+id/action_browse_to_watchlist"
            app:destination="@id/watchlistFragment"/>
        <action
            android:id="@+id/action_browse_to_settings"
            app:destination="@id/settingsFragment"/>
        <action
            android:id="@+id/action_browse_to_profiles"
            app:destination="@id/profileSelectionFragment"
            app:popUpTo="@id/browseFragment"
            app:popUpToInclusive="true"/>
    </fragment>

    <!-- Search -->
    <fragment
        android:id="@+id/searchFragment"
        android:name="com.simplstudios.simplstream.presentation.search.SearchFragment"
        android:label="Search">
        <action
            android:id="@+id/action_search_to_movieDetail"
            app:destination="@id/movieDetailFragment"/>
        <action
            android:id="@+id/action_search_to_tvDetail"
            app:destination="@id/tvShowDetailFragment"/>
    </fragment>

    <!-- Movie Detail -->
    <fragment
        android:id="@+id/movieDetailFragment"
        android:name="com.simplstudios.simplstream.presentation.detail.MovieDetailFragment"
        android:label="Movie">
        <argument
            android:name="movieId"
            app:argType="integer"/>
        <action
            android:id="@+id/action_movieDetail_to_player"
            app:destination="@id/playerActivity"/>
    </fragment>

    <!-- TV Show Detail -->
    <fragment
        android:id="@+id/tvShowDetailFragment"
        android:name="com.simplstudios.simplstream.presentation.detail.TvShowDetailFragment"
        android:label="TV Show">
        <argument
            android:name="tvShowId"
            app:argType="integer"/>
        <action
            android:id="@+id/action_tvDetail_to_season"
            app:destination="@id/seasonDetailFragment">
            <argument
                android:name="tvShowId"
                app:argType="integer"/>
            <argument
                android:name="seasonNumber"
                app:argType="integer"/>
        </action>
    </fragment>

    <!-- Season Detail -->
    <fragment
        android:id="@+id/seasonDetailFragment"
        android:name="com.simplstudios.simplstream.presentation.detail.SeasonDetailFragment"
        android:label="Season">
        <argument
            android:name="tvShowId"
            app:argType="integer"/>
        <argument
            android:name="seasonNumber"
            app:argType="integer"/>
        <action
            android:id="@+id/action_season_to_player"
            app:destination="@id/playerActivity"/>
    </fragment>

    <!-- Watchlist -->
    <fragment
        android:id="@+id/watchlistFragment"
        android:name="com.simplstudios.simplstream.presentation.watchlist.WatchlistFragment"
        android:label="My List">
        <action
            android:id="@+id/action_watchlist_to_movieDetail"
            app:destination="@id/movieDetailFragment"/>
        <action
            android:id="@+id/action_watchlist_to_tvDetail"
            app:destination="@id/tvShowDetailFragment"/>
    </fragment>

    <!-- Settings -->
    <fragment
        android:id="@+id/settingsFragment"
        android:name="com.simplstudios.simplstream.presentation.settings.SettingsFragment"
        android:label="Settings"/>

    <!-- Player (Activity, not Fragment) -->
    <activity
        android:id="@+id/playerActivity"
        android:name="com.simplstudios.simplstream.presentation.player.PlayerActivity"
        android:label="Player">
        <argument
            android:name="contentId"
            app:argType="integer"/>
        <argument
            android:name="contentType"
            app:argType="string"/>
        <argument
            android:name="seasonNumber"
            app:argType="integer"
            android:defaultValue="-1"/>
        <argument
            android:name="episodeNumber"
            app:argType="integer"
            android:defaultValue="-1"/>
    </activity>

</navigation>
```

## 10.6 Remote Key Handling

```kotlin
// presentation/MainActivity.kt

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Global key handling
        return when (keyCode) {
            KeyEvent.KEYCODE_SEARCH -> {
                // Navigate to search
                navigateToSearch()
                true
            }
            KeyEvent.KEYCODE_MENU -> {
                // Show options menu / source selector
                showOptionsMenu()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    private fun navigateToSearch() {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.searchFragment)
    }
    
    private fun showOptionsMenu() {
        // Implementation depends on current screen
    }
}
```
