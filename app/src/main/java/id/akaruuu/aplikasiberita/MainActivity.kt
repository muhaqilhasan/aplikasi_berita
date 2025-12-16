package id.akaruuu.aplikasiberita

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.akaruuu.aplikasiberita.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NewsAdapter
    private var allNews: List<News> = listOf()
    private var savedNewsIds: MutableSet<String> = mutableSetOf()

    // State Filter
    private var activeTab = "beranda"
    private var activeCategory = "Semua"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSavedData()
        setupData()
        setupRecyclerView()
        setupCategories()
        setupSearch()
        setupBottomNav()

        applyFilter()
    }

    private fun loadSavedData() {
        val sharedPref = getSharedPreferences("BookmarkPrefs", Context.MODE_PRIVATE)
        savedNewsIds = sharedPref.getStringSet("saved_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveBookmarkState() {
        val sharedPref = getSharedPreferences("BookmarkPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putStringSet("saved_ids", savedNewsIds)
            apply()
        }
    }

    private fun setupData() {
        val jsonString = assets.open("news_data.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<News>>() {}.type
        allNews = Gson().fromJson(jsonString, listType)
        allNews.forEach { it.isSaved = savedNewsIds.contains(it.id.toString()) }
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            onClick = { news ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("EXTRA_NEWS", news)
                startActivity(intent)
            },
            onSaveClick = { news ->
                if (news.isSaved) {
                    savedNewsIds.remove(news.id.toString())
                    news.isSaved = false
                } else {
                    savedNewsIds.add(news.id.toString())
                    news.isSaved = true
                }
                saveBookmarkState()

                // Refresh item spesifik agar tidak flicker seluruh layar
                // Namun untuk simplicity saat filter aktif, kita panggil applyFilter
                applyFilter()
            }
        )
        binding.rvNews.layoutManager = LinearLayoutManager(this)
        binding.rvNews.adapter = adapter
    }

    private fun setupCategories() {
        val categories = listOf("Semua", "Teknologi", "Olahraga", "Bisnis", "Wisata")

        // Bersihkan dulu jika ada (untuk mencegah duplikasi saat recreate)
        binding.layoutCategories.removeAllViews()

        categories.forEach { cat ->
            val chip = TextView(this)
            chip.text = cat
            chip.setPadding(48, 20, 48, 20) // Padding diperbesar sedikit
            chip.textSize = 14f

            // Set margins
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)
            chip.layoutParams = params

            chip.setOnClickListener {
                activeCategory = cat
                updateCategoryUI()
                applyFilter()
            }
            binding.layoutCategories.addView(chip)
        }
        // Panggil update UI sekali di awal
        updateCategoryUI()
    }

    // PERBAIKAN: Gunakan setBackgroundResource agar shape rounded tidak hilang
    private fun updateCategoryUI() {
        for (i in 0 until binding.layoutCategories.childCount) {
            val view = binding.layoutCategories.getChildAt(i) as TextView
            if (view.text == activeCategory) {
                view.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                view.setBackgroundResource(R.drawable.bg_chip_active) // Pakai Drawable
            } else {
                view.setTextColor(ContextCompat.getColor(this, R.color.black)) // Atau abu tua
                view.setBackgroundResource(R.drawable.bg_chip_inactive) // Pakai Drawable
            }
        }
    }

    private fun setupSearch() {
        binding.btnSearchMode.setOnClickListener {
            binding.tvAppTitle.visibility = View.GONE
            binding.btnSearchMode.visibility = View.GONE
            binding.searchContainer.visibility = View.VISIBLE
            binding.etSearch.requestFocus()
            binding.scrollCategories.visibility = View.GONE
        }

        binding.btnCloseSearch.setOnClickListener {
            binding.etSearch.setText("")
            searchQuery = ""
            binding.searchContainer.visibility = View.GONE
            binding.tvAppTitle.visibility = View.VISIBLE
            binding.btnSearchMode.visibility = View.VISIBLE
            binding.scrollCategories.visibility = View.VISIBLE
            applyFilter()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
                applyFilter()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener {
            activeTab = "beranda"
            updateBottomNavUI()
            binding.scrollCategories.visibility = View.VISIBLE
            applyFilter()
        }

        binding.navSaved.setOnClickListener {
            activeTab = "saved"
            updateBottomNavUI()
            binding.scrollCategories.visibility = View.GONE
            applyFilter()
        }
    }

    private fun updateBottomNavUI() {
        val blue = 0xFF2563EB.toInt()
        val gray = 0xFF9CA3AF.toInt()

        // Ganti warna ikon dan text
        if (activeTab == "beranda") {
            binding.icHome.setColorFilter(blue)
            binding.txtHome.setTextColor(blue)
            binding.icSaved.setColorFilter(gray)
            binding.txtSaved.setTextColor(gray)
            binding.tvAppTitle.text = "BeritaKita"
        } else {
            binding.icHome.setColorFilter(gray)
            binding.txtHome.setTextColor(gray)
            binding.icSaved.setColorFilter(blue)
            binding.txtSaved.setTextColor(blue)
            binding.tvAppTitle.text = "Disimpan"
        }

        // Ganti Icon drawable juga jika mau (misal filled vs outline),
        // tapi color filter sudah cukup untuk indikasi visual.
    }

    private fun applyFilter() {
        val filteredList = allNews.filter { news ->
            val matchesTab = if (activeTab == "saved") news.isSaved else true

            val matchesCategory = if (activeTab == "beranda" && activeCategory != "Semua") {
                news.category == activeCategory
            } else true

            val matchesSearch = if (searchQuery.isNotEmpty()) {
                news.title.contains(searchQuery, ignoreCase = true) || news.summary.contains(searchQuery, ignoreCase = true)
            } else true

            matchesTab && matchesCategory && matchesSearch
        }

        // Logic Headline:
        // Tampilkan headline HANYA jika:
        // 1. Tab Beranda
        // 2. Kategori "Semua"
        // 3. Tidak sedang mencari
        // 4. Ada datanya
        val showHeadline = (activeTab == "beranda" && activeCategory == "Semua" && searchQuery.isEmpty())

        adapter.setData(filteredList, showHeadline)

        binding.tvEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }
}