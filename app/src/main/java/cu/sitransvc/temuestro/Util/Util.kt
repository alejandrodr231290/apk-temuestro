package cu.sitransvc.temuestro.Util


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Base64
import android.util.Patterns
import org.joda.time.DateTime
import java.io.ByteArrayOutputStream
import java.security.Key
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Base64.getDecoder
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


class Util {
    companion object {

        val LICENCIA_ENCRYPT_KEY = "TEVENDOSITRANSVC"
        var MES_MIN = arrayOf(
            "Ene",
            "Feb",
            "Mar",
            "Abr",
            "May",
            "Jun",
            "Jul",
            "Ago",
            "Sep",
            "Oct",
            "Nov",
            "Dic"
        )

        @SuppressLint("MissingPermission")
        fun Vibrar(c: Context?, mili: Long) {
            var vibrator: Vibrator? = c?.getSystemService(VIBRATOR_SERVICE) as Vibrator?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(mili, 10))
            } else {
                vibrator?.vibrate(mili)

            }
        }

        fun getfechaTXT(fecha: Long): String? {
            val t = DateTime(fecha)
            return "" + t.dayOfMonth()
                .get() + "/" + MES_MIN.get(t.monthOfYear - 1) + "/" + t.year()
                .get()
        }

        fun getfechaTXT2(fecha: Long): String? {
            val t = DateTime(fecha)
            return "" + t.dayOfMonth()
                .get() + "/" + MES_MIN.get(t.monthOfYear - 1)
        }

        fun getHoraXT(fecha: Long): String? {
            var txt = ""
            val t = DateTime(fecha)
            val hora = t.hourOfDay().get()
            var min = "" + t.minuteOfHour().get()
            if (min.length == 1) {
                min = "0$min"
            }
            if (hora < 12) {
                txt += "" + hora
                txt += ":$min"
                txt += " am"
            } else if (hora == 12) {
                txt += "" + hora
                txt += ":$min"
                txt += " pm"
            } else { //es mayor q 12
                txt += "" + hora % 12
                txt += ":$min"
                txt += " pm"
            }
            return txt
        }

        @Throws(Exception::class)
        fun encript(text: String): String? {
            val aesKey: Key = SecretKeySpec(LICENCIA_ENCRYPT_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, aesKey)
            var encrypted = cipher.doFinal(text.toByteArray())
            encrypted = Base64.encode(encrypted, Base64.DEFAULT)
            return "" + String(encrypted)
        }

        @Throws(Exception::class)
        fun decrypt(encrypted: String?): String? {
            val encryptedBytes = Base64.decode(encrypted, Base64.DEFAULT)
            val aesKey: Key = SecretKeySpec(LICENCIA_ENCRYPT_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, aesKey,IvParameterSpec(LICENCIA_ENCRYPT_KEY.toByteArray()))
            return String(cipher.doFinal(encryptedBytes))

        }

        fun getCode(a: Activity): String {
            val pref = a.getPreferences(Context.MODE_PRIVATE)
            var code = pref?.getString("code", "")
            if (code == "") {
                with(pref.edit()) {
                    val abc = "1234567890"
                    var t: String = ""
                    val r = Random
                    for (i in 0..9) {
                        t += "" + abc.get(r.nextInt(0, abc.length - 1))
                    }
                    putString("code", "" + t)
                    apply()
                }
            }
            code = pref?.getString("code", "")
            return code.toString()
        }

        fun getLicencia(a: Activity): Long {
            val pref = a.getPreferences(Context.MODE_PRIVATE)
            val licencia = pref.getLong("licencia", 0)
            return licencia
        }

        fun licencia_Activa(a: Activity): Boolean {
            val pref = a.getPreferences(Context.MODE_PRIVATE)
            val licencia = pref.getLong("licencia", 0)
            val actual = Date().time
            return licencia > actual
        }

        fun getTime(strDate: String?): Long {
            return try {
                val sdf = SimpleDateFormat("dd-MM-yyyy")
                sdf.parse(strDate).time
            } catch (e: ParseException) {
                0
            }
        }

        @Throws(Exception::class)
        fun decodeBase64(input: String): Bitmap {
            val decodedBytes = Base64.decode(input.toByteArray(), Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }


        fun isValidUrl(value: String): Boolean {
            val pattern = Pattern.compile("[0-255].[0-255].[0-255].[0-255]")
            val matcher = pattern.matcher(value)
            val ip = matcher.matches()
            val p = Patterns.WEB_URL
            val m = p.matcher(value.lowercase())
            val web = m.matches()
            return ip || web
        }

        fun encodeImage(uri: Uri, c: Context): String {
            return try {
                val imageStream = c.contentResolver.openInputStream(uri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                val baos = ByteArrayOutputStream()
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val b = baos.toByteArray()
                Base64.encodeToString(b, Base64.DEFAULT)
            } catch (e: java.lang.Exception) {
                ""
            }
        }

        fun decodeBase64(input: String, c: Context?): Bitmap? {
            return try {
                val decodedBytes =
                    Base64.decode(input.toByteArray(), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: java.lang.Exception) {
                null
            }
        }
    }


}

