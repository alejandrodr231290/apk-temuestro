package cu.sitransvc.temuestro.DB.entity

import androidx.room.Embedded
import androidx.room.Relation

 class Productos_Almacen {
    @Embedded
    var parent: AlmacenDB? = null

    @Relation(
        parentColumn = "id_alamcen",
        entityColumn = "id_alamcen",
        entity = ProductoDB::class)
      var productoDBS: List<ProductoDB>? = null
}