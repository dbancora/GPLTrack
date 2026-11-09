package com.example.gpltrack.data.backup

import com.example.gpltrack.data.viewmodel.ProfileViewModel
import com.example.gpltrack.data.viewmodel.RefuelViewModel
import com.example.gpltrack.data.viewmodel.WashViewModel
import org.json.JSONArray
import org.json.JSONObject

internal object BackupManager {

    private const val CURRENT_VERSION = 1

    data class ImportResult(
        val refuels: Int,
        val washes: Int,
        val profileImported: Boolean
    )

    fun export(
        refuelVm: RefuelViewModel,
        washVm: WashViewModel,
        profileVm: ProfileViewModel
    ): String = JSONObject()
        .put("version", CURRENT_VERSION)
        .put("refuels", refuelVm.exportJsonArray())
        .put("washes",  washVm.exportJsonArray())
        .put("profile", profileVm.exportJsonObject())
        .toString(2)

    fun import(
        json: String,
        refuelVm: RefuelViewModel,
        washVm: WashViewModel,
        profileVm: ProfileViewModel
    ): ImportResult {
        val trimmed = json.trim()

        // Vecchio formato: array JSON puro di rifornimenti (backup pre-unificazione)
        if (trimmed.startsWith("[")) {
            return ImportResult(
                refuels = refuelVm.importJson(trimmed),
                washes = 0,
                profileImported = false
            )
        }

        // Formato intermedio (v1 rifornimenti+lavaggi senza profilo): niente "profile"
        val root = JSONObject(trimmed)
        val refuels = root.optJSONArray("refuels")?.let { refuelVm.importJsonArray(it) } ?: 0
        val washes  = root.optJSONArray("washes")?.let  { washVm.importJsonArray(it)  } ?: 0
        val profileImported = root.optJSONObject("profile")?.let {
            profileVm.importJsonObject(it); true
        } ?: false

        return ImportResult(refuels, washes, profileImported)
    }
}