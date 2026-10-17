package com.example.ui.reels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import com.example.ui.components.InstagramFilters
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.audio.AudioPlayerManager
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.Song
import com.example.data.model.UserProfile
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.RepostDialog
import com.example.ui.theme.LikeRed
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.SecondaryPink

import kotlinx.coroutines.flow.StateFlow

@Composable
fun ReelsScreen(
    reels: List<Post>,
    currentUser: UserProfile,
    userLikes: Set<String>,
    userSaves: Set<String>,
    followingIds: Set<String>,
    onLikeReel: (Post) -> Unit,
    onRepostReel: (Post, String?) -> Unit,
    onSaveReel: (Post) -> Unit,
    onShareReel: (Post) -> Unit,
    onFollowCreator: (creatorId: String) -> Unit,
    onUserClick: (userId: String) -> Unit,
    onAddComment: (postId: String, postAuthorId: String, text: String, parentId: String?, replyTo: String?, parentAuthorId: String?) -> Unit,
    onDeleteComment: (postId: String, commentId: String) -> Unit,
    getCommentsFlow: (postId: String) -> StateFlow<List<Comment>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPlayingSong by AudioPlayerManager.currentPlayingSong.collectAsState()
    val isPlaying by AudioPlayerManager.isPlaying.collectAsState()

    var activeCommentPost by remember { mutableStateOf<Post?>(null) }
    var activeRepostPost by remember { mutableStateOf<Post?>(null) }

    if (reels.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Reels",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Reels uploaded yet",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap the + button to create the first Reel!",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            items(reels, key = { it.id }) { reel ->
                val isLiked = userLikes.contains("reel_${reel.id}") || userLikes.contains("post_${reel.id}")
                val isSaved = userSaves.contains(reel.id)
                val isFollowing = followingIds.contains(reel.authorId)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(680.dp)
                        .testTag("reel_item_${reel.id}")
                ) {
                    // Media background
                    AsyncImage(
                        model = reel.mediaUrl,
                        contentDescription = "Reel Content",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlays for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )

                    // Top Bar header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 36.dp, start = 20.dp, end = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reels",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Floating Right Actions
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Like
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { onLikeReel(reel) },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    .testTag("reel_like_button_${reel.id}")
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like Reel",
                                    tint = if (isLiked) LikeRed else Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = "${reel.likesCount}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Comment
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { activeCommentPost = reel },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    .testTag("reel_comment_button_${reel.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Comment Reel",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "${reel.commentsCount}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Repost
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { activeRepostPost = reel },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    .testTag("reel_repost_button_${reel.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Repost Reel",
                                    tint = if (reel.repostsCount > 0) RepostGreen else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "${reel.repostsCount}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share into DM
                        IconButton(
                            onClick = { onShareReel(reel) },
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .testTag("reel_share_button_${reel.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Share Reel",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Save bookmark
                        IconButton(
                            onClick = { onSaveReel(reel) },
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .testTag("reel_save_button_${reel.id}")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save Reel",
                                tint = if (isSaved) SecondaryPink else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Bottom Author & Caption Info
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.78f)
                            .padding(start = 16.dp, bottom = 28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val authorAvatar = reel.authorPhotoUrl.ifBlank {
                                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"
                            }
                            AsyncImage(
                                model = authorAvatar,
                                contentDescription = reel.authorUsername,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { onUserClick(reel.authorId) },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "@${reel.authorUsername}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.clickable { onUserClick(reel.authorId) }
                            )

                            if (reel.authorId != currentUser.id) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = { onFollowCreator(reel.authorId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) Color.White.copy(alpha = 0.25f) else SecondaryPink
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (isFollowing) "Following" else "Follow",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (reel.caption.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = reel.caption,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
                            )
                        }

                        val isThisReelPlaying = isPlaying && currentPlayingSong?.id == "reel_${reel.id}"
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(if (isThisReelPlaying) SecondaryPink.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val title = if (reel.songTitle.isNotBlank()) reel.songTitle else "Original Reel Audio"
                                    val artist = if (reel.songArtist.isNotBlank()) reel.songArtist else "@${reel.authorUsername}"
                                    AudioPlayerManager.playSong(
                                        context,
                                        Song(
                                            id = "reel_${reel.id}",
                                            title = title,
                                            artist = artist,
                                            coverUrl = reel.songCoverUrl.ifBlank { reel.mediaUrl },
                                            audioUrl = ""
                                        )
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isThisReelPlaying) Icons.Default.Pause else Icons.Default.MusicNote,
                                contentDescription = "Audio",
                                tint = if (isThisReelPlaying) Color.White else SecondaryPink,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val songName = if (reel.songTitle.isNotBlank()) "${reel.songTitle} • ${reel.songArtist}"
                            else "Original Audio • @${reel.authorUsername}"
                            Text(
                                text = if (isThisReelPlaying) "▶ $songName" else songName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // Comment Sheet
    val currentCommentPost = activeCommentPost
    if (currentCommentPost != null) {
        val commentsList by getCommentsFlow(currentCommentPost.id).collectAsState()
        CommentBottomSheet(
            comments = commentsList,
            currentUser = currentUser,
            onDismiss = { activeCommentPost = null },
            onAddComment = { text, parentId, replyTo, parentAuthorId ->
                onAddComment(currentCommentPost.id, currentCommentPost.authorId, text, parentId, replyTo, parentAuthorId)
            },
            onDeleteComment = { commentId ->
                onDeleteComment(currentCommentPost.id, commentId)
            }
        )
    }

    // Repost Dialog
    val currentRepostPost = activeRepostPost
    if (currentRepostPost != null) {
        RepostDialog(
            post = currentRepostPost,
            onDismiss = { activeRepostPost = null },
            onConfirmRepost = { comment ->
                onRepostReel(currentRepostPost, comment)
            }
        )
    }
}
