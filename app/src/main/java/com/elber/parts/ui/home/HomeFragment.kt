package com.elber.parts.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.elber.parts.R
import com.elber.parts.data.db.AppDatabase
import com.elber.parts.data.db.Category
import com.elber.parts.data.db.Product
import com.elber.parts.data.db.ProductDao
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val autoHandler = Handler(Looper.getMainLooper())
    private var autoRunnable: Runnable? = null

    // DB
    private lateinit var productDao: ProductDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ----- banners (composed) -----
        val pager = view.findViewById<ViewPager2>(R.id.bannerPager)
        val dots = view.findViewById<LinearLayout>(R.id.dots)

        val banners = listOf(
            Banner(
                bgRes = R.drawable.aluminum_banner_bg,
                title = "70–50% OFF",
                sub1 = "Now in Aluminum",
                sub2 = "All Materials",
                cta = "Shop Now",
                category = Category.ALUMINUM
            ),
            Banner(
                bgRes = R.drawable.screws_banner_bg,
                title = "50–40% OFF",
                sub1 = "Now in Screws",
                sub2 = "All Materials",
                cta = "Shop Now",
                category = Category.SCREWS
            )
        )

        // on banner CTA tap
        pager.adapter = BannerPagerAdapter(banners) { tapped ->
            val args = bundleOf("category" to tapped.category?.name)
            findNavController().navigate(R.id.categoryFragment, args)
        }
        pager.offscreenPageLimit = 1
        pager.setPageTransformer(MarginPageTransformer(12))

        fun refreshDots(pos: Int) {
            dots.removeAllViews()
            for (i in banners.indices) {
                val dot = layoutInflater.inflate(R.layout.item_page_dot, dots, false)
                dot.isSelected = i == pos
                dots.addView(dot)
                if (i < banners.lastIndex) {
                    val spacer = View(requireContext())
                    spacer.layoutParams = LinearLayout.LayoutParams(8, 1)
                    dots.addView(spacer)
                }
            }
        }
        refreshDots(0)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = refreshDots(position)
        })

        // auto–swipe every 4s
        autoRunnable = object : Runnable {
            override fun run() {
                if (banners.isNotEmpty()) {
                    val next = (pager.currentItem + 1) % banners.size
                    pager.setCurrentItem(next, true)
                }
                autoHandler.postDelayed(this, 4000L)
            }
        }

        // ----- chips -> Category -----
        val chips = view.findViewById<LinearLayout>(R.id.rowChips)
        val chipToCategory = listOf(
            Category.RESIN,
            Category.ALUMINUM,
            Category.FILAMENTS,
            Category.SCREWS
        )
        for (i in 0 until chips.childCount) {
            val chip = chips.getChildAt(i)
            chip.isClickable = true
            chip.isFocusable = true
            chip.setOnClickListener {
                val cat = chipToCategory.getOrNull(i)
                val args = bundleOf("category" to cat?.name)
                findNavController().navigate(R.id.categoryFragment, args)
            }
        }

        // ----- products carousel (from DB; two cards wide) -----
        val rv = view.findViewById<RecyclerView>(R.id.rvProducts)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = ProductAdapter(emptyList())
        rv.adapter = adapter
        rv.addItemDecoration(HSpaceDecoration(2))

        // DB init
        productDao = AppDatabase.getInstance(requireContext()).productDao()

        // Seed once if empty, then observe
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (productDao.count() == 0) {
                    productDao.insertAll(sampleProducts())
                }
            }
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productDao.all().collectLatest { list ->
                    adapter.submit(list.map { it.toCard(requireContext()) })
                }
            }
        }

        // "View all" + red card
        view.findViewById<MaterialButton>(R.id.btnViewAll).setOnClickListener {
            findNavController().navigate(R.id.categoryFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cardShopCategory).setOnClickListener {
            findNavController().navigate(R.id.categoryFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        autoRunnable?.let { autoHandler.postDelayed(it, 4000L) }
    }

    override fun onPause() {
        super.onPause()
        autoHandler.removeCallbacksAndMessages(null)
    }
}

// ---------- helpers & banner adapter ----------

private class HSpaceDecoration(private val spaceDp: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val space = view.resources.displayMetrics.density * spaceDp
        outRect.right = space.toInt()
    }
}

