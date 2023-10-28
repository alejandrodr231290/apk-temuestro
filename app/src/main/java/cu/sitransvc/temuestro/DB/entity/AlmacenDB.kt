package cu.sitransvc.temuestro.DB.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Almacen")
data class AlmacenDB(
    @ColumnInfo(name = "id_alamcen") val id_alamcen: Int,
    @ColumnInfo(name = "codigo", defaultValue = "") val codigo: String,
    @ColumnInfo(name = "nombre", defaultValue = "") val nombre: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)