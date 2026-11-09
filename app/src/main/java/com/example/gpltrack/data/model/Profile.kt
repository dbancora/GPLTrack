package com.example.gpltrack.data.model

data class Profile(
    val model: String = "Dacia Duster",
    val version: String = "",
    val year: String = "2026",
    val plate: String = "",
    val ownerName: String = "",
    //val ownerPhone: String = "",
    //val ownerEmail: String = "",
    val bolloExpiry: Long? = null,
    val insuranceExpiry: Long? = null,
    val insuranceCompany: String = "",
    val policyNumber: String = "",
    //Aggiungo info sulla patente:
    val licencePlateNumber: String = "",
    val licencePlateExpire: Long? = null
)