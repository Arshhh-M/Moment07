package com.example.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Conversation
import com.example.data.model.Song
import com.example.data.model.UserNote
import com.example.data.model.UserProfile
import com.example.ui.components.NotesBar
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    conversations: List<Conversation>,
    allUsers: List<UserProfile>,
    currentUser: UserProfile,
    notes: List<UserNote> = emptyList(),
    onSaveNote: (text: String, song: Song?) -> Unit = { _, _ -> },
    onDeleteNote: () -> Unit = {},
    onOpenConversation: (conversationId: String, otherUser: UserProfile) -> Unit,
    onStartNewChat: (otherUser: UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isNewChatDialogOpen by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, searchQuery, currentUser) {
        if (searchQuery.isBlank()) conversations
        else {
            val q = searchQuery.lowercase()
            conversations.filter { conv ->
                val otherUsername = conv.participantUsernames.firstOrNull { it != currentUser.username } ?: ""
                val otherName = conv.participantNames.firstOrNull { it != currentUser.displayName } ?: ""
                otherUsername.lowercase().contains(q) || otherName.lowercase().contains(q) || conv.lastMessage.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { isNewChatDialogOpen = true },
                        modifier = Modifier.testTag("new_message_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "New Message")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search chats or username...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("messages_search_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Instagram-style Notes Bar in Messages
            NotesBar(
                currentUser = currentUser,
                notes = notes,
                onSaveNote = onSaveNote,
                onDeleteNote = onDeleteNote
            )

            // End-to-End Encryption Security Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = PrimaryViolet,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "End-to-End Encrypted • Chats are cryptographically secured",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = "No chats",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No conversations yet" else "No matching chats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Start chatting with friends on SocialX",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { isNewChatDialogOpen = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start a Conversation")
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        val otherIndex = conv.participantIds.indexOfFirst { it != currentUser.id }.takeIf { it >= 0 } ?: 0
                        val otherId = conv.participantIds.getOrNull(otherIndex) ?: ""
                        val otherUsername = conv.participantUsernames.getOrNull(otherIndex) ?: "user"
                        val otherDisplayName = conv.participantNames.getOrNull(otherIndex) ?: otherUsername
                        val otherPhoto = conv.participantPhotos.getOrNull(otherIndex) ?: ""

                        val otherProfile = UserProfile(
                            id = otherId,
                            username = otherUsername,
                            displayName = otherDisplayName,
                            photoUrl = otherPhoto
                        )

                        val isOtherTyping = conv.typingUserIds.contains(otherId)

                        ConversationRow(
                            conversation = conv,
                            otherUser = otherProfile,
                            isTyping = isOtherTyping,
                            onClick = { onOpenConversation(conv.id, otherProfile) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }

    if (isNewChatDialogOpen) {
        NewChatDialog(
            users = allUsers.filter { it.id != currentUser.id },
            onDismiss = { isNewChatDialogOpen = false },
            onSelectUser = { selectedUser ->
                isNewChatDialogOpen = false
                onStartNewChat(selectedUser)
            }
        )
    }
}

@Composable
fun ConversationRow(
    conversation: Conversation,
    otherUser: UserProfile,
    isTyping: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_row_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            val photo = if (otherUser.photoUrl.isNotBlank()) otherUser.photoUrl
            else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"

            AsyncImage(
                model = photo,
                contentDescription = otherUser.username,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Online indicator dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = otherUser.displayName.ifBlank { otherUser.username },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(conversation.lastMessageTimestamp?.toDate() ?: conversation.createdAt?.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            if (isTyping) {
                Text(
                    text = "Typing...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = conversation.lastMessage.ifBlank { "Started a chat" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NewChatDialog(
    users: List<UserProfile>,
    onDismiss: () -> Unit,
    onSelectUser: (UserProfile) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(users, search) {
        if (search.isBlank()) users
        else users.filter { it.username.contains(search, true) || it.displayName.contains(search, true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Message", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search by username...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(filtered, key = { it.id }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectUser(user) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val photo = if (user.photoUrl.isNotBlank()) user.photoUrl
                            else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"

                            AsyncImage(
                                model = photo,
                                contentDescription = user.username,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = user.displayName.ifBlank { user.username },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${user.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
