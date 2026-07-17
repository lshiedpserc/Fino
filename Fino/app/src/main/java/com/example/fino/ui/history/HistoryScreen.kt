package com.example.fino.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fino.data.AppDao
import com.example.fino.data.TransactionEntity
import com.example.fino.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(dao: AppDao) {
    val allTransactions by dao.getAllTransactions().collectAsState(initial = emptyList())
    var filterMode by remember { mutableStateOf(0) } // 0: All, 1: Income, 2: Expense
    var searchQuery by remember { mutableStateOf("") }

    val filteredTransactions = allTransactions.filter {
        (filterMode == 0 || (filterMode == 1 && it.isIncome) || (filterMode == 2 && !it.isIncome)) &&
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.JAPAN)
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.JAPAN)
    val dateFormatter = SimpleDateFormat("MM/dd", Locale.JAPAN)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("取引履歴", style = AppTypography.titleLarge, color = OnSurface)
            Text("最近の収支状況を確認しましょう。", style = AppTypography.bodyMedium, color = OnSurfaceVariant)
        }

        // Mock Spending Chart Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism(cornerRadius = 12.dp, padding = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("週間の支出", style = AppTypography.labelSmall, color = OnSurfaceVariant)
                    Text("¥12,405", style = AppTypography.titleMedium, color = Primary, modifier = Modifier.padding(top = 4.dp))
                }
                Box(
                    modifier = Modifier
                        .background(TertiaryFixedDim.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+12%", style = AppTypography.labelSmall, color = TertiaryFixedDim, fontSize = 10.sp)
                }
            }

            // Simplified Mock Chart
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.8f)
                        lineTo(size.width * 0.1f, size.height * 0.6f)
                        lineTo(size.width * 0.25f, size.height * 0.9f)
                        lineTo(size.width * 0.4f, size.height * 0.4f)
                        lineTo(size.width * 0.5f, size.height * 0.5f)
                        lineTo(size.width * 0.6f, size.height * 0.2f)
                        lineTo(size.width * 0.75f, size.height * 0.7f)
                        lineTo(size.width * 0.9f, size.height * 0.3f)
                        lineTo(size.width, size.height * 0.1f)
                    }
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(listOf(PrimaryFixedDim, SecondaryFixedDim)),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(it, style = AppTypography.labelSmall, fontSize = 10.sp, color = OnSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }

        // Search & Filters
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().glassmorphism(cornerRadius = 8.dp, padding = 0.dp),
                placeholder = { Text("取引を検索...", color = OnSurfaceVariant.copy(alpha=0.5f)) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = SurfaceTint,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = OnSurface, unfocusedTextColor = OnSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton("すべて", filterMode == 0) { filterMode = 0 }
                FilterButton("収入", filterMode == 1) { filterMode = 1 }
                FilterButton("支出", filterMode == 2) { filterMode = 2 }
            }
        }

        // Transaction List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (filteredTransactions.isEmpty()) {
                Text("取引履歴がありません。", color = OnSurfaceVariant, style = AppTypography.bodyMedium)
            } else {
                filteredTransactions.forEach { transaction ->
                    TransactionItem(transaction, currencyFormatter, timeFormatter, dateFormatter)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SurfaceTint.copy(alpha = 0.1f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) SurfaceTint else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = AppTypography.labelSmall,
            color = if (isSelected) PrimaryFixedDim else OnSurfaceVariant
        )
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    currencyFormatter: NumberFormat,
    timeFormatter: SimpleDateFormat,
    dateFormatter: SimpleDateFormat
) {
    val date = Date(transaction.date)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism(cornerRadius = 12.dp, padding = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (transaction.isIncome) TertiaryFixedDim.copy(alpha=0.1f) else PrimaryFixedDim.copy(alpha=0.1f))
                    .border(1.dp, if (transaction.isIncome) TertiaryFixedDim.copy(alpha=0.2f) else PrimaryFixedDim.copy(alpha=0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(transaction.iconName.take(1).uppercase(), color = if (transaction.isIncome) TertiaryFixedDim else PrimaryFixedDim)
            }
            Column {
                Text(transaction.title, style = AppTypography.titleMedium, fontSize = 16.sp, color = OnSurface)
                Text("${transaction.category} • ${dateFormatter.format(date)} ${timeFormatter.format(date)}", style = AppTypography.labelSmall, fontSize = 11.sp, color = OnSurfaceVariant.copy(alpha = 0.7f))
            }
        }
        val sign = if (transaction.isIncome) "+" else "-"
        val color = if (transaction.isIncome) TertiaryFixedDim else OnSurface
        Text(
            text = "$sign${currencyFormatter.format(transaction.amount)}",
            style = AppTypography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
