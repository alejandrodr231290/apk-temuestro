package cu.sitransvc.temuestro.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiProducto(
    @SerializedName("id") @Expose var id: Int,
    @SerializedName("codigo") @Expose var codigo: String,
    @SerializedName("descripcion") @Expose var descripcion: String,
    @SerializedName("unidad_medida") @Expose var unidad_medida: String,
    @SerializedName("existencias") @Expose var existencias: Int,
    @SerializedName("bloqueados") @Expose var bloqueados: Int,
    @SerializedName("precio") @Expose var precio: Double,
    @SerializedName("costo") @Expose var costo: Double,
)