package cu.sitransvc.temuestro.Util

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import cu.sitransvc.temuestro.DB.AppDatabase
import cu.sitransvc.temuestro.DB.entity.ProductoDB
import cu.sitransvc.temuestro.FRAGMENT.ProductoFragment
import cu.sitransvc.temuestro.R
import java.text.DecimalFormat
import java.text.NumberFormat


class ListaProductosAdapter(var listaoriginal: List<ProductoDB>?, val pf: ProductoFragment) :
    RecyclerView.Adapter<ListaProductosAdapter.ViewHolder>(), Filterable {
    var list: List<ProductoDB>? = null
    var formatter: NumberFormat? = null

    init {
        list = listaoriginal;
        formatter = DecimalFormat("###,###.##")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ViewHolder(v, pf)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = list?.get(position)
        p?.let {
            val im =
                AppDatabase.getInstance(pf.requireContext())?.imagenDao()?.getByID(it.id_producto)
            if (im != null) {
                Glide.with(pf.requireContext()).load(Util.decodeBase64(im.imagen))
                    .apply(RequestOptions().centerCrop())
                    .into(holder.imagen)

            } else {
                Glide.with(pf.requireContext()).load(R.drawable.box)
                    .apply(RequestOptions().centerCrop())
                    .into(holder.imagen)
                //  holder.imagen.setImageResource(R.drawable.box)
            }
            holder.venta.text = "$" + formatter!!.format(it.precio)
            holder.existencias.text = "" + (it.existencias - it.bloqueados)
            holder.unidad_medida.text = "" + it.unidad_medida
            holder.descripcion.text = "" + it.descripcion
            holder.codigo.text = "" + it.codigo
            holder.id_producto = it.id_producto
            holder.id_alamcen = it.id_alamcen
        }
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                list = results.values as List<ProductoDB>?
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence): FilterResults {
                var filteredResults: List<ProductoDB>?
                filteredResults = if (constraint.length == 0) {
                    listaoriginal
                } else {
                    getFilteredResults(constraint.toString().lowercase())
                }
                val results = FilterResults()
                results.values = filteredResults
                results.count = filteredResults?.size ?: 0
                return results
            }
        }
    }

    protected fun getFilteredResults(constraint: String): List<ProductoDB> {
        val results: MutableList<ProductoDB> = ArrayList()
        listaoriginal?.forEach {
            if (it.descripcion.lowercase().contains(constraint)
                || it.codigo.lowercase().contains(constraint)
            ) {
                results.add(it)
            }
        }
        return results
    }

    class ViewHolder(v: View, pf: ProductoFragment) : RecyclerView.ViewHolder(v) {
        val descripcion: TextView
        val existencias: TextView
        val unidad_medida: TextView
        val venta: TextView
        val codigo: TextView
        val imagen: ImageView
        var pf: ProductoFragment
        var id_producto: Int = 0
        var id_alamcen: Int = 0

        init {
            this.pf = pf
            descripcion = v.findViewById(R.id.descripcion)
            existencias = v.findViewById(R.id.existencias)
            venta = v.findViewById(R.id.venta)
            unidad_medida = v.findViewById(R.id.unidad_medida)
            codigo = v.findViewById(R.id.codigo)
            imagen = v.findViewById(R.id.imagen)

            v.setOnClickListener {
                if (!pf.loadAnimator!!.isRunning&&!pf.loadAnimator!!.isStarted &&pf.binding.fab.visibility==View.VISIBLE) {
                    pf.loadAnimator?.apply {
                        setTarget(v)
                        start()
                        doOnEnd {
                            val bundle = Bundle()
                            bundle.putInt("id_producto", id_producto)
                            bundle.putInt("id_alamcen", id_alamcen)
                            NavHostFragment.findNavController(pf).navigate(R.id.action_ProductoFragment_to_detalleProductoFragment,bundle )
                        }
                    }
                }

            }
        }
    }
}

