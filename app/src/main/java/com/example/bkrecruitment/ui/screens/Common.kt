package com.example.bkrecruitment.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bkrecruitment.ui.theme.AppColors

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.PrimaryText,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.SecondaryText,
            )
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    accent: String,
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(AppColors.Highlight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = accent,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.HighlightText,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.PrimaryText,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.SecondaryText,
            )
            if (actionLabel != null && onClick != null) {
                TextButton(onClick = onClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun MetaRow(left: String, right: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = left,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.PrimaryText,
        )
        Text(
            text = right,
            style = MaterialTheme.typography.labelLarge,
            color = AppColors.SecondaryText,
        )
    }
}

@Composable
fun ItemDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = AppColors.Divider,
    )
}

@Composable
fun ActionRow(
    leftLabel: String,
    rightLabel: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextButton(
            onClick = onLeftClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(leftLabel)
        }
        TextButton(
            onClick = onRightClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(rightLabel)
        }
    }
}
