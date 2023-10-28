package cu.sitransvc.temuestro.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cu.sitransvc.temuestro.DB.dao.AlmacenDAO
import cu.sitransvc.temuestro.DB.dao.ImagenDAO
import cu.sitransvc.temuestro.DB.dao.ProductoDAO
import cu.sitransvc.temuestro.DB.entity.AlmacenDB
import cu.sitransvc.temuestro.DB.entity.ImagenDB
import cu.sitransvc.temuestro.DB.entity.ProductoDB
import net.sqlcipher.database.SupportFactory

@Database(entities = [AlmacenDB::class,ImagenDB::class,ProductoDB::class ], version = 1, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {

    abstract fun almacenDao() : AlmacenDAO
    abstract fun imagenDao() : ImagenDAO
    abstract fun productoDao() : ProductoDAO

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val PASSPHRASE = "sitransvc2022"
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                      INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, "db.db")
                        .allowMainThreadQueries()
                        .openHelperFactory(SupportFactory(PASSPHRASE.toByteArray())) //para cifrar
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }


    }
}