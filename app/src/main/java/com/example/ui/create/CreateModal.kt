package com.example.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Story
import com.example.data.model.UserProfile
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
    var customMediaUrl by remember { mutableStateOf("") }

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

    var selectedMedia by remember { mutableStateOf(presetImages[0]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create on SocialX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                                if (type == "reel") {
                                    selectedMedia = presetReels[0]
                                } else {
                                    selectedMedia = presetImages[0]
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

            Spacer(modifier = Modifier.height(16.dp))

            // Caption input
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = {
                    Text(
                        when (selectedType) {
                            "reel" -> "Reel caption (use #hashtags and @mentions)..."
                            "story" -> "Story text overlay..."
                            else -> "What's on your mind? #tag @mention..."
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_caption_input"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Select Media:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Presets carousel
            val mediaList = if (selectedType == "reel") presetReels else presetImages
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mediaList) { url ->
                    val isPicked = selectedMedia == url && customMediaUrl.isBlank()
                    Box(
                        modifier = Modifier
                            .size(if (selectedType == "reel") 75.dp else 70.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isPicked) 3.dp else 1.dp,
                                color = if (isPicked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedMedia = url
                                customMediaUrl = ""
                            }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Media option",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom media URL option
            OutlinedTextField(
                value = customMediaUrl,
                onValueChange = { customMediaUrl = it },
                label = { Text("Or enter custom image/video URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Preview box
            val currentMedia = customMediaUrl.ifBlank { selectedMedia }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = currentMedia,
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "PREVIEW",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    val finalMedia = customMediaUrl.ifBlank { selectedMedia }
                    val hashtags = Regex("#([a-zA-Z0-9_]+)").findAll(caption).map { it.groupValues[1] }.toList()
                    val mentions = Regex("@([a-zA-Z0-9_]+)").findAll(caption).map { it.groupValues[1] }.toList()

                    if (selectedType == "story") {
                        val story = Story(
                            authorId = currentUser.id,
                            authorUsername = currentUser.username,
                            authorDisplayName = currentUser.displayName,
                            authorPhotoUrl = currentUser.photoUrl,
                            mediaUrl = finalMedia,
                            caption = caption.trim(),
                            mentions = mentions
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
                            mediaUrl = finalMedia,
                            mediaType = if (selectedType == "reel") "video" else "image",
                            hashtags = hashtags,
                            mentions = mentions
                        )
                        onCreatePost(post)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_content_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "reel") SecondaryPink else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = when (selectedType) {
                        "reel" -> Icons.Default.Movie
                        "story" -> Icons.Default.AddCircle
                        else -> Icons.Default.Send
                    },
                    contentDescription = "Publish"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedType) {
                        "reel" -> "Share Reel"
                        "story" -> "Share to Story"
                        else -> "Publish Post"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
