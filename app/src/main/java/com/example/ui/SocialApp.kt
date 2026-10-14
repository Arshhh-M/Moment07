package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Post
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.ui.components.ShareToDmSheet
import com.example.ui.components.StoryViewerDialog
import com.example.ui.create.CreateModal
import com.example.ui.home.HomeScreen
import com.example.ui.messages.ChatDetailScreen
import com.example.ui.messages.MessagesScreen
import com.example.ui.notifications.NotificationsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.reels.ReelsScreen
import com.example.ui.search.SearchExploreScreen

enum class NavScreen(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search),
    CREATE("Create", Icons.Filled.AddBox, Icons.Outlined.AddBox),
    REELS("Reels", Icons.Filled.Movie, Icons.Outlined.Movie),
    MESSAGES("Chat", Icons.Filled.Chat, Icons.Outlined.ChatBubbleOutline),
    NOTIFICATIONS("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun SocialApp(
    viewModel: SocialViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentNavScreen by remember { mutableStateOf(NavScreen.HOME) }
    var activeChatRecipient by remember { mutableStateOf<UserProfile?>(null) }
    var activeChatConversationId by remember { mutableStateOf<String?>(null) }
    var inspectedUserId by remember { mutableStateOf<String?>(null) }
    var activeViewingStory by remember { mutableStateOf<Story?>(null) }
    var sharingPostToDm by remember { mutableStateOf<Post?>(null) }
    var isCreateModalOpen by remember { mutableStateOf(false) }

    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val feedPosts by viewModel.feedPosts.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val userLikes by viewModel.userLikes.collectAsState()
    val userSaves by viewModel.userSaves.collectAsState()
    val followingIds by viewModel.followingIds.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val unreadMessagesCount by viewModel.unreadMessagesCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()

    val profile = currentUserProfile ?: UserProfile(id = viewModel.currentUserId, username = "loading")

    // Handle back button when inside secondary sub-screens
    if (activeChatConversationId != null) {
        val recipient = activeChatRecipient ?: UserProfile()
        val isRecipientTyping = remember(conversations, recipient.id) {
            val conv = conversations.find { it.id == activeChatConversationId }
            conv?.typingUserIds?.contains(recipient.id) == true
        }

        ChatDetailScreen(
            conversationId = activeChatConversationId!!,
            recipient = recipient,
            currentUser = profile,
            messages = activeChatMessages,
            isRecipientTyping = isRecipientTyping,
            onBack = {
                activeChatConversationId = null
                activeChatRecipient = null
                viewModel.setActiveConversation(null)
            },
            onSendMessage = { text ->
                viewModel.sendMessage(activeChatConversationId!!, recipient.id, text)
            },
            onTypingChanged = { isTyping ->
                viewModel.setTyping(activeChatConversationId!!, isTyping)
            }
        )
        return
    }

    if (inspectedUserId != null && inspectedUserId != profile.id) {
        BackHandler { inspectedUserId = null }
        val targetUser = allUsers.find { it.id == inspectedUserId } ?: UserProfile(id = inspectedUserId!!)
        val targetUserPosts = feedPosts.filter { it.authorId == inspectedUserId }

        ProfileScreen(
            profileUser = targetUser,
            currentUser = profile,
            userPosts = targetUserPosts,
            savedPosts = emptyList(),
            isFollowing = followingIds.contains(inspectedUserId),
            onToggleFollow = { viewModel.toggleFollow(inspectedUserId!!) },
            onSendMessage = {
                val other = targetUser
                currentNavScreen = NavScreen.MESSAGES
                inspectedUserId = null
                // Open conversation
                val convId = listOf(profile.id, other.id).sorted().joinToString("_")
                activeChatConversationId = convId
                activeChatRecipient = other
                viewModel.setActiveConversation(convId)
            },
            onUpdateProfile = { _, _, _ -> },
            onSignOut = onSignOut,
            onPostClick = {},
            onBack = { inspectedUserId = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavScreen.values().forEach { screen ->
                    val isSelected = currentNavScreen == screen
                    val badgeCount = when (screen) {
                        NavScreen.MESSAGES -> unreadMessagesCount
                        NavScreen.NOTIFICATIONS -> unreadNotificationsCount
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (screen == NavScreen.CREATE) {
                                isCreateModalOpen = true
                            } else {
                                currentNavScreen = screen
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (badgeCount > 0) {
                                        Badge {
                                            Text(if (badgeCount > 9) "9+" else "$badgeCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNavScreen) {
                NavScreen.HOME -> {
                    HomeScreen(
                        posts = feedPosts,
                        stories = stories,
                        notes = notes,
                        currentUser = profile,
                        userLikes = userLikes,
                        userSaves = userSaves,
                        followingIds = followingIds,
                        unreadMessagesCount = unreadMessagesCount,
                        unreadNotificationsCount = unreadNotificationsCount,
                        onLikePost = { viewModel.toggleLike(it) },
                        onRepostPost = { post, quote -> viewModel.repost(post, quote) },
                        onSavePost = { viewModel.toggleSave(it) },
                        onDeletePost = { viewModel.deletePost(it) },
                        onAddComment = { postId, postAuthorId, text, parentId, replyTo, parentAuthorId ->
                            viewModel.addComment(postId, postAuthorId, text, parentId, replyTo, parentAuthorId)
                        },
                        onDeleteComment = { postId, commentId ->
                            viewModel.deleteComment(postId, commentId)
                        },
                        onAddStoryClick = { isCreateModalOpen = true },
                        onStoryClick = { story ->
                            activeViewingStory = story
                            viewModel.viewStory(story.id)
                        },
                        onSaveNote = { text, song ->
                            viewModel.saveNote(text, song?.title ?: "", song?.artist ?: "", song?.coverUrl ?: "")
                        },
                        onDeleteNote = { viewModel.deleteNote() },
                        onSharePostToDm = { post -> sharingPostToDm = post },
                        onOpenMessages = { currentNavScreen = NavScreen.MESSAGES },
                        onOpenNotifications = { currentNavScreen = NavScreen.NOTIFICATIONS },
                        onUserClick = { userId -> inspectedUserId = userId },
                        getCommentsFlow = { postId -> viewModel.getCommentsForPost(postId) }
                    )
                }

                NavScreen.SEARCH -> {
                    SearchExploreScreen(
                        users = allUsers,
                        posts = feedPosts,
                        currentUser = profile,
                        followingIds = followingIds,
                        onToggleFollow = { viewModel.toggleFollow(it) },
                        onUserClick = { userId -> inspectedUserId = userId },
                        onPostClick = {}
                    )
                }

                NavScreen.CREATE -> {
                    // Handled via modal, fallback to Home
                }

                NavScreen.REELS -> {
                    ReelsScreen(
                        reels = reels,
                        currentUser = profile,
                        userLikes = userLikes,
                        userSaves = userSaves,
                        followingIds = followingIds,
                        onLikeReel = { viewModel.toggleLike(it) },
                        onRepostReel = { post, quote -> viewModel.repost(post, quote) },
                        onSaveReel = { viewModel.toggleSave(it) },
                        onShareReel = { post -> sharingPostToDm = post },
                        onFollowCreator = { creatorId -> viewModel.toggleFollow(creatorId) },
                        onUserClick = { userId -> inspectedUserId = userId },
                        onAddComment = { postId, postAuthorId, text, parentId, replyTo, parentAuthorId ->
                            viewModel.addComment(postId, postAuthorId, text, parentId, replyTo, parentAuthorId)
                        },
                        onDeleteComment = { postId, commentId ->
                            viewModel.deleteComment(postId, commentId)
                        },
                        getCommentsFlow = { postId -> viewModel.getCommentsForPost(postId) }
                    )
                }

                NavScreen.MESSAGES -> {
                    MessagesScreen(
                        conversations = conversations,
                        allUsers = allUsers,
                        currentUser = profile,
                        notes = notes,
                        onSaveNote = { text, song ->
                            viewModel.saveNote(text, song?.title ?: "", song?.artist ?: "", song?.coverUrl ?: "")
                        },
                        onDeleteNote = { viewModel.deleteNote() },
                        onOpenConversation = { convId, otherUser ->
                            activeChatConversationId = convId
                            activeChatRecipient = otherUser
                            viewModel.setActiveConversation(convId)
                        },
                        onStartNewChat = { otherUser ->
                            val convId = listOf(profile.id, otherUser.id).sorted().joinToString("_")
                            activeChatConversationId = convId
                            activeChatRecipient = otherUser
                            viewModel.setActiveConversation(convId)
                        }
                    )
                }

                NavScreen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onNotificationClick = { notif ->
                            viewModel.markNotificationRead(notif.id)
                        },
                        onMarkAllAsRead = { viewModel.markAllNotificationsRead() },
                        onUserClick = { userId -> inspectedUserId = userId }
                    )
                }

                NavScreen.PROFILE -> {
                    val myPosts = feedPosts.filter { it.authorId == profile.id }
                    val mySavedPosts = feedPosts.filter { userSaves.contains(it.id) }

                    ProfileScreen(
                        profileUser = profile,
                        currentUser = profile,
                        userPosts = myPosts,
                        savedPosts = mySavedPosts,
                        isFollowing = false,
                        onToggleFollow = {},
                        onSendMessage = {},
                        onUpdateProfile = { name, bio, photo ->
                            viewModel.updateProfile(name, bio, photo)
                        },
                        onSignOut = onSignOut,
                        onPostClick = {}
                    )
                }
            }
        }
    }

    // Create Modal (Post / Reel / Story)
    if (isCreateModalOpen) {
        CreateModal(
            currentUser = profile,
            onDismiss = { isCreateModalOpen = false },
            onCreatePost = { post ->
                viewModel.createPost(post)
                if (post.type == "reel") {
                    currentNavScreen = NavScreen.REELS
                } else {
                    currentNavScreen = NavScreen.HOME
                }
            },
            onCreateStory = { story ->
                viewModel.createStory(story)
                currentNavScreen = NavScreen.HOME
            }
        )
    }

    // Story Viewer Dialog
    val viewingStory = activeViewingStory
    if (viewingStory != null) {
        StoryViewerDialog(
            story = viewingStory,
            currentUser = profile,
            onDismiss = { activeViewingStory = null },
            onLikeStory = { viewModel.likeStory(viewingStory) },
            onReplyStory = { reply -> viewModel.replyToStory(viewingStory, reply) }
        )
    }

    // Share to DM Sheet
    val sharingPost = sharingPostToDm
    if (sharingPost != null) {
        ShareToDmSheet(
            post = sharingPost,
            users = allUsers.filter { it.id != profile.id },
            onDismiss = { sharingPostToDm = null },
            onSendToUser = { recipient ->
                viewModel.sharePostIntoDm(sharingPost, recipient)
            }
        )
    }
}
