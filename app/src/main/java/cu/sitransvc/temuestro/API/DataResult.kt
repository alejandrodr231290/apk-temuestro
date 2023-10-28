package cu.sitransvc.temuestro.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DataResult(
    @SerializedName("almacenes") @Expose var almacenes: List<ApiAlmacen>,
    @SerializedName("imagenes") @Expose var imagenes: List<ApiImagen>
 )
