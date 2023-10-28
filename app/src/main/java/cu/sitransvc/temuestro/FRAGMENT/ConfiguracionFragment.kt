package cu.sitransvc.temuestro.FRAGMENT

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.gson.JsonObject
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import cu.sitransvc.temuestro.API.ApiInterface
import cu.sitransvc.temuestro.API.LoginResult
import cu.sitransvc.temuestro.Util.Util
import cu.sitransvc.temuestro.R
import cu.sitransvc.temuestro.databinding.FragmentConfiguracionBinding
import id.ionbit.ionalert.IonAlert
import org.joda.time.DateTime
import org.joda.time.Days
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ConfiguracionFragment : Fragment() {

    private var _binding: FragmentConfiguracionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var cargando: AlertDialog? = null

    var vibrar =true
    var notificacion_flotante = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfiguracionBinding.inflate(inflater, container, false)
        return binding.root

    }
    var licencia: AlertDialog? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /****menu cargando*****/
        val builder = AlertDialog.Builder(requireContext(), R.style.Translucent_NoTitle)
        val inflater = requireActivity().layoutInflater
        builder.setView(inflater.inflate(R.layout.cargando, null))
        builder.setCancelable(false)
        cargando = builder.create()

        binding.fab.setOnClickListener {
            checquear()
        }






       val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_licencia, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_licencia -> {
                             mostrar_licencia()
                             true

                    }
                    android.R.id.home -> {  //ojo para salir
                        activity?.onBackPressed()
                        true

                    }else -> false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }


    fun mensaje_error(mensaje: String) {
        if(notificacion_flotante){
            DynamicToast.makeError(requireContext(), mensaje).show()
        }else{
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

    fun  checquear(){
        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        vibrar = pref.getBoolean("vibrar", true)
        notificacion_flotante = pref.getBoolean("notificacion", true)
        val BASE_IP = pref.getString("host", "api.vc.sitrans.cu")!!
        val BASE_PORT = pref.getString("puerto", "8000")!!
        val USER = pref.getString("username", "admin")
        val PASSWORD = pref.getString("password", "admin")
        val url = "https://" + BASE_IP + ":" + BASE_PORT + "/";

        val user = JsonObject()
        user.addProperty("username", USER)
        user.addProperty("password", PASSWORD)
        Log.e("JSON", "" + user.toString())
        Log.e("URL", "" + url)
        binding.fab.visibility = View.INVISIBLE
        cargando!!.show()
        val apiCall = ApiInterface.loguin(url)
        apiCall.loguin(user).enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                Log.e("responsecode", "" + response.code())
                Log.e("responsemensaje", "" + response.message())
                if (response.isSuccessful) {
                    val loguin = response.body()
                    val rol = loguin?.rol
                    val token = loguin?.token
                    val empresa = loguin?.empresa

                    with(pref.edit()) {
                        putString("token", "" + token)
                        putInt("rol", rol!!)
                        Log.e("token","" +token )
                        putString("empresa", "" + empresa)
                        apply()
                    }


                    if(notificacion_flotante){
                        DynamicToast.makeSuccess(requireContext(), "Conectado a "+" "+empresa).show()
                    }else{
                        IonAlert(requireContext(), IonAlert.SUCCESS_TYPE)
                            .setTitleText("Conectado a "+" "+empresa)
                            .show()
                    }


                    cargando!!.dismiss()
                    binding.fab.visibility = View.VISIBLE
                    if(vibrar){
                        Util.Vibrar(context,150)
                    }


                } else {
                    if (response.code() == 401) {
                        mensaje_error(getString(R.string.m_autentificacion_fail))
                    } else  if (response.code() == 500) {
                        mensaje_error(getString(R.string.m_error_500))
                    }else {
                        mensaje_error( getString(R.string.m_res_inesperada))
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


    fun mostrar_licencia() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val vista: View = inflater.inflate(R.layout.dialog_mostrar_licencia, null)
        builder.setView(vista)
        builder.setCancelable(true)
        licencia = builder.create()
        val dias = vista.findViewById<TextView>(R.id.dias)
        val fecha = vista.findViewById<TextView>(R.id.fecha)
        val lic= Util.getLicencia(requireActivity())
        fecha.text= Util.getfechaTXT(lic)
        val d = Days.daysBetween(DateTime(), DateTime(Date(lic))).days + 1
        dias.text = "" + d
        licencia!!.show()
    }

}