package com.example.data.roadmap

data class DevRoadmapSummary(
    val id: String,
    val title: String,
    val icon: String,
    val nodes: Int,
    val resources: Int,
    val difficulty: String,
    val time: String
)

data class DevRoadmapResource(
    val title: String,
    val url: String,
    val type: String? = null,
    val free: Boolean? = true
)

data class DevRoadmapNode(
    val id: String,
    val title: String,
    val icon: String,
    val category: String,
    val description: String,
    val resources: List<DevRoadmapResource> = emptyList(),
    val children: List<String> = emptyList(),
    val difficulty: String? = null
)

data class DevRoadmapDetail(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val categories: List<String> = emptyList(),
    val nodes: List<DevRoadmapNode> = emptyList()
)

enum class TopicStatus(val label: String, val symbol: String) {
    COMPLETED("Completed", "✓"),
    IN_PROGRESS("Current / In Progress", "→"),
    NOT_STARTED("Not Started", "○"),
    NEEDS_REVIEW("Needs Review", "↻")
}
