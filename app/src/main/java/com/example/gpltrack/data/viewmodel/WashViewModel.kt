package com.example.gpltrack.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.gpltrack.data.model.Wash
import com.example.gpltrack.data.model.WashType
import org.json.JSONArray
import org.json.JSONObject

class WashViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("duster_washes", Context.MODE_PRIVATE)
    private val items = mutableStateListOf<Wash>()

    val washes: List<Wash> get() = items

    init {
        val raw = prefs.getString("washes", null)
        if (raw != null) {
            val loaded = (0 until JSONArray(raw).length()).map {
                fromJson(JSONArray(raw).getJSONObject(it))
            }
            replaceAll(loaded)
        }
    }

    fun add(w: Wash) { replaceAll(items + w); save() }
    fun delete(id: Long) { replaceAll(items.filter { it.id != id }); save() }

    private fun toJson(w: Wash): JSONObject = JSONObject()
        .put("id", w.id)
        .put("day", w.epochDay)
        .put("type", w.type.name)
        .put("interior", w.interior)
        .put("cost", w.cost)

    private fun fromJson(o: JSONObject): Wash = Wash(
        id = o.getLong("id"),
        epochDay = o.getLong("day"),
        type = runCatching { WashType.valueOf(o.getString("type")) }
            .getOrDefault(WashType.Manuale),
        interior = o.optBoolean("interior", false),
        cost = o.getDouble("cost")
    )

    fun exportJsonArray(): JSONArray = JSONArray().also { arr ->
        items.forEach { arr.put(toJson(it)) }
    }

    fun importJsonArray(arr: JSONArray): Int {
        val loaded = (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        replaceAll((items + loaded).distinctBy { it.id })
        save()
        return loaded.size
    }

    private fun replaceAll(list: List<Wash>) {
        val sorted = list.sortedWith(
            compareByDescending<Wash> { it.epochDay }.thenByDescending { it.id }
        )
        items.clear()
        items.addAll(sorted)
    }

    private fun save() {
        prefs.edit().putString("washes", exportJsonArray().toString()).apply()
    }
}