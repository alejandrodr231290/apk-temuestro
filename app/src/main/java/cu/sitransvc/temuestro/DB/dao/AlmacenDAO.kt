package cu.sitransvc.temuestro.DB.dao

import androidx.room.*
import cu.sitransvc.temuestro.DB.entity.AlmacenDB

@Dao interface AlmacenDAO {

    @Insert
    fun insert(AlmacenDB: AlmacenDB)

    @Query("SELECT * FROM Almacen")
    fun getAll(): List<AlmacenDB>

    @Query("SELECT * FROM Almacen WHERE id_alamcen=:id")
    fun getByID(id:Int): AlmacenDB

    @Update
    fun update(AlmacenDB: AlmacenDB)

    @Delete
    fun delete(AlmacenDB: AlmacenDB)

    @Query("DELETE FROM Almacen")
    fun deleteall()
}