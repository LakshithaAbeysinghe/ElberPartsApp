package com.elber.parts.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elber.parts.R
import com.elber.parts.data.db.AppDatabase
import com.elber.parts.data.db.Category
import com.elber.parts.data.db.Product
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryFragment : Fragment(R.layout.fragment_category) {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: CategoryGridAdapter

    // Room DAO
    private val dao by lazy { AppDatabase.getInstance(requireContext()).productDao() }

    // current collecting job (so we can switch category cleanly)
    private var collectJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView as a 2-column grid
        rv = view.findViewById(R.id.rvProducts)
        val grid = GridLayoutManager(requireContext(), 2)
        rv.layoutManager = grid
        rv.setHasFixedSize(true)

        // Edge spacing = 12dp, horizontal gap = 20dp, vertical gap = 10dp
        rv.addItemDecoration(CenteredGridSpacingDecoration(edgeDp = 12f, horizontalDp = 20f, verticalDp = 10f))

        // Tap -> Product Details
        adapter = CategoryGridAdapter(emptyList()) { card ->
            val args = Bundle().apply {
                putString("title", card.title)
                putString("desc",  card.subtitle)
                putString("price", card.price)
                putString("imageName", card.imageName)
                putString("size", card.size) // nullable is OK
            }
            findNavController().navigate(R.id.productDetailFragment, args)
        }
        rv.adapter = adapter

        // Initial category from nav args (nullable, accepts "resin" or "RESIN")
        val initial: Category? = arguments?.getString("category")
            ?.let { runCatching { Category.valueOf(it.uppercase()) }.getOrNull() }

        setTitle(view, initial)
        collectFor(initial)

        // Chip clicks (if header row exists in the layout)
        view.findViewById<LinearLayout?>(R.id.chipResin)?.setOnClickListener {
            onCategoryChosen(view, Category.RESIN)
        }
        view.findViewById<LinearLayout?>(R.id.chipAluminum)?.setOnClickListener {
            onCategoryChosen(view, Category.ALUMINUM)
        }
        view.findViewById<LinearLayout?>(R.id.chipFilaments)?.setOnClickListener {
            onCategoryChosen(view, Category.FILAMENTS)
        }
        view.findViewById<LinearLayout?>(R.id.chipScrews)?.setOnClickListener {
            onCategoryChosen(view, Category.SCREWS)
        }
    }

    private fun onCategoryChosen(root: View, cat: Category) {
        setTitle(root, cat)
        rv.scrollToPosition(0) // keep UX tidy when switching
        collectFor(cat)
    }

    private fun setTitle(root: View, cat: Category?) {
        root.findViewById<TextView?>(R.id.tvTitle)?.text = when (cat) {
            null -> "All Categories"
            Category.RESIN -> "Resins"
            Category.ALUMINUM -> "Aluminum"
            Category.FILAMENTS -> "Filaments"
            Category.SCREWS -> "Screws"
        }
    }

    /** (Re)collect products for a given category (or all when null). */
    private fun collectFor(cat: Category?) {
        collectJob?.cancel()
        collectJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = if (cat != null) dao.byCategory(cat) else dao.all()
                flow.collectLatest { list ->
                    adapter.submit(list.map { it.toCard(requireContext()) })
                }
            }
        }
    }
}

/* -------- Local UI model + grid adapter -------- */

private data class CategoryCard(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val price: String,
    val imageName: String?,   // for detail screen
    val size: String?         // optional display on detail screen
)

private class CategoryGridAdapter(
    private var items: List<CategoryCard>,
    private val onClick: (CategoryCard) -> Unit
) : RecyclerView.Adapter<CategoryGridAdapter.VH>() {

    fun submit(newItems: List<CategoryCard>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.img)
        val title: TextView = view.findViewById(R.id.title)
        val sub: TextView = view.findViewById(R.id.sub)
        val price: TextView = view.findViewById(R.id.price)
        init { view.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onClick(items[pos])
        } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.img.setImageResource(item.imageRes)
        holder.title.text = item.title
        holder.sub.text = item.subtitle
        holder.price.text = item.price
    }
}

/* DB -> UI mapper (resolves drawable by imageName, safe fallback) */
private fun Product.toCard(ctx: android.content.Context): CategoryCard {
    val resId = imageName?.let {
        ctx.resources.getIdentifier(it, "drawable", ctx.packageName)
    } ?: 0

    // naive size derivation: take first 4 chars if they're digits (e.g., "4040")
    val derivedSize = title.take(4).takeIf { it.all(Char::isDigit) }

    return CategoryCard(
        imageRes = if (resId != 0) resId else R.drawable.aluminum,
        title = title,
        subtitle = description,
        price = priceText,
        imageName = imageName,
        size = derivedSize
    )
}

/* -------- ItemDecoration with horizontal gap = 20dp, vertical gap = 10dp -------- */
private class CenteredGridSpacingDecoration(
    private val edgeDp: Float,
    private val horizontalDp: Float,
    private val verticalDp: Float
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val density = parent.resources.displayMetrics.density
        val edge = (edgeDp * density).toInt()
        val horizontal = (horizontalDp * density).toInt()
        val vertical = (verticalDp * density).toInt()

        val position = parent.getChildAdapterPosition(view)
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 2
        val column = position % spanCount

        // Vertical spacing
        outRect.top = if (position < spanCount) edge else vertical
        outRect.bottom = edge

        // Horizontal spacing
        if (column == 0) { // left column
            outRect.left = edge
            outRect.right = horizontal / 2
        } else {           // right column
            outRect.left = horizontal / 2
            outRect.right = edge
        }
    }
}
