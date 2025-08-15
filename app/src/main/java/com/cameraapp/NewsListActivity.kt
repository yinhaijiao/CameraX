package com.example.cameraapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NewsListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<NewsItem>()
    
    // 拍照相关 - 继承自MainActivity的逻辑
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
        setContentView(R.layout.activity_news_list)
        
        // 初始化拍照功能
        cameraHelper = CameraHelper(this)
        
        initViews()
        setupRecyclerView()
        loadNews()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewNews)
        swipeRefresh = findViewById(R.id.swipeRefreshLayout)
        
        swipeRefresh.setOnRefreshListener {
            loadNews()
        }
    }
    
    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(newsList) { newsItem ->
            val intent = Intent(this, NewsDetailActivity::class.java)
            intent.putExtra("news_item", newsItem)
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = newsAdapter
    }
    
    private fun loadNews() {
        swipeRefresh.isRefreshing = true
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newsData = fetchNewsFromApi()
                withContext(Dispatchers.Main) {
                    updateNewsList(newsData)
                    swipeRefresh.isRefreshing = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    swipeRefresh.isRefreshing = false
                    // 如果网络请求失败，使用模拟数据
                    loadMockNews()
                    Toast.makeText(this@NewsListActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun fetchNewsFromApi(): List<NewsItem> {
        // 使用免费的新闻API，这里使用聚合数据的免费新闻API
        val url = "https://v.juhe.cn/toutiao/index?type=top&key=f4542aac89dd0a4d14dce0dbf94a9ba6"
        
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            reader.close()
            
            return parseNewsFromJson(response)
        }
        return emptyList()
    }
    
    private fun parseNewsFromJson(jsonString: String): List<NewsItem> {
        val newsList = mutableListOf<NewsItem>()
        
        val jsonObject = org.json.JSONObject(jsonString)

        // 检查是否有 error_code 且不为 0
        if (jsonObject.getInt("error_code") != 0) {
            return emptyList()
        }

        // 获取 result 对象（JSONObject）
        val resultObject = jsonObject.getJSONObject("result")

        // 获取 data 数组（JSONArray）
        val dataArray = resultObject.getJSONArray("data")

        for (i in 0 until dataArray.length()) {
            val newsObject = dataArray.getJSONObject(i)
            val newsItem = NewsItem(
                id = newsObject.getString("uniquekey"),
                title = newsObject.getString("title"),
                summary = newsObject.optString("category", "综合新闻"),
                content = newsObject.optString("title", ""), // 注意：API 无 content 字段，可用 title 或 url 替代
                imageUrl = newsObject.optString("thumbnail_pic_s", ""),
                publishTime = newsObject.optString("date", ""),
                source = newsObject.optString("author_name", "新闻源")
            )
            newsList.add(newsItem)
        }
        
        return newsList
    }
    
    private fun loadMockNews() {
        val mockNews = listOf(
            NewsItem(
                "1", 
                "科技创新驱动高质量发展", 
                "科技日报", 
                "随着科技创新的不断推进，我国在人工智能、新能源等领域取得了重大突破...",
                "",
                "2024-03-15",
                "科技日报"
            ),
            NewsItem(
                "2", 
                "绿色发展理念深入人心", 
                "环保新闻", 
                "近年来，绿色发展理念在全社会得到广泛认同，环保产业蓬勃发展...",
                "",
                "2024-03-14",
                "环保时报"
            ),
            NewsItem(
                "3", 
                "教育改革持续深化", 
                "教育要闻", 
                "教育部门持续推进教育改革，着力提升教育质量，促进教育公平...",
                "",
                "2024-03-13",
                "教育日报"
            ),
            NewsItem(
                "4", 
                "数字经济发展势头强劲", 
                "经济新闻", 
                "数字经济作为新的经济增长点，在推动经济转型升级方面发挥重要作用...",
                "",
                "2024-03-12",
                "经济日报"
            ),
            NewsItem(
                "5", 
                "文化传承与创新并重", 
                "文化新闻", 
                "在传承优秀传统文化的同时，积极推进文化创新，提升文化软实力...",
                "",
                "2024-03-11",
                "文化报"
            )
        )
        
        updateNewsList(mockNews)
    }
    
    private fun updateNewsList(newsData: List<NewsItem>) {
        newsList.clear()
        newsList.addAll(newsData)
        newsAdapter.notifyDataSetChanged()
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