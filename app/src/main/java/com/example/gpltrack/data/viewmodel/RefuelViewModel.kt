package com.example.gpltrack.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.gpltrack.data.model.Fuel
import com.example.gpltrack.data.model.Refuel
import org.json.JSONArray
import org.json.JSONObject

class RefuelViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("duster_tracker", Context.MODE_PRIVATE)
    private val items = mutableStateListOf<Refuel>()

    val refuels: List<Refuel> get() = items

    init {
        val raw = prefs.getString("refuels", null)
        if (raw != null) {
            val arr = JSONArray(raw)
            replaceAll((0 until arr.length()).map { fromJson(arr.getJSONObject(it)) })
        }
    }

    private fun toJson(r: Refuel): JSONObject = JSONObject()
        .put("id", r.id)
        .put("day", r.epochDay)
        .put("odometer", r.odometer) // ← NUOVO
        .put("kmGpl", r.kmGpl)       // ← NUOVO
        .put("l", r.liters)
        .put("cost", r.cost)
        .put("fuel", r.fuel.name)
        .put("full", r.fullTank)

    private fun fromJson(o: JSONObject): Refuel {
        val fuel = runCatching { Fuel.valueOf(o.optString("fuel", Fuel.Benzina.name)) }
            .getOrDefault(Fuel.Benzina)

        return Refuel(
            id = o.getLong("id"),
            epochDay = o.getLong("day"),
            odometer = o.optInt("odometer", 0), //Se ho dei dati vecchi.. Retrocompatibilita'
            kmGpl = o.optInt("kmGpl", o.optInt("km", 0)), //Se ho dei dati vecchi.. Retrocompatibilita'
            liters = o.getDouble("l"),
            cost = o.getDouble("cost"),
            fuel = fuel,
            fullTank = o.optBoolean("full", true)
        )
    }

    fun exportJsonArray(): JSONArray = JSONArray().also { arr ->
        items.forEach { arr.put(toJson(it)) }
    }

    fun importJsonArray(arr: JSONArray): Int {
        val loaded = (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        replaceAll((items + loaded).distinctBy { it.id })
        save()
        return loaded.size
    }

    fun exportJson(): String = exportJsonArray().toString()

    fun importJson(json: String): Int = importJsonArray(JSONArray(json))

    fun add(r: Refuel) { replaceAll(items + r); save() }
    fun delete(id: Long) { replaceAll(items.filter { it.id != id }); save() }

    private fun replaceAll(list: List<Refuel>) {
        val sorted = list.sortedWith(
            compareByDescending<Refuel> { it.epochDay }.thenByDescending { it.id }
        )
        items.clear()
        items.addAll(sorted)
    }

    private fun save() {
        val arr = JSONArray()
        items.forEach { arr.put(toJson(it)) }
        prefs.edit().putString("refuels", arr.toString()).apply()
    }
}