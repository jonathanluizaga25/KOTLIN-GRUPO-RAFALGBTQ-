package org.ucb.appp1.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

object AppSpacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
}

private val lightColors = lightColorScheme(
    primary = Color(0xFF155A74),
    onPrimary = Color.White,
    secondary = Color(0xFF765327),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF2F5),
    onSurfaceVariant = Color(0xFF31434C)
)

private val darkColors = darkColorScheme(
    primary = Color(0xFF83D0EF),
    onPrimary = Color(0xFF003545),
    secondary = Color(0xFFE9C48D),
    background = Color(0xFF101A20),
    surface = Color(0xFF19252D),
    surfaceVariant = Color(0xFF24343D),
    onSurfaceVariant = Color(0xFFD5E5EB)
)

@Composable
fun AppTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) darkColors else lightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        contentPadding = PaddingValues(horizontal = AppSpacing.medium, vertical = AppSpacing.small)
    ) {
        Text(text)
    }
}

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(AppSpacing.medium)) { content() }
    }
}

@Composable
fun AppTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
fun LoadingView(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

@Composable
fun EmptyView(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

@Composable
fun ErrorView(text: String, retryText: String, onRetry: () -> Unit) {
    Column {
        Text(text, color = MaterialTheme.colorScheme.error)
        AppButton(retryText, onRetry)
    }
}
