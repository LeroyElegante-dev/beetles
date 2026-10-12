package com.example.beetles

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.beetles.ui.AuthorsFragment
import com.example.beetles.ui.GameFragment
import com.example.beetles.ui.RegistrationFragment
import com.example.beetles.ui.RulesFragment
import com.example.beetles.ui.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = TAB_TITLES.size

    override fun createFragment(position: Int): Fragment = when (position) {
        TAB_REGISTRATION -> RegistrationFragment()
        TAB_GAME -> GameFragment()
        TAB_RULES -> RulesFragment()
        TAB_AUTHORS -> AuthorsFragment()
        TAB_SETTINGS -> SettingsFragment()
        else -> error("Unknown tab: $position")
    }

    companion object {
        const val TAB_REGISTRATION = 0
        const val TAB_GAME = 1
        const val TAB_RULES = 2
        const val TAB_AUTHORS = 3
        const val TAB_SETTINGS = 4

        val TAB_TITLES = intArrayOf(
            R.string.tab_registration,
            R.string.tab_game,
            R.string.tab_rules,
            R.string.tab_authors,
            R.string.tab_settings
        )
    }
}
