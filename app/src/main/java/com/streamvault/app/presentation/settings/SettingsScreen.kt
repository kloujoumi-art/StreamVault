package com.streamvault.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamvault.app.ui.theme.*
import com.streamvault.app.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.logoutConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLogoutConfirmation,
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(onLogout) }) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLogoutConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StreamVaultBlack)
            )
        },
        containerColor = StreamVaultBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account Info
            item {
                SettingsCard {
                    SettingsSectionTitle("Account", Icons.Default.Person)
                    SettingsInfoItem(label = "Server", value = state.serverUrl.take(40))
                    SettingsInfoItem(label = "Username", value = state.username)
                }
            }

            // Appearance
            item {
                SettingsCard {
                    SettingsSectionTitle("Appearance", Icons.Default.Palette)
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        SettingsClickableItem(
                            label = "Theme",
                            value = when (state.themeMode) {
                                "dark" -> "Dark"
                                "light" -> "Light"
                                else -> "System"
                            },
                            icon = Icons.Default.DarkMode,
                            onClick = { expanded = true }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("system" to "System", "dark" to "Dark", "light" to "Light").forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { viewModel.setThemeMode(key); expanded = false },
                                    leadingIcon = {
                                        if (state.themeMode == key) Icon(Icons.Default.Check, null, tint = StreamVaultAccentCyan)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Playback
            item {
                SettingsCard {
                    SettingsSectionTitle("Playback", Icons.Default.PlayCircle)
                    SettingsSwitchItem(
                        label = "Auto Play",
                        description = "Automatically play next episode",
                        checked = state.autoPlay,
                        onCheckedChange = viewModel::setAutoPlay,
                        icon = Icons.Default.PlayArrow
                    )
                    HorizontalDivider(color = Color.White.copy(0.1f))
                    SettingsSwitchItem(
                        label = "Resume Playback",
                        description = "Continue from where you left off",
                        checked = state.resumePlayback,
                        onCheckedChange = viewModel::setResumePlayback,
                        icon = Icons.Default.History
                    )
                    HorizontalDivider(color = Color.White.copy(0.1f))
                    var qualityExpanded by remember { mutableStateOf(false) }
                    Box {
                        SettingsClickableItem(
                            label = "Video Quality",
                            value = when (state.videoQuality) {
                                "hd" -> "HD"
                                "sd" -> "SD"
                                else -> "Auto"
                            },
                            icon = Icons.Default.HighQuality,
                            onClick = { qualityExpanded = true }
                        )
                        DropdownMenu(expanded = qualityExpanded, onDismissRequest = { qualityExpanded = false }) {
                            listOf("auto" to "Auto", "hd" to "HD", "sd" to "SD").forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { viewModel.setVideoQuality(key); qualityExpanded = false },
                                    leadingIcon = {
                                        if (state.videoQuality == key) Icon(Icons.Default.Check, null, tint = StreamVaultAccentCyan)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Storage
            item {
                SettingsCard {
                    SettingsSectionTitle("Storage", Icons.Default.Storage)
                    SettingsActionItem(
                        label = "Clear Watch History",
                        icon = Icons.Default.History,
                        iconTint = StreamVaultWarning,
                        onClick = viewModel::clearHistory
                    )
                    HorizontalDivider(color = Color.White.copy(0.1f))
                    SettingsActionItem(
                        label = "Clear All Favorites",
                        icon = Icons.Default.FavoriteBorder,
                        iconTint = StreamVaultWarning,
                        onClick = viewModel::clearFavorites
                    )
                }
            }

            // About
            item {
                SettingsCard {
                    SettingsSectionTitle("About", Icons.Default.Info)
                    SettingsInfoItem(label = "App", value = Constants.APP_NAME)
                    SettingsInfoItem(label = "Version", value = "1.0.0")
                }
            }

            // Sign Out
            item {
                Button(
                    onClick = viewModel::showLogoutConfirmation,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StreamVaultCardDark)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = StreamVaultAccentCyan, modifier = Modifier.size(18.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = StreamVaultAccentCyan)
    }
}

@Composable
private fun SettingsInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 1)
    }
}

@Composable
private fun SettingsSwitchItem(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.5f))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StreamVaultAccentCyan)
        )
    }
}

@Composable
private fun SettingsClickableItem(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = StreamVaultAccentCyan)
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsActionItem(label: String, icon: ImageVector, iconTint: Color, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = iconTint)
        }
    }
}
