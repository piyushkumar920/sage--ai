package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.auth.AuthViewModel
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

enum class AuthScreenMode {
    LOGIN,
    CREATE_ACCOUNT,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    initialMode: AuthScreenMode = AuthScreenMode.LOGIN,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMode by remember { mutableStateOf(initialMode) }
    val uiState by authViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Form fields state
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentMode) {
                                AuthScreenMode.LOGIN -> "Account Login"
                                AuthScreenMode.CREATE_ACCOUNT -> "Create Account"
                                AuthScreenMode.FORGOT_PASSWORD -> "Reset Password"
                            },
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                authViewModel.clearState()
                                onBack()
                            },
                            modifier = Modifier.testTag("auth_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SageTextPrimary
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                authViewModel.clearState()
                                onBack()
                            },
                            modifier = Modifier.testTag("auth_continue_guest_button")
                        ) {
                            Text(
                                text = "Guest Mode",
                                color = SagePrimaryLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header brand badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SageGlassL2)
                        .border(1.dp, SageGlassBorderGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌿",
                        fontSize = 26.sp
                    )
                }

                Text(
                    text = when (currentMode) {
                        AuthScreenMode.LOGIN -> "Welcome back to Sage"
                        AuthScreenMode.CREATE_ACCOUNT -> "Join Sage AI Learning"
                        AuthScreenMode.FORGOT_PASSWORD -> "Recover Your Account"
                    },
                    color = SageTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when (currentMode) {
                        AuthScreenMode.LOGIN -> "Log in with your Firebase account to link your learning journey."
                        AuthScreenMode.CREATE_ACCOUNT -> "Create your credentials securely with Firebase Authentication."
                        AuthScreenMode.FORGOT_PASSWORD -> "Enter your registered email and we'll send a password reset link."
                    },
                    color = SageTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Error Message banner (if any)
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState.errorMessage != null) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_error_card"),
                            level = GlassLevel.L1,
                            borderColor = SageError.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = SageError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = uiState.errorMessage.orEmpty(),
                                        color = SageError,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (currentMode == AuthScreenMode.LOGIN) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SageGlassL2)
                                            .clickable {
                                                authViewModel.clearState()
                                                currentMode = AuthScreenMode.CREATE_ACCOUNT
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "New to Sage? Tap to create an account",
                                            color = SagePrimaryLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = SagePrimaryLight,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Success Message banner (if any)
                AnimatedVisibility(
                    visible = uiState.successMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState.successMessage != null) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_success_card"),
                            level = GlassLevel.L1,
                            borderColor = SageSuccess.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SageSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = uiState.successMessage.orEmpty(),
                                    color = SageSuccess,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Main Glass Form Container
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_form_container"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Full Name (Only for Create Account)
                        if (currentMode == AuthScreenMode.CREATE_ACCOUNT) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name", color = SageTextSecondary, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SagePrimaryLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                colors = authTextFieldColors(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )
                        }

                        // Email Field (All modes)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", color = SageTextSecondary, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = SagePrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = if (currentMode == AuthScreenMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = authTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        // Password Field (LOGIN and CREATE_ACCOUNT only)
                        if (currentMode != AuthScreenMode.FORGOT_PASSWORD) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", color = SageTextSecondary, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = SagePrimaryLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = SageTextSecondary
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (currentMode == AuthScreenMode.CREATE_ACCOUNT) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    onDone = { focusManager.clearFocus() }
                                ),
                                colors = authTextFieldColors(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_password_input")
                            )
                        }

                        // Confirm Password (CREATE_ACCOUNT only)
                        if (currentMode == AuthScreenMode.CREATE_ACCOUNT) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password", color = SageTextSecondary, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = SagePrimaryLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                            tint = SageTextSecondary
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                colors = authTextFieldColors(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input")
                            )
                        }

                        // Forgot Password Link (Only in Login mode)
                        if (currentMode == AuthScreenMode.LOGIN) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    color = SageGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable {
                                            authViewModel.clearState()
                                            currentMode = AuthScreenMode.FORGOT_PASSWORD
                                        }
                                        .padding(vertical = 4.dp)
                                        .testTag("auth_forgot_password_button")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Primary Action Button
                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = SagePrimary,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            GlassButton(
                                text = when (currentMode) {
                                    AuthScreenMode.LOGIN -> "Login"
                                    AuthScreenMode.CREATE_ACCOUNT -> "Create Account"
                                    AuthScreenMode.FORGOT_PASSWORD -> "Send Reset Email"
                                },
                                onClick = {
                                    focusManager.clearFocus()
                                    when (currentMode) {
                                        AuthScreenMode.LOGIN -> {
                                            authViewModel.login(email, password) {
                                                onAuthSuccess()
                                            }
                                        }
                                        AuthScreenMode.CREATE_ACCOUNT -> {
                                            authViewModel.createAccount(
                                                name = fullName,
                                                email = email,
                                                password = password,
                                                confirmPassword = confirmPassword
                                            ) {
                                                onAuthSuccess()
                                            }
                                        }
                                        AuthScreenMode.FORGOT_PASSWORD -> {
                                            authViewModel.sendPasswordReset(email) {
                                                // Kept on screen to display confirmation message
                                            }
                                        }
                                    }
                                },
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_primary_action_button")
                            )
                        }

                        HorizontalDivider(
                            color = SageGlassBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Mode Switching Toggles
                        when (currentMode) {
                            AuthScreenMode.LOGIN -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Don't have an account?",
                                        color = SageTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Create Account",
                                        color = SagePrimaryLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .clickable {
                                                authViewModel.clearState()
                                                currentMode = AuthScreenMode.CREATE_ACCOUNT
                                            }
                                            .padding(4.dp)
                                            .testTag("auth_switch_to_create_button")
                                    )
                                }
                            }
                            AuthScreenMode.CREATE_ACCOUNT -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Already have an account?",
                                        color = SageTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Login",
                                        color = SagePrimaryLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .clickable {
                                                authViewModel.clearState()
                                                currentMode = AuthScreenMode.LOGIN
                                            }
                                            .padding(4.dp)
                                            .testTag("auth_switch_to_login_button")
                                    )
                                }
                            }
                            AuthScreenMode.FORGOT_PASSWORD -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Remembered your credentials?",
                                        color = SageTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Back to Login",
                                        color = SagePrimaryLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .clickable {
                                                authViewModel.clearState()
                                                currentMode = AuthScreenMode.LOGIN
                                            }
                                            .padding(4.dp)
                                            .testTag("auth_back_to_login_from_reset_button")
                                    )
                                }
                            }
                        }
                    }
                }

                // Security & Privacy Notice Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🔒 Security & Account Architecture",
                            color = SageTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Credentials are authenticated directly with Google Firebase over encrypted TLS. Passwords are never stored on device or sent to any custom server. Guest progress is fully preserved.",
                            color = SageTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SageGlassL1,
    unfocusedContainerColor = SageGlassL1,
    disabledContainerColor = SageGlassL1,
    focusedBorderColor = SagePrimaryLight,
    unfocusedBorderColor = SageGlassBorder,
    focusedTextColor = SageTextPrimary,
    unfocusedTextColor = SageTextPrimary,
    cursorColor = SagePrimaryLight
)
