package dev.mrichter.mapf.visualiser

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Choice(val label: String, val value: String)

@Composable
@Preview
fun MapSelectMenu(choices: List<Choice>, onSelect: (String) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    val selected = remember { mutableStateOf<Choice?>(null) }
    val width = remember { mutableStateOf(0.dp) }

    Column() {
        OutlinedTextField(
            value = selected.value?.label ?: "",
            onValueChange = {},
            modifier = Modifier
                .onSizeChanged { width.value = it.width.dp }
                .clickable { expanded.value = !expanded.value },
            readOnly = true,
            enabled = false,
            label = { Text("Map selection") },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    "contentDescription"
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.Black,
                disabledLabelColor = Color.Black,
                disabledTrailingIconColor = Color.Black
            )
        )

        DropdownMenu(
            expanded = expanded.value,
            modifier = Modifier.width(width = width.value),
            onDismissRequest = { expanded.value = false },
        ) {
            choices.map {
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    selected.value = it
                    onSelect(it.value)
                }) {
                    Text(it.label)
                }
            }
        }
    }
}
