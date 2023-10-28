package cu.sitransvc.temuestro.FRAGMENT

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.ImagePicker.Companion.with
import com.google.gson.JsonObject
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import cu.sitransvc.temuestro.API.ApiInterface
import cu.sitransvc.temuestro.API.ImagenResult
import cu.sitransvc.temuestro.API.LoginResult
import cu.sitransvc.temuestro.DB.AppDatabase
import cu.sitransvc.temuestro.DB.entity.ImagenDB
import cu.sitransvc.temuestro.DB.entity.ProductoDB
import cu.sitransvc.temuestro.R
import cu.sitransvc.temuestro.Util.Util
import cu.sitransvc.temuestro.databinding.FragmentDetalleProductoBinding
import id.ionbit.ionalert.IonAlert
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class DetalleProductoFragment : Fragment() {
    private var _binding: FragmentDetalleProductoBinding? = null
    private val binding get() = _binding!!
    var cargando: AlertDialog? = null
    var check_token: Boolean = true
    var base64imagen = "";
    var vibrar = true
    var notificacion_flotante = true
    var producto: ProductoDB = ProductoDB(0, 0, 0, 0, "", "", "", 0.0,0.0)
    var BASE_IP = "api.vc.sitrans.cu"
    var BASE_PORT = "8000"
    var accion_eliminar = "Eliminar"
    var accion_insertar = "Insertar"
    var accion = ""
    var tipo = 1
    var rol = 0
    var id_alamcen = -1
    var id_producto = -1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (getArguments() == null) {
            requireActivity().onBackPressed()
        }


        getArguments().let {
            id_alamcen = it?.getInt("id_alamcen", -1) ?: -1
            id_producto = it?.getInt("id_producto", -1) ?: -1
        }

        if (id_producto == -1 || id_alamcen == -1) {
            requireActivity().onBackPressed()
        }
        Log.e("id_producto id_alamcen", "" + id_alamcen + " " + id_producto)
        //  val almacen= AppDatabase.getInstance(requireContext())?.almacenDao()?.getByID(id_alamcen!!)
        producto = AppDatabase.getInstance(requireContext())?.productoDao()
            ?.getByIdAlmacenIdProducto(id_alamcen, id_producto)!!
        val imagen = AppDatabase.getInstance(requireContext())?.imagenDao()?.getByID(id_producto)

        if (imagen != null) {
            Glide.with(requireContext()).load(Util.decodeBase64(imagen.imagen))
                .into(binding.imagen)

        } else {
            Glide.with(requireContext()).load(R.drawable.box)
                .into(binding.imagen)
        }
        producto.let {
            val formatter: NumberFormat = DecimalFormat("#.##")
            binding.venta.text = "$" + formatter.format(it.precio)
            binding.costo.text = "$" + formatter.format(it.costo)
            binding.existencias.text = "" + it.existencias
            binding.bloqueados.text = "" + it.bloqueados
            binding.disponibles.text = "" + (it.existencias - it.bloqueados)
            binding.um1.text = "" + it.unidad_medida
            binding.um2.text = "" + it.unidad_medida
            binding.um3.text = "" + it.unidad_medida
            binding.descripcion.text = "" + it.descripcion
            binding.codigo.text = "" + it.codigo

        }
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    android.R.id.home -> {  //ojo para salir
                        activity?.onBackPressed()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        /****menu cargando*****/
        val builder = AlertDialog.Builder(requireContext(), R.style.Translucent_NoTitle)
        val inflater = requireActivity().layoutInflater
        builder.setView(inflater.inflate(R.layout.cargando, null))
        builder.setCancelable(false)
        cargando = builder.create()
        binding.imagenEliminar.setOnClickListener {
            IonAlert(requireContext())
                .setTitleText("Desea eliminar la imagen?")
                .setConfirmText("Si")
                .setConfirmClickListener({
                   it.hide()
                   accion = accion_eliminar
                   base64imagen = ""
                   conectarimagen()
                })
                .setCancelText("No")
                .setCancelClickListener({
                    it.cancel()
                }).show()
        }
        binding.imagenCamara.setOnClickListener {
            tipo = 1
            accion = accion_insertar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissionsCAM()
            } else {
                capturarImagen()
            }
        }
        binding.imagenGaleria.setOnClickListener {
            tipo = 2
            accion = accion_insertar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissionsCAM()
            } else {
                capturarImagen()
            }
        }

        actualizarVista()
    }

    override fun onResume() {
        super.onResume()
        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        vibrar = pref.getBoolean("vibrar", true)
        notificacion_flotante = pref.getBoolean("notificacion", true)
        BASE_IP = pref.getString("host", "api.vc.sitrans.cu")!!
        BASE_PORT = pref.getString("puerto", "8000")!!
        rol = pref.getInt("rol", 0)
        actualizarVista()
    }

    fun actualizarVista() {
        if (rol == 0) {
            binding.layaut.layoutParams = LinearLayout.LayoutParams(0, 0)
        } else {
            val imagen =
                AppDatabase.getInstance(requireContext())?.imagenDao()?.getByID(id_producto)
            if (imagen != null) {

                binding.imagenEliminar.visibility = View.VISIBLE
            } else {
                binding.imagenEliminar.visibility = View.INVISIBLE

            }


            binding.layaut.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        }
    }

    private fun cargartoken() {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val USER = pref.getString("username", "admin")
        val PASSWORD = pref.getString("password", "admin")
        val url = "https://" + BASE_IP + ":" + BASE_PORT + "/"
        val user = JsonObject()
        user.addProperty("username", USER)
        user.addProperty("password", PASSWORD)
        Log.e("JSON", "" + user.toString())
        Log.e("URL", "" + url)
        val apiCall = ApiInterface.loguin(url)
        apiCall.loguin(user).enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                if (response.isSuccessful) {
                    val loguin = response.body()
                    val rol = loguin?.rol
                    val token = loguin?.token
                    val empresa = loguin?.empresa
                    with(pref.edit()) {
                        putString("token", "" + token)
                        putInt("rol", rol!!)
                        putString("empresa", "" + empresa)
                        apply()
                    }
                    actualizarVista()
                    check_token = false  //true de new
                    conectarimagen()
                } else {
                    if (response.code() == 401) {
                        mensaje_error(getString(R.string.m_autentificacion_fail))
                    } else {
                        Log.e("Respuesta Inesperada2", response.toString())
                        mensaje_error(getString(R.string.m_res_inesperada))
                    }
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                mensaje_error(getString(R.string.m_error_conexion))
            }

        })


    }

    private fun conectarimagen() {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val token = "" + pref.getString("token", "")
        val url = "https://" + BASE_IP + ":" + BASE_PORT + "/"
        val apiCall = ApiInterface.chequear(url, token)
        cargando!!.show()
        Log.e("URL", "" + url)
        val data = JsonObject()
        data.addProperty("base64imagen", base64imagen) //vaciopara q elimine
        Log.e("JSON", "" + data.toString())
        Log.e("URL", "" + url)
        apiCall.imagenes(producto.id_producto, data).enqueue(object : Callback<ImagenResult> {
            override fun onResponse(call: Call<ImagenResult>, response: Response<ImagenResult>) {

                Log.e("RESPONSE IMAGEN", "" + response.code())
                Log.e("RESPONSE IMAGEN", "" + response.message())
                if (response.isSuccessful) {

                    val result = response.body()
                    //Log.e("ALMACENES", "" + result?.almacenes?.size)
                    if (accion == accion_eliminar) {
                        Glide.with(requireContext()).load(R.drawable.box)
                            .into(binding.imagen)   //borro imagen
                        AppDatabase.getInstance(requireContext())?.imagenDao()
                            ?.deleteID(producto.id_producto)  //borro imagen DB

                    }
                    if (accion == accion_insertar) {
                        Glide.with(requireContext()).load(Util.decodeBase64(base64imagen))
                            .into(binding.imagen)   //borro imagen
                        var imadb = AppDatabase.getInstance(requireContext())?.imagenDao()
                            ?.getByID(producto.id_producto)
                        if (imadb != null) {
                            imadb.imagen = base64imagen
                            AppDatabase.getInstance(requireContext())?.imagenDao()?.update(imadb)
                        } else {
                            imadb = ImagenDB(producto.id_producto, base64imagen)
                            AppDatabase.getInstance(requireContext())?.imagenDao()?.insert(imadb)
                        }

                    }

                    actualizarVista()
                    //*****Mostrar OK********
                    if (notificacion_flotante) {
                        DynamicToast.makeSuccess(requireContext(), result?.message).show()
                    } else {
                        IonAlert(
                            requireContext(),
                            IonAlert.SUCCESS_TYPE
                        ).setTitleText(result?.message).show()
                    }

                    cargando!!.dismiss()

                    if (vibrar) {
                        Util.Vibrar(context, 150)
                    }
                    check_token = true  //true de new


                } else {
                    if (response.code() == 401) {  //TOKEN INVALIDO o Expirado
                        if (check_token) {
                            Log.e("check_token", "check_token")
                            cargartoken()
                        } else {
                            mensaje_error(getString(R.string.m_autentificacion_fail))
                        }

                    } else if (response.code() == 417 || response.code() == 203) { //error en parametros
                        var body = response.errorBody()
                        if (body != null) {
                            val string = JSONObject(body.string()).getString("message")
                            if (string.isEmpty()) {
                                mensaje_error(string)
                            } else {
                                mensaje_error(getString(R.string.m_res_inesperada))
                            }


                        } else {
                            mensaje_error(getString(R.string.m_res_inesperada))

                        }
                    } else if (response.code() == 500) {
                        mensaje_error(getString(R.string.m_error_500))
                    } else {
                        Log.e("Respuesta Inesperada1", response.toString())
                        mensaje_error(getString(R.string.m_res_inesperada))
                    }

                }


            }

            override fun onFailure(call: Call<ImagenResult>, t: Throwable) {
                mensaje_error(getString(R.string.m_error_conexion))
            }

        })
    }

    fun mensaje_error(mensaje: String) {
        if (notificacion_flotante) {
            DynamicToast.makeError(requireContext(), mensaje).show()
        } else {
            IonAlert(requireContext(), IonAlert.ERROR_TYPE).setTitleText(mensaje).show()
        }
        if (vibrar) {
            Util.Vibrar(context, 50)
        }
        cargando!!.dismiss()

    }

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        try {
            if (it.resultCode == Activity.RESULT_OK) {
                var uri = it.data!!.data
                if (uri != null) {
                    val base64 = Util.encodeImage(uri, requireContext())
                    if (base64 != "") {
                        base64imagen = base64
                        conectarimagen();
                    } else {
                        mensaje_error("Error al recortar imagen")
                    }
                } else {
                    mensaje_error("Error al recortar imagen")
                }
            } else if (it.resultCode == ImagePicker.RESULT_ERROR) {
                mensaje_error("Error al recortar imagen")
            }
        } catch (e: Exception) {
            mensaje_error("Error al recortar imagen")
        }
    }

    fun capturarImagen() {
        if (tipo == 1) { //camara
            val i = with(requireActivity())
                .cropSquare()
                .cameraOnly() //We have to define what image provider we want to use
                .maxResultSize(
                    512,
                    512,
                    false
                ) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent()
            launcher.launch(i)
        }
        if (tipo == 2) { //galeria
            val i = with(requireActivity())
                .cropSquare()
                .galleryOnly() //We have to define what image provider we want to use
                .maxResultSize(
                    512,
                    512,
                    false
                ) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent()
            launcher.launch(i)
        }
    }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                capturarImagen()
            } else {
                // mostrar explicacion
            }
        }

    private fun checkPermissionsCAM() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                cargarImagen()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

    }

    private fun cargarImagen() {
        if (tipo == 1) { //camara
            val i = with(requireActivity())
                .cropSquare()
                .cameraOnly() //We have to define what image provider we want to use
                .maxResultSize(
                    512,
                    512,
                    false
                ) //Final image resolution will beless than 1080 x 1080(Optional)
                .createIntent()
            launcher.launch(i)
        }
        if (tipo == 2) { //galeria
            val i = with(requireActivity())
                .cropSquare()
                .galleryOnly() //We have to define what image provider we want to use
                .maxResultSize(
                    512,
                    512,
                    false
                ) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent()
            launcher.launch(i)
        }
    }


}