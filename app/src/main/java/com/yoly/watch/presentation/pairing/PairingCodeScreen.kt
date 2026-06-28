package com.yoly.watch.presentation.pairing

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.tooling.preview.devices.WearDevices
import com.yoly.watch.R
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.presentation.theme.YolywatchTheme

@Composable
fun PairingCodeRoute(
    viewModel: PairingViewModel = viewModel(factory = PairingViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    PairingCodeScreen(
        uiState = uiState,
        syncStatus = syncStatus,
        onRetry = viewModel::loadCode,
        onContinue = viewModel::goToHome,
        onRePair = viewModel::rePair,
        onSync = viewModel::syncNow,
    )
}

@Composable
fun PairingCodeScreen(
    uiState: PairingUiState,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    onRePair: () -> Unit,
    syncStatus: SyncUiState = SyncUiState.Idle,
    onSync: () -> Unit = {},
) {
    YolywatchTheme {
        val view = LocalView.current
        val keepScreenOn = uiState is PairingUiState.Loading || uiState is PairingUiState.Success
        if (!LocalInspectionMode.current) {
            DisposableEffect(keepScreenOn) {
                view.keepScreenOn = keepScreenOn
                onDispose { view.keepScreenOn = false }
            }
        }

        val scrollState = rememberScrollState()
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            positionIndicator = { PositionIndicator(scrollState = scrollState) },
        ) {
            if (uiState is PairingUiState.Home) {
                HomePager(syncStatus = syncStatus, onSync = onSync, onRePair = onRePair)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                ) {
                    when (uiState) {
                        PairingUiState.Loading -> LoadingContent()
                        is PairingUiState.Success -> SuccessContent(uiState)
                        PairingUiState.Confirmed -> ConfirmedContent(onContinue)
                        is PairingUiState.Error -> ErrorContent(uiState.message, onRetry)
                        PairingUiState.Home -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
    Text(
        text = stringResource(R.string.pairing_loading),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.caption2,
        color = MaterialTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SuccessContent(state: PairingUiState.Success) {
    Text(
        text = stringResource(R.string.pairing_title).uppercase(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.caption1,
        color = MaterialTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )

    CodeRing(state = state)

    Text(
        text = stringResource(R.string.pairing_expires_in, state.remainingSeconds),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.caption2,
        color = MaterialTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
        text = stringResource(R.string.pairing_instructions),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body2,
        color = MaterialTheme.colors.onBackground,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CodeRing(state: PairingUiState.Success) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        label = "codeRingProgress",
    )
    Box(
        modifier = Modifier.size(124.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 5.dp,
            indicatorColor = MaterialTheme.colors.primary,
            trackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.15f),
        )
        Text(
            text = state.code.value.formatAsGroupedCode(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.display2.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
        )
    }
}

@Composable
private fun ConfirmedContent(onContinue: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val inInspection = LocalInspectionMode.current
    val scale = remember { Animatable(if (inInspection) 1f else 0f) }

    if (!inInspection) {
        val toneGenerator = remember {
            runCatching {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME)
            }.getOrNull()
        }
        DisposableEffect(Unit) {
            onDispose { toneGenerator?.release() }
        }

        LaunchedEffect(Unit) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    Text(
        text = "✓",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        style = MaterialTheme.typography.display1.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value),
    )
    Text(
        text = stringResource(R.string.pairing_confirmed_title),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.title3,
        color = MaterialTheme.colors.onBackground,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.pairing_confirmed_message),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body2,
        color = MaterialTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
    Chip(
        onClick = onContinue,
        label = {
            Text(
                text = stringResource(R.string.pairing_continue),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        colors = ChipDefaults.primaryChipColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
    )
}

@Composable
private fun HomePager(
    syncStatus: SyncUiState,
    onSync: () -> Unit,
    onRePair: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            0 -> HomePage(syncStatus = syncStatus, onSync = onSync)
            else -> RePairPage(onRePair = onRePair)
        }
    }
}

@Composable
private fun HomePage(syncStatus: SyncUiState, onSync: () -> Unit) {
    val subtitle = when (syncStatus) {
        SyncUiState.Idle -> stringResource(R.string.home_message)
        SyncUiState.Syncing -> stringResource(R.string.home_syncing)
        is SyncUiState.Done -> stringResource(R.string.home_sync_done)
        SyncUiState.Error -> stringResource(R.string.home_sync_error)
    }
    val subtitleColor =
        if (syncStatus is SyncUiState.Error) MaterialTheme.colors.error
        else MaterialTheme.colors.onSurfaceVariant

    PageScaffold(
        title = stringResource(R.string.home_title),
        subtitle = subtitle,
        subtitleColor = subtitleColor,
    ) {
        BottomEdgeButton(onClick = onSync, enabled = syncStatus != SyncUiState.Syncing) {
            if (syncStatus == SyncUiState.Syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    indicatorColor = MaterialTheme.colors.onPrimary,
                    trackColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.3f),
                )
            } else {
                Text(
                    text = stringResource(R.string.home_sync),
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.button,
                )
            }
        }
    }
}

@Composable
private fun RePairPage(onRePair: () -> Unit) {
    PageScaffold(
        title = stringResource(R.string.home_repair_title),
        subtitle = stringResource(R.string.home_repair_hint),
    ) {
        BottomEdgeButton(onClick = onRePair) {
            Text(
                text = stringResource(R.string.pairing_repair),
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.button,
            )
        }
    }
}

@Composable
private fun PageScaffold(
    title: String,
    subtitle: String,
    subtitleColor: Color = MaterialTheme.colors.onSurfaceVariant,
    bottomButton: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(top = 28.dp, bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.title2,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = subtitle,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                color = subtitleColor,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            bottomButton()
        }
    }
}

@Composable
private fun BottomEdgeButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(BottomEdgeShape)
            .background(MaterialTheme.colors.primary)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private val BottomEdgeShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val corner = h * 0.35f
    moveTo(corner, 0f)
    lineTo(w - corner, 0f)
    quadraticTo(w, 0f, w, corner)
    lineTo(w, h * 0.55f)
    quadraticTo(w * 0.5f, h, 0f, h * 0.55f)
    lineTo(0f, corner)
    quadraticTo(0f, 0f, corner, 0f)
    close()
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Text(
        text = stringResource(R.string.pairing_error),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body2,
        color = MaterialTheme.colors.error,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = message,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.caption2,
        color = MaterialTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
    Chip(
        onClick = onRetry,
        label = {
            Text(
                text = stringResource(R.string.pairing_retry),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        colors = ChipDefaults.primaryChipColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
    )
}

private fun String.formatAsGroupedCode(): String =
    if (length == 6) "${substring(0, 3)} ${substring(3)}" else this

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun SuccessPreview() {
    PairingCodeScreen(
        uiState = PairingUiState.Success(
            PairingCode("pair-1", "482915", 120),
            remainingSeconds = 78,
        ),
        onRetry = {},
        onContinue = {},
        onRePair = {},
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun ConfirmedPreview() {
    PairingCodeScreen(
        uiState = PairingUiState.Confirmed,
        onRetry = {},
        onContinue = {},
        onRePair = {},
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun HomePreview() {
    PairingCodeScreen(
        uiState = PairingUiState.Home,
        onRetry = {},
        onContinue = {},
        onRePair = {},
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun HomeSyncingPreview() {
    PairingCodeScreen(
        uiState = PairingUiState.Home,
        syncStatus = SyncUiState.Syncing,
        onRetry = {},
        onContinue = {},
        onRePair = {},
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun RePairTilePreview() {
    YolywatchTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            RePairPage(onRePair = {})
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun LoadingPreview() {
    PairingCodeScreen(
        uiState = PairingUiState.Loading,
        onRetry = {},
        onContinue = {},
        onRePair = {},
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun ErrorPreview() {
    PairingCodeScreen(
        uiState = PairingUiState.Error("Délai dépassé"),
        onRetry = {},
        onContinue = {},
        onRePair = {},
    )
}
