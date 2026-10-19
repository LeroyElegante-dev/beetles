package com.example.beetles.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.beetles.MainActivity
import com.example.beetles.MainPagerAdapter
import com.example.beetles.R
import com.example.beetles.data.CurrentPlayerStore
import com.example.beetles.data.GameSettingsStore
import com.example.beetles.databinding.FragmentGameBinding
import com.example.beetles.db.BeetlesDatabase
import com.example.beetles.db.ScoreEntity
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsStore: GameSettingsStore
    private lateinit var currentPlayerStore: CurrentPlayerStore
    private var roundTimer: CountDownTimer? = null
    private var score = 0
    private var hits = 0
    private var misses = 0
    private var playing = false

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
        settingsStore = GameSettingsStore(requireContext())
        currentPlayerStore = CurrentPlayerStore(requireContext())
        refreshHud()
        refreshCurrentPlayer()

        binding.gameField.onHit = { points ->
            if (playing) {
                score += points
                hits += 1
                refreshHud()
            }
        }
        binding.gameField.onMiss = {
            if (playing) {
                score = (score - MISS_PENALTY).coerceAtLeast(0)
                misses += 1
                refreshHud()
            }
        }

        binding.btnStart.setOnClickListener { startRound() }
        binding.btnMenu.setOnClickListener { showGameMenu(it) }
    }

    override fun onResume() {
        super.onResume()
        if (::currentPlayerStore.isInitialized) {
            refreshCurrentPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (playing) {
            endRound(showToast = false)
        }
    }

    override fun onDestroyView() {
        roundTimer?.cancel()
        binding.gameField.stop()
        _binding = null
        super.onDestroyView()
    }

    private fun startRound() {
        if (!currentPlayerStore.hasPlayer()) {
            Toast.makeText(requireContext(), R.string.error_no_player, Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.openTab(MainPagerAdapter.TAB_REGISTRATION)
            return
        }
        val settings = settingsStore.load()
        score = 0
        hits = 0
        misses = 0
        playing = true
        refreshHud()
        binding.btnStart.visibility = View.GONE
        binding.gameField.start(settings)

        roundTimer?.cancel()
        roundTimer = object : CountDownTimer(settings.roundDurationSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTime.text = getString(
                    R.string.game_time,
                    (millisUntilFinished / 1000L).toInt()
                )
            }

            override fun onFinish() {
                endRound(showToast = true)
            }
        }.start()
        binding.tvTime.text = getString(R.string.game_time, settings.roundDurationSeconds)
    }

    private fun endRound(showToast: Boolean) {
        playing = false
        roundTimer?.cancel()
        roundTimer = null
        binding.gameField.stop()
        binding.btnStart.visibility = View.VISIBLE
        binding.btnStart.setText(R.string.game_again)
        refreshHud()
        if (showToast && isAdded) {
            saveScore()
            Toast.makeText(
                requireContext(),
                getString(R.string.game_over, score, accuracyPercent()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveScore() {
        val playerId = currentPlayerStore.getPlayerId()
        if (playerId == CurrentPlayerStore.NO_PLAYER) return
        val accuracy = accuracyPercent()
        viewLifecycleOwner.lifecycleScope.launch {
            val db = BeetlesDatabase.get(requireContext())
            val player = db.playerDao().getById(playerId) ?: return@launch
            db.scoreDao().insert(
                ScoreEntity(
                    playerId = player.id,
                    playerName = player.fullName,
                    score = score,
                    difficulty = player.difficulty,
                    hits = hits,
                    misses = misses,
                    accuracy = accuracy,
                    createdAt = System.currentTimeMillis()
                )
            )
            if (isAdded) {
                Toast.makeText(requireContext(), R.string.score_saved, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshCurrentPlayer() {
        viewLifecycleOwner.lifecycleScope.launch {
            val id = currentPlayerStore.getPlayerId()
            val player = if (id == CurrentPlayerStore.NO_PLAYER) {
                null
            } else {
                BeetlesDatabase.get(requireContext()).playerDao().getById(id)
            }
            val binding = _binding ?: return@launch
            binding.tvCurrentPlayer.text = if (player == null) {
                getString(R.string.current_player_none)
            } else {
                getString(R.string.current_player, player.fullName)
            }
        }
    }

    private fun refreshHud() {
        binding.tvScore.text = getString(R.string.game_score, score)
        binding.tvHits.text = getString(R.string.game_hits, hits)
        binding.tvMisses.text = getString(R.string.game_misses, misses)
        binding.tvAccuracy.text = getString(R.string.game_accuracy, accuracyPercent())
        if (!playing) {
            val duration = if (::settingsStore.isInitialized) {
                settingsStore.load().roundDurationSeconds
            } else {
                0
            }
            binding.tvTime.text = getString(R.string.game_time, duration)
        }
    }

    private fun accuracyPercent(): Int {
        val shots = hits + misses
        if (shots == 0) return 0
        return hits * 100 / shots
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
        const val MISS_PENALTY = 5
        const val MENU_PLAYERS = 1
        const val MENU_RECORDS = 2
        const val MENU_RULES = 3
        const val MENU_AUTHORS = 4
        const val MENU_SETTINGS = 5
    }
}
