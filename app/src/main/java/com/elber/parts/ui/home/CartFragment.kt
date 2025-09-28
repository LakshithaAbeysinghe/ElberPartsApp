package com.elber.parts.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elber.parts.R
import com.elber.parts.data.cart.CartLine
import com.elber.parts.data.cart.CartRepository
import com.elber.parts.util.formatLkr
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvOrderTotal: TextView
    private lateinit var btnContinue: View

    private lateinit var adapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back arrow
        view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topBar)
            .setNavigationOnClickListener { findNavController().navigateUp() }

        rv = view.findViewById(R.id.rvCart)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvOrderTotal = view.findViewById(R.id.tvOrderTotal)
        btnContinue = view.findViewById(R.id.btnContinue)

        // ✅ THIS was missing — without a LayoutManager nothing is drawn
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = CartAdapter(
            onInc = { index -> CartRepository.incAt(index) },
            onDec = { index -> CartRepository.decAt(index) },
            onDelete = { index -> CartRepository.removeAt(index) }
        )
        rv.adapter = adapter

        // Observe cart
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                CartRepository.items.collectLatest { list ->
                    adapter.submitList(list)
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    tvOrderTotal.text = CartRepository.orderTotalRupees.formatLkr()
                }
            }
        }

        btnContinue.setOnClickListener {
            if (CartRepository.items.value.isEmpty()) {
                Snackbar.make(view, "Cart is Empty", Snackbar.LENGTH_SHORT).show()
            } else {
                // TODO: navigate to checkout screen
                Snackbar.make(view, "Proceeding to checkout…", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

/* ---------------- Adapter ---------------- */

private class CartAdapter(
    private val onInc: (Int) -> Unit,
    private val onDec: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : ListAdapter<CartLine, CartAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<CartLine>() {
        override fun areItemsTheSame(oldItem: CartLine, newItem: CartLine) =
            oldItem.title == newItem.title && oldItem.unitPriceText == newItem.unitPriceText

        override fun areContentsTheSame(oldItem: CartLine, newItem: CartLine) =
            oldItem == newItem
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.img)
        val title: TextView = v.findViewById(R.id.title)
        val unitPrice: TextView = v.findViewById(R.id.unitPrice)
        val tvQty: TextView = v.findViewById(R.id.tvQty)
        val tvLineTotal: TextView = v.findViewById(R.id.tvLineTotal)
        val btnDec: ImageButton = v.findViewById(R.id.btnDec)
        val btnInc: ImageButton = v.findViewById(R.id.btnInc)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = getItem(position)

        h.title.text = item.title
        h.unitPrice.text = item.unitPriceText
        h.tvQty.text = item.qty.toString()
        h.tvLineTotal.text = item.lineTotalRupees.formatLkr()

        val ctx = h.itemView.context
        val resId = item.imageName?.let { ctx.resources.getIdentifier(it, "drawable", ctx.packageName) } ?: 0
        h.img.setImageResource(if (resId != 0) resId else R.drawable.aluminum)

        h.btnInc.setOnClickListener {
            val idx = h.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) onInc(idx)
        }
        h.btnDec.setOnClickListener {
            val idx = h.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) onDec(idx)
        }
        h.btnDelete.setOnClickListener {
            val idx = h.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) onDelete(idx)
        }
    }
}
