package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SocialViewModel(
    private val repository: SocialRepository,
    val currentUserId: String
) : ViewModel() {

    // Current user profile
    val currentUserProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Posts & Reels
    val feedPosts: StateFlow<List<Post>> = repository.observePosts("all")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reels: StateFlow<List<Post>> = repository.observePosts("reel")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stories
    val stories: StateFlow<List<Story>> = repository.observeStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes (Instagram-style status notes with songs)
    val notes: StateFlow<List<UserNote>> = repository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Social graph & interactions
    val userLikes: StateFlow<Set<String>> = repository.observeUserLikes(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val userSaves: StateFlow<Set<String>> = repository.observeSavedPostIds(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val followingIds: StateFlow<Set<String>> = repository.observeFollowing(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Conversations & unread message count
    val conversations: StateFlow<List<Conversation>> = repository.observeConversations(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadMessagesCount: StateFlow<Int> = conversations.map { list ->
        list.count { conv ->
            conv.lastMessageSenderId.isNotBlank() && conv.lastMessageSenderId != currentUserId
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Notifications & unread notifications count
    val notifications: StateFlow<List<SocialNotification>> = repository.observeNotifications(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // All registered users for search/explore/share
    val allUsers: StateFlow<List<UserProfile>> = repository.searchUsers("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Comments cache per post ID
    private val commentsFlowMap = mutableMapOf<String, StateFlow<List<Comment>>>()

    fun getCommentsForPost(postId: String): StateFlow<List<Comment>> {
        return commentsFlowMap.getOrPut(postId) {
            repository.observeComments(postId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    // Active chat messages
    private val _activeChatConversationId = MutableStateFlow<String?>(null)
    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeChatConversationId.flatMapLatest { convId ->
        if (convId != null) repository.observeMessages(convId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setActiveConversation(conversationId: String?) {
        _activeChatConversationId.value = conversationId
        if (conversationId != null) {
            viewModelScope.launch {
                repository.markConversationRead(conversationId, currentUserId)
            }
        }
    }

    // Interactions
    fun toggleLike(post: Post) {
        val user = currentUserProfile.value ?: return
        val isLiked = userLikes.value.contains("${post.type}_${post.id}") || userLikes.value.contains("post_${post.id}")
        viewModelScope.launch {
            repository.toggleLike(
                targetType = post.type,
                targetId = post.id,
                targetAuthorId = post.authorId,
                user = user,
                isCurrentlyLiked = isLiked,
                targetPreview = post.caption.take(40),
                isRepost = post.isRepost
            )
        }
    }

    fun toggleSave(post: Post) {
        viewModelScope.launch {
            repository.toggleSave(currentUserId, post.id)
        }
    }

    fun repost(post: Post, quoteComment: String?) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.createRepost(post, user, quoteComment)
        }
    }

    fun addComment(
        postId: String,
        postAuthorId: String,
        text: String,
        parentId: String?,
        replyTo: String?,
        parentAuthorId: String?
    ) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.addComment(postId, postAuthorId, text, user, parentId, replyTo, parentAuthorId)
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(postId, commentId)
        }
    }

    fun createPost(post: Post) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.createPost(post, user)
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            repository.deletePost(post.id, post.authorId, post.type)
        }
    }

    fun createStory(story: Story) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.createStory(story, user)
        }
    }

    fun viewStory(storyId: String) {
        viewModelScope.launch {
            repository.markStoryViewed(storyId, currentUserId)
        }
    }

    fun likeStory(story: Story) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.toggleLike(
                targetType = "story",
                targetId = story.id,
                targetAuthorId = story.authorId,
                user = user,
                isCurrentlyLiked = false,
                targetPreview = story.caption.take(30)
            )
        }
    }

    fun replyToStory(story: Story, replyText: String) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.replyToStory(story, user, replyText)
        }
    }

    fun toggleFollow(targetUserId: String) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.toggleFollow(currentUserId, targetUserId, user)
        }
    }

    fun updateProfile(displayName: String, bio: String, photoUrl: String) {
        val user = currentUserProfile.value ?: return
        val updated = user.copy(displayName = displayName, bio = bio, photoUrl = photoUrl)
        viewModelScope.launch {
            repository.saveUserProfile(updated)
        }
    }

    fun sendMessage(conversationId: String, recipientId: String, text: String) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.sendMessage(conversationId, user, recipientId, text)
        }
    }

    fun sharePostIntoDm(post: Post, recipient: UserProfile) {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            val convId = repository.getOrCreateConversation(user, recipient)
            repository.sendMessage(
                conversationId = convId,
                sender = user,
                recipientId = recipient.id,
                text = "Check out this ${post.type} by @${post.authorUsername}",
                sharedPostId = post.id,
                sharedPostPreview = post.mediaUrl
            )
            repository.createNotification(
                recipientId = recipient.id,
                actor = user,
                type = "share",
                targetId = post.id,
                targetPreview = post.caption.take(40)
            )
        }
    }

    fun setTyping(conversationId: String, isTyping: Boolean) {
        viewModelScope.launch {
            repository.setTyping(conversationId, currentUserId, isTyping)
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(currentUserId)
        }
    }

    suspend fun getOrCreateConversationId(otherUser: UserProfile): String {
        val user = currentUserProfile.value ?: return ""
        return repository.getOrCreateConversation(user, otherUser)
    }

    fun saveNote(text: String, songTitle: String = "", songArtist: String = "", songCoverUrl: String = "") {
        val user = currentUserProfile.value ?: return
        viewModelScope.launch {
            val note = UserNote(
                id = user.id,
                userId = user.id,
                username = user.username,
                displayName = user.displayName,
                userPhotoUrl = user.photoUrl,
                text = text.trim(),
                songTitle = songTitle,
                songArtist = songArtist,
                songCoverUrl = songCoverUrl
            )
            repository.saveNote(note)
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            repository.deleteNote(currentUserId)
        }
    }

    // Story Highlights
    private val highlightsFlowMap = mutableMapOf<String, StateFlow<List<StoryHighlight>>>()

    fun getHighlightsForUser(userId: String): StateFlow<List<StoryHighlight>> {
        return highlightsFlowMap.getOrPut(userId) {
            repository.observeHighlights(userId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    fun saveHighlight(title: String, coverUrl: String, storyIds: List<String>, highlightId: String = "") {
        viewModelScope.launch {
            val user = currentUserProfile.value ?: return@launch
            val matchingStories = stories.value.filter { storyIds.contains(it.id) }
            val highlight = StoryHighlight(
                id = highlightId,
                userId = user.id,
                title = title.trim(),
                coverUrl = coverUrl.ifBlank { matchingStories.firstOrNull()?.mediaUrl ?: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&fit=crop" },
                storyIds = storyIds,
                stories = matchingStories
            )
            repository.saveHighlight(highlight)
        }
    }

    fun deleteHighlight(highlightId: String) {
        viewModelScope.launch {
            repository.deleteHighlight(currentUserId, highlightId)
        }
    }
}
