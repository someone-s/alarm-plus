package com.eden.alarmplus.ui.alarms.edit.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.ui.theme.PolygonShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GroupTitle(
    title: String?,
    setTitle: (String?) -> Unit,
) {
    val clip = remember { PolygonShape(MaterialShapes.Cookie6Sided) }
    var showTitleInput by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title ?: "Unnamed",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.clickable {
                showTitleInput = true
            },
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            modifier = Modifier
                .size(56.dp)
                .clip(clip),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            shape = RectangleShape,
            onClick = {
                showTitleInput = true
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = stringResource(R.string.main_ui_alarms_edit_group_grouptitle_action),
            )
        }
    }

    if (showTitleInput)
        TitleInput(
            title = title,
            onSubmit = { newTitle ->
                setTitle(newTitle)
                showTitleInput = false
            },
            onDismiss = {
                showTitleInput = false
            },
        )
}