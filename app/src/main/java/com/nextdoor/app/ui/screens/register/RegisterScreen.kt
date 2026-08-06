package com.nextdoor.app.ui.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nextdoor.app.ui.components.BackTopBar
import com.nextdoor.app.ui.components.ErrorBanner
import com.nextdoor.app.ui.components.PillTextField
import com.nextdoor.app.ui.components.PrimaryButton
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: RegisterViewModel = hiltViewModel()
    val state = viewModel.uiState

    val scrollScope = rememberCoroutineScope()
    val bringIntoView = remember { BringIntoViewRequester() }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            scrollScope.launch { bringIntoView.bringIntoView() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBg)
    ) {
        BackTopBar(title = "Criar conta", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ErrorBanner(
                message = state.error.orEmpty(),
                modifier = Modifier.bringIntoViewRequester(bringIntoView)
            )

            SectionCard(title = "Dados da conta") {
                PillTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "seu@email.com",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    enabled = !state.submitting,
                    autofillTypes = listOf(AutofillType.EmailAddress)
                )
                PillTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    placeholder = "Nome de usuário",
                    leadingIcon = Icons.Default.Person,
                    enabled = !state.submitting,
                    autofillTypes = listOf(AutofillType.Username)
                )
                PillTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Senha",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    enabled = !state.submitting,
                    autofillTypes = listOf(AutofillType.NewPassword)
                )
                Text(
                    text = "Mínimo de 6 caracteres, com pelo menos 1 letra maiúscula e 1 número ou símbolo.",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            SectionCard(title = "Endereço") {
                PillTextField(
                    value = state.street,
                    onValueChange = viewModel::onStreetChange,
                    placeholder = "Rua",
                    leadingIcon = Icons.Default.LocationOn,
                    enabled = !state.submitting
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.width(96.dp)) {
                        PillTextField(
                            value = state.number,
                            onValueChange = viewModel::onNumberChange,
                            placeholder = "Número",
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                            enabled = !state.submitting
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PillTextField(
                            value = state.complement,
                            onValueChange = viewModel::onComplementChange,
                            placeholder = "Complemento (opcional)",
                            enabled = !state.submitting
                        )
                    }
                }
                PillTextField(
                    value = state.neighborhood,
                    onValueChange = viewModel::onNeighborhoodChange,
                    placeholder = "Bairro",
                    enabled = !state.submitting
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PillTextField(
                            value = state.city,
                            onValueChange = viewModel::onCityChange,
                            placeholder = "Cidade",
                            enabled = !state.submitting
                        )
                    }
                    Box(modifier = Modifier.width(64.dp)) {
                        PillTextField(
                            value = state.uf,
                            onValueChange = viewModel::onUfChange,
                            placeholder = "UF",
                            maxLength = 2,
                            enabled = !state.submitting,
                            textAlign = TextAlign.Center,
                            placeholderTextAlign = TextAlign.Center
                        )
                    }
                }
                PillTextField(
                    value = state.cep,
                    onValueChange = viewModel::onCepChange,
                    placeholder = "CEP",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    onImeAction = { viewModel.register(onRegistered) },
                    maxLength = 8,
                    enabled = !state.submitting,
                    autofillTypes = listOf(AutofillType.PostalCode)
                )
            }

            PrimaryButton(
                text = "Cadastrar",
                onClick = { viewModel.register(onRegistered) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                loading = state.submitting
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = TextMuted
        )
        content()
    }
}
