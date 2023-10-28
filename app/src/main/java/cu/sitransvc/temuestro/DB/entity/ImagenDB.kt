package cu.sitransvc.temuestro.DB.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Imagen")
data class ImagenDB(
    @ColumnInfo(name = "id_producto") val id_producto: Int,
    @ColumnInfo(name = "imagen") var imagen: String,
    @PrimaryKey (autoGenerate = true) val id: Int=0,
)