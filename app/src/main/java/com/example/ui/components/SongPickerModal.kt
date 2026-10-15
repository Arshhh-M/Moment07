package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.audio.AudioPlayerManager
import com.example.data.model.Song
import com.example.data.music.MusicCatalog
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPickerModal(
    onDismiss: () -> Unit,
    onSongSelected: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTopTab by remember { mutableIntStateOf(0) } // 0: 1M+ Library, 1: Upload Music
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }

    val currentPlayingSong by AudioPlayerManager.currentPlayingSong.collectAsState()
    val isPlaying by AudioPlayerManager.isPlaying.collectAsState()

    // Upload section state
    var uploadTitle by remember { mutableStateOf("") }
    var uploadArtist by remember { mutableStateOf("") }
    var uploadGenre by remember { mutableStateOf("Trending") }
    var uploadAudioUri by remember { mutableStateOf<Uri?>(null) }
    var uploadSuccessMessage by remember { mutableStateOf<String?>(null) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadAudioUri = uri
        }
    }

    val songs = remember(searchQuery, selectedGenre, MusicCatalog.uploadedSongs.size) {
        MusicCatalog.searchSongs(searchQuery, selectedGenre)
    }

    ModalBottomSheet(
        onDismissRequest = {
            AudioPlayerManager.pause()
            onDismiss()
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryViolet.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = PrimaryViolet,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Music & Audio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "1,000,000+ tracks & custom audio upload",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = {
                    AudioPlayerManager.pause()
                    onDismiss()
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs (1M+ Library vs Upload Music)
            TabRow(
                selectedTabIndex = selectedTopTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryViolet
            ) {
                Tab(
                    selected = selectedTopTab == 0,
                    onClick = { selectedTopTab = 0 },
                    text = { Text("1M+ Music Catalog", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTopTab == 1,
                    onClick = { selectedTopTab = 1 },
                    text = { Text("Upload Audio", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTopTab == 0) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 1M+ songs, artists, Spotify hits...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_search_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Genre Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MusicCatalog.GENRES) { genre ->
                        val isSelected = selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryViolet,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Song List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        val isThisPlaying = currentPlayingSong?.id == song.id && isPlaying
                        SongRowItem(
                            song = song,
                            isPlaying = isThisPlaying,
                            onTogglePlay = {
                                AudioPlayerManager.playSong(context, song)
                            },
                            onSelect = {
                                onSongSelected(song)
                                onDismiss()
                            }
                        )
                    }
                }
            } else {
                // Upload Custom Audio Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Upload Your Own Music / Audio Track",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add your original soundtrack, remix, voice note, or local music file to use in your Stories, Reels, and Posts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("Track / Song Title *") },
                        placeholder = { Text("e.g. Midnight Waves, My Acoustic Intro") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uploadArtist,
                        onValueChange = { uploadArtist = it },
                        label = { Text("Artist / Creator Name *") },
                        placeholder = { Text("e.g. Your Name or Band") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Audio file selector button
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { audioPickerLauncher.launch("audio/*") }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uploadAudioUri != null) Icons.Default.CheckCircle else Icons.Default.AudioFile,
                                    contentDescription = null,
                                    tint = PrimaryViolet
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uploadAudioUri != null) "Audio File Selected ✓" else "Choose Audio File (.mp3, .wav, .m4a)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = uploadAudioUri?.lastPathSegment ?: "Tap to browse local device audio files",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (uploadSuccessMessage != null) {
                        Text(
                            text = uploadSuccessMessage!!,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            if (uploadTitle.isNotBlank()) {
                                val newSong = Song(
                                    id = "custom_${System.currentTimeMillis()}",
                                    title = uploadTitle.trim(),
                                    artist = uploadArtist.ifBlank { "Original Creator" }.trim(),
                                    album = "User Uploads",
                                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300&fit=crop",
                                    genre = uploadGenre,
                                    durationSeconds = 180,
                                    audioUrl = uploadAudioUri?.toString() ?: ""
                                )
                                MusicCatalog.addCustomSong(newSong)
                                uploadSuccessMessage = "Track \"${newSong.title}\" added to your catalog!"
                                onSongSelected(newSong)
                                onDismiss()
                            }
                        },
                        enabled = uploadTitle.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add & Select Track", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRowItem(
    song: Song,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSelect: () -> Unit
) {
    Surface(
        color = if (isPlaying) PrimaryViolet.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art with real play button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play / Pause Overlay with real audio playback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isPlaying) 0.55f else 0.3f))
                        .clickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) SecondaryPink else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isPlaying) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "▶ Playing",
                            color = SecondaryPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${song.artist} • ${song.album.ifBlank { song.genre }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${song.durationSeconds / 60}:${String.format("%02d", song.durationSeconds % 60)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${song.genre}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onSelect,
                modifier = Modifier.testTag("select_song_${song.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Use Song",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
