package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val postsCount: Long = 0L,
    val reelsCount: Long = 0L,
    val isOnline: Boolean = false,
    val passwordHash: String = "",
    val passwordSalt: String = "",
    @ServerTimestamp val lastActive: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UsernameRecord(
    @DocumentId val username: String = "",
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Post(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorPhotoUrl: String = "",
    val type: String = "post", // "post" or "reel"
    val caption: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "image", // "image" or "video"
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val repostsCount: Long = 0L,
    val sharesCount: Long = 0L,
    val savesCount: Long = 0L,
    val hashtags: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val isRepost: Boolean = false,
    val originalPostId: String? = null,
    val originalAuthorId: String? = null,
    val originalAuthorUsername: String? = null,
    val repostComment: String? = null,
    val songTitle: String = "",
    val songArtist: String = "",
    val songCoverUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Story(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorPhotoUrl: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "image", // "image" or "video"
    val caption: String = "",
    val songTitle: String = "",
    val songArtist: String = "",
    val songCoverUrl: String = "",
    val mentions: List<String> = emptyList(),
    val viewers: List<String> = emptyList(),
    val likesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Comment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorPhotoUrl: String = "",
    val text: String = "",
    val parentCommentId: String? = null,
    val replyToUsername: String? = null,
    val likesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LikeRecord(
    @DocumentId val id: String = "",
    val targetType: String = "post", // "post", "reel", "repost", "story", "comment"
    val targetId: String = "",
    val userId: String = "",
    val userUsername: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FollowRecord(
    @DocumentId val id: String = "",
    val followerId: String = "",
    val followingId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SaveRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantUsernames: List<String> = emptyList(),
    val participantNames: List<String> = emptyList(),
    val participantPhotos: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    @ServerTimestamp val lastMessageTimestamp: Timestamp? = null,
    val typingUserIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val recipientId: String = "",
    val text: String = "",
    val mediaUrl: String? = null,
    val sharedPostId: String? = null,
    val sharedPostPreview: String? = null,
    val reaction: String? = null,
    val status: String = "sent", // "sent", "delivered", "read"
    val isEncrypted: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SocialNotification(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val actorId: String = "",
    val actorUsername: String = "",
    val actorPhotoUrl: String = "",
    val type: String = "",
    val targetId: String = "",
    val targetPreview: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserPresence(
    @DocumentId val userId: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp val lastSeen: Timestamp? = null
)

@IgnoreExtraProperties
data class UserNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val userPhotoUrl: String = "",
    val text: String = "",
    val songTitle: String = "",
    val songArtist: String = "",
    val songCoverUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val coverUrl: String,
    val durationSeconds: Int = 180,
    val genre: String = "Pop",
    val audioUrl: String = ""
)
