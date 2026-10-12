package com.example.beetles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.beetles.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = MainPagerAdapter.TAB_TITLES.size - 1

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setText(MainPagerAdapter.TAB_TITLES[position])
        }.attach()
    }

    fun openTab(position: Int) {
        binding.viewPager.currentItem = position
    }
}
