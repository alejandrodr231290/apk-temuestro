package cu.sitransvc.temuestro.Util

import android.animation.Animator
import android.animation.AnimatorInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import cu.sitransvc.temuestro.DB.entity.AlmacenDB
import cu.sitransvc.temuestro.FRAGMENT.ProductoFragment
import cu.sitransvc.temuestro.R

class ListaAlmacenAdapter(private val list: List<AlmacenDB>?, private val pf: ProductoFragment) :
    RecyclerView.Adapter<ListaAlmacenAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_almacen, parent, false)
        return ViewHolder(v, pf)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.a = list?.get(position)
        holder.nombre.text = "" + list?.get(position)?.nombre
        if (pf.almacen_seleccionado == list?.get(position)?.id_alamcen ?: -1) {
            holder.imagen.setImageResource(R.drawable.circle_ckeck)
        } else {
            holder.imagen.setImageResource(R.drawable.circle_unckeck)
        }
    }

    override fun getItemCount(): Int {
        return list!!.size
    }


    class ViewHolder(v: View, pf: ProductoFragment) : RecyclerView.ViewHolder(v) {
        val nombre: TextView

        val imagen: ImageView
        var a: AlmacenDB? = null
        var pf: ProductoFragment


        init {
            this.pf = pf
            nombre = v.findViewById(R.id.nombre)
            imagen = v.findViewById(R.id.imagen)
            v.setOnClickListener { v ->
                if (!pf.loadAnimator!!.isRunning&&!pf.loadAnimator!!.isStarted &&pf.binding.fab.visibility==View.VISIBLE) {
                    a?.let {
                        if (it.id_alamcen != pf.almacen_seleccionado) {
                            pf.almacen_seleccionado = it.id_alamcen;
                            pf.insertAlmacenSeleccionado()
                            imagen.setImageResource(R.drawable.circle_ckeck)
                            pf.mostrarDatos()
                            pf.dialog.dismiss()
                        } else {
                            pf.loadAnimator?.apply {
                                setTarget(v)
                                start()
                            }

                        }
                    }
                }
            }


        }
    }
}




