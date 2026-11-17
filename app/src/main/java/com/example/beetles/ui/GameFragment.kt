package com.example.beetles.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.beetles.MainActivity
import com.example.beetles.MainPagerAdapter
import com.example.beetles.R
import com.example.beetles.databinding.FragmentGameBinding
import com.example.beetles.widget.GoldRateWidgetProvider
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gameField.onHit = { points -> viewModel.onHit(points) }
        binding.gameField.onMiss = { viewModel.onMiss() }
        binding.gameField.onBonusCollected = { viewModel.onBonusCollected() }

        binding.btnStart.setOnClickListener {
            viewModel.startRound()
            val state = viewModel.state.value
            if (state.playing) {
                binding.gameField.start(state.settings, state.goldPoints)
            }
        }
        binding.btnMenu.setOnClickListener { showGameMenu(it) }

        observeState()
        observeEvents()
        viewModel.refreshGoldRate()
        restoreFieldIfPlaying()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCurrentPlayer()
        restoreFieldIfPlaying()
        if (isAdded) {
            GoldRateWidgetProvider.requestUpdate(requireContext())
        }
    }

    override fun onPause() {
        // Stop drawing only. Score/timer live in ViewModel and survive rotation.
        _binding?.gameField?.stop()
        super.onPause()
    }

    override fun onDestroyView() {
        _binding?.gameField?.stop()
        _binding = null
        super.onDestroyView()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    val binding = _binding ?: return@collect
                    binding.tvScore.text = getString(R.string.game_score, state.score)
                    binding.tvHits.text = getString(R.string.game_hits, state.hits)
                    binding.tvMisses.text = getString(R.string.game_misses, state.misses)
                    binding.tvAccuracy.text = getString(R.string.game_accuracy, state.accuracyPercent)
                    binding.tvTime.text = getString(R.string.game_time, state.secondsLeft)
                    binding.tvCurrentPlayer.text = if (state.currentPlayerName.isNullOrBlank()) {
                        getString(R.string.current_player_none)
                    } else {
                        getString(R.string.current_player, state.currentPlayerName)
                    }
                    if (state.playing) {
                        binding.btnStart.visibility = View.GONE
                    } else {
                        binding.btnStart.visibility = View.VISIBLE
                        binding.btnStart.setText(
                            if (state.score > 0 || state.hits > 0 || state.misses > 0) {
                                R.string.game_again
                            } else {
                                R.string.game_start
                            }
                        )
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        GameEvent.NeedPlayer -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.error_no_player,
                                Toast.LENGTH_SHORT
                            ).show()
                            (activity as? MainActivity)?.openTab(MainPagerAdapter.TAB_REGISTRATION)
                        }
                        GameEvent.BonusActivated -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.bonus_activated,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is GameEvent.RoundFinished -> {
                            _binding?.gameField?.stop()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.game_over, event.score, event.accuracy),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        GameEvent.ScoreSaved -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.score_saved,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun restoreFieldIfPlaying() {
        val state = viewModel.state.value
        val binding = _binding ?: return
        if (state.playing) {
            binding.gameField.start(state.settings, state.goldPoints)
            binding.btnStart.visibility = View.GONE
        }
    }

    private fun showGameMenu(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menu.add(0, MENU_PLAYERS, 0, R.string.tab_registration)
            menu.add(0, MENU_RECORDS, 1, R.string.tab_records)
            menu.add(0, MENU_RULES, 2, R.string.tab_rules)
            menu.add(0, MENU_AUTHORS, 3, R.string.tab_authors)
            menu.add(0, MENU_SETTINGS, 4, R.string.tab_settings)
            setOnMenuItemClickListener { item ->
                val position = when (item.itemId) {
                    MENU_PLAYERS -> MainPagerAdapter.TAB_REGISTRATION
                    MENU_RECORDS -> MainPagerAdapter.TAB_RECORDS
                    MENU_RULES -> MainPagerAdapter.TAB_RULES
                    MENU_AUTHORS -> MainPagerAdapter.TAB_AUTHORS
                    MENU_SETTINGS -> MainPagerAdapter.TAB_SETTINGS
                    else -> return@setOnMenuItemClickListener false
                }
                (activity as? MainActivity)?.openTab(position)
                true
            }
            show()
        }
    }

    private companion object {
        const val MENU_PLAYERS = 1
        const val MENU_RECORDS = 2
        const val MENU_RULES = 3
        const val MENU_AUTHORS = 4
        const val MENU_SETTINGS = 5
    }
}
