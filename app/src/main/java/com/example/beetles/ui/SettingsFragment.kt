package com.example.beetles.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.beetles.R
import com.example.beetles.data.GameSettingsStore
import com.example.beetles.databinding.FragmentSettingsBinding
import com.example.beetles.model.GameSettings

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var store: GameSettingsStore
    private lateinit var settings: GameSettings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = GameSettingsStore(requireContext())
        settings = store.load()
        applySettingsToSeekBars(settings)

        bindSeekBar(binding.sbSpeed, binding.tvSpeedValue) {
            getString(R.string.settings_value, progress + 1)
        }
        bindSeekBar(binding.sbMaxBeetles, binding.tvMaxBeetlesValue) {
            getString(R.string.settings_value, progress + 1)
        }
        bindSeekBar(binding.sbBonusInterval, binding.tvBonusIntervalValue) {
            getString(R.string.settings_value_seconds, progress + 5)
        }
        bindSeekBar(binding.sbRoundDuration, binding.tvRoundDurationValue) {
            getString(R.string.settings_value_seconds, progress + 30)
        }
        refreshLabels()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applySettingsToSeekBars(settings: GameSettings) {
        binding.sbSpeed.progress = settings.speed - 1
        binding.sbMaxBeetles.progress = settings.maxBeetles - 1
        binding.sbBonusInterval.progress = settings.bonusIntervalSeconds - 5
        binding.sbRoundDuration.progress = settings.roundDurationSeconds - 30
    }

    private fun currentSettings(): GameSettings = GameSettings(
        speed = binding.sbSpeed.progress + 1,
        maxBeetles = binding.sbMaxBeetles.progress + 1,
        bonusIntervalSeconds = binding.sbBonusInterval.progress + 5,
        roundDurationSeconds = binding.sbRoundDuration.progress + 30
    )

    private fun refreshLabels() {
        binding.tvSpeedValue.text = getString(R.string.settings_value, binding.sbSpeed.progress + 1)
        binding.tvMaxBeetlesValue.text =
            getString(R.string.settings_value, binding.sbMaxBeetles.progress + 1)
        binding.tvBonusIntervalValue.text = getString(
            R.string.settings_value_seconds,
            binding.sbBonusInterval.progress + 5
        )
        binding.tvRoundDurationValue.text = getString(
            R.string.settings_value_seconds,
            binding.sbRoundDuration.progress + 30
        )
    }

    private fun bindSeekBar(
        seekBar: SeekBar,
        valueView: TextView,
        format: SeekBar.() -> CharSequence
    ) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valueView.text = seekBar?.format()
                if (fromUser) {
                    settings = currentSettings()
                    store.save(settings)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }
}
