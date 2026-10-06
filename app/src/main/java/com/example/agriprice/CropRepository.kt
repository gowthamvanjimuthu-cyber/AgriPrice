package com.example.agriprice

import com.google.firebase.firestore.FirebaseFirestore

class CropRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getCrops(
        onSuccess: (List<Crop>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("crops")
            .get()
            .addOnSuccessListener { result ->
                val crops = result.documents.mapNotNull { document ->
                    document.toObject(Crop::class.java)
                }

                onSuccess(crops)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}