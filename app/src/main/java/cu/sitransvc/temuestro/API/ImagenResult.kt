package cu.sitransvc.temuestro.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ImagenResult(
    @SerializedName("code") @Expose var code: Int,
    @SerializedName("message") @Expose var message: String,

)
