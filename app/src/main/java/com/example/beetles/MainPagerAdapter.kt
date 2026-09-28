package com.example.beetles

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.beetles.ui.AuthorsFragment
import com.example.beetles.ui.RegistrationFragment
import com.example.beetles.ui.RulesFragment
import com.example.beetles.ui.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = TAB_TITLES.size

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> RegistrationFragment()
        1 -> RulesFragment()
        2 -> AuthorsFragment()
        3 -> SettingsFragment()
        else -> error("Unknown tab: $position")
    }

    companion object {
        val TAB_TITLES = intArrayOf(
            R.string.tab_registration,
            R.string.tab_rules,
            R.string.tab_authors,
            R.string.tab_settings
        )
    }
}
