package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassSurface
import com.example.viewmodel.MythicViewModel

@Composable
fun SettingsScreen(
    viewModel: MythicViewModel,
    onBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationTrackingEnabled by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 40.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        GlassSurface(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggle(
                    title = "Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                SettingToggle(
                    title = "Location Tracking",
                    checked = locationTrackingEnabled,
                    onCheckedChange = { locationTrackingEnabled = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Connect With Us", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        // Simple list of links
        Text(text = "Twitter: https://x.com/mythic_app1", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(text = "TikTok: https://www.tiktok.com/@mythic.app", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(text = "Instagram: https://www.instagram.com/mythic.app", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(text = "YouTube: https://www.youtube.com/@mythic_app", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(text = "Reddit: https://www.reddit.com/user/Mythic_app", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(text = "Discord: https://discord.gg/2dusvsUz5v", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "App Credits", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        CreditItem(role = "Leader", name = "Senuja")
        CreditItem(role = "Sub Leader", name = "Meshark")
        CreditItem(role = "Head Design", name = "Abilash")
        CreditItem(role = "Head Coder", name = "Maleesha")
        CreditItem(role = "Head Implementor", name = "Gavinda")
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Special Thanks", color = Color.White, style = MaterialTheme.typography.titleSmall)
        Text(text = "Sasan Vidunitha, Anuki, Senusha", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Special Thanks to Teachers", color = Color.White, style = MaterialTheme.typography.titleSmall)
        Text(text = "Sachindra Teacher, Oshandi Teacher, Savindra Teacher", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Our Principal", color = Color.White, style = MaterialTheme.typography.titleSmall)
        Text(text = "Dr. Anushke Perera", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CreditItem(role: String, name: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$role:", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(text = name, color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SettingToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF9AF04D),
                checkedTrackColor = Color(0xFF003300)
            )
        )
    }
}
