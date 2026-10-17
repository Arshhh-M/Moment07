package com.example.ui.messages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.crypto.E2EEncryptionHelper
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.ui.components.formatTimestamp
import com.example.ui.components.formatDetailedRelativeTime
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    recipient: UserProfile,
    currentUser: UserProfile,
    messages: List<ChatMessage>,
    isRecipientTyping: Boolean,
    onBack: () -> Unit,
    onSendMessage: (text: String) -> Unit,
    onTypingChanged: (isTyping: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var messageText by remember { mutableStateOf("") }
    var showE2EEDialog by remember { mutableStateOf(false) }
    var showAboutAccountDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Typing debounce
    LaunchedEffect(messageText) {
        if (messageText.isNotBlank()) {
            onTypingChanged(true)
            delay(2500)
            onTypingChanged(false)
        } else {
            onTypingChanged(false)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onTypingChanged(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showAboutAccountDialog = true }
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            val photo = if (recipient.photoUrl.isNotBlank()) recipient.photoUrl
                            else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"

                            AsyncImage(
                                model = photo,
                                contentDescription = recipient.username,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(OnlineGreen)
                                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = recipient.displayName.ifBlank { recipient.username },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = PrimaryViolet,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (isRecipientTyping) {
                                Text(
                                    text = "Typing......",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            } else {
                                Text(
                                    text = "@${recipient.username} • Active now",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAboutAccountDialog = true },
                        modifier = Modifier.testTag("chat_about_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About this account",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showE2EEDialog = true },
                        modifier = Modifier.testTag("chat_encryption_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "End-to-End Encryption",
                            tint = PrimaryViolet
                        )
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
            // E2EE Notice Header Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clickable { showE2EEDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
                        text = "Messages are End-to-End Encrypted (AES-256). Tap to verify.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(messages, key = { _, it -> it.id }) { index, msg ->
                    val isMine = msg.senderId == currentUser.id
                    Column {
                        MessageBubble(
                            message = msg,
                            isMine = isMine
                        )
                        // "Seen / Sent" indicator under the latest sent message
                        if (isMine && index == messages.lastIndex) {
                            val isSeen = msg.status == "read"
                            val statusLabel = formatDetailedRelativeTime(msg.createdAt?.toDate(), if (isSeen) "Seen" else "Sent")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 3.dp, end = 6.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSeen) Icons.Default.DoneAll else Icons.Default.Check,
                                    contentDescription = "Status",
                                    tint = if (isSeen) PrimaryViolet else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (isRecipientTyping) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "@${recipient.username} is typing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "......",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Message...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText.trim())
                                messageText = ""
                                onTypingChanged(false)
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        modifier = Modifier.testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showE2EEDialog) {
        EncryptionVerificationDialog(
            currentUser = currentUser,
            recipient = recipient,
            onDismiss = { showE2EEDialog = false }
        )
    }

    if (showAboutAccountDialog) {
        AboutAccountDialog(
            user = recipient,
            onDismiss = { showAboutAccountDialog = false }
        )
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) PrimaryViolet else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 18.dp
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .testTag("message_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // If message is a shared post preview
                if (message.sharedPostId != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            if (!message.sharedPostPreview.isNullOrBlank()) {
                                AsyncImage(
                                    model = message.sharedPostPreview,
                                    contentDescription = "Shared Content",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Shared SocialX Content",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEncrypted) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = if (isMine) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = formatTimestamp(message.createdAt?.toDate()),
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.status == "read") Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = message.status,
                            tint = if (message.status == "read") Color(0xFF67E8F9) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EncryptionVerificationDialog(
    currentUser: UserProfile,
    recipient: UserProfile,
    onDismiss: () -> Unit
) {
    val safetyBlocks = remember(currentUser.id, recipient.id) {
        E2EEncryptionHelper.getSafetyNumber(currentUser.id, recipient.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security",
                tint = PrimaryViolet,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("End-to-End Encryption", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Messages and calls between you and @${recipient.username} are protected with end-to-end encryption. No third party, not even SocialX, can read or listen to them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Verify Safety Number (60 Digits):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        safetyBlocks.chunked(3).forEach { rowBlocks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowBlocks.forEach { block ->
                                    Text(
                                        text = block,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet)
            ) {
                Text("Verified")
            }
        }
    )
}

@Composable
fun AboutAccountDialog(
    user: UserProfile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "About this account",
                tint = PrimaryViolet,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("About this account", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User banner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val photo = if (user.photoUrl.isNotBlank()) user.photoUrl
                    else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"
                    AsyncImage(
                        model = photo,
                        contentDescription = user.username,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.displayName.ifBlank { user.username },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = PrimaryViolet,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Detail Items
                AccountDetailRow(
                    icon = Icons.Default.CalendarToday,
                    title = "Date joined",
                    subtitle = "October 2024 (Active member)"
                )
                AccountDetailRow(
                    icon = Icons.Default.Public,
                    title = "Account based in",
                    subtitle = "India / Global"
                )
                AccountDetailRow(
                    icon = Icons.Default.Badge,
                    title = "Former usernames",
                    subtitle = "0 (Original handle preserved)"
                )
                AccountDetailRow(
                    icon = Icons.Default.Lock,
                    title = "End-to-End Encryption",
                    subtitle = "Active (AES-256 GCM cryptographically secured)"
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AccountDetailRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryViolet,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

