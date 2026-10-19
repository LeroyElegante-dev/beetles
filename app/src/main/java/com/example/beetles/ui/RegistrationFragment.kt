package com.example.beetles.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.beetles.R
import com.example.beetles.data.CurrentPlayerStore
import com.example.beetles.databinding.FragmentRegistrationBinding
import com.example.beetles.db.BeetlesDatabase
import com.example.beetles.db.PlayerEntity
import com.example.beetles.model.Gender
import com.example.beetles.model.Player
import com.example.beetles.model.ZodiacSign
import kotlinx.coroutines.launch
import java.util.Calendar

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentPlayerStore: CurrentPlayerStore
    private var savedPlayers: List<PlayerEntity> = emptyList()
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentPlayerStore = CurrentPlayerStore(requireContext())

        val today = Calendar.getInstance()
        selectedYear = today.get(Calendar.YEAR)
        selectedMonth = today.get(Calendar.MONTH) + 1
        selectedDay = today.get(Calendar.DAY_OF_MONTH)

        updateDifficultyLabel(binding.sbDifficulty.progress)
        updateZodiacPreview()

        binding.sbDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateDifficultyLabel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedYear = year
            selectedMonth = month + 1
            selectedDay = dayOfMonth
            updateZodiacPreview()
        }

        binding.btnRegister.setOnClickListener { registerPlayer() }
        binding.btnSelectPlayer.setOnClickListener { selectSavedPlayer() }
    }

    override fun onResume() {
        super.onResume()
        loadSavedPlayers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadSavedPlayers() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = BeetlesDatabase.get(requireContext())
            savedPlayers = db.playerDao().getAll()
            val binding = _binding ?: return@launch
            val names = if (savedPlayers.isEmpty()) {
                listOf(getString(R.string.no_saved_players))
            } else {
                savedPlayers.map { it.fullName }
            }
            binding.spSavedPlayers.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
            binding.btnSelectPlayer.isEnabled = savedPlayers.isNotEmpty()
            val current = savedPlayers.find { it.id == currentPlayerStore.getPlayerId() }
            refreshCurrentPlayer(current?.fullName)
            if (current != null) {
                val index = savedPlayers.indexOf(current)
                if (index >= 0) binding.spSavedPlayers.setSelection(index)
            }
        }
    }

    private fun selectSavedPlayer() {
        if (savedPlayers.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_select_player, Toast.LENGTH_SHORT).show()
            return
        }
        val index = binding.spSavedPlayers.selectedItemPosition
        val player = savedPlayers.getOrNull(index) ?: return
        currentPlayerStore.setPlayerId(player.id)
        refreshCurrentPlayer(player.fullName)
        binding.tvSummary.text = formatPlayer(player.toPlayer())
        Toast.makeText(requireContext(), R.string.player_selected, Toast.LENGTH_SHORT).show()
    }

    private fun registerPlayer() {
        val fullName = binding.etFullName.text?.toString().orEmpty().trim()
        if (fullName.isEmpty()) {
            binding.etFullName.error = getString(R.string.error_empty_name)
            return
        }

        val gender = if (binding.rbMale.isChecked) Gender.MALE else Gender.FEMALE
        val course = binding.spCourse.selectedItemPosition + 1
        val difficulty = binding.sbDifficulty.progress + 1
        val zodiacSign = ZodiacSign.from(selectedMonth, selectedDay)

        val player = Player(
            fullName = fullName,
            gender = gender,
            course = course,
            difficulty = difficulty,
            birthYear = selectedYear,
            birthMonth = selectedMonth,
            birthDay = selectedDay,
            zodiacSign = zodiacSign
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val db = BeetlesDatabase.get(requireContext())
            val id = db.playerDao().insert(PlayerEntity.from(player))
            currentPlayerStore.setPlayerId(id)
            val saved = player.copy(id = id)
            val binding = _binding ?: return@launch
            binding.ivZodiac.setImageResource(saved.zodiacSign.drawableRes())
            binding.tvZodiacName.setText(saved.zodiacSign.labelRes())
            binding.tvSummary.text = formatPlayer(saved)
            refreshCurrentPlayer(saved.fullName)
            Toast.makeText(requireContext(), R.string.player_saved, Toast.LENGTH_SHORT).show()
            loadSavedPlayers()
        }
    }

    private fun refreshCurrentPlayer(name: String?) {
        binding.tvCurrentPlayer.text = if (name.isNullOrBlank()) {
            getString(R.string.current_player_none)
        } else {
            getString(R.string.current_player, name)
        }
    }

    private fun updateDifficultyLabel(progress: Int) {
        binding.tvDifficultyValue.text = getString(R.string.difficulty_value, progress + 1)
    }

    private fun updateZodiacPreview() {
        val sign = ZodiacSign.from(selectedMonth, selectedDay)
        binding.ivZodiac.setImageResource(sign.drawableRes())
        binding.tvZodiacName.setText(sign.labelRes())
    }

    private fun formatPlayer(player: Player): String {
        val gender = getString(
            when (player.gender) {
                Gender.MALE -> R.string.gender_male
                Gender.FEMALE -> R.string.gender_female
            }
        )
        val birthDate = "%02d.%02d.%04d".format(
            player.birthDay,
            player.birthMonth,
            player.birthYear
        )
        return getString(
            R.string.player_summary,
            player.fullName,
            gender,
            player.course,
            player.difficulty,
            birthDate,
            getString(player.zodiacSign.labelRes())
        )
    }

    private fun ZodiacSign.labelRes(): Int = when (this) {
        ZodiacSign.ARIES -> R.string.zodiac_aries
        ZodiacSign.TAURUS -> R.string.zodiac_taurus
        ZodiacSign.GEMINI -> R.string.zodiac_gemini
        ZodiacSign.CANCER -> R.string.zodiac_cancer
        ZodiacSign.LEO -> R.string.zodiac_leo
        ZodiacSign.VIRGO -> R.string.zodiac_virgo
        ZodiacSign.LIBRA -> R.string.zodiac_libra
        ZodiacSign.SCORPIO -> R.string.zodiac_scorpio
        ZodiacSign.SAGITTARIUS -> R.string.zodiac_sagittarius
        ZodiacSign.CAPRICORN -> R.string.zodiac_capricorn
        ZodiacSign.AQUARIUS -> R.string.zodiac_aquarius
        ZodiacSign.PISCES -> R.string.zodiac_pisces
    }

    private fun ZodiacSign.drawableRes(): Int = when (this) {
        ZodiacSign.ARIES -> R.drawable.ic_zodiac_aries
        ZodiacSign.TAURUS -> R.drawable.ic_zodiac_taurus
        ZodiacSign.GEMINI -> R.drawable.ic_zodiac_gemini
        ZodiacSign.CANCER -> R.drawable.ic_zodiac_cancer
        ZodiacSign.LEO -> R.drawable.ic_zodiac_leo
        ZodiacSign.VIRGO -> R.drawable.ic_zodiac_virgo
        ZodiacSign.LIBRA -> R.drawable.ic_zodiac_libra
        ZodiacSign.SCORPIO -> R.drawable.ic_zodiac_scorpio
        ZodiacSign.SAGITTARIUS -> R.drawable.ic_zodiac_sagittarius
        ZodiacSign.CAPRICORN -> R.drawable.ic_zodiac_capricorn
        ZodiacSign.AQUARIUS -> R.drawable.ic_zodiac_aquarius
        ZodiacSign.PISCES -> R.drawable.ic_zodiac_pisces
    }
}
