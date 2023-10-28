package cu.sitransvc.temuestro.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiImagen(
    @SerializedName("id_producto") @Expose var id_producto: Int,
    @SerializedName("data_imgen") @Expose var data_imgen: String,
 )
