package com.example.fino.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fino.data.AppDao
import com.example.fino.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(dao: AppDao, onNavigateToHistory: () -> Unit) {
    val totalIncome by dao.getTotalIncome().collectAsState(initial = 0.0)
    val totalExpense by dao.getTotalExpense().collectAsState(initial = 0.0)
    val goal by dao.getAssetGoal().collectAsState(initial = null)
    val recentTransactions by dao.getAllTransactions().collectAsState(initial = emptyList())

    val balance = (totalIncome ?: 0.0) - (totalExpense ?: 0.0)
    val targetAmount = goal?.targetAmount ?: 1000000L
    val progress = if (targetAmount > 0) (balance / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.JAPAN)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
            )
            Text(
                text = "FINO",
                style = AppTypography.displayLarge,
                fontSize = 24.sp,
                color = PrimaryFixedDim
            )
            Box(modifier = Modifier.size(32.dp)) // Placeholder for settings
        }

        // Monthly Balance Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism(cornerRadius = 16.dp, padding = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "今月の残高", style = AppTypography.titleMedium, fontSize = 14.sp, color = OnSurfaceVariant)
            Text(
                text = currencyFormatter.format(balance),
                style = AppTypography.displayLarge,
                color = PrimaryFixedDim
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryFixedDim)
                ) {
                    Text("送金", color = OnPrimary, style = AppTypography.titleMedium, fontSize = 14.sp)
                }
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Text("チャージ", color = PrimaryFixedDim, style = AppTypography.titleMedium, fontSize = 14.sp)
                }
            }
        }

        // Asset Goal Progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism(cornerRadius = 16.dp, padding = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("資産目標の進捗", style = AppTypography.titleMedium, color = OnSurface)
                val remaining = (targetAmount - balance).coerceAtLeast(0.0)
                Text("残り: ${currencyFormatter.format(remaining)}", style = AppTypography.labelSmall, color = SecondaryFixedDim)
                Text("目標: ${currencyFormatter.format(targetAmount)}", fontSize = 12.sp, color = OnSurfaceVariant)
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = OnSurfaceVariant.copy(alpha = 0.2f),
                    strokeWidth = 6.dp
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = SecondaryFixedDim,
                    strokeWidth = 6.dp
                )
                Text("${(progress * 100).toInt()}%", style = AppTypography.labelSmall, color = OnSurface, fontWeight = FontWeight.Bold)
            }
        }

        // Recent Transactions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("最近の取引", style = AppTypography.titleMedium, color = OnSurface)
                Text("すべて見る", style = AppTypography.labelSmall, color = PrimaryFixedDim)
            }

            recentTransactions.take(3).forEach { transaction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphism(cornerRadius = 8.dp, padding = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (transaction.isIncome) TertiaryContainer.copy(alpha=0.2f) else Error.copy(alpha=0.2f))
                        )
                        Column {
                            Text(transaction.title, style = AppTypography.bodyMedium, fontWeight = FontWeight.SemiBold, color = OnSurface)
                            Text(transaction.category, style = AppTypography.labelSmall, color = OnSurfaceVariant)
                        }
                    }
                    val sign = if (transaction.isIncome) "+" else "-"
                    val color = if (transaction.isIncome) TertiaryFixedDim else Error
                    Text(
                        text = "$sign ${currencyFormatter.format(transaction.amount)}",
                        style = AppTypography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
