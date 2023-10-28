package cu.sitransvc.temuestro.DB.dao

import androidx.room.*
import cu.sitransvc.temuestro.DB.entity.ProductoDB

@Dao
interface ProductoDAO {

    @Insert
    fun insert(ProductoDB: ProductoDB)

    @Query("SELECT * FROM Producto")
    fun getAll(): List<ProductoDB>

    @Query("SELECT SUM(costo*existencias) FROM Producto ")
    fun getAllMoney(): Double

    @Query("SELECT SUM(costo*existencias) FROM Producto WHERE id_alamcen=:id_alamcen")
    fun getAllMoney(id_alamcen:Int): Double

    @Query("SELECT * FROM Producto WHERE id_alamcen=:id")
    fun getByIdAlmacen(id:Int): List<ProductoDB>

    @Query("SELECT * FROM Producto WHERE id_alamcen=:id_alamcen AND id_producto=:id_producto")
    fun getByIdAlmacenIdProducto(id_alamcen:Int,id_producto:Int): ProductoDB

    @Query("SELECT * FROM Producto WHERE id_alamcen=:id AND existencias-bloqueados>0")
    fun getByIdAlmacen2(id:Int): List<ProductoDB>

    @Update
    fun update(ProductoDB: ProductoDB)

    @Delete
    fun delete(ProductoDB: ProductoDB)

    @Query("DELETE FROM Producto WHERE id_alamcen=:id")
    fun deleteall_Almacen(id:Int)

    @Query("DELETE FROM Producto")
    fun deleteall()

}