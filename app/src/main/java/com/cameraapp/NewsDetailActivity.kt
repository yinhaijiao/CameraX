package com.example.cameraapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class NewsDetailActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var titleTextView: TextView
    private lateinit var sourceTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var newsImageView: ImageView
    
    // 拍照相关
    private lateinit var cameraHelper: CameraHelper
    private var isVolumeKeyPressed = false
    private var isVolumeContinuousShoot = false
    private val continuousShootHandler = Handler(Looper.getMainLooper())
    
    private val startContinuousShootRunnable = Runnable {
        if (isVolumeKeyPressed) {
            isVolumeContinuousShoot = true
            continuousShootHandler.post(continuousShootRunnable)
        }
    }
    
    private val continuousShootRunnable = object : Runnable {
        override fun run() {
            if (isVolumeContinuousShoot && isVolumeKeyPressed) {
                cameraHelper.takePhoto()
                continuousShootHandler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        
        // 初始化拍照功能
        cameraHelper = CameraHelper(this)
        
        initViews()
        setupToolbar()
        loadNewsDetail()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        titleTextView = findViewById(R.id.textViewTitle)
        sourceTextView = findViewById(R.id.textViewSource)
        timeTextView = findViewById(R.id.textViewTime)
        contentTextView = findViewById(R.id.textViewContent)
        newsImageView = findViewById(R.id.imageViewNews)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "新闻详情"
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun loadNewsDetail() {
        val newsItem = intent.getParcelableExtra<NewsItem>("news_item")
        newsItem?.let { news ->
            titleTextView.text = news.title
            sourceTextView.text = news.source
            timeTextView.text = news.publishTime
            
            // 生成详细的新闻内容
            val detailedContent = generateDetailedContent(news)
            contentTextView.text = detailedContent
            
            // 如果有图片URL，这里可以加载图片
            if (news.imageUrl.isNotEmpty()) {
                // 这里可以使用图片加载库如Glide或Picasso
                // 暂时隐藏图片
                newsImageView.visibility = android.view.View.GONE
            } else {
                newsImageView.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun generateDetailedContent(newsItem: NewsItem): String {
        // 根据新闻标题生成相应的详细内容
        return when {
            newsItem.title.contains("科技") -> {
                """
                ${newsItem.title}
                
                　　近日，科技创新在推动经济高质量发展方面发挥着越来越重要的作用。专家表示，随着人工智能、大数据、云计算等新兴技术的快速发展，科技创新正成为引领发展的第一动力。
                
                　　在人工智能领域，我国在算法创新、芯片研发、应用场景等方面都取得了显著进展。多家科技企业推出的AI产品在国际市场上具有较强的竞争力。
                
                　　同时，新能源技术的突破为绿色发展提供了强有力的支撑。太阳能、风能等可再生能源的利用效率不断提升，储能技术也在加速发展。
                
                　　业内人士认为，未来需要继续加大科技创新投入，完善创新生态系统，为经济社会发展提供更强劲的动力。
                """.trimIndent()
            }
            newsItem.title.contains("教育") -> {
                """
                ${newsItem.title}
                
                　　教育是国之大计、党之大计。近年来，教育改革持续深化，教育公平和质量显著提升。
                
                　　在基础教育方面，义务教育均衡发展取得重要进展，城乡教育差距进一步缩小。同时，教育信息化建设加快推进，为提升教育质量提供了新的途径。
                
                　　高等教育方面，"双一流"建设取得积极成效，高校服务国家战略和区域发展的能力不断增强。职业教育改革也在加速推进，产教融合、校企合作模式日益完善。
                
                　　专家指出，教育改革要坚持以人民为中心，着力解决教育发展不平衡不充分的问题，努力办好人民满意的教育。
                """.trimIndent()
            }
            newsItem.title.contains("经济") || newsItem.title.contains("数字") -> {
                """
                ${newsItem.title}
                
                　　数字经济作为新时代经济发展的重要引擎，正在深刻改变着经济发展方式和社会生活方式。
                
                　　数据显示，我国数字经济规模持续扩大，占GDP的比重不断提升。电子商务、移动支付、共享经济等新业态新模式蓬勃发展，为经济增长注入新动能。
                
                　　在数字化转型方面，传统产业积极拥抱数字技术，通过数字化、智能化改造提升生产效率和产品质量。数字技术与实体经济深度融合，催生出更多创新应用。
                
                　　专家认为，要继续推进数字经济健康发展，加强数字基础设施建设，完善数字经济治理体系，为经济社会发展提供新的增长动力。
                """.trimIndent()
            }
            else -> {
                """
                ${newsItem.title}
                
                　　这是一条重要新闻，体现了当前社会发展的新趋势和新特点。相关部门和社会各界对此高度关注。
                
                　　据了解，相关工作正在有序推进，各项措施逐步落实。专业人士表示，这一发展对于促进社会进步具有重要意义。
                
                　　在具体实施过程中，将坚持以人民为中心的发展思想，注重统筹协调，确保各项工作取得实效。
                
                　　下一步，将继续深化相关工作，不断完善工作机制，为实现更高质量发展贡献力量。
                """.trimIndent()
            }
        }
    }
    
    // 音量键拍照功能
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.repeatCount == 0) {
                    isVolumeKeyPressed = true
                    cameraHelper.takePhoto()
                    continuousShootHandler.postDelayed(startContinuousShootRunnable, 100)
                }
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                isVolumeKeyPressed = false
                isVolumeContinuousShoot = false
                continuousShootHandler.removeCallbacks(startContinuousShootRunnable)
                continuousShootHandler.removeCallbacks(continuousShootRunnable)
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraHelper.cleanup()
        isVolumeKeyPressed = false
        isVolumeContinuousShoot = false
        continuousShootHandler.removeCallbacks(startContinuousShootRunnable)
        continuousShootHandler.removeCallbacks(continuousShootRunnable)
    }
}