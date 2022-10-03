package com.hariom.sampleapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.hariom.sampleapp.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: VideoAdapter
    private val videos = ArrayList<String>()
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private val link = "https://pixabay.com/api/videos/?key=30318542-3643438283fd2b73570f711a7&q=yellow+flowers&pretty=true"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch video url from pixabay.com
        fetchData()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = VideoAdapter(this, videos, object : VideoAdapter.OnVideoPreparedListener {
            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                exoPlayerItems.add(exoPlayerItem)
            }
        })

        binding.viewPager2.adapter = adapter

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                if (previousIndex != -1) {
                    val player = exoPlayerItems[previousIndex].exoPlayer
                    player.pause()
                    player.playWhenReady = false
                }
                val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                if (newIndex != -1) {
                    val player = exoPlayerItems[newIndex].exoPlayer
                    player.playWhenReady = true
                    player.play()
                }
            }
        })
    }


    private fun fetchData(){
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest( Request.Method.GET, link, { response ->

                val data =  response.toString()
                val jObject = JSONObject(data)
                val jArray: JSONArray = jObject.getJSONArray("hits")
                for(i in 0 until jArray.length()){
                    val jObject2: JSONObject = jArray.getJSONObject(i)
                    val jObject3: JSONObject = jObject2.getJSONObject("videos")
                    val jObject4: JSONObject = jObject3.getJSONObject("tiny")
                    val url = jObject4.getString("url")

                    videos.add(url)
                }
            },
            {
                Toast.makeText(this.applicationContext, "some error occured", Toast.LENGTH_SHORT).show()
            })
        queue.add(request)
    }


    override fun onPause() {
        super.onPause()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }
}