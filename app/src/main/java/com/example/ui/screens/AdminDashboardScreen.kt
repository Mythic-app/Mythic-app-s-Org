package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassSurface
import com.example.viewmodel.MythicViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: MythicViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reports", "Uploads", "Users", "Analytics")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 40.dp)
    ) {
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, color = Color.White) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GlassSurface(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (selectedTab) {
                0 -> Text("Reports list here", color = Color.White, modifier = Modifier.padding(16.dp))
                1 -> Text("Uploads list here", color = Color.White, modifier = Modifier.padding(16.dp))
                2 -> Text("Users list here", color = Color.White, modifier = Modifier.padding(16.dp))
                3 -> Text("Analytics data here", color = Color.White, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
