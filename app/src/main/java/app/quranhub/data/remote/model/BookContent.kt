package app.quranhub.data.remote.model

import com.google.gson.annotations.SerializedName

data class BookContent(
    var id: Int = 0,
    var name: String? = null,
    var path: String? = null,
    @SerializedName("created_at")
    var createdAt: Long = 0,
    @SerializedName("updated_at")
    var updatedAt: Long = 0,
    var size: Int = 0,
    var author: String? = null,
    var downloadStatus: Int = 0,
    var downloadId: Long = 0
)