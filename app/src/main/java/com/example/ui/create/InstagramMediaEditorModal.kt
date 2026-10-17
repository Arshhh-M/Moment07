package com.example.ui.create

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.ui.components.InstagramFilter
import com.example.ui.components.InstagramFilters
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryPink

/**
 * Instagram-grade Media Editor Modal.
 * Offers real-time photo/video filters, location stickers, music tags,
 * custom text overlays with font styling, and aspect ratio formatting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramMediaEditorModal(
    mediaUrl: String,
    initialFilter: String = "Normal",
    initialLocation: String = "",
    initialStickers: List<String> = emptyList(),
    initialTextOverlay: String = "",
    initialAspectRatio: String = "original",
    attachedSong: Song? = null,
    onDismiss: () -> Unit,
    onApplyEdits: (
        filterName: String,
        locationTag: String,
        stickers: List<String>,
        textOverlay: String,
        aspectRatio: String
    ) -> Unit
) {
    var selectedFilterName by remember { mutableStateOf(initialFilter) }
    var locationTag by remember { mutableStateOf(initialLocation) }
    val stickers = remember { mutableStateListOf<String>().apply { addAll(initialStickers) } }
    var textOverlay by remember { mutableStateOf(initialTextOverlay) }
    var textFontFamily by remember { mutableStateOf("Modern") }
    var selectedRatio by remember { mutableStateOf(initialAspectRatio) }
    var activeTool by remember { mutableStateOf("filter") } // "filter", "stickers", "text", "crop"

    var isAddingCustomLocation by remember { mutableStateOf(false) }
    var customLocationText by remember { mutableStateOf("") }

    val currentFilter = remember(selectedFilterName) {
        InstagramFilters.getFilter(selectedFilterName)
    }

    val quickLocations = listOf(
        "📍 Mumbai", "📍 Delhi", "📍 Bengaluru", "📍 Goa", "📍 London", "📍 New York", "📍 Dubai", "📍 Tokyo"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PrimaryViolet.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Instagram Studio",
                                color = SecondaryPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onApplyEdits(
                                selectedFilterName,
                                locationTag,
                                stickers.toList(),
                                textOverlay,
                                selectedRatio
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Apply",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Main Media Preview Canvas with Active Filter & Overlays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val aspectMod = when (selectedRatio) {
                        "1:1" -> Modifier.aspectRatio(1f)
                        "4:5" -> Modifier.aspectRatio(0.8f)
                        "9:16" -> Modifier.aspectRatio(9f / 16f)
                        else -> Modifier.fillMaxSize()
                    }

                    Box(
                        modifier = aspectMod
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF18181B)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Image with Instagram Color Filter
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = "Media Canvas",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.colorMatrix(currentFilter.colorMatrix)
                        )

                        // Top Overlays: Location & Music Sticker
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (locationTag.isNotBlank()) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = locationTag,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove location",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable { locationTag = "" }
                                        )
                                    }
                                }
                            }

                            // Dynamic Attached Music Sticker
                            if (attachedSong != null || stickers.any { it.startsWith("🎵") }) {
                                val songDisplay = attachedSong?.title ?: "Trending Track"
                                val artistDisplay = attachedSong?.artist ?: "Artist"
                                Surface(
                                    color = PrimaryViolet.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(12.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$songDisplay • $artistDisplay",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Center Text Overlay (Instagram Story Style)
                        if (textOverlay.isNotBlank()) {
                            Surface(
                                color = when (textFontFamily) {
                                    "Neon" -> Color.Black.copy(alpha = 0.8f)
                                    "Serif" -> Color.White.copy(alpha = 0.9f)
                                    else -> Color.Black.copy(alpha = 0.65f)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .align(Alignment.Center)
                            ) {
                                Text(
                                    text = textOverlay,
                                    color = if (textFontFamily == "Serif") Color.Black else if (textFontFamily == "Neon") SecondaryPink else Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = when (textFontFamily) {
                                        "Serif" -> FontFamily.Serif
                                        "Monospace" -> FontFamily.Monospace
                                        else -> FontFamily.Default
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Bottom Stickers Row
                        if (stickers.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                stickers.forEach { sticker ->
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(12.dp),
                                        shadowElevation = 4.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sticker,
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.DarkGray,
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable { stickers.remove(sticker) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Tool Category Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        Triple("filter", "Filters", Icons.Default.AutoAwesome),
                        Triple("stickers", "Stickers", Icons.Default.EmojiEmotions),
                        Triple("text", "Text", Icons.Default.TextFields),
                        Triple("crop", "Aspect", Icons.Default.Crop)
                    ).forEach { (id, label, icon) ->
                        val isSelected = activeTool == id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { activeTool = id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) SecondaryPink else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tool Content Shelf
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFF121214))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    when (activeTool) {
                        "filter" -> {
                            // Instagram Filter Carousel
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(InstagramFilters.ALL) { filter ->
                                    val isSelected = selectedFilterName.equals(filter.name, ignoreCase = true)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { selectedFilterName = filter.name }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) SecondaryPink else Color.DarkGray,
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                        ) {
                                            AsyncImage(
                                                model = mediaUrl,
                                                contentDescription = filter.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                colorFilter = ColorFilter.colorMatrix(filter.colorMatrix)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = filter.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SecondaryPink else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        "stickers" -> {
                            // Instagram Sticker Tray
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Tap to add Instagram stickers:",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(quickLocations) { loc ->
                                        Surface(
                                            color = if (locationTag == loc) SecondaryPink else Color(0xFF27272A),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.clickable { locationTag = loc }
                                        ) {
                                            Text(
                                                text = loc,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val stickerOptions = listOf(
                                        "🔥 100% VIBES", "❤️ LOVE IT", "⚡ MOOD", "❓ ASK ME",
                                        "✨ GLOW", "🎉 PARTY", "☕ CHILL", "🚀 ON THE MOVE"
                                    )
                                    items(stickerOptions) { s ->
                                        Surface(
                                            color = if (stickers.contains(s)) PrimaryViolet else Color(0xFF27272A),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.clickable {
                                                if (stickers.contains(s)) stickers.remove(s)
                                                else stickers.add(s)
                                            }
                                        ) {
                                            Text(
                                                text = s,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "text" -> {
                            // Text Overlay Input & Font selector
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = textOverlay,
                                    onValueChange = { textOverlay = it },
                                    placeholder = { Text("Add text on your post...", color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = SecondaryPink,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("Modern", "Neon", "Serif", "Monospace").forEach { f ->
                                        val isSel = textFontFamily == f
                                        Surface(
                                            color = if (isSel) PrimaryViolet else Color(0xFF27272A),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable { textFontFamily = f }
                                        ) {
                                            Text(
                                                text = f,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    if (textOverlay.isNotBlank()) {
                                        TextButton(onClick = { textOverlay = "" }) {
                                            Text("Clear", color = Color.Red, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        "crop" -> {
                            // Aspect Ratio selection
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    Pair("original", "Original"),
                                    Pair("1:1", "1:1 Square"),
                                    Pair("4:5", "4:5 Portrait"),
                                    Pair("9:16", "9:16 Story/Reel")
                                ).forEach { (ratio, label) ->
                                    val isSel = selectedRatio == ratio
                                    Surface(
                                        color = if (isSel) PrimaryViolet else Color(0xFF27272A),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickable { selectedRatio = ratio }
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = ratio.uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = label,
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
