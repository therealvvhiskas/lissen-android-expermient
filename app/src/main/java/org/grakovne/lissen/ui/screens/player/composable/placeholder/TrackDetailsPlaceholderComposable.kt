package org.grakovne.lissen.ui.screens.player.composable.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

@Composable
fun TrackDetailsPlaceholderComposable(
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .shimmer()
                .background(Color.Gray)

        )
    }

    Spacer(Modifier.padding(vertical = 8.dp))

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .shimmer()
            .background(Color.Gray)

    ) {
        Text(
            text = "",
            style = typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center,

            modifier = Modifier
                .padding(horizontal = 16.dp)

        )
    }

    Spacer(Modifier.padding(vertical = 4.dp))

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(0.5f)
    ) {
        Text(
            text = "",
            style = typography.bodyMedium,
            color = colorScheme.onBackground,
            modifier = Modifier
                .shimmer()
                .background(Color.Gray)
        )
    }
}