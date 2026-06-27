package com.atilfaz.app.presentation.auth

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.rememberIsTV

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val isTV = rememberIsTV()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AtilfazBackground)
    ) {
        if (isTV) {
            // ── TV : layout deux colonnes centré ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 64.dp, vertical = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colonne gauche : logo + branding
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(AtilfazBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "ATILFAZ",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Premium Streaming",
                        fontSize = 18.sp,
                        color = AtilfazTextSecond,
                        textAlign = TextAlign.Center
                    )
                }

                // Colonne droite : formulaire
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Connexion",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    SmField(
                        value = state.serverUrl,
                        onValueChange = viewModel::onServerUrlChange,
                        label = "URL du serveur Xtream",
                        placeholder = "http://votre-serveur.com:8080",
                        icon = Icons.Default.Link,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                        isTV = true
                    )

                    SmField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = "Nom d'utilisateur",
                        placeholder = "Votre identifiant",
                        icon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                        isTV = true
                    )

                    SmField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Mot de passe",
                        placeholder = "Votre mot de passe",
                        icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus(); viewModel.login() },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = AtilfazTextHint
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isTV = true
                    )

                    if (state.error != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF3A0000))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = AtilfazLiveRed, modifier = Modifier.size(20.dp))
                            Text(state.error ?: "", fontSize = 15.sp, color = Color(0xFFFF8A80))
                        }
                    }

                    Button(
                        onClick = viewModel::login,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AtilfazBlue,
                            disabledContainerColor = AtilfazBlue.copy(alpha = 0.4f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(Modifier.size(24.dp), Color.White, 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Connexion...", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("SE CONNECTER", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }

                    Text(
                        "Utilisez la télécommande pour naviguer entre les champs",
                        fontSize = 12.sp,
                        color = AtilfazTextHint,
                        textAlign = TextAlign.Center
                    )
                }
            }

        } else {
            // ── Téléphone : layout vertical ────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AtilfazBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircleFilled, null, tint = Color.White, modifier = Modifier.size(54.dp))
                }

                Spacer(Modifier.height(20.dp))

                Text("ATILFAZ", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 3.sp)
                Spacer(Modifier.height(4.dp))
                Text("Premium Streaming Experience", fontSize = 13.sp, color = AtilfazTextSecond, textAlign = TextAlign.Center)

                Spacer(Modifier.height(44.dp))

                SmField(
                    value = state.serverUrl,
                    onValueChange = viewModel::onServerUrlChange,
                    label = "URL Xtream",
                    placeholder = "http://votre-serveur.com:8080",
                    icon = Icons.Default.Link,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(Modifier.height(14.dp))

                SmField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Nom d'utilisateur",
                    placeholder = "Votre identifiant",
                    icon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(Modifier.height(14.dp))

                SmField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Mot de passe",
                    placeholder = "Votre mot de passe",
                    icon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus(); viewModel.login() },
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = AtilfazTextHint
                            )
                        }
                    },
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )

                if (state.error != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3A0000))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = AtilfazLiveRed, modifier = Modifier.size(16.dp))
                        Text(state.error ?: "", fontSize = 13.sp, color = Color(0xFFFF8A80))
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = viewModel::login,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtilfazBlue,
                        disabledContainerColor = AtilfazBlue.copy(alpha = 0.4f)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), Color.White, 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Connexion...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.PersonAdd, null)
                        Spacer(Modifier.width(8.dp))
                        Text("SE CONNECTER", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text("Propulsé par Xtream Codes API", fontSize = 12.sp, color = AtilfazTextHint, textAlign = TextAlign.Center)
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SmField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isTV: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = if (isTV) 15.sp else 13.sp) },
        placeholder = { Text(placeholder, fontSize = if (isTV) 15.sp else 13.sp, color = AtilfazTextHint) },
        leadingIcon = {
            Icon(icon, null, tint = AtilfazBlueLight, modifier = Modifier.size(if (isTV) 24.dp else 20.dp))
        },
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
        singleLine = true,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(10.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = if (isTV) 17.sp else 15.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AtilfazBlueLight,
            unfocusedBorderColor = AtilfazBorder,
            focusedLabelColor = AtilfazBlueLight,
            unfocusedLabelColor = AtilfazTextHint,
            focusedTextColor = Color.White,
            unfocusedTextColor = AtilfazTextSecond,
            cursorColor = AtilfazBlueLight,
            focusedContainerColor = AtilfazCard,
            unfocusedContainerColor = AtilfazCard
        )
    )
}
