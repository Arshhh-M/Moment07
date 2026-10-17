package com.example.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Song
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.ui.components.SongPickerModal
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModal(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onCreatePost: (post: Post) -> Unit,
    onCreateStory: (story: Story) -> Unit
) {
    var selectedType by remember { mutableStateOf("post") } // "post", "reel", "story"
    var caption by remember { mutableStateOf("") }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var isPickingSong by remember { mutableStateOf(false) }

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&fit=crop", // abstract violet
        "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&fit=crop", // vibrant art
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&fit=crop", // landscape
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&fit=crop", // portrait
        "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&fit=crop"  // space neon
    )

    val presetReels = listOf(
        "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800&fit=crop", // concert lights
        "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&fit=crop", // glowing wave
        "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800&fit=crop"  // city skyline
    )

    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var fallbackMedia by remember { mutableStateOf(presetImages[0]) }

    // Official Android Photo and Video Picker (No URL input needed!)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
        }
    }

    val activeMedia = selectedMediaUri ?: fallbackMedia

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New ${when (selectedType) { "reel" -> "Reel"; "story" -> "Story"; else -> "Post" }}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selector Tabs: Post, Reel, Story
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    Triple("post", "Post", Icons.Default.Image),
                    Triple("reel", "Reel", Icons.Default.Movie),
                    Triple("story", "Story", Icons.Default.AddCircle)
                ).forEach { (type, label, icon) ->
                    val isSelected = selectedType == type
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedType = type
                                if (selectedMediaUri == null) {
                                    fallbackMedia = if (type == "reel") presetReels[0] else presetImages[0]
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Gallery Picker Button (Opens device gallery directly)
            Button(
                onClick = {
                    val mediaType = if (selectedType == "reel") {
                        ActivityResultContracts.PickVisualMedia.VideoOnly
                    } else {
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    }
                    mediaPickerLauncher.launch(PickVisualMediaRequest(mediaType))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("open_gallery_picker_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = if (selectedType == "reel") Icons.Default.VideoLibrary else Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedMediaUri != null) "Change Photo/Video from Gallery" else "Choose from Gallery",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gallery selection status
            if (selectedMediaUri != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Media selected from your Gallery ✓",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val mediaType = if (selectedType == "reel") {
                                ActivityResultContracts.PickVisualMedia.VideoOnly
                            } else {
                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            }
                            mediaPickerLauncher.launch(PickVisualMediaRequest(mediaType))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Select from Device Gallery",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (selectedType == "reel") "Choose your video file" else "Choose photos or videos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caption input
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = {
                    Text(
                        when (selectedType) {
                            "reel" -> "Write a caption #hashtags @mention..."
                            "story" -> "Add text for your story..."
                            else -> "What's on your mind? #tag @mention..."
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_caption_input"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Attached Music Badge / Button
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPickingSong = true }
                    .testTag("create_add_music_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PrimaryViolet, SecondaryPink))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedSong != null) selectedSong!!.title else "Add Music (1M+ Songs & Uploads)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (selectedSong != null) "${selectedSong!!.artist} • ${selectedSong!!.genre}" else "Spotify hits, Bollywood, Lo-Fi, or your own audio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (selectedSong != null) {
                        IconButton(onClick = { selectedSong = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove music")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select song",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Media Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = activeMedia,
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (selectedMediaUri != null) "Selected from Gallery ✓" else "Sample Preview",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (selectedSong != null) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = SecondaryPink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${selectedSong!!.title} • ${selectedSong!!.artist}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share / Publish Button
            Button(
                onClick = {
                    val mediaToUse = activeMedia
                    if (selectedType == "story") {
                        val story = Story(
                            authorId = currentUser.id,
                            authorUsername = currentUser.username,
                            authorPhotoUrl = currentUser.photoUrl,
                            mediaUrl = mediaToUse,
                            caption = caption.trim(),
                            songTitle = selectedSong?.title ?: "",
                            songArtist = selectedSong?.artist ?: "",
                            songCoverUrl = selectedSong?.coverUrl ?: ""
                        )
                        onCreateStory(story)
                    } else {
                        val post = Post(
                            authorId = currentUser.id,
                            authorUsername = currentUser.username,
                            authorDisplayName = currentUser.displayName,
                            authorPhotoUrl = currentUser.photoUrl,
                            type = selectedType,
                            caption = caption.trim(),
                            mediaUrl = mediaToUse,
                            songTitle = selectedSong?.title ?: "",
                            songArtist = selectedSong?.artist ?: "",
                            songCoverUrl = selectedSong?.coverUrl ?: ""
                        )
                        onCreatePost(post)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_post_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = when (selectedType) {
                        "reel" -> "Share Reel"
                        "story" -> "Share to Your Story"
                        else -> "Share Post"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (isPickingSong) {
        SongPickerModal(
            onDismiss = { isPickingSong = false },
            onSongSelected = { song ->
                selectedSong = song
                isPickingSong = false
            }
        )
    }
}
