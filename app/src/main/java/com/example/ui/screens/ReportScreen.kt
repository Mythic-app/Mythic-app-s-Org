package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassSurface
import com.example.viewmodel.MythicViewModel
import com.example.data.remote.SupabaseReport
import java.util.UUID

@Composable
fun ReportScreen(viewModel: MythicViewModel) {
    var contentId by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var priority by remember { mutableFloatStateOf(0.5f) }
    
    val currentUser by viewModel.currentUser.collectAsState()
    val recentReports by viewModel.recentReports.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchRecentReports()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 40.dp)
    ) {
        item {
            Text(
                text = "Report Issue",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "\"The only thing necessary for the triumph of evil is for good men to do nothing.\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item {
            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = contentId,
                        onValueChange = { contentId = it },
                        label = { Text("Content ID", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for report", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Priority: ${(priority * 10).toInt()}", color = Color.White)
                    Slider(
                        value = priority,
                        onValueChange = { priority = it },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF9AF04D),
                            activeTrackColor = Color(0xFF9AF04D)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentUser != null && contentId.isNotBlank() && reason.isNotBlank()) {
                                viewModel.submitReport(
                                    SupabaseReport(
                                        id = UUID.randomUUID().toString(),
                                        userId = currentUser!!.id,
                                        contentId = contentId,
                                        reason = reason,
                                        priority = priority,
                                        createdAt = null
                                    )
                                )
                                contentId = ""
                                reason = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9AF04D))
                    ) {
                        Text("Submit Report", color = Color.Black)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Recent Reports", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(recentReports) { report ->
            GlassSurface(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "Report on ${report.contentId}: ${report.reason}",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
