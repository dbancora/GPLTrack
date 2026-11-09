package com.example.gpltrack.data.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.example.gpltrack.data.model.Profile
import org.json.JSONObject
import java.io.File

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("duster_profile", Context.MODE_PRIVATE)
    private val photoFile = File(app.filesDir, "vehicle_photo.jpg")

    var profile by mutableStateOf(Profile())
        private set

    var photo by mutableStateOf<ImageBitmap?>(null)
        private set

    init {
        profile = load()
        photo = if (photoFile.exists()) {
            BitmapFactory.decodeFile(photoFile.absolutePath)?.asImageBitmap()
        } else null
    }

    fun update(transform: (Profile) -> Profile) {
        profile = transform(profile)
        save()
    }

    fun setPhoto(uri: Uri): Boolean = runCatching {
        val resolver = getApplication<Application>().contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 1600) sample *= 2

        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("Immagine non leggibile")

        val rotation = resolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val bitmap = if (rotation != 0f) {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height,
                Matrix().apply { postRotate(rotation) }, true)
        } else decoded

        photoFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        photo = bitmap.asImageBitmap()
    }.isSuccess

    fun clearPhoto() {
        photoFile.delete()
        photo = null
    }

    private fun profileToJson(p: Profile): JSONObject {
        val o = JSONObject()
            .put("model", p.model)
            .put("version", p.version)
            .put("year", p.year)
            .put("plate", p.plate)
            .put("ownerName", p.ownerName)
            //.put("ownerPhone", p.ownerPhone)
            //.put("ownerEmail", p.ownerEmail)
            .put("insuranceCompany", p.insuranceCompany)
            .put("policyNumber", p.policyNumber)
            .put("licencePlateNumber", p.licencePlateNumber)
        p.bolloExpiry?.let { o.put("bollo", it) }
        p.insuranceExpiry?.let { o.put("insurance", it) }
        p.licencePlateExpire?.let { o.put("licencePlate", it) }
        return o
    }

    private fun profileFromJson(o: JSONObject): Profile = Profile(
        model = o.optString("model", "Dacia Duster"),
        version = o.optString("version", ""),
        year = o.optString("year", "2026"),
        plate = o.optString("plate", ""),
        ownerName = o.optString("ownerName", ""),
        //ownerPhone = o.optString("ownerPhone", ""),
        //ownerEmail = o.optString("ownerEmail", ""),
        bolloExpiry = if (o.has("bollo")) o.getLong("bollo") else null,
        insuranceExpiry = if (o.has("insurance")) o.getLong("insurance") else null,
        insuranceCompany = o.optString("insuranceCompany", ""),
        policyNumber = o.optString("policyNumber", ""),
        licencePlateNumber = o.optString("licencePlateNumber", ""),
        licencePlateExpire = if (o.has("licencePlate")) o.getLong("licencePlate") else null
    )
    private fun save() {
        prefs.edit().putString("profile", profileToJson(profile).toString()).apply()
    }

    private fun load(): Profile {
        val raw = prefs.getString("profile", null) ?: return Profile()
        return runCatching { profileFromJson(JSONObject(raw)) }.getOrDefault(Profile())
    }

    fun exportJsonObject(): JSONObject = profileToJson(profile)

    fun importJsonObject(o: JSONObject) {
        profile = profileFromJson(o)
        save()
    }
}