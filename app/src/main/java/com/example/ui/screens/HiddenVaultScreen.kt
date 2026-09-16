package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.MediaThumbnail
import com.example.ui.theme.GalleryGradient
import com.example.ui.theme.PurpleAccent

@Composable
fun HiddenVaultScreen(
    isUnlocked: Boolean,
    isPinSet: Boolean,
    hiddenList: List<MediaItem>,
    gridColumns: Int,
    onBack: () -> Unit,
    onUnlock: (String) -> Boolean,
    onSetPin: (String) -> Unit,
    onLock: () -> Unit,
    onUnhideItem: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var setupStep by remember { mutableStateOf(1) } // 1: enter new pin, 2: confirm pin
    var tempPin by remember { mutableStateOf("") }
    var selectedItemForAction by remember { mutableStateOf<MediaItem?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("vault_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = "Hidden Vault",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isUnlocked) "${hiddenList.size} hidden items" else "PIN Protected",
                                style = MaterialTheme.typography.labelSmall,
                                color = PurpleAccent
                            )
                        }
                    }

                    if (isUnlocked) {
                        IconButton(
                            onClick = onLock,
                            modifier = Modifier.testTag("vault_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Vault",
                                tint = PurpleAccent
                            )
                        }
                    }
                }
            }

            if (!isUnlocked) {
                // PIN Lock / Setup Keypad Screen
                PinKeypadView(
                    isPinSet = isPinSet,
                    enteredPin = enteredPin,
                    pinError = pinError,
                    setupStep = setupStep,
                    onNumberClick = { digit ->
                        if (enteredPin.length < 4) {
                            enteredPin += digit
                            pinError = null
                            if (enteredPin.length == 4) {
                                if (!isPinSet) {
                                    // Setup Flow
                                    if (setupStep == 1) {
                                        tempPin = enteredPin
                                        enteredPin = ""
                                        setupStep = 2
                                    } else {
                                        if (enteredPin == tempPin) {
                                            onSetPin(enteredPin)
                                            enteredPin = ""
                                            setupStep = 1
                                        } else {
                                            pinError = "PINs do not match. Try again."
                                            enteredPin = ""
                                            setupStep = 1
                                        }
                                    }
                                } else {
                                    // Unlock Flow
                                    val success = onUnlock(enteredPin)
                                    if (!success) {
                                        pinError = "Incorrect PIN"
                                        enteredPin = ""
                                    }
                                }
                            }
                        }
                    },
                    onBackspace = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            pinError = null
                        }
                    }
                )
            } else {
                // Unlocked Vault Media Grid
                if (hiddenList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = PurpleAccent.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Vault is Empty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To hide photos or videos, open any photo and tap the 'Hide' button in the viewer.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize().testTag("vault_grid")
                    ) {
                        item(span = { GridItemSpan(gridColumns) }) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = PurpleAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Items stored here are hidden from the normal gallery. Tap an item to unhide or delete it.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(hiddenList, key = { it.id }) { item ->
                            MediaThumbnail(
                                item = item,
                                isFavorite = false,
                                onClick = { selectedItemForAction = item }
                            )
                        }
                    }
                }
            }
        }
    }

    // Action dialog for hidden item
    selectedItemForAction?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItemForAction = null },
            title = { Text(text = item.displayName) },
            text = { Text("What would you like to do with this hidden file?") },
            confirmButton = {
                Button(
                    onClick = {
                        onUnhideItem(item.id)
                        selectedItemForAction = null
                    },
                    modifier = Modifier.testTag("vault_dialog_unhide_button")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unhide to Gallery")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onDeleteItem(item.id)
                        selectedItemForAction = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("vault_dialog_delete_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Permanently")
                }
            }
        )
    }
}

@Composable
private fun PinKeypadView(
    isPinSet: Boolean,
    enteredPin: String,
    pinError: String?,
    setupStep: Int,
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GalleryGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = when {
                !isPinSet && setupStep == 1 -> "Create a 4-Digit PIN"
                !isPinSet && setupStep == 2 -> "Confirm Your 4-Digit PIN"
                else -> "Enter PIN to Unlock"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (!isPinSet) "Your PIN secures hidden photos and videos locally on this phone." else "Protecting your private media vault",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN bullet indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) PurpleAccent else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        if (pinError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = pinError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Numeric Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(68.dp))
                        } else if (key == "DEL") {
                            Surface(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onBackspace)
                                    .testTag("pin_key_del"),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable { onNumberClick(key) }
                                    .testTag("pin_key_$key"),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape,
                                tonalElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
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