private data class Banner(
    val bgRes: Int,
    val title: String,
    val sub1: String,
    val sub2: String,
    val cta: String,
    val category: Category? = null
)

private class BannerPagerAdapter(
    private val items: List<Banner>,
    private val onClick: (Banner) -> Unit
) : RecyclerView.Adapter<BannerPagerAdapter.VH>() {

    inner class VH(val root: View) : RecyclerView.ViewHolder(root) {
        val bg: ImageView = root.findViewById(R.id.bg)
        val title: TextView = root.findViewById(R.id.title)
        val sub1: TextView = root.findViewById(R.id.sub1)
        val sub2: TextView = root.findViewById(R.id.sub2)
        val cta: MaterialButton = root.findViewById(R.id.cta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = items[position]
        holder.bg.setImageResource(b.bgRes)
        holder.title.text = b.title
        holder.sub1.text = b.sub1
        holder.sub2.text = b.sub2
        holder.cta.text = b.cta
        holder.cta.setOnClickListener { onClick(b) }
        holder.root.setOnClickListener { onClick(b) }
    }
}

/** DB -> UI mapping (resolves drawable by name) */
private fun Product.toCard(ctx: android.content.Context): ProductCard {
    val resId = imageName?.let {
        ctx.resources.getIdentifier(it, "drawable", ctx.packageName)
    } ?: 0
    return ProductCard(
        imageRes = if (resId != 0) resId else R.drawable.aluminum,
        title = title,
        subtitle = description,
        price = priceText
    )
}

/** Seed data used on first run only */
private fun sampleProducts(): List<Product> = listOf(
    Product(
        id = 0L,
        category = Category.RESIN,
        title = "Transparent High-Temp Resin (eSUN) 500g",
        description = "Crystal clear finish. High-temperature resistance for functional parts.",
        priceText = "LKR 9,500.00",
        imageName = "resin_transparent_ht_500g"
    ),
    Product(
        id = 0L,
        category = Category.RESIN,
        title = "White Water-Washable Resin (eSUN) 500g",
        description = "Easy cleanup with water. Low odor and crisp details.",
        priceText = "LKR 6,500.00",
        imageName = "resin_white_ww_500g"
    ),
    Product(
        id = 0L,
        category = Category.ALUMINUM,
        title = "4040 (20 Series) Aluminum Profile 2m • Type V",
        description = "Premium V-slot profile for frames and automation projects.",
        priceText = "LKR 14,400.00",
        imageName = "aluminum_4040_2m_vslot"
    ),
    Product(
        id = 0L,
        category = Category.ALUMINUM,
        title = "2020 Aluminum Profile 1m (990mm) • Type V",
        description = "Lightweight V-slot profile for compact builds and jigs.",
        priceText = "LKR 2,400.00",
        imageName = "aluminum_2020_1m_vslot"
    ),
    Product(
        id = 0L,
        category = Category.FILAMENTS,
        title = "eSUN PLA-Matte • Milky White 1kg",
        description = "Matte finish to hide layer lines. Easy printing, low warp.",
        priceText = "LKR 4,600.00",
        imageName = "filament_pla_matte_milky_white_1kg"
    ),
    Product(
        id = 0L,
        category = Category.FILAMENTS,
        title = "eSUN PLA+ Refilament • Black 1kg",
        description = "Tough PLA+ from reclaimed material. Great strength and gloss.",
        priceText = "LKR 4,550.00",
        imageName = "filament_pla_plus_refilament_black_1kg"
    ),
    Product(
        id = 0L,
        category = Category.SCREWS,
        title = "M5 Nylon Self-Locking Nut • SS304",
        description = "Locking insert for vibration resistance. Corrosion-proof stainless.",
        priceText = "LKR 12.00",
        imageName = "nut_m5_nylon_lock_304"
    ),
    Product(
        id = 0L,
        category = Category.SCREWS,
        title = "M3 Hex Acorn Cap Nut • SS304",
        description = "Closed dome protects threads and gives a clean finish.",
        priceText = "LKR 8.00",
        imageName = "nut_m3_acorn_cap_304"
    )
)
