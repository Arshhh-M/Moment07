package com.example.ui.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.audio.AudioPlayerManager
import com.example.data.model.Song
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun StoryBar(
    stories: List<Story>,
    currentUser: UserProfile,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Story) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Current user add story
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable { onAddStoryClick() }
                    .testTag("add_story_button")
            ) {
                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val photo = if (currentUser.photoUrl.isNotBlank()) currentUser.photoUrl
                    else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop"

                    AsyncImage(
                        model = photo,
                        contentDescription = "My Story",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Story",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active stories
        items(stories, key = { it.id }) { story ->
            val hasViewed = story.viewers.contains(currentUser.id)
            val ringBrush = if (hasViewed) {
                Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.3f)))
            } else {
                Brush.linearGradient(listOf(StoryGradientStart, StoryGradientMiddle, StoryGradientEnd))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable { onStoryClick(story) }
                    .testTag("story_avatar_${story.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, ringBrush, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val avatar = if (story.authorPhotoUrl.isNotBlank()) story.authorPhotoUrl
                    else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop"

                    AsyncImage(
                        model = avatar,
                        contentDescription = story.authorUsername,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.authorUsername,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StoryViewerDialog(
    story: Story,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onLikeStory: () -> Unit,
    onReplyStory: (reply: String) -> Unit,
    onAddToHighlight: ((story: Story, name: String) -> Unit)? = null
) {
    var replyText by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }
    var showHighlightDialog by remember { mutableStateOf(false) }
    var highlightName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val currentPlayingSong by AudioPlayerManager.currentPlayingSong.collectAsState()
    val isPlaying by AudioPlayerManager.isPlaying.collectAsState()

    // Automatically play the story's music if attached
    LaunchedEffect(story.id, story.songTitle) {
        if (story.songTitle.isNotBlank()) {
            AudioPlayerManager.playSong(
                context,
                Song(
                    id = "story_${story.id}",
                    title = story.songTitle,
                    artist = story.songArtist,
                    coverUrl = story.songCoverUrl.ifBlank { story.mediaUrl },
                    audioUrl = ""
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioPlayerManager.stop()
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Story Media
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = "Story Media",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp, start = 16.dp, end = 16.dp)
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = story.authorPhotoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop" },
                        contentDescription = "Author",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.authorUsername,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatTimestamp(story.createdAt?.toDate()),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    // Viewers count badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Viewers",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${story.viewers.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (story.authorId == currentUser.id && onAddToHighlight != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SecondaryPink,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable {
                                highlightName = story.caption.ifBlank { "Vibes ✨" }
                                showHighlightDialog = true
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Highlight",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Highlight",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Music audio sticker if story has song attached
                if (story.songTitle.isNotBlank()) {
                    val isStoryPlaying = isPlaying && currentPlayingSong?.id == "story_${story.id}"
                    Surface(
                        color = if (isStoryPlaying) SecondaryPink.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable {
                                AudioPlayerManager.playSong(
                                    context,
                                    Song(
                                        id = "story_${story.id}",
                                        title = story.songTitle,
                                        artist = story.songArtist,
                                        coverUrl = story.songCoverUrl.ifBlank { story.mediaUrl },
                                        audioUrl = ""
                                    )
                                )
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isStoryPlaying) Icons.Default.Pause else Icons.Default.MusicNote,
                                contentDescription = "Song",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isStoryPlaying) "▶ ${story.songTitle} • ${story.songArtist}" else "${story.songTitle} • ${story.songArtist}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Caption overlay if exists
            if (story.caption.isNotBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = story.caption,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Bottom reply bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Send reply to @${story.authorUsername}...", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onReplyStory(replyText.trim())
                            replyText = ""
                            onDismiss()
                        }
                    },
                    enabled = replyText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send reply",
                        tint = if (replyText.isNotBlank()) Color.White else Color.Gray
                    )
                }

                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        onLikeStory()
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Story",
                        tint = if (isLiked) LikeRed else Color.White
                    )
                }
            }
        }
    }

    if (showHighlightDialog) {
        AlertDialog(
            onDismissRequest = { showHighlightDialog = false },
            title = { Text("Add Story to Highlights", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter a name for this Story Highlight:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = highlightName,
                        onValueChange = { highlightName = it },
                        label = { Text("Highlight Name *") },
                        placeholder = { Text("e.g. Vibes ✨, Goa 🏖️, Memories ❤️") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (highlightName.isNotBlank()) {
                            onAddToHighlight?.invoke(story, highlightName.trim())
                            showHighlightDialog = false
                        }
                    },
                    enabled = highlightName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add to Highlights")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHighlightDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
