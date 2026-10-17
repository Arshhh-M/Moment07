package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.data.model.UserNote
import com.example.data.model.UserProfile
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryPink

@Composable
fun NotesBar(
    currentUser: UserProfile,
    notes: List<UserNote>,
    onSaveNote: (text: String, song: Song?) -> Unit,
    onDeleteNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditNoteModalOpen by remember { mutableStateOf(false) }

    val myNote = notes.find { it.userId == currentUser.id }
    val otherNotes = notes.filter { it.userId != currentUser.id }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // User's own note / Add Note item
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(82.dp)
                        .clickable { isEditNoteModalOpen = true }
                        .testTag("user_note_bubble")
                ) {
                    // Bubble snippet
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .defaultMinSize(minHeight = 28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (myNote != null && myNote.songTitle.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Song",
                                    tint = PrimaryViolet,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = if (myNote != null && myNote.text.isNotBlank()) myNote.text else "Share note...",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (myNote != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Avatar with "+" badge
                    Box(modifier = Modifier.size(56.dp)) {
                        AsyncImage(
                            model = currentUser.photoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop" },
                            contentDescription = "Your avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(PrimaryViolet)
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add note",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your note",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Other community notes
            items(otherNotes, key = { it.id }) { note ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(82.dp)
                ) {
                    // Note thought bubble
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .defaultMinSize(minHeight = 28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (note.songTitle.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Song",
                                    tint = SecondaryPink,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = note.text,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Avatar
                    AsyncImage(
                        model = note.userPhotoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop" },
                        contentDescription = note.username,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.username,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    if (isEditNoteModalOpen) {
        EditNoteDialog(
            currentText = myNote?.text ?: "",
            currentSongTitle = myNote?.songTitle ?: "",
            currentSongArtist = myNote?.songArtist ?: "",
            currentSongCover = myNote?.songCoverUrl ?: "",
            onDismiss = { isEditNoteModalOpen = false },
            onSave = { text, song ->
                onSaveNote(text, song)
                isEditNoteModalOpen = false
            },
            onDelete = {
                onDeleteNote()
                isEditNoteModalOpen = false
            }
        )
    }
}

@Composable
fun EditNoteDialog(
    currentText: String,
    currentSongTitle: String,
    currentSongArtist: String,
    currentSongCover: String,
    onDismiss: () -> Unit,
    onSave: (text: String, song: Song?) -> Unit,
    onDelete: () -> Unit
) {
    var noteText by remember { mutableStateOf(currentText) }
    var selectedSong by remember {
        mutableStateOf<Song?>(
            if (currentSongTitle.isNotBlank()) {
                Song(
                    id = "current",
                    title = currentSongTitle,
                    artist = currentSongArtist,
                    coverUrl = currentSongCover
                )
            } else null
        )
    }
    var isPickingSong by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Share a Note", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Share what's on your mind and attach a song for 24 hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= 240) noteText = it },
                    placeholder = { Text("What's on your mind? (240 letters max)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4,
                    supportingText = {
                        Text("${noteText.length}/240", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Song selection row
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPickingSong = true }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = PrimaryViolet
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (selectedSong != null) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedSong!!.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = selectedSong!!.artist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { selectedSong = null }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove song", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Text(
                                text = "Add music from 1M+ tracks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteText, selectedSong) },
                enabled = noteText.isNotBlank() || selectedSong != null,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet)
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            Row {
                if (currentText.isNotBlank()) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (isPickingSong) {
        SongPickerModal(
            onDismiss = { isPickingSong = false },
            onSongSelected = { song ->
                selectedSong = song
                isPickingSong = false
            }
        )
    }
}
