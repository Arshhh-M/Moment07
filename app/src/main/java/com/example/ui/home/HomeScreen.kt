package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalContext
import com.example.data.audio.AudioPlayerManager
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.Song
import com.example.data.model.Story
import com.example.data.model.UserNote
import com.example.data.model.UserProfile
import com.example.ui.components.*
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryPink

import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<Post>,
    stories: List<Story>,
    notes: List<UserNote> = emptyList(),
    currentUser: UserProfile,
    userLikes: Set<String>,
    userSaves: Set<String>,
    followingIds: Set<String>,
    unreadMessagesCount: Int,
    unreadNotificationsCount: Int,
    onLikePost: (Post) -> Unit,
    onRepostPost: (Post, String?) -> Unit,
    onSavePost: (Post) -> Unit,
    onDeletePost: (Post) -> Unit,
    onAddComment: (postId: String, postAuthorId: String, text: String, parentId: String?, replyTo: String?, parentAuthorId: String?) -> Unit,
    onDeleteComment: (postId: String, commentId: String) -> Unit,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Story) -> Unit,
    onSaveNote: (text: String, song: Song?) -> Unit = { _, _ -> },
    onDeleteNote: () -> Unit = {},
    onSharePostToDm: (Post) -> Unit,
    onOpenMessages: () -> Unit,
    onOpenNotifications: () -> Unit,
    onUserClick: (userId: String) -> Unit,
    getCommentsFlow: (postId: String) -> StateFlow<List<Comment>>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = For You, 1 = Following
    var activeCommentPost by remember { mutableStateOf<Post?>(null) }
    var activeRepostPost by remember { mutableStateOf<Post?>(null) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var isBrowsingMusicModalOpen by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, selectedTab, followingIds) {
        if (selectedTab == 1) {
            posts.filter { followingIds.contains(it.authorId) || it.authorId == currentUser.id }
        } else {
            posts
        }
    }

    // Automatic music playback synchronization across the app's feed:
    // When user scrolls through the feed, smoothly transition track context to the visible post's song
    LaunchedEffect(listState, filteredPosts) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstIndex ->
                // Stories is index 0, TabRow is index 1, Posts start at index 2
                val postIndex = firstIndex - 2
                if (postIndex in filteredPosts.indices) {
                    val currentPost = filteredPosts[postIndex]
                    if (currentPost.songTitle.isNotBlank()) {
                        val activeSong = AudioPlayerManager.currentPlayingSong.value
                        val postSongId = "post_${currentPost.id}"
                        // If already playing audio, smoothly transition context without interrupting rhythm
                        if (activeSong != null && activeSong.id != postSongId && AudioPlayerManager.isPlaying.value) {
                            AudioPlayerManager.playSong(
                                context,
                                Song(
                                    id = postSongId,
                                    title = currentPost.songTitle,
                                    artist = currentPost.songArtist,
                                    coverUrl = currentPost.songCoverUrl.ifBlank { currentPost.mediaUrl }
                                )
                            )
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryViolet, SecondaryPink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "X",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SocialX",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Notifications icon with badge
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.testTag("unread_notif_badge")
                                ) {
                                    Text(if (unreadNotificationsCount > 9) "9+" else "$unreadNotificationsCount")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.testTag("home_notif_button")
                        ) {
                            Icon(
                                imageVector = if (unreadNotificationsCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }

                    // Messages icon with badge
                    BadgedBox(
                        badge = {
                            if (unreadMessagesCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("unread_msg_badge")
                                ) {
                                    Text(if (unreadMessagesCount > 9) "9+" else "$unreadMessagesCount")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenMessages,
                            modifier = Modifier.testTag("home_messages_button")
                        ) {
                            Icon(
                                imageVector = if (unreadMessagesCount > 0) Icons.Filled.Chat else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Messages"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 76.dp)
            ) {
                // Stories Row
                item {
                    StoryBar(
                        stories = stories,
                        currentUser = currentUser,
                        onAddStoryClick = onAddStoryClick,
                        onStoryClick = onStoryClick
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }

                // Feed Tabs (For You vs Following)
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "For You",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Following (${followingIds.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Empty state
                if (filteredPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedTab == 1) "No posts from accounts you follow yet." else "No posts yet. Be the first to share!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = onAddStoryClick) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Post or Story")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredPosts, key = { it.id }) { post ->
                        val isLiked = userLikes.contains("${post.type}_${post.id}") || userLikes.contains("post_${post.id}")
                        val isSaved = userSaves.contains(post.id)

                        PostCard(
                            post = post,
                            currentUser = currentUser,
                            isLiked = isLiked,
                            isSaved = isSaved,
                            onLikeClick = { onLikePost(post) },
                            onCommentClick = { activeCommentPost = post },
                            onRepostClick = { activeRepostPost = post },
                            onShareClick = { onSharePostToDm(post) },
                            onSaveClick = { onSavePost(post) },
                            onUserClick = onUserClick,
                            onDeleteClick = if (post.authorId == currentUser.id) { { onDeletePost(post) } } else null
                        )
                    }
                }
            }

            // Docked Mini Music Player on Home Feed
            MiniMusicPlayer(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                onOpenSongPicker = { isBrowsingMusicModalOpen = true }
            )
        }
    }

    if (isBrowsingMusicModalOpen) {
        SongPickerModal(
            onDismiss = { isBrowsingMusicModalOpen = false },
            onSongSelected = { song ->
                AudioPlayerManager.playSong(context, song)
                isBrowsingMusicModalOpen = false
            }
        )
    }

    // Comment Sheet Dialog
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
                onRepostPost(currentRepostPost, comment)
            }
        )
    }
}
