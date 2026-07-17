package com.example.fino.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fino.data.AppDao
import com.example.fino.data.AssetGoalEntity
import com.example.fino.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GoalsScreen(dao: AppDao) {
    val coroutineScope = rememberCoroutineScope()

    val totalIncome by dao.getTotalIncome().collectAsState(initial = 0.0)
    val totalExpense by dao.getTotalExpense().collectAsState(initial = 0.0)
    val currentAssets = ((totalIncome ?: 0.0) - (totalExpense ?: 0.0)).toLong()

    val goal by dao.getAssetGoal().collectAsState(initial = null)

    var targetAmount by remember { mutableStateOf(1000000L) }

    LaunchedEffect(goal) {
        if (goal != null) {
            targetAmount = goal!!.targetAmount
        }
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.JAPAN)
    val remaining = targetAmount - currentAssets

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("資産目標", style = AppTypography.titleLarge, color = OnSurface)
            Text("目標までの軌跡を設定", style = AppTypography.labelSmall, color = OnSurfaceVariant)
        }

        // Visualization Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism(cornerRadius = 16.dp, padding = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("現在の資産", style = AppTypography.labelSmall, color = OnSurfaceVariant)
                    Text(currencyFormatter.format(currentAssets), style = AppTypography.titleMedium, color = OnSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("目標金額", style = AppTypography.labelSmall, color = PrimaryFixedDim)
                    Text(currencyFormatter.format(targetAmount), style = AppTypography.titleMedium, color = Primary)
                }
            }

            // Progress Bar
            val progress = if (targetAmount > 0) (currentAssets.toFloat() / targetAmount).coerceIn(0f, 1f) else 0f
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = PrimaryContainer,
                    trackColor = SurfaceContainerHighest
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = AppTypography.labelSmall, color = Outline, fontSize = 10.sp)
                    Text("${(progress * 100).toInt()}% 達成", style = AppTypography.labelSmall, color = Outline, fontSize = 10.sp)
                    Text("100%", style = AppTypography.labelSmall, color = Outline, fontSize = 10.sp)
                }
            }
        }

        // Target Slider Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("目標の調整", style = AppTypography.labelSmall, color = OnSurfaceVariant)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(cornerRadius = 8.dp, padding = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = currencyFormatter.format(targetAmount),
                    style = AppTypography.titleLarge,
                    color = Primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Slider(
                    value = targetAmount.toFloat(),
                    onValueChange = { targetAmount = it.toLong() },
                    valueRange = 500000f..5000000f,
                    steps = 90, // 5,000,000 - 500,000 = 4,500,000 / 50,000 = 90 steps
                    colors = SliderDefaults.colors(
                        thumbColor = Primary,
                        activeTrackColor = PrimaryFixedDim,
                        inactiveTrackColor = SurfaceContainerHighest
                    )
                )
            }
        }

        // Remaining Amount Display
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("目標まで残り", style = AppTypography.labelSmall, color = SecondaryFixedDim)
            if (remaining > 0) {
                Text(
                    text = currencyFormatter.format(remaining),
                    style = AppTypography.displayLarge,
                    color = Primary
                )
            } else {
                Text(
                    text = "目標達成",
                    style = AppTypography.displayLarge,
                    color = Tertiary
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                coroutineScope.launch {
                    dao.insertOrUpdateAssetGoal(AssetGoalEntity(id = 1, targetAmount = targetAmount))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryFixedDim)
        ) {
            Text("目標設定を確定", color = OnPrimary, style = AppTypography.titleMedium)
        }
    }
}
