package cu.sitransvc.temuestro.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginResult(
    @SerializedName("rol") @Expose var rol: Int,
    @SerializedName("empresa") @Expose var empresa: String,
    @SerializedName("token") @Expose var token: String,
)
