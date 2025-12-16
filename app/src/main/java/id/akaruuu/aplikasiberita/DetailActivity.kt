package id.akaruuu.aplikasiberita

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import id.akaruuu.aplikasiberita.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        val news = intent.getParcelableExtra<News>("EXTRA_NEWS")

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (news != null) {
            binding.tvDetailCategory.text = news.category
            binding.tvDetailTitle.text = news.title
            binding.tvDetailAuthor.text = "${news.author} • ${news.date} ${news.time}"
            binding.tvDetailSummary.text = news.summary
            binding.tvDetailContent.text = news.content

            Glide.with(this)
                .load(news.image)
                .centerCrop()
                .into(binding.imgDetail)
        }
    }
}