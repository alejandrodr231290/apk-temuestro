package cu.sitransvc.temuestro.DB.dao

import androidx.room.*

import cu.sitransvc.temuestro.DB.entity.ImagenDB

@Dao interface ImagenDAO {

    @Query("SELECT * FROM Imagen")
    fun getAll(): List<ImagenDB>

    @Query("SELECT * FROM Imagen WHERE id_producto=:id_producto")
    fun getByID(id_producto:Int): ImagenDB

    @Insert
    fun insert(ImagenDB: ImagenDB)

    @Update
    fun update(ImagenDB: ImagenDB)

    @Delete
    fun delete(ImagenDB: ImagenDB)

    @Query("DELETE FROM Imagen WHERE id_producto=:id_producto")
    fun deleteID(id_producto:Int)

    @Query("DELETE FROM Imagen")
    fun deleteall()


}