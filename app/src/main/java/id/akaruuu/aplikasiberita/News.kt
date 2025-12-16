package id.akaruuu.aplikasiberita

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Parcelable agar data bisa dikirim antar Activity lewat Intent
@Parcelize
data class News(
    val id: Int,
    val category: String,
    val title: String,
    val author: String,
    val date: String,
    val time: String,
    val image: String,
    val content: String,
    val summary: String,
    var isSaved: Boolean = false // Field tambahan untuk status simpan lokal di RAM
) : Parcelable