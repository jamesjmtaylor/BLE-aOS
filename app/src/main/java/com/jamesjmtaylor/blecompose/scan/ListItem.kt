package com.jamesjmtaylor.blecompose.scan

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun ListItem(name: String, modifier: Modifier) {
    Text(text = name,
        fontSize = 16.sp,
        color= MaterialTheme.colors.secondaryVariant,
        modifier = modifier)
}
