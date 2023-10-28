package cu.sitransvc.temuestro.Util

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import cu.sitransvc.temuestro.FRAGMENT.ProductoFragment
import cu.sitransvc.temuestro.R


class EmptyDataObserver (pf: ProductoFragment): RecyclerView.AdapterDataObserver() {

    //private var emptyView: View? = null
    private var pf: ProductoFragment? = null

    init {
        this.pf = pf
        //emptyView= pf.getLayoutInflater().inflate(R.layout.item_vacio, null)
        checkIfEmpty()
    }


    private fun checkIfEmpty() {
        val pref = PreferenceManager.getDefaultSharedPreferences(pf!!.requireActivity())
        val token =pref.getString("token", "")!!
        val almacen_seleccionado = pref.getInt("almacen_seleccionado", -1)
     //  Log.e("CHECK","CAN "+pf!!.binding.lista.adapter?.itemCount )
        Log.e("CHECK","token :"+token )
        //Log.e("CHECK","almacen_seleccionado :"+almacen_seleccionado )

        if (pf!!.binding.lista.adapter?.itemCount == 0){
             pf!!.binding.layVacio.visibility =  View.VISIBLE
             pf!!.binding.lista.visibility =View.GONE

              if(token.equals("")){    //esta al principio

                pf!!.binding.imagen.setImageResource(R.drawable.conetar)
                pf!!.binding.textConexion.setText(R.string.conecte)
                pf!!.binding.fab.visibility=View.INVISIBLE
                pf!!.binding.layVacio.setOnClickListener(){
                    if (!pf!!.loadAnimator!!.isRunning&&!pf!!.loadAnimator!!.isStarted ) {
                        pf!!.loadAnimator?.apply {
                            setTarget(pf!!.binding.layVacio)
                            start()
                            doOnEnd {
                                NavHostFragment.findNavController(pf!!).navigate(R.id.action_ProductoFragment_to_ConfigracionFragment)
                            }
                        }
                    }
               }
            }else{
                  pf!!.menu_buscar?.setVisible(true)
                  pf!!.binding.imagen.setImageResource(R.drawable.empty)
                  pf!!.binding.textConexion.setText(R.string.no_encontrado)
            }
        }else{
            pf!!.binding.layVacio.visibility =  View.GONE
            pf!!.binding.lista.visibility =View.VISIBLE
        }

    }

    override fun onChanged() {
        super.onChanged()
        checkIfEmpty()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        super.onItemRangeChanged(positionStart, itemCount)
    }

}