package com.nextdoor.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.data.dto.AddressDto
import com.nextdoor.app.ui.components.DangerButton
import com.nextdoor.app.ui.components.ErrorBanner
import com.nextdoor.app.ui.components.ErrorState
import com.nextdoor.app.ui.components.GhostButton
import com.nextdoor.app.ui.components.PillTextField
import com.nextdoor.app.ui.components.PlainTopBar
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.components.TextButton
import com.nextdoor.app.ui.components.ToastController
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalDivider
import com.nextdoor.app.ui.theme.Olive50
import com.nextdoor.app.ui.theme.Olive200
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.Olive700
import com.nextdoor.app.ui.theme.Rose600
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.theme.TextStyles

@Composable
fun ProfileScreen(onLoggedOut: () -> Unit) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBg)
    ) {
        PlainTopBar(title = "Perfil")

        when {
            state.loading -> ProfileSkeleton()
            state.error -> ErrorState(title = "Não foi possível carregar seu perfil", onRetry = { viewModel.load() })
            else -> ProfileContent(state = state, viewModel = viewModel)
        }
    }

    state.editingAddress?.let { address ->
        EditAddressDialog(
            address = address,
            saving = state.addressSaving,
            error = state.addressError,
            onDismiss = viewModel::cancelEdit,
            onSave = viewModel::saveAddress
        )
    }

    if (state.confirmDelete) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Excluir conta?", fontFamily = SerifFamily, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("Esta ação é permanente.", fontSize = 12.sp, color = TextMuted) },
            confirmButton = {
                TextButton(text = "Excluir", onClick = viewModel::deleteAccount, color = Rose600)
            },
            dismissButton = {
                TextButton(text = "Cancelar", onClick = viewModel::cancelDelete, color = TextPrimary)
            }
        )
    }
}

@Composable
private fun ProfileContent(state: ProfileUiState, viewModel: ProfileViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProfileHeader(username = state.username, email = state.email) }

        item {
            ProfileCard(title = "Conta") {
                if (!state.saveError.isNullOrBlank()) {
                    ErrorBanner(message = state.saveError!!)
                    Spacer(Modifier.height(12.dp))
                }
                PillTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "E-mail",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = !state.saving
                )
                Spacer(Modifier.height(12.dp))
                PillTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    placeholder = "Nome de usuário",
                    imeAction = ImeAction.Done,
                    onImeAction = { viewModel.saveAccount() },
                    enabled = !state.saving
                )
                Spacer(Modifier.height(16.dp))
                SaveAccountButton(loading = state.saving, onClick = viewModel::saveAccount)
            }
        }

        item {
            ProfileCard(title = "Endereços") {
                if (state.addresses.isEmpty()) {
                    Text("Nenhum endereço cadastrado", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.height(12.dp))
                } else {
                    state.addresses.forEachIndexed { index, address ->
                        AddressRow(
                            address = address,
                            isPrimary = index == 0,
                            onEdit = { viewModel.startEdit(address) }
                        )
                        if (index != state.addresses.lastIndex) {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                GhostButton(
                    text = "+ Adicionar endereço",
                    onClick = { ToastController.show("Em breve") },
                    modifier = Modifier.fillMaxWidth(),
                    textColor = Olive700
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DangerButton(
                    text = "Sair",
                    onClick = viewModel::logout,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    text = "Excluir conta",
                    onClick = viewModel::requestDelete,
                    modifier = Modifier.fillMaxWidth(),
                    color = Rose600
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(username: String, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Olive50, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(username),
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Olive700
            )
        }
        Column {
            Text(
                text = username,
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Text(text = email, fontSize = 12.sp, color = TextMuted)
        }
    }
}

private fun initialsOf(username: String): String =
    username.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
        .ifBlank { "?" }

@Composable
private fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(text = title, style = TextStyles.Eyebrow, color = TextMuted)
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun SaveAccountButton(loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TextPrimary, RoundedCornerShape(percent = 50))
            .clickable(enabled = !loading, onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Salvar alterações",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddressRow(address: AddressDto, isPrimary: Boolean, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPrimary) Olive50 else Color.White)
            .border(1.dp, if (isPrimary) Olive600 else NaturalBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = if (isPrimary) Olive600 else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Endereço",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${address.street}, ${address.addressNumber}",
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${address.neighborhood} · CEP ${address.cep}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Editar endereço",
            tint = TextMuted,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onEdit)
        )
    }
}

@Composable
private fun EditAddressDialog(
    address: AddressDto,
    saving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var street by remember(address.id) { mutableStateOf(address.street) }
    var addressNumber by remember(address.id) { mutableStateOf(address.addressNumber) }
    var neighborhood by remember(address.id) { mutableStateOf(address.neighborhood) }
    var cep by remember(address.id) { mutableStateOf(address.cep) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Editar endereço", fontFamily = SerifFamily, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!error.isNullOrBlank()) {
                    ErrorBanner(message = error!!)
                }
                PillTextField(
                    value = street,
                    onValueChange = { street = it },
                    placeholder = "Rua",
                    enabled = !saving
                )
                PillTextField(
                    value = addressNumber,
                    onValueChange = { addressNumber = it },
                    placeholder = "Número",
                    keyboardType = KeyboardType.Number,
                    enabled = !saving
                )
                PillTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    placeholder = "Bairro",
                    enabled = !saving
                )
                PillTextField(
                    value = cep,
                    onValueChange = { cep = it.filter { c -> c.isDigit() }.take(8) },
                    placeholder = "CEP",
                    keyboardType = KeyboardType.Number,
                    enabled = !saving
                )
            }
        },
        confirmButton = {
            TextButton(
                text = "Salvar",
                onClick = { onSave(street.trim(), addressNumber.trim(), neighborhood.trim(), cep.trim()) },
                color = Olive600
            )
        },
        dismissButton = {
            TextButton(text = "Cancelar", onClick = onDismiss, color = TextPrimary)
        }
    )
}

@Composable
private fun ProfileSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBlock(
                modifier = Modifier.size(64.dp),
                shape = CircleShape
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(modifier = Modifier.width(140.dp).height(16.dp), shape = RoundedCornerShape(8.dp))
                SkeletonBlock(modifier = Modifier.width(180.dp).height(12.dp), shape = RoundedCornerShape(6.dp))
            }
        }
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp)
        )
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(24.dp)
        )
    }
}
