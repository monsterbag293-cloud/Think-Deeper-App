package com.example.data.local

import org.json.JSONArray
import org.json.JSONObject

object JsonHelper {
  fun serializeActivities(steps: List<ActivityStep>): String {
    val array = JSONArray()
    for (step in steps) {
      val obj = JSONObject()
      obj.put("agent", step.agent)
      obj.put("name", step.name)
      obj.put("status", step.status)
      obj.put("detail", step.detail)
      array.put(obj)
    }
    return array.toString()
  }

  fun deserializeActivities(jsonStr: String?): List<ActivityStep> {
    if (jsonStr.isNullOrBlank() || jsonStr == "[]") return emptyList()
    val list = mutableListOf<ActivityStep>()
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          ActivityStep(
            agent = obj.optString("agent", "Agent 1"),
            name = obj.optString("name", "Step"),
            status = obj.optString("status", "done"),
            detail = obj.optString("detail", "")
          )
        )
      }
    } catch (_: Exception) {
      // Fallback to empty
    }
    return list
  }
}
