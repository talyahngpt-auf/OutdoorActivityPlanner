package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils


import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


object EmojiPicker {


    fun showNativeEmojiKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}


@Composable
fun EmojiTextField(
    emoji: String,
    onEmojiChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Tap to choose emoji"
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isFocused by remember { mutableStateOf(false) }


    OutlinedTextField(
        value = emoji,
        onValueChange = { newValue ->
            // Only take the first emoji character
            if (newValue.isNotEmpty()) {
                onEmojiChange(newValue.take(2)) // Take 2 to handle complex emojis
            } else {
                onEmojiChange(newValue)
            }
        },
        modifier = modifier,
        textStyle = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        ),
        placeholder = {
            Text(
                text = "😀",
                style = TextStyle(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        },
        singleLine = true,
        label = { Text("Activity Icon") }
    )
}


/**
 * A button that shows a large emoji and opens the emoji picker when clicked
 */
@Composable
fun EmojiPickerButton(
    selectedEmoji: String,
    onEmojiChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }


    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = selectedEmoji.ifEmpty { "😀" },
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("Choose Icon")
    }


    if (showDialog) {
        EmojiPickerDialog(
            currentEmoji = selectedEmoji,
            onDismiss = { showDialog = false },
            onEmojiSelected = { emoji ->
                onEmojiChange(emoji)
                showDialog = false
            }
        )
    }
}


/**
 * Dialog that provides an emoji input field
 * Users can type or use their device's emoji keyboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerDialog(
    currentEmoji: String,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    var tempEmoji by remember { mutableStateOf(currentEmoji.ifEmpty { "📌" }) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose an Icon") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tap the field below and use your emoji keyboard",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )


                Spacer(modifier = Modifier.height(16.dp))


                // Large preview
                Surface(
                    modifier = Modifier.size(100.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tempEmoji,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))


                // Emoji input field
                EmojiTextField(
                    emoji = tempEmoji,
                    onEmojiChange = { tempEmoji = it },
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = "💡 Tip: Long-press emojis for variations",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEmojiSelected(tempEmoji) },
                enabled = tempEmoji.isNotEmpty()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


/**
 * Quick emoji selector with common options plus access to full keyboard
 * Best of both worlds: quick picks + full emoji access
 */
@Composable
fun QuickEmojiSelector(
    selectedEmoji: String,
    onEmojiChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullPicker by remember { mutableStateOf(false) }


    Column(modifier = modifier) {
        Text(
            text = "Quick Pick",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Quick pick emojis (most commonly used for activities)
        val quickEmojis = listOf(
            "📌", "✅", "🎯", "⭐", "💡", "🔥", "💪", "🎨",
            "📚", "✍️", "🎵", "🎮", "☕", "🍕", "🏃", "🚴",
            "🏊", "⛰️", "🏖️", "🧘", "🛍️", "🎬", "📸", "🌳"
        )


        // Grid of quick emojis
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            quickEmojis.chunked(8).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { emoji ->
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onEmojiChange(emoji) },
                            color = if (emoji == selectedEmoji)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small,
                            tonalElevation = if (emoji == selectedEmoji) 4.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(12.dp))


        // Button to access full emoji keyboard
        OutlinedButton(
            onClick = { showFullPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("More Emojis...")
        }
    }


    if (showFullPicker) {
        EmojiPickerDialog(
            currentEmoji = selectedEmoji,
            onDismiss = { showFullPicker = false },
            onEmojiSelected = { emoji ->
                onEmojiChange(emoji)
                showFullPicker = false
            }
        )
    }
}

