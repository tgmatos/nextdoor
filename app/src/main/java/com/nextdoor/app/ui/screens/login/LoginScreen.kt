package com.nextdoor.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nextdoor.app.ui.components.ErrorBanner
import com.nextdoor.app.ui.components.PillTextField
import com.nextdoor.app.ui.components.PrimaryButton
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.Olive100
import com.nextdoor.app.ui.theme.Olive200
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val state = viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBg)
    ) {
        // Decorative blurred olive circles (behind content, non-interactive).
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-64).dp, y = (-64).dp)
                .size(256.dp)
                .background(Olive200.copy(alpha = 0.4f), CircleShape)
                .blur(48.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 64.dp, y = 80.dp)
                .size(288.dp)
                .background(Olive100.copy(alpha = 0.5f), CircleShape)
                .blur(56.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .widthIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo chip
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .background(Olive600, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "NextDoor",
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "O mercado do seu bairro, na sua porta",
                fontSize = 12.sp,
                color = TextMuted
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PillTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "seu@email.com",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = !state.submitting,
                    autofillTypes = listOf(AutofillType.EmailAddress)
                )
                PillTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Sua senha",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = { viewModel.login(onLoggedIn) },
                    enabled = !state.submitting,
                    autofillTypes = listOf(AutofillType.Password)
                )

                ErrorBanner(message = state.error.orEmpty())

                PrimaryButton(
                    text = "Entrar",
                    onClick = { viewModel.login(onLoggedIn) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.submitting,
                    loading = state.submitting
                )
            }

            Spacer(Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Não tem conta? ", fontSize = 12.sp, color = TextMuted)
                Text(
                    text = "Cadastre-se",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Olive600,
                    modifier = Modifier.clickable(onClick = onNavigateRegister)
                )
            }
        }
    }
}
