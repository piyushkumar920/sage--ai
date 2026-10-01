package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.curriculum.CurriculumDepartment
import com.example.data.profile.AcademicProfile
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGlowEnd
import com.example.ui.theme.SageGlowStart
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

/**
 * Dialog allowing students to select or update their persistent Academic Profile.
 * Configures Department, Programme, Regulation, and Current Semester.
 */
@Composable
fun AcademicProfileDialog(
    currentProfile: AcademicProfile?,
    departments: List<CurriculumDepartment>,
    onSaveProfile: (department: CurriculumDepartment, semester: Int) -> Unit,
    onDismiss: () -> Unit,
    isDismissable: Boolean = true
) {
    var selectedDeptId by remember(currentProfile, departments) {
        mutableStateOf(currentProfile?.departmentId ?: departments.firstOrNull()?.id ?: "it")
    }
    var selectedSemester by remember(currentProfile) {
        mutableIntStateOf(currentProfile?.semester ?: 1)
    }

    val selectedDepartment = remember(selectedDeptId, departments) {
        departments.find { it.id == selectedDeptId } ?: departments.firstOrNull()
    }

    val maxSemesters = if (selectedDepartment?.id == "mca") 4 else 8

    Dialog(onDismissRequest = { if (isDismissable) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SageGlassBorderGlow, RoundedCornerShape(24.dp))
                .testTag("academic_profile_dialog"),
            color = SageSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(SagePrimaryStart, SagePrimary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Academic Profile",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentProfile == null) "Set Up Academic Profile" else "Edit Academic Profile",
                                color = SageTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Defines your curriculum source of truth",
                                color = SageTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isDismissable) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp).testTag("close_profile_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SageTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Department Selection Header
                Text(
                    text = "SELECT DEPARTMENT & PROGRAMME",
                    color = SagePrimaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Department List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(departments) { dept ->
                        val isSelected = dept.id == selectedDeptId
                        val deptIcon = getDepartmentEmoji(dept.id)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SagePrimary.copy(alpha = 0.18f) else SageGlassL1)
                                .border(
                                    1.dp,
                                    if (isSelected) SagePrimaryLight else SageGlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedDeptId = dept.id
                                    if (selectedSemester > (if (dept.id == "mca") 4 else 8)) {
                                        selectedSemester = 1
                                    }
                                }
                                .padding(12.dp)
                                .testTag("dept_item_${dept.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = deptIcon,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Column {
                                        Text(
                                            text = dept.name,
                                            color = if (isSelected) SagePrimaryLight else SageTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = "${dept.programme} • ${dept.regulation}",
                                            color = SageTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = SagePrimaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Semester Selection Header
                Text(
                    text = "CURRENT SEMESTER",
                    color = SagePrimaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Semester Selector Grid / Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (sem in 1..4) {
                        val isSelected = selectedSemester == sem
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SagePrimary else SageGlassL2)
                                .border(
                                    1.dp,
                                    if (isSelected) SagePrimaryLight else SageGlassBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedSemester = sem }
                                .padding(vertical = 10.dp)
                                .testTag("sem_btn_$sem"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sem $sem",
                                color = if (isSelected) Color.White else SageTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (maxSemesters > 4) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (sem in 5..8) {
                            val isSelected = selectedSemester == sem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SagePrimary else SageGlassL2)
                                    .border(
                                        1.dp,
                                        if (isSelected) SagePrimaryLight else SageGlassBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedSemester = sem }
                                    .padding(vertical = 10.dp)
                                    .testTag("sem_btn_$sem"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sem $sem",
                                    color = if (isSelected) Color.White else SageTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Action Button
                Button(
                    onClick = {
                        if (selectedDepartment != null) {
                            onSaveProfile(selectedDepartment, selectedSemester)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_academic_profile_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Text(
                        text = if (currentProfile == null) "Set Up Profile" else "Save Academic Profile",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getDepartmentEmoji(deptId: String): String {
    return when (deptId.lowercase()) {
        "it" -> "💻"
        "cse_aiml" -> "🧠"
        "cst" -> "🖥️"
        "ece" -> "📡"
        "ee" -> "⚡"
        "me" -> "⚙️"
        "ce" -> "🏗️"
        "bme" -> "🔬"
        "bba" -> "🏢"
        "mca" -> "🎓"
        else -> "📚"
    }
}
