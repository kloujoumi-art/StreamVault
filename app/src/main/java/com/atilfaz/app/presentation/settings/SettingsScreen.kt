package com.atilfaz.app.presentation.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.logoutConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLogoutConfirmation,
            containerColor = AtilfazCardAlt,
            title = { Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out?", color = AtilfazTextSecond) },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(onLogout) }) {
                    Text("SIGN OUT", color = AtilfazLiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLogoutConfirmation) {
                    Text("CANCEL", color = AtilfazTextSecond)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AtilfazBackground)) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF0A0A0A))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Settings, null, tint = AtilfazBlueLight, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("SETTINGS", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 92.dp)
        ) {
            // ── Account Card ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(colors = listOf(AtilfazBlueDark, Color(0xFF0D47A1)))
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Avatar
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(AtilfazBlueLight.copy(0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.username.ifEmpty { "User" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = state.serverUrl.take(36).let { if (state.serverUrl.length > 36) "$it..." else it },
                            fontSize = 12.sp,
                            color = Color.White.copy(0.7f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AtilfazGreen.copy(0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AtilfazGreen))
                            Text("ACTIVE", fontSize = 10.sp, color = AtilfazGreen, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Appearance ────────────────────────────────────────────────
            SettingsSection(title = "APPEARANCE", icon = Icons.Default.Palette) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    SettingsTile(
                        icon = Icons.Default.DarkMode,
                        label = "Theme",
                        value = when (state.themeMode) { "dark" -> "Dark"; "light" -> "Light"; else -> "System" },
                        onClick = { expanded = true }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier.background(AtilfazCardAlt)) {
                        listOf("system" to "System Default", "dark" to "Dark Mode", "light" to "Light Mode").forEach { (k, v) ->
                            DropdownMenuItem(
                                text = { Text(v, color = if (state.themeMode == k) AtilfazBlueLight else Color.White, fontSize = 14.sp) },
                                onClick = { viewModel.setThemeMode(k); expanded = false },
                                leadingIcon = {
                                    if (state.themeMode == k) Icon(Icons.Default.Check, null, tint = AtilfazBlueLight, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Playback ──────────────────────────────────────────────────
            SettingsSection(title = "PLAYBACK", icon = Icons.Default.PlayCircle) {
                SettingsToggle(
                    icon = Icons.Default.PlayArrow,
                    label = "Auto Play",
                    description = "Automatically play next episode",
                    checked = state.autoPlay,
                    onCheckedChange = viewModel::setAutoPlay
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AtilfazDivider)
                SettingsToggle(
                    icon = Icons.Default.History,
                    label = "Resume Playback",
                    description = "Continue from where you left off",
                    checked = state.resumePlayback,
                    onCheckedChange = viewModel::setResumePlayback
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AtilfazDivider)
                var qualityExpanded by remember { mutableStateOf(false) }
                Box {
                    SettingsTile(
                        icon = Icons.Default.HighQuality,
                        label = "Video Quality",
                        value = when (state.videoQuality) { "hd" -> "HD"; "sd" -> "SD"; else -> "Auto" },
                        onClick = { qualityExpanded = true }
                    )
                    DropdownMenu(expanded = qualityExpanded, onDismissRequest = { qualityExpanded = false },
                        modifier = Modifier.background(AtilfazCardAlt)) {
                        listOf("auto" to "Auto", "hd" to "HD (720p+)", "sd" to "SD (480p)").forEach { (k, v) ->
                            DropdownMenuItem(
                                text = { Text(v, color = if (state.videoQuality == k) AtilfazBlueLight else Color.White, fontSize = 14.sp) },
                                onClick = { viewModel.setVideoQuality(k); qualityExpanded = false },
                                leadingIcon = {
                                    if (state.videoQuality == k) Icon(Icons.Default.Check, null, tint = AtilfazBlueLight, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Storage ───────────────────────────────────────────────────
            SettingsSection(title = "STORAGE", icon = Icons.Default.Storage) {
                SettingsAction(icon = Icons.Default.History, label = "Clear Watch History", color = AtilfazWarning, onClick = viewModel::clearHistory)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AtilfazDivider)
                SettingsAction(icon = Icons.Default.FavoriteBorder, label = "Clear Favorites", color = AtilfazWarning, onClick = viewModel::clearFavorites)
            }

            Spacer(Modifier.height(12.dp))

            // ── About ─────────────────────────────────────────────────────
            SettingsSection(title = "ABOUT", icon = Icons.Default.Info) {
                SettingsInfoRow(label = "Application", value = Constants.APP_NAME)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AtilfazDivider)
                SettingsInfoRow(label = "Version", value = "1.0.0")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AtilfazDivider)
                SettingsInfoRow(label = "API", value = "Xtream Codes")
            }

            Spacer(Modifier.height(20.dp))

            // ── Logout ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AtilfazLiveRed.copy(0.12f))
                    .clickable { viewModel.showLogoutConfirmation() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Logout, null, tint = AtilfazLiveRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("SIGN OUT", color = AtilfazLiveRed, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = AtilfazBlueLight, modifier = Modifier.size(15.dp))
            Text(title, fontSize = 11.sp, color = AtilfazBlueLight, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AtilfazCard)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsTile(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = AtilfazTextSecond, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = AtilfazBlueLight)
        Icon(Icons.Default.ChevronRight, null, tint = AtilfazTextHint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsToggle(icon: ImageVector, label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = AtilfazTextSecond, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, color = Color.White)
            Text(description, fontSize = 12.sp, color = AtilfazTextHint)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AtilfazBlue,
                uncheckedThumbColor = AtilfazTextHint,
                uncheckedTrackColor = AtilfazCard
            )
        )
    }
}

@Composable
private fun SettingsAction(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 14.sp, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = AtilfazTextHint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = AtilfazTextSecond)
        Text(value, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}
