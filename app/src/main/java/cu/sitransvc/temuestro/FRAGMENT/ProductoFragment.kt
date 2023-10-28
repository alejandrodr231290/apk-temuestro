package cu.sitransvc.temuestro.FRAGMENT

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import cu.sitransvc.temuestro.API.*
import cu.sitransvc.temuestro.DB.AppDatabase
import cu.sitransvc.temuestro.DB.entity.AlmacenDB
import cu.sitransvc.temuestro.DB.entity.ImagenDB
import cu.sitransvc.temuestro.DB.entity.ProductoDB
import cu.sitransvc.temuestro.R
import cu.sitransvc.temuestro.Util.EmptyDataObserver
import cu.sitransvc.temuestro.Util.ListaAlmacenAdapter
import cu.sitransvc.temuestro.Util.ListaProductosAdapter
import cu.sitransvc.temuestro.Util.Util
import cu.sitransvc.temuestro.databinding.FragmentProductoBinding
import id.ionbit.ionalert.IonAlert
import org.dhatim.fastexcel.Color
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ProductoFragment : Fragment() {


    var _binding: FragmentProductoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
     val binding get() = _binding!!
     var loadAnimator: Animator? = null

    var vibrar = true
    var notificacion_flotante = true
    var notificacion_conexion = true
    var BASE_IP = "api.vc.sitrans.cu"
    var BASE_PORT = "8000"
    var empresa = "SITRANS VC"
    var almacen_seleccionado: Int = -1
    var token: String = ""
    var ultimo_check: Long = 0
    var check_token: Boolean=true
    var adapter = ListaProductosAdapter(ArrayList(), this)

    var cargando: AlertDialog? = null
    var searchView: SearchView? = null
      lateinit var dialog: AlertDialog
     var menu_buscar:MenuItem? = null
     var  menu_exportar :MenuItem? = null
     var  menu_inv_tot :MenuItem? = null
     var  menu_almacen:MenuItem? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProductoBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /****Licencia Chequeo******/
        Log.e("Licencia", "" + Util.getLicencia(requireActivity()))
        if (!Util.licencia_Activa(requireActivity())) {
            mostrarlicencia()
        }

        /******cargando********/
        val builder = AlertDialog.Builder(
            requireContext(), R.style.Translucent_NoTitle
        )
        val inflater = requireActivity().layoutInflater
        builder.setView(inflater.inflate(R.layout.cargando, null))
        builder.setCancelable(false)
        cargando = builder.create()
        ////****ANIMACION*****////
        loadAnimator = AnimatorInflater.loadAnimator(requireContext(), R.animator.parpadear)
        /******menu********/
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_main, menu)
                Log.e("","")
                menu_buscar= menu.findItem(R.id.menu_buscar)
                menu_exportar = menu.findItem(R.id.menu_exportar)
                menu_inv_tot = menu.findItem(R.id.menu_inv_tot)
                menu_almacen = menu.findItem(R.id.menu_almacen)

                searchView = menu_buscar?.actionView as SearchView
                searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(s: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(s: String): Boolean {
                        adapter.filter.filter(s)
                         return false
                    }

                })
                   menu_exportar?.setVisible(almacen_seleccionado!=-1)
                   menu_inv_tot?.setVisible(almacen_seleccionado!=-1)
                   menu_almacen?.setVisible(almacen_seleccionado!=-1)
                   menu_buscar?.setVisible(almacen_seleccionado!=-1)
            }

            override fun onMenuClosed(menu: Menu) {
                super.onMenuClosed(menu)


            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_configuracion -> {

                        NavHostFragment.findNavController(this@ProductoFragment)
                            .navigate(R.id.action_ProductoFragment_to_ConfigracionFragment)
                        true
                    }
                    R.id.menu_acercade -> {
                        NavHostFragment.findNavController(this@ProductoFragment)
                            .navigate(R.id.action_ProductoFragment_to_acercaDeFragment )
                        true
                    }

                    R.id.menu_inv_tot -> {
                        mostrar_inventarototal()
                        true
                    }
                    R.id.menu_exportar -> {
                        // pregumtar por exportar
                        exportar()
                        true
                    }
                    R.id.menu_almacen -> {
                        mostrarSeleccionarAlmacen()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        /////////*****************//////////////

        binding.fab.setOnClickListener {

             cargaralmacenes()

        }

        //*************cargo lista********///////////
        binding.lista.layoutManager = LinearLayoutManager(requireContext())
        binding.lista.setHasFixedSize(true)
        binding.lista.adapter=adapter
        val emptyDataObserver = EmptyDataObserver( this)
        adapter.registerAdapterDataObserver(emptyDataObserver)
    }




    private fun cargaralmacenes() {
        binding.fab.visibility=View.INVISIBLE
        searchView?.setIconified(true);
        searchView?.onActionViewCollapsed()
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val url = "https://" + BASE_IP + ":" + BASE_PORT + "/"
        val apiCall = ApiInterface.chequear(url, token)
        binding.fab.visibility = View.INVISIBLE
        cargando!!.show()
        Log.e("URL", "" + url)
        apiCall.almacenes().enqueue(object : Callback<DataResult> {
            override fun onResponse(call: Call<DataResult>, response: Response<DataResult>) {
                if (response.isSuccessful) {
                    //**guardar datos de ultima carga***///
                    ultimo_check = Date().time
                    with(pref.edit()) {
                        putLong("ultimo_check", ultimo_check)
                        apply()
                    }
                    /////********//////
                    val result = response.body()
                    Log.e("ALMACENES", "" + result?.almacenes?.size)
                    guardarDatos(result?.almacenes, result?.imagenes)
                    mostrarDatos()

                    //*****Mostrar OK********

                    if (notificacion_flotante) {
                        DynamicToast.makeSuccess(
                            requireContext(),
                            getString(R.string.carga_completa)
                        ).show()
                    } else {
                        IonAlert(requireContext(), IonAlert.SUCCESS_TYPE)
                            .setTitleText(getString(R.string.carga_completa))
                            .show()
                    }

                    cargando!!.dismiss()
                    binding.fab.visibility = View.VISIBLE
                    if (vibrar) {
                        Util.Vibrar(context, 150)
                    }
                    check_token=true  //true de new


                } else {
                    Log.e("ERROR", "" + response.toString())


                    if (response.code() == 401) {  //TOKEN INVALIDO o Expirado
                        if (check_token) {
                            // Log.e("TOKEN EXPIRADO", "" + response.message())
                            cargartoken()

                        } else {

                            // Log.e("TOKEN EXPIRADO 2", "" + response.message())
                            mensaje_error(getString(R.string.m_autentificacion_fail))
                        }

                    } else  if (response.code() == 500) {
                        mensaje_error(getString(R.string.m_error_500))
                    }else {
                        Log.e("Respuesta Inesperada1", response.toString())
                        mensaje_error(getString(R.string.m_res_inesperada))
                    }

                }


            }

            override fun onFailure(call: Call<DataResult>, t: Throwable) {
                Log.e("ERROR", "" + t.localizedMessage)
                mensaje_error(getString(R.string.m_error_conexion))
            }

        })


    }

    fun mostrarDatos() {
        var text = ""
        menu_exportar?.setVisible(almacen_seleccionado!=-1)
        menu_inv_tot?.setVisible(almacen_seleccionado!=-1)
        menu_almacen?.setVisible(almacen_seleccionado!=-1)
        menu_buscar?.setVisible(almacen_seleccionado!=-1)

        if (almacen_seleccionado != -1) {
            val lp = AppDatabase.getInstance(requireContext())?.productoDao()?.getByIdAlmacen2(almacen_seleccionado)
            adapter.listaoriginal=lp
            adapter.list=lp
            adapter.notifyDataSetChanged()
            Log.e("adapter1", "len " +lp!!.size)
            Log.e("adapter2", "len " +adapter.listaoriginal!!.size)
            //muestro contador
            val a = AppDatabase.getInstance(requireContext())?.almacenDao()
                ?.getByID(almacen_seleccionado)
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                "T-Muestro: " + (a?.nombre ?: "---")
            text += empresa
            if (notificacion_conexion) {
                text += "\t \tChequeado: "
                text += Util.getfechaTXT2(ultimo_check) + " " + Util.getHoraXT(ultimo_check)
            }
            binding.textConctado.text = text
         } else {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "T-Muestro"
            binding.textConctado.text = ""
            binding.layConexoion.layoutParams=LinearLayout.LayoutParams(0,0)


        }

    }

    override fun onResume() {
        super.onResume()
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        vibrar = pref.getBoolean("vibrar", true)
        notificacion_flotante = pref.getBoolean("notificacion", true)
        notificacion_conexion = pref.getBoolean("conexion", true)
        BASE_IP = prefs.getString("host", "api.vc.sitrans.cu")!!
        BASE_PORT = prefs.getString("puerto", "8000")!!
        empresa = pref.getString("empresa", "")!!
        ultimo_check = pref.getLong("ultimo_check", 0)
        token =pref.getString("token", "")!!
        almacen_seleccionado = pref.getInt("almacen_seleccionado", -1)

        menu_exportar?.setVisible(almacen_seleccionado!=-1)
        menu_inv_tot?.setVisible(almacen_seleccionado!=-1)
        menu_almacen?.setVisible(almacen_seleccionado!=-1)
        menu_buscar?.setVisible(almacen_seleccionado!=-1)

        mostrarDatos()

    }

    private fun guardarDatos(almacenes: List<ApiAlmacen>?, imagenes: List<ApiImagen>?) {

        //***********Limpio DB*************//////
        AppDatabase.getInstance(requireContext())?.almacenDao()?.deleteall()
        AppDatabase.getInstance(requireContext())?.productoDao()?.deleteall()
        //////////*********/////////
        var id_almacen = 0
        var seleccionado = false
        //agrego almacenes
        almacenes?.forEach {
            id_almacen = it.id
            val a = AlmacenDB(
                id_almacen,
                it.codigo,
                it.nombre
            )
            //chequeo si el seleccionado se chequeo previamente
            if (!seleccionado && id_almacen == almacen_seleccionado) {
                seleccionado = true
            }

            AppDatabase.getInstance(requireContext())?.almacenDao()?.insert(a)
            val productos = it.productos
            productos.forEach {
                val p = ProductoDB(
                    id_almacen,
                    it.id,
                    it.existencias,
                    it.bloqueados,
                    it.codigo,
                    it.descripcion,
                    it.unidad_medida,
                    it.precio,
                    it.costo
                )
                AppDatabase.getInstance(requireContext())?.productoDao()?.insert(p)
            }
        }


        //seleccionodo cambió al chequear
        if (!seleccionado) {
            almacen_seleccionado = id_almacen
            insertAlmacenSeleccionado()
        }


        //elimino imagenes
        AppDatabase.getInstance(requireContext())?.imagenDao()?.deleteall()
        //agrego imagenes
        imagenes?.forEach {
            val i = ImagenDB(it.id_producto, it.data_imgen)
            AppDatabase.getInstance(requireContext())?.imagenDao()?.insert(i)
        }


    }

    fun mensaje_error(mensaje: String) {
        if (notificacion_flotante) {
            DynamicToast.makeError(requireContext(), mensaje).show()
        } else {
            IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                .setTitleText(mensaje)
                .show()
        }
        if (vibrar) {
            Util.Vibrar(context, 50)
        }
        cargando!!.dismiss()
        binding.fab.visibility = View.VISIBLE
    }

    fun mensaje_ok(mensaje: String) {
        if (notificacion_flotante) {
            DynamicToast.makeSuccess(requireContext(), mensaje).show()
        } else {
            IonAlert(requireContext(), IonAlert.SUCCESS_TYPE)
                .setTitleText(mensaje)
                .show()
        }
        if (vibrar) {
            Util.Vibrar(context, 50)
        }
        cargando!!.dismiss()
        binding.fab.visibility = View.VISIBLE
    }


    private fun cargartoken() {
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
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
                    token = loguin?.token!!
                    val empresa = loguin.empresa
                    with(pref.edit()) {
                        putString("token", "" + token)
                        putInt("rol", rol!!)
                        putString("empresa", "" + empresa)
                        apply()
                    }
                    check_token=false
                    cargaralmacenes()
                } else {
                    if (response.code() == 401) {
                        mensaje_error(getString(R.string.m_autentificacion_fail))
                    } else  if (response.code() == 500) {
                        mensaje_error(getString(R.string.m_error_500))
                    }else {
                        Log.e("Respuesta Inesperada", response.toString())
                        mensaje_error(getString(R.string.m_res_inesperada))
                    }
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                mensaje_error(getString(R.string.m_error_conexion))
            }

        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun insertAlmacenSeleccionado() {
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        with(pref.edit()) {
            putInt("almacen_seleccionado", almacen_seleccionado)
            apply()
        }

    }

    fun mostrarlicencia() {

        val code_act = Util.getCode(requireActivity())
        var fecha_act = Util.getLicencia(requireActivity())

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val vista = inflater.inflate(R.layout.dialog_licencia, null)
        builder.setView(vista)
        builder.setCancelable(false)
        dialog = builder.create()
        val code = vista.findViewById<TextView>(R.id.code)
        val codeactivacion = vista.findViewById<TextView>(R.id.codeactivacion)
        code.text = code_act
        codeactivacion.text = ""
        val pegar = vista.findViewById<ImageView>(R.id.pegar)
        val compartir = vista.findViewById<ImageView>(R.id.compartir)
        val compartir2 = vista.findViewById<ImageView>(R.id.compartir2)
        compartir.setOnClickListener {
            loadAnimator?.apply {
                setTarget(it)
                start()
            }
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                code_act
            )
            shareIntent.type = "text/plain"
            startActivity(Intent.createChooser(shareIntent, "Compartir vía"))
        }
        compartir2.setOnClickListener {
            loadAnimator?.apply {
                setTarget(it)
                start()
            }
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", code_act)
            clipboard.setPrimaryClip(clip)
            DynamicToast.makeSuccess(requireContext(), "Código copiado").show()
        }
        pegar.setOnClickListener {
            loadAnimator?.apply {
                setTarget(it)
                start()
            }
            val clipboard =  requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val data = clipboard.primaryClip
                if (data!!.itemCount > 0) {
                    val text = data.getItemAt(0).coerceToText(context)
                    if (text != null) {
                        codeactivacion.text = text
                        //chequeo

                        try {
                            var des = Util.decrypt("" + text)
                            Log.e("ACTIVACION", "" + des)
                            val json = JSONObject(des)
                            val codigo= json.getString("codigo")
                            val fecha= json.getString("fecha")
                            val time_fecha=Util.getTime(fecha);
                            Log.e("fecha_act", "" + fecha_act)
                            Log.e("time_fecha", "" + time_fecha)
                            Log.e("code", "" + code)
                            if (codigo != code_act) {
                                Log.e("ERROR", "CODE CONF: " + code)
                                mensaje_error("Código Incorrecto")
                                return@setOnClickListener
                            }

                            if (time_fecha > fecha_act) {
                                val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                                val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                                with(pref.edit()) {
                                    putLong("licencia", time_fecha)
                                    apply()
                                }
                                with(prefs.edit()) {
                                    if(json.has("host")){
                                        putString("host", json.getString("host"))
                                    }
                                    if(json.has("puerto")){
                                        putString("puerto", json.getString("puerto"))
                                    }
                                    if(json.has("username")){
                                        putString("username", json.getString("username"))
                                    }
                                    if(json.has("password")){
                                        putString("password", json.getString("password"))
                                    }
                                    apply()
                                }
                                dialog.hide()
                                DynamicToast.makeSuccess(
                                    requireContext(),
                                    "Licencia hasta: " + Util.getfechaTXT(time_fecha)
                                ).show()

                             } else {
                                mensaje_error("Código Incorrecto")
                                return@setOnClickListener
                            }

                           /*Log.e("ACTIVACION0", "" + des)
                            val split = des!!.split("\n")
                            val cod_check: String = split.get(0)
                            val cod_time: String = split.get(2)
                            if (cod_check != code_act) {
                                Log.e("ERROR", "CODE CONF: " + code)
                                Log.e("ERROR", "CODE SPLIT: " + split[0])
                                DynamicToast.makeError(requireContext(), "Código Incorrecto").show()
                                return@setOnClickListener
                            }
                            val newactiv: Long = Util.getTime(cod_time)

                            if (newactiv > fecha_act) {
                                fecha_act = newactiv
                                Util.setLicencia(requireActivity(), newactiv)
                                DynamicToast.makeSuccess(
                                    requireContext(),
                                    "Licencia hasta: " + Util.getfechaTXT(newactiv)
                                ).show()
                                dialog.dismiss()
                                /*NavHostFragment.findNavController(this@ProductoFragment)
                                    .navigate(R.id.action_ProductoFragment_to_ConfigracionFragment)*/
                            } else {
                                DynamicToast.makeError(requireContext(), "Código Incorrecto").show()
                                return@setOnClickListener
                            }*/
                        } catch (e: Exception) {
                            mensaje_error("Código Incorrecto: "+e.message)

                        }
                    }
                }
            }
        }
        dialog.show()
    }

    fun mostrarSeleccionarAlmacen() {
        if (almacen_seleccionado == -1) {
            mensaje_error(getString(R.string.vacio))
            return
        }
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val vista = inflater.inflate(R.layout.dialog_seleccionar_almacen, null)
        builder.setView(vista)
        builder.setCancelable(true)
        dialog = builder.create()
        val lista: RecyclerView = vista.findViewById(R.id.lista)
        //cargo lista
        lista.layoutManager = LinearLayoutManager(requireContext())
        lista.setHasFixedSize(true)
        val la = AppDatabase.getInstance(requireContext())?.almacenDao()?.getAll()
        lista.adapter = ListaAlmacenAdapter(la, this)
        // lista.adapter.
        dialog.show()
    }


    fun mostrar_inventarototal() {
        if (almacen_seleccionado == -1) {
            mensaje_error(getString(R.string.vacio))
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val vista: View = inflater.inflate(R.layout.dialog_mostrar_inventario_toal, null)
        builder.setView(vista)
        builder.setCancelable(true)
        dialog = builder.create()
        val cup = vista.findViewById<TextView>(R.id.cup)
        val copiar = vista.findViewById<ImageView>(R.id.copiar)
        val can = AppDatabase.getInstance(requireContext())?.productoDao()?.getAllMoney()
        var txt=""
        val formatter = DecimalFormat("###,###.##")
        val almacenes=AppDatabase.getInstance(requireContext())?.almacenDao()?.getAll()
        almacenes?.forEach {
            txt+="<p><b>➢"+it.nombre+":</b>  $"+formatter.format(AppDatabase.getInstance(requireContext())?.productoDao()?.getAllMoney(it.id_alamcen)).replace(","," ")+"</p>"
        }
        txt+="<p style=\"text-align:center\"> <br> </p><p><b>➤TOTAL:</b>  $" + formatter.format(can).replace(","," ") + "</p> "

        cup.text =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(txt, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(txt)
        }

        copiar.setOnClickListener {
            loadAnimator?.apply {
                setTarget(it)
                start()
            }
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", "" + formatter.format(can))
            clipboard.setPrimaryClip(clip)

            DynamicToast.makeSuccess(requireContext(), "Inventario total copiado").show()
        }
        dialog.show()
    }

   private fun exportar() {
       if (almacen_seleccionado == -1) {
           mensaje_error(getString(R.string.vacio))
           return

       }
       val a =
           AppDatabase.getInstance(requireContext())?.almacenDao()?.getByID(almacen_seleccionado)
       val p = AppDatabase.getInstance(requireContext())?.productoDao()
           ?.getByIdAlmacen2(almacen_seleccionado)

       var nombre = "Almacén " + a?.nombre + " "
       nombre += "_" + getCurrentDateTime()
       nombre += ".xls"
       try {
           val file = File(requireActivity().filesDir,nombre)
           /*if(!file.exists()){
               file.createNewFile()
               Log.e("Compartir", "FICHERO CREADO: " + nombre)
           }*/
           Log.e("FileOutputStream", "FileOutputStream " + nombre)
           val fos = FileOutputStream(file)
           Log.e("Compartir", "FileOutputStream " + nombre)
           val wb = Workbook(fos, "Application", "1.0")
           val ws: Worksheet = wb.newWorksheet("Productos")
           val headers = arrayOf(
               "Descripción",
               "Código",
               "Existencia",
               "UM",
               "Precio CUP"
           )
           var pos = 0
           var col = 0
           for (header in headers) {
               ws.value(pos, col, header)
               ws.style(pos, col).bold().fontColor(Color.DARK_RED)
               col++
           }
          pos++
          p?.map {
               ws.value(pos,0,it.descripcion)
               ws.value(pos,1,it.codigo)
               ws.value(pos,2,(it.existencias - it.bloqueados).toDouble())
               ws.value(pos,3,it.unidad_medida)
               ws.value(pos,4,it.precio)
               pos++
           }
           pos++
           ws.value(pos,0,"Productos Totales")
           ws.value(pos,1,p?.size)
           pos++
           ws.value(pos,0,"Fecha Exportado")
           ws.value(pos,1,(Util.getfechaTXT(Date().time) + " " + Util.getHoraXT(Date().time)))
           wb.finish()

           Log.e("Compartir", "Compartir: " + file.path)
           val uri = FileProvider.getUriForFile(
               requireActivity(),
               "cu.sitransvc.temuestro.fileprovider",
               file
           )

           val shareIntent = Intent(Intent.ACTION_SEND)
           shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
           shareIntent.type = "text/text"
           shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
           startActivity(Intent.createChooser(shareIntent, "Compartir Fichero"))
           Log.e("Dir", "FIN2")
       } catch (e: java.lang.Exception) {
           Log.e("ERR", "ShareFile: " + e.message)
           mensaje_error("Error al Exportar")

       }
   }

    private fun getCurrentDateTime(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           // Log.d(TAG, "getCurrentDateTime: greater than O")
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss"))
        } else {
           // Log.d(TAG, "getCurrentDateTime: less than O")
            val SDFormat = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss")
            SDFormat.format(Date())
        }
    }

}