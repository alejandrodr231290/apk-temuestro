package cu.sitransvc.temuestro.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiAlmacen(
    @SerializedName("id") @Expose var id: Int,
    @SerializedName("codigo") @Expose var codigo: String,
    @SerializedName("nombre") @Expose var nombre: String,
    @SerializedName("productos") @Expose var productos: List<ApiProducto>
)
