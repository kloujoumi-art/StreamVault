package com.atilfaz.desktop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atilfaz.desktop.api.XtreamCredentials

@Composable
fun LoginScreen(onLogin: (XtreamCredentials) -> Unit, errorMessage: String?, isLoading: Boolean) {
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Row(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // ── Panneau gauche : branding ──────────────────────────────────────
        Box(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .background(Color(0xFF080818)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(24.dp)).background(BlueMain),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircleFilled, null, tint = Color.White, modifier = Modifier.size(60.dp))
                }
                Text("ATILFAZ IPTV", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 3.sp)
                Text("Version PC Portable", fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(32.dp))
                InfoRow(Icons.Default.Tv, "Live TV, Films, Séries")
                InfoRow(Icons.Default.Wifi, "Streaming Xtream Codes")
                InfoRow(Icons.Default.DevicesOther, "Windows / Linux / Mac")
                InfoRow(Icons.Default.FolderOpen, "Aucune installation requise")
            }
        }

        // ── Panneau droit : formulaire ─────────────────────────────────────
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().background(BgColor),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(420.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(36.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("Connexion", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Entrez vos identifiants Xtream Codes", fontSize = 13.sp, color = TextSecondary)

                    // URL
                    LoginField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = "URL du serveur",
                        placeholder = "http://votre-serveur.com:8080",
                        icon = Icons.Default.Link,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                        onIme = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Username
                    LoginField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Nom d'utilisateur",
                        placeholder = "votre_identifiant",
                        icon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        onIme = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Password
                    LoginField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        placeholder = "votre_mot_de_passe",
                        icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        onIme = {
                            focusManager.clearFocus()
                            if (serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                onLogin(XtreamCredentials(serverUrl.trim(), username.trim(), password.trim()))
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = TextHint
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    // Erreur
                    AnimatedVisibility(errorMessage != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3A0000)).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = LiveRed, modifier = Modifier.size(16.dp))
                            Text(errorMessage ?: "", fontSize = 13.sp, color = Color(0xFFFF8A80))
                        }
                    }

                    // Bouton connexion
                    Button(
                        onClick = {
                            if (serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                onLogin(XtreamCredentials(serverUrl.trim(), username.trim(), password.trim()))
                            }
                        },
                        enabled = !isLoading && serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueMain)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(20.dp), Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Connexion en cours...", fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.Login, null)
                            Spacer(Modifier.width(8.dp))
                            Text("SE CONNECTER", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = BlueLight, modifier = Modifier.size(18.dp))
        Text(text, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onIme: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder, color = TextHint) },
        leadingIcon = { Icon(icon, null, tint = BlueLight) },
        trailingIcon = trailingIcon,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onIme() }, onDone = { onIme() }),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BlueLight,
            unfocusedBorderColor = BorderColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = TextSecondary,
            cursorColor = BlueLight,
            focusedContainerColor = Color(0xFF1A1A1A),
            unfocusedContainerColor = Color(0xFF111111)
        )
    )
}
