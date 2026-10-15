package com.example.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.StoryHighlight
import com.example.data.model.UserProfile
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileUser: UserProfile,
    currentUser: UserProfile,
    userPosts: List<Post>,
    savedPosts: List<Post>,
    highlights: List<StoryHighlight> = emptyList(),
    savedAccounts: List<UserProfile> = emptyList(),
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onSendMessage: () -> Unit,
    onUpdateProfile: (displayName: String, bio: String, photoUrl: String) -> Unit,
    onSignOut: () -> Unit,
    onPostClick: (Post) -> Unit,
    onCreateHighlight: (title: String, coverUrl: String) -> Unit = { _, _ -> },
    onViewHighlight: (StoryHighlight) -> Unit = {},
    onSwitchAccount: (userId: String) -> Unit = {},
    onAddNewAccount: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isMyProfile = profileUser.id == currentUser.id
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Posts, 1 = Reels, 2 = Saved
    var showEditDialog by remember { mutableStateOf(false) }
    var showCreateHighlightDialog by remember { mutableStateOf(false) }
    var showAccountsSheet by remember { mutableStateOf(false) }

    // Direct Gallery Picker for DP (Profile Picture)
    val dpPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateProfile(profileUser.displayName, profileUser.bio, uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (isMyProfile) showAccountsSheet = true
                        }
                    ) {
                        Text(
                            text = "@${profileUser.username}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isMyProfile) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Switch Accounts",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isMyProfile) {
                        IconButton(
                            onClick = { showAccountsSheet = true },
                            modifier = Modifier.testTag("switch_accounts_top_button")
                        ) {
                            Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = "Accounts")
                        }
                        IconButton(onClick = onSignOut, modifier = Modifier.testTag("profile_sign_out_button")) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Sign out")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar / DP with Gallery Tap
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            if (isMyProfile) {
                                dpPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        }
                ) {
                    val photo = if (profileUser.photoUrl.isNotBlank()) profileUser.photoUrl
                    else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop"

                    AsyncImage(
                        model = photo,
                        contentDescription = profileUser.username,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, PrimaryViolet, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    if (isMyProfile) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryViolet)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change DP",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    } else if (profileUser.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(22.dp))

                // Stats Counters
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatCounter(count = userPosts.size.toLong(), label = "Posts")
                    StatCounter(count = profileUser.followersCount, label = "Followers")
                    StatCounter(count = profileUser.followingCount, label = "Following")
                }
            }

            // Name and Bio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profileUser.displayName.ifBlank { profileUser.username },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = PrimaryViolet,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (profileUser.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = profileUser.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isMyProfile) {
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_profile_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            dpPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change DP", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onToggleFollow,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_follow_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isFollowing) "Following" else "Follow", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onSendMessage,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_message_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Message", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Story Highlights Tray
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    text = "Story Highlights",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isMyProfile) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showCreateHighlightDialog = true }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "New Highlight",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "New",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Default & User Highlights
                    val displayHighlights = if (highlights.isNotEmpty()) highlights else listOf(
                        StoryHighlight(id = "h1", title = "Vibes ✨", coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&fit=crop"),
                        StoryHighlight(id = "h2", title = "Travel ✈️", coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=300&fit=crop"),
                        StoryHighlight(id = "h3", title = "Memories ❤️", coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300&fit=crop")
                    )

                    items(displayHighlights, key = { it.id }) { highlight ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onViewHighlight(highlight) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Brush.linearGradient(listOf(PrimaryViolet, SecondaryPink)), CircleShape)
                                    .padding(3.dp)
                            ) {
                                AsyncImage(
                                    model = highlight.coverUrl.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&fit=crop" },
                                    contentDescription = highlight.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = highlight.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Profile Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.GridOn, contentDescription = "Posts") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Movie, contentDescription = "Reels") }
                )
                if (isMyProfile) {
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Saved") }
                    )
                }
            }

            // Tab Content Grid
            val displayedPosts = when (selectedTab) {
                1 -> userPosts.filter { it.type == "reel" }
                2 -> savedPosts
                else -> userPosts.filter { it.type == "post" }
            }

            if (displayedPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (selectedTab) {
                                1 -> Icons.Default.Movie
                                2 -> Icons.Default.BookmarkBorder
                                else -> Icons.Default.GridOn
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedTab) {
                                1 -> "No reels yet"
                                2 -> "No saved posts yet"
                                else -> "No posts yet"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(displayedPosts, key = { it.id }) { post ->
                        val media = post.mediaUrl
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onPostClick(post) }
                        ) {
                            AsyncImage(
                                model = media,
                                contentDescription = post.caption,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (post.type == "reel") {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reel",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = profileUser.displayName,
            currentBio = profileUser.bio,
            currentPhoto = profileUser.photoUrl,
            onDismiss = { showEditDialog = false },
            onSave = { name, bio, photo ->
                showEditDialog = false
                onUpdateProfile(name, bio, photo)
            }
        )
    }

    if (showCreateHighlightDialog) {
        CreateHighlightDialog(
            onDismiss = { showCreateHighlightDialog = false },
            onCreate = { title, cover ->
                showCreateHighlightDialog = false
                onCreateHighlight(title, cover)
            }
        )
    }

    if (showAccountsSheet) {
        AccountsBottomSheet(
            currentUserId = currentUser.id,
            accounts = savedAccounts.ifEmpty { listOf(currentUser) },
            onDismiss = { showAccountsSheet = false },
            onSelectAccount = { uid ->
                showAccountsSheet = false
                onSwitchAccount(uid)
            },
            onAddAccount = {
                showAccountsSheet = false
                onAddNewAccount()
            }
        )
    }
}

@Composable
fun StatCounter(count: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentPhoto: String,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, photoUrl: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var photoUri by remember { mutableStateOf(currentPhoto) }

    val avatarGalleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Change avatar DP from gallery
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .clickable {
                            avatarGalleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    val p = if (photoUri.isNotBlank()) photoUri
                    else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop"

                    AsyncImage(
                        model = p,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Choose from gallery",
                            tint = Color.White
                        )
                    }
                }
                TextButton(onClick = {
                    avatarGalleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Change photo from Gallery", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 150) bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, photoUri) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateHighlightDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, coverUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverUrl = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Story Highlight", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cover picker
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUrl.isNotBlank()) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = PrimaryViolet)
                            Text("Cover", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Highlight Name *") },
                    placeholder = { Text("e.g. Goa 🏖️, Memories ❤️, Vibes ✨") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), coverUrl)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Highlight")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsBottomSheet(
    currentUserId: String,
    accounts: List<UserProfile>,
    onDismiss: () -> Unit,
    onSelectAccount: (userId: String) -> Unit,
    onAddAccount: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Switch Accounts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            accounts.forEach { acc ->
                val isSelected = acc.id == currentUserId
                Surface(
                    color = if (isSelected) PrimaryViolet.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectAccount(acc.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val photo = if (acc.photoUrl.isNotBlank()) acc.photoUrl
                        else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"

                        AsyncImage(
                            model = photo,
                            contentDescription = acc.username,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "@${acc.username}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = acc.displayName.ifBlank { acc.username },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active account",
                                tint = PrimaryViolet,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Account / Create New ID Button
            Button(
                onClick = onAddAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_another_account_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Account or Create New ID", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
