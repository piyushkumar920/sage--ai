package com.example.data.roadmap

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class RoadmapLoader(private val context: Context) {

    private val cachedSummaries = mutableListOf<DevRoadmapSummary>()
    private val cachedDetails = mutableMapOf<String, DevRoadmapDetail>()

    fun getSummaries(): List<DevRoadmapSummary> {
        if (cachedSummaries.isNotEmpty()) return cachedSummaries

        try {
            val jsonStr = context.assets.open("roadmaps/index.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            val list = mutableListOf<DevRoadmapSummary>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DevRoadmapSummary(
                        id = obj.optString("id", ""),
                        title = obj.optString("title", ""),
                        icon = obj.optString("icon", "🧭"),
                        nodes = obj.optInt("nodes", 0),
                        resources = obj.optInt("resources", 0),
                        difficulty = obj.optString("difficulty", "Beginner"),
                        time = obj.optString("time", "6 months")
                    )
                )
            }
            cachedSummaries.clear()
            cachedSummaries.addAll(list)
        } catch (e: Exception) {
            Log.e("RoadmapLoader", "Error loading roadmaps/index.json", e)
        }

        return cachedSummaries
    }

    fun getRoadmapDetail(roadmapId: String): DevRoadmapDetail? {
        cachedDetails[roadmapId]?.let { return it }

        try {
            val fileName = "roadmaps/$roadmapId.json"
            val jsonStr = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val obj = JSONObject(jsonStr)

            val title = obj.optString("title", "Roadmap")
            val description = obj.optString("description", "")
            val icon = obj.optString("icon", "🧭")

            val categoriesArray = obj.optJSONArray("categories")
            val categories = mutableListOf<String>()
            if (categoriesArray != null) {
                for (i in 0 until categoriesArray.length()) {
                    categories.add(categoriesArray.optString(i))
                }
            }

            val nodesArray = obj.optJSONArray("nodes")
            val nodes = mutableListOf<DevRoadmapNode>()
            if (nodesArray != null) {
                for (i in 0 until nodesArray.length()) {
                    val nObj = nodesArray.getJSONObject(i)

                    val resourcesArray = nObj.optJSONArray("resources")
                    val resources = mutableListOf<DevRoadmapResource>()
                    if (resourcesArray != null) {
                        for (r in 0 until resourcesArray.length()) {
                            val rObj = resourcesArray.getJSONObject(r)
                            resources.add(
                                DevRoadmapResource(
                                    title = rObj.optString("title", ""),
                                    url = rObj.optString("url", ""),
                                    type = rObj.optString("type", "docs"),
                                    free = rObj.optBoolean("free", true)
                                )
                            )
                        }
                    }

                    val childrenArray = nObj.optJSONArray("children")
                    val children = mutableListOf<String>()
                    if (childrenArray != null) {
                        for (c in 0 until childrenArray.length()) {
                            children.add(childrenArray.optString(c))
                        }
                    }

                    nodes.add(
                        DevRoadmapNode(
                            id = nObj.optString("id", "node-$i"),
                            title = nObj.optString("title", "Topic"),
                            icon = nObj.optString("icon", "📌"),
                            category = nObj.optString("category", "fundamentals"),
                            description = nObj.optString("description", ""),
                            resources = resources,
                            children = children,
                            difficulty = nObj.optString("difficulty", "Beginner")
                        )
                    )
                }
            }

            val detail = DevRoadmapDetail(
                id = roadmapId,
                title = title,
                description = description,
                icon = icon,
                categories = categories,
                nodes = nodes
            )
            cachedDetails[roadmapId] = detail
            return detail
        } catch (e: Exception) {
            Log.e("RoadmapLoader", "Error loading roadmap $roadmapId", e)
            return null
        }
    }
}
