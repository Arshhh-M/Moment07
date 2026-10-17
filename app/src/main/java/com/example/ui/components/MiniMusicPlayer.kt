package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.audio.AudioPlayerManager
import com.example.data.model.Song
import com.example.data.music.MusicCatalog
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryPink

/**
 * Mini Music Player Control Component.
 * Integrates into the Home Feed, displays current playing track with real-time seeking,
 * play/pause, next/previous controls, and expandable full-screen player.
 */
@Composable
fun MiniMusicPlayer(
    modifier: Modifier = Modifier,
    onOpenSongPicker: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentSong by AudioPlayerManager.currentPlayingSong.collectAsState()
    val isPlaying by AudioPlayerManager.isPlaying.collectAsState()
    val progress by AudioPlayerManager.playbackProgress.collectAsState()
    val currentSec by AudioPlayerManager.currentPositionSeconds.collectAsState()
    val totalSec by AudioPlayerManager.totalDurationSeconds.collectAsState()

    var isExpandedModalOpen by remember { mutableStateOf(false) }

    // If no song is loaded yet, pull default top hit from MusicCatalog ready to play
    val displaySong = currentSong ?: MusicCatalog.TRENDING_SONGS.firstOrNull() ?: Song(
        id = "default_1",
        title = "Espresso",
        artist = "Sabrina Carpenter",
        album = "Top Hits",
        coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&fit=crop",
        durationSeconds = 175
    )

    // Rotating vinyl animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpandedModalOpen = true }
            .testTag("mini_music_player"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Seek bar indicator (thin progress line at top of mini player)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = PrimaryViolet,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vinyl / Album Art
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = displaySong.coverUrl.ifBlank { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&fit=crop" },
                        contentDescription = displaySong.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (isPlaying) rotation else 0f),
                        contentScale = ContentScale.Crop
                    )
                    // Center vinyl hole
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song Info & Time
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpandedModalOpen = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displaySong.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isPlaying) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SecondaryPink.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PLAYING",
                                    color = SecondaryPink,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${displaySong.artist} • ${formatSeconds(currentSec)} / ${formatSeconds(totalSec)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controls: Previous, Play/Pause, Next
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { AudioPlayerManager.playPrevious(context) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        color = PrimaryViolet,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable {
                                if (currentSong == null) {
                                    AudioPlayerManager.playSong(context, displaySong)
                                } else {
                                    AudioPlayerManager.togglePlayPause(context)
                                }
                            }
                            .testTag("mini_player_play_pause")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { AudioPlayerManager.playNext(context) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Expanded Full-Screen Music Player Modal
    if (isExpandedModalOpen) {
        ExpandedMusicPlayerModal(
            song = displaySong,
            isPlaying = isPlaying,
            progress = progress,
            currentSec = currentSec,
            totalSec = totalSec,
            onDismiss = { isExpandedModalOpen = false },
            onTogglePlay = {
                if (currentSong == null) {
                    AudioPlayerManager.playSong(context, displaySong)
                } else {
                    AudioPlayerManager.togglePlayPause(context)
                }
            },
            onSeek = { AudioPlayerManager.seekTo(it) },
            onNext = { AudioPlayerManager.playNext(context) },
            onPrev = { AudioPlayerManager.playPrevious(context) },
            onBrowseLibrary = {
                isExpandedModalOpen = false
                onOpenSongPicker?.invoke()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedMusicPlayerModal(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    currentSec: Int,
    totalSec: Int,
    onDismiss: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onBrowseLibrary: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Collapse")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM SPOTIFY & CATALOG",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = song.genre,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onBrowseLibrary) {
                    Icon(imageVector = Icons.Default.LibraryMusic, contentDescription = "Catalog")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large Album Art Cover
            Surface(
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = song.coverUrl.ifBlank { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&fit=crop" },
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Song Title & Artist
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${song.artist} • ${song.album.ifBlank { "Single" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Progress / Seek Slider
            Slider(
                value = progress,
                onValueChange = { onSeek(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("music_seek_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryViolet,
                    activeTrackColor = PrimaryViolet,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            )

            // Time stamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatSeconds(currentSec),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatSeconds(totalSec),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev, modifier = Modifier.size(54.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(34.dp)
                    )
                }

                Surface(
                    color = PrimaryViolet,
                    shape = CircleShape,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(68.dp)
                        .clickable { onTogglePlay() }
                        .testTag("expanded_player_play_pause")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(onClick = onNext, modifier = Modifier.size(54.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Browse Full 1M+ Songs button
            OutlinedButton(
                onClick = onBrowseLibrary,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse 1M+ Songs & Upload Tracks")
            }
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
