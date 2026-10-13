package com.example.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialNotification
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.LikeRed
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.SecondaryPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<SocialNotification>,
    onNotificationClick: (SocialNotification) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onUserClick: (userId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(
                            onClick = onMarkAllAsRead,
                            modifier = Modifier.testTag("mark_all_read_button")
                        ) {
                            Text("Mark all read", fontWeight = FontWeight.Bold)
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
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "No notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No notifications yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Likes, comments, mentions and follows will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationRow(
                        notification = notif,
                        onClick = { onNotificationClick(notif) },
                        onUserClick = { onUserClick(notif.actorId) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    notification: SocialNotification,
    onClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val (icon, iconTint, message) = remember(notification.type, notification.actorUsername) {
        val user = "@${notification.actorUsername}"
        when (notification.type) {
            "like_post" -> Triple(Icons.Default.Favorite, LikeRed, "$user liked your post.")
            "like_reel" -> Triple(Icons.Default.Favorite, LikeRed, "$user liked your reel.")
            "like_repost" -> Triple(Icons.Default.Favorite, LikeRed, "$user liked your repost.")
            "like_story" -> Triple(Icons.Default.Favorite, LikeRed, "$user liked your story.")
            "comment" -> Triple(Icons.Default.ChatBubble, PrimaryViolet, "$user commented on your post.")
            "reply" -> Triple(Icons.Default.Reply, PrimaryViolet, "$user replied to your comment.")
            "repost_post" -> Triple(Icons.Default.Repeat, RepostGreen, "$user reposted your post.")
            "repost_reel" -> Triple(Icons.Default.Repeat, RepostGreen, "$user reposted your reel.")
            "follow" -> Triple(Icons.Default.PersonAdd, SecondaryPink, "$user started following you.")
            "mention_post" -> Triple(Icons.Default.AlternateEmail, PrimaryViolet, "$user mentioned you in a post.")
            "mention_reel" -> Triple(Icons.Default.AlternateEmail, PrimaryViolet, "$user mentioned you in a reel.")
            "mention_story" -> Triple(Icons.Default.AlternateEmail, PrimaryViolet, "$user mentioned you in their story.")
            "story_reply" -> Triple(Icons.Default.Send, SecondaryPink, "$user replied to your story.")
            "share" -> Triple(Icons.Default.Share, PrimaryViolet, "$user shared your content.")
            else -> Triple(Icons.Default.Notifications, PrimaryViolet, "$user interacted with your content.")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!notification.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("notification_row_${notification.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(46.dp)) {
            val photo = if (notification.actorPhotoUrl.isNotBlank()) notification.actorPhotoUrl
            else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"

            AsyncImage(
                model = photo,
                contentDescription = notification.actorUsername,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable { onUserClick() },
                contentScale = ContentScale.Crop
            )

            Surface(
                color = iconTint,
                shape = CircleShape,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (notification.targetPreview.isNotBlank()) {
                Text(
                    text = "\"${notification.targetPreview}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text(
                text = formatTimestamp(notification.createdAt?.toDate()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
