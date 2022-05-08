package dev.mrichter.mapf.visualiser

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun NumericField(label: String, value: Int?, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value?.toString().orEmpty(),
        modifier = Modifier.width(100.dp),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        onValueChange = { input ->
            input.toIntOrNull()?.also { inputValue ->
                onChange(inputValue)
            }
        }
    )
}