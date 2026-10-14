package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.crypto.E2EEncryptionHelper
import com.example.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SocialRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private const val TAG = "SocialRepository"
    }

    // ==========================================
    // USERS & PROFILES
    // ==========================================

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing user profile $userId", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        if (userId.isBlank()) return null
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) doc.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user profile $userId", e)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        return try {
            val batch = db.batch()
            val userRef = db.collection("users").document(profile.id)
            val usernameRef = db.collection("usernames").document(profile.username.lowercase())

            val cleanProfile = profile.copy(
                username = profile.username.trim().lowercase(),
                displayName = profile.displayName.trim()
            )

            val usernameData = mapOf(
                "userId" to profile.id,
                "username" to cleanProfile.username,
                "createdAt" to FieldValue.serverTimestamp()
            )
            batch.set(userRef, cleanProfile)
            batch.set(usernameRef, usernameData)
            batch.commit().await()
            true
        } catch (e: Exception) {
            println("SAVE PROFILE ERROR: ${e.message}")
            e.printStackTrace()
            Log.e(TAG, "Failed to save user profile", e)
            false
        }
    }

    suspend fun isUsernameAvailable(username: String, currentUserId: String = ""): Boolean {
        val clean = username.trim().lowercase().removePrefix("@")
        if (clean.length < 2) return false
        return try {
            val doc = db.collection("usernames").document(clean).get().await()
            if (!doc.exists()) true else (currentUserId.isNotBlank() && doc.getString("userId") == currentUserId)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking username", e)
            false
        }
    }

    suspend fun getUserByUsername(username: String): UserProfile? {
        val clean = username.trim().lowercase().removePrefix("@")
        if (clean.isBlank()) return null
        return try {
            val recordDoc = db.collection("usernames").document(clean).get().await()
            val userId = recordDoc.getString("userId") ?: return null
            getUserProfile(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding user by username @$clean", e)
            null
        }
    }

    fun searchUsers(query: String): Flow<List<UserProfile>> = callbackFlow {
        val q = query.trim().lowercase()
        val queryRef = if (q.isBlank()) {
            db.collection("users").limit(30)
        } else {
            db.collection("users")
                .orderBy("username")
                .startAt(q)
                .endAt(q + "\uf8ff")
                .limit(20)
        }

        val listener = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error searching users", error)
                return@addSnapshotListener
            }
            val users = snapshot?.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updatePresence(userId: String, isOnline: Boolean) {
        if (userId.isBlank()) return
        try {
            val presenceRef = db.collection("presence").document(userId)
            presenceRef.set(
                mapOf(
                    "userId" to userId,
                    "isOnline" to isOnline,
                    "lastSeen" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            db.collection("users").document(userId).update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastActive" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update presence for $userId", e)
        }
    }

    // ==========================================
    // POSTS & REELS
    // ==========================================

    fun observePosts(feedType: String = "all"): Flow<List<Post>> = callbackFlow {
        val query = when (feedType) {
            "reel" -> db.collection("posts").whereEqualTo("type", "reel")
            "post" -> db.collection("posts").whereEqualTo("type", "post")
            else -> db.collection("posts")
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing posts for feed $feedType", error)
                return@addSnapshotListener
            }
            val posts = snapshot?.toObjects(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                ?: emptyList()
            trySend(posts)
        }
        awaitClose { listener.remove() }
    }

    fun observeUserPosts(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = db.collection("posts")
            .whereEqualTo("authorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing posts for user $userId", error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: Post, author: UserProfile): String? {
        return try {
            val postRef = db.collection("posts").document()
            val newPost = post.copy(
                authorId = author.id,
                authorUsername = author.username,
                authorDisplayName = author.displayName,
                authorPhotoUrl = author.photoUrl
            )
            postRef.set(newPost).await()

            // Increment user post/reel counter
            val counterField = if (post.type == "reel") "reelsCount" else "postsCount"
            db.collection("users").document(author.id).update(counterField, FieldValue.increment(1)).await()

            // Check for user mentions in caption (e.g. @neeraj)
            notifyMentions(post.caption, author, if (post.type == "reel") "mention_reel" else "mention_post", postRef.id)

            postRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create post", e)
            null
        }
    }

    suspend fun deletePost(postId: String, authorId: String, postType: String) {
        try {
            db.collection("posts").document(postId).delete().await()
            val counterField = if (postType == "reel") "reelsCount" else "postsCount"
            db.collection("users").document(authorId).update(counterField, FieldValue.increment(-1)).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete post", e)
        }
    }

    suspend fun createRepost(
        originalPost: Post,
        reposter: UserProfile,
        repostComment: String?
    ): Boolean {
        return try {
            val repostDocRef = db.collection("posts").document()
            val repost = Post(
                authorId = reposter.id,
                authorUsername = reposter.username,
                authorDisplayName = reposter.displayName,
                authorPhotoUrl = reposter.photoUrl,
                type = originalPost.type,
                caption = originalPost.caption,
                mediaUrl = originalPost.mediaUrl,
                mediaType = originalPost.mediaType,
                isRepost = true,
                originalPostId = originalPost.id,
                originalAuthorId = originalPost.authorId,
                originalAuthorUsername = originalPost.authorUsername,
                repostComment = repostComment?.takeIf { it.isNotBlank() }
            )
            repostDocRef.set(repost).await()

            // Update original post repost counter
            db.collection("posts").document(originalPost.id).update("repostsCount", FieldValue.increment(1)).await()

            // Notify original author if different
            if (originalPost.authorId != reposter.id) {
                createNotification(
                    recipientId = originalPost.authorId,
                    actor = reposter,
                    type = if (originalPost.type == "reel") "repost_reel" else "repost_post",
                    targetId = repostDocRef.id,
                    targetPreview = repostComment ?: originalPost.caption.take(40)
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to repost", e)
            false
        }
    }

    // ==========================================
    // LIKES
    // ==========================================

    fun observeUserLikes(userId: String): Flow<Set<String>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }
        val listener = db.collection("likes")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing user likes", error)
                    return@addSnapshotListener
                }
                val set = snapshot?.documents?.mapNotNull { doc ->
                    val type = doc.getString("targetType")
                    val id = doc.getString("targetId")
                    if (type != null && id != null) "${type}_$id" else null
                }?.toSet() ?: emptySet()
                trySend(set)
            }
        awaitClose { listener.remove() }
    }

    suspend fun toggleLike(
        targetType: String,
        targetId: String,
        targetAuthorId: String,
        user: UserProfile,
        isCurrentlyLiked: Boolean,
        targetPreview: String = "",
        isRepost: Boolean = false
    ) {
        val likeDocId = "${targetType}_${targetId}_${user.id}"
        val likeRef = db.collection("likes").document(likeDocId)
        try {
            if (isCurrentlyLiked) {
                likeRef.delete().await()
                if (targetType in listOf("post", "reel", "repost")) {
                    db.collection("posts").document(targetId).update("likesCount", FieldValue.increment(-1)).await()
                } else if (targetType == "story") {
                    db.collection("stories").document(targetId).update("likesCount", FieldValue.increment(-1)).await()
                }
            } else {
                likeRef.set(
                    LikeRecord(
                        targetType = targetType,
                        targetId = targetId,
                        userId = user.id,
                        userUsername = user.username
                    )
                ).await()
                if (targetType in listOf("post", "reel", "repost")) {
                    db.collection("posts").document(targetId).update("likesCount", FieldValue.increment(1)).await()
                } else if (targetType == "story") {
                    db.collection("stories").document(targetId).update("likesCount", FieldValue.increment(1)).await()
                }

                // Send notification to author
                if (targetAuthorId != user.id && targetAuthorId.isNotBlank()) {
                    val notifType = when {
                        isRepost -> "like_repost"
                        targetType == "reel" -> "like_reel"
                        targetType == "story" -> "like_story"
                        else -> "like_post"
                    }
                    createNotification(
                        recipientId = targetAuthorId,
                        actor = user,
                        type = notifType,
                        targetId = targetId,
                        targetPreview = targetPreview
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
        }
    }

    // ==========================================
    // COMMENTS
    // ==========================================

    fun observeComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("posts").document(postId).collection("comments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing comments for $postId", error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.sortedBy { it.createdAt ?: Timestamp(0, 0) }
                    ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(
        postId: String,
        postAuthorId: String,
        text: String,
        user: UserProfile,
        parentCommentId: String? = null,
        replyToUsername: String? = null,
        parentAuthorId: String? = null
    ): Boolean {
        return try {
            val commentRef = db.collection("posts").document(postId).collection("comments").document()
            val comment = Comment(
                postId = postId,
                authorId = user.id,
                authorUsername = user.username,
                authorDisplayName = user.displayName,
                authorPhotoUrl = user.photoUrl,
                text = text.trim(),
                parentCommentId = parentCommentId,
                replyToUsername = replyToUsername
            )
            commentRef.set(comment).await()

            // Update commentsCount on post
            db.collection("posts").document(postId).update("commentsCount", FieldValue.increment(1)).await()

            // Notify parent comment author if this is a reply
            if (parentAuthorId != null && parentAuthorId != user.id) {
                createNotification(
                    recipientId = parentAuthorId,
                    actor = user,
                    type = "reply",
                    targetId = postId,
                    targetPreview = text.take(60)
                )
            } else if (postAuthorId != user.id) {
                createNotification(
                    recipientId = postAuthorId,
                    actor = user,
                    type = "comment",
                    targetId = postId,
                    targetPreview = text.take(60)
                )
            }

            // Also check for mentions in comments
            notifyMentions(text, user, "mention_post", postId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment", e)
            false
        }
    }

    suspend fun deleteComment(postId: String, commentId: String) {
        try {
            db.collection("posts").document(postId).collection("comments").document(commentId).delete().await()
            db.collection("posts").document(postId).update("commentsCount", FieldValue.increment(-1)).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment", e)
        }
    }

    // ==========================================
    // STORIES
    // ==========================================

    fun observeStories(): Flow<List<Story>> = callbackFlow {
        val listener = db.collection("stories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing stories", error)
                    return@addSnapshotListener
                }
                val stories = snapshot?.toObjects(Story::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    ?: emptyList()
                trySend(stories)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createStory(story: Story, author: UserProfile): Boolean {
        return try {
            val storyRef = db.collection("stories").document()
            val newStory = story.copy(
                authorId = author.id,
                authorUsername = author.username,
                authorDisplayName = author.displayName,
                authorPhotoUrl = author.photoUrl
            )
            storyRef.set(newStory).await()

            // Notify mentions in story
            notifyMentions(story.caption, author, "mention_story", storyRef.id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create story", e)
            false
        }
    }

    suspend fun markStoryViewed(storyId: String, viewerId: String) {
        if (viewerId.isBlank()) return
        try {
            db.collection("stories").document(storyId)
                .update("viewers", FieldValue.arrayUnion(viewerId)).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to mark story viewed", e)
        }
    }

    suspend fun replyToStory(story: Story, user: UserProfile, replyText: String) {
        try {
            val convId = getOrCreateConversation(user, UserProfile(id = story.authorId, username = story.authorUsername))
            sendMessage(
                conversationId = convId,
                sender = user,
                recipientId = story.authorId,
                text = "Replied to story: \"$replyText\"",
                sharedPostId = story.id,
                sharedPostPreview = story.mediaUrl
            )
            createNotification(
                recipientId = story.authorId,
                actor = user,
                type = "story_reply",
                targetId = story.id,
                targetPreview = replyText
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reply to story", e)
        }
    }

    // ==========================================
    // FOLLOWS
    // ==========================================

    fun observeFollowing(currentUserId: String): Flow<Set<String>> = callbackFlow {
        if (currentUserId.isBlank()) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }
        val listener = db.collection("follows")
            .whereEqualTo("followerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing follows", error)
                    return@addSnapshotListener
                }
                val set = snapshot?.documents?.mapNotNull { it.getString("followingId") }?.toSet() ?: emptySet()
                trySend(set)
            }
        awaitClose { listener.remove() }
    }

    suspend fun toggleFollow(currentUserId: String, targetUserId: String, currentUser: UserProfile) {
        if (currentUserId == targetUserId || currentUserId.isBlank() || targetUserId.isBlank()) return
        val followDocId = "${currentUserId}_${targetUserId}"
        val followRef = db.collection("follows").document(followDocId)
        try {
            val doc = followRef.get().await()
            if (doc.exists()) {
                followRef.delete().await()
                db.collection("users").document(currentUserId).update("followingCount", FieldValue.increment(-1)).await()
                db.collection("users").document(targetUserId).update("followersCount", FieldValue.increment(-1)).await()
            } else {
                followRef.set(
                    FollowRecord(
                        followerId = currentUserId,
                        followingId = targetUserId
                    )
                ).await()
                db.collection("users").document(currentUserId).update("followingCount", FieldValue.increment(1)).await()
                db.collection("users").document(targetUserId).update("followersCount", FieldValue.increment(1)).await()

                // Notify target user
                createNotification(
                    recipientId = targetUserId,
                    actor = currentUser,
                    type = "follow",
                    targetId = currentUserId,
                    targetPreview = "started following you"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling follow", e)
        }
    }

    // ==========================================
    // SAVES
    // ==========================================

    fun observeSavedPostIds(userId: String): Flow<Set<String>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }
        val listener = db.collection("saves")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing saves", error)
                    return@addSnapshotListener
                }
                val set = snapshot?.documents?.mapNotNull { it.getString("postId") }?.toSet() ?: emptySet()
                trySend(set)
            }
        awaitClose { listener.remove() }
    }

    suspend fun toggleSave(userId: String, postId: String) {
        val saveDocId = "${userId}_${postId}"
        val saveRef = db.collection("saves").document(saveDocId)
        try {
            val doc = saveRef.get().await()
            if (doc.exists()) {
                saveRef.delete().await()
                db.collection("posts").document(postId).update("savesCount", FieldValue.increment(-1)).await()
            } else {
                saveRef.set(SaveRecord(userId = userId, postId = postId)).await()
                db.collection("posts").document(postId).update("savesCount", FieldValue.increment(1)).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling save", e)
        }
    }

    // ==========================================
    // CONVERSATIONS & DIRECT MESSAGING
    // ==========================================

    fun observeConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("conversations")
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing conversations for $userId", error)
                    return@addSnapshotListener
                }
                val convs = snapshot?.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.sortedByDescending { it.lastMessageTimestamp ?: it.createdAt ?: Timestamp(0, 0) }
                    ?: emptyList()
                trySend(convs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getOrCreateConversation(user1: UserProfile, user2: UserProfile): String {
        val convId = listOf(user1.id, user2.id).sorted().joinToString("_")
        val convRef = db.collection("conversations").document(convId)
        val doc = convRef.get().await()
        if (!doc.exists()) {
            val conv = Conversation(
                participantIds = listOf(user1.id, user2.id),
                participantUsernames = listOf(user1.username, user2.username),
                participantNames = listOf(user1.displayName, user2.displayName),
                participantPhotos = listOf(user1.photoUrl, user2.photoUrl),
                lastMessage = "",
                lastMessageSenderId = "",
                typingUserIds = emptyList()
            )
            convRef.set(conv).await()
        }
        return convId
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("conversations").document(conversationId).collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing messages for $conversationId", error)
                    return@addSnapshotListener
                }
                val rawMessages = snapshot?.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.sortedBy { it.createdAt ?: Timestamp(0, 0) }
                    ?: emptyList()
                val decryptedMessages = rawMessages.map { msg ->
                    if (E2EEncryptionHelper.isMessageEncrypted(msg.text)) {
                        msg.copy(
                            text = E2EEncryptionHelper.decrypt(msg.text, conversationId),
                            isEncrypted = true
                        )
                    } else {
                        msg.copy(isEncrypted = false)
                    }
                }
                trySend(decryptedMessages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(
        conversationId: String,
        sender: UserProfile,
        recipientId: String,
        text: String,
        sharedPostId: String? = null,
        sharedPostPreview: String? = null
    ): Boolean {
        return try {
            val convRef = db.collection("conversations").document(conversationId)
            val msgRef = convRef.collection("messages").document()
            val cipherText = E2EEncryptionHelper.encrypt(text.trim(), conversationId)
            val msg = ChatMessage(
                conversationId = conversationId,
                senderId = sender.id,
                senderUsername = sender.username,
                recipientId = recipientId,
                text = cipherText,
                sharedPostId = sharedPostId,
                sharedPostPreview = sharedPostPreview,
                status = "sent",
                isEncrypted = true
            )
            msgRef.set(msg).await()

            // Update conversation snippet and clear typing state
            convRef.update(
                mapOf(
                    "lastMessage" to if (sharedPostId != null) "Shared a post" else text.take(60),
                    "lastMessageSenderId" to sender.id,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "typingUserIds" to FieldValue.arrayRemove(sender.id)
                )
            ).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            false
        }
    }

    suspend fun setTyping(conversationId: String, userId: String, isTyping: Boolean) {
        if (conversationId.isBlank() || userId.isBlank()) return
        try {
            val convRef = db.collection("conversations").document(conversationId)
            if (isTyping) {
                convRef.update("typingUserIds", FieldValue.arrayUnion(userId)).await()
            } else {
                convRef.update("typingUserIds", FieldValue.arrayRemove(userId)).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update typing status", e)
        }
    }

    suspend fun markConversationRead(conversationId: String, currentUserId: String) {
        try {
            val unreadQuery = db.collection("conversations").document(conversationId)
                .collection("messages")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("status", "sent")
                .limit(20)
                .get().await()

            val batch = db.batch()
            for (doc in unreadQuery.documents) {
                batch.update(doc.reference, "status", "read")
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.w(TAG, "Error marking messages read", e)
        }
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    fun observeNotifications(userId: String): Flow<List<SocialNotification>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing notifications for $userId", error)
                    return@addSnapshotListener
                }
                val notifs = snapshot?.toObjects(SocialNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    ?: emptyList()
                trySend(notifs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markNotificationRead(notificationId: String) {
        try {
            db.collection("notifications").document(notificationId).update("isRead", true).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error marking notification read", e)
        }
    }

    suspend fun markAllNotificationsRead(userId: String) {
        try {
            val unread = db.collection("notifications")
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("isRead", false)
                .limit(50)
                .get().await()

            val batch = db.batch()
            for (doc in unread.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.w(TAG, "Error marking all notifications read", e)
        }
    }

    suspend fun createNotification(
        recipientId: String,
        actor: UserProfile,
        type: String,
        targetId: String,
        targetPreview: String
    ) {
        if (recipientId == actor.id || recipientId.isBlank()) return
        try {
            val notifRef = db.collection("notifications").document()
            val notif = SocialNotification(
                recipientId = recipientId,
                actorId = actor.id,
                actorUsername = actor.username,
                actorPhotoUrl = actor.photoUrl,
                type = type,
                targetId = targetId,
                targetPreview = targetPreview,
                isRead = false
            )
            notifRef.set(notif).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification", e)
        }
    }

    private suspend fun notifyMentions(text: String, actor: UserProfile, type: String, targetId: String) {
        val mentionRegex = Regex("@([a-zA-Z0-9_]{2,30})")
        val matches = mentionRegex.findAll(text).map { it.groupValues[1].lowercase() }.toSet()
        for (username in matches) {
            if (username == actor.username.lowercase()) continue
            try {
                val userDoc = db.collection("usernames").document(username).get().await()
                val targetUserId = userDoc.getString("userId")
                if (targetUserId != null && targetUserId != actor.id) {
                    createNotification(
                        recipientId = targetUserId,
                        actor = actor,
                        type = type,
                        targetId = targetId,
                        targetPreview = text.take(60)
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error notifying mention @$username", e)
            }
        }
    }

    // ==========================================
    // USER NOTES (Instagram-style Notes with Music)
    // ==========================================

    fun observeNotes(): Flow<List<UserNote>> = callbackFlow {
        val listener = db.collection("notes")
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing notes", error)
                    return@addSnapshotListener
                }
                val notes = snapshot?.toObjects(UserNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    ?: emptyList()
                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveNote(note: UserNote): Boolean {
        return try {
            db.collection("notes").document(note.userId).set(note).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save note", e)
            false
        }
    }

    suspend fun deleteNote(userId: String): Boolean {
        return try {
            db.collection("notes").document(userId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
