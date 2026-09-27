package com.eden.alarmplus.ui.main.alarms.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.ui.common.FormatViewModel
import com.eden.alarmplus.ui.common.FormatViewModelProvider

@Composable
fun GroupItem(
    group: GroupData,
    active: Boolean,
    onSelect: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    shapes: ListItemShapes = ListItemDefaults.segmentedShapes(0, 1),
    formatViewModel: FormatViewModel = viewModel(factory = FormatViewModelProvider.Factory),
) {
    val firstTime = group.alarms.firstOrNull()?.info?.time

    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val dismissBoxState = remember(group) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = positionalThreshold,
        )
    }

    val currentFormatUiState by formatViewModel.currentFormatUiState.collectAsState()

    SwipeToDismissBox(
        state = dismissBoxState,
        onDismiss = { onDelete() },
        backgroundContent = {
            SegmentedListItem(
                shapes = shapes,
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                ),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = stringResource(R.string.ui_main_alarms_overview_groupitem_deleteaction),
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(when (dismissBoxState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
                            }),
                    )
                }
            )
        },
        content = {
            SegmentedListItem(
                shapes = shapes,
                colors = if (active)
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                else
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSelect() },
                content = {
                    Column {
                        Text(
                            text = group.info.name ?: "",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (firstTime != null) currentFormatUiState.timeFormatter.format(firstTime) else "--:--",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.displayMedium,
                        )
                        Row {
                            group.alarms
                                .map { it.info.time }
                                .forEach { remainingTime ->
                                    Text(
                                        text = currentFormatUiState.timeFormatter.format(remainingTime),
                                        fontWeight = FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                        }
                    }
                },
                trailingContent = {
                    Switch(
                        checked = group.info.active,
                        onCheckedChange = onToggle,
                    )
                },
            )
        }
    )
}