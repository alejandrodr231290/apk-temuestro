package cu.sitransvc.temuestro.DB.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Producto"/*,
    foreignKeys = [
        ForeignKey(entity = AlmacenDB::class,
            parentColumns = ["id_alamcen"],
            childColumns = ["id_alamcen"],
            onDelete = CASCADE)]*/
)
data class ProductoDB(

    @ColumnInfo(name = "id_alamcen") val id_alamcen: Int,
    @ColumnInfo(name = "id_producto") val id_producto: Int,

    @ColumnInfo(name = "existencias") val existencias: Int,
    @ColumnInfo(name = "bloqueados") val bloqueados: Int,

    @ColumnInfo(name = "codigo", defaultValue = "") val codigo: String,
    @ColumnInfo(name = "descripcion", defaultValue = "") val descripcion: String,
    @ColumnInfo(name = "unidad_medida", defaultValue = "") val unidad_medida: String,

    @ColumnInfo(name = "precio") val precio: Double,
    @ColumnInfo(name = "costo") val costo: Double,
    @PrimaryKey (autoGenerate = true) val id: Int=0,


   )