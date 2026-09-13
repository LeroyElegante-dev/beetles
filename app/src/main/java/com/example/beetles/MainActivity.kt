package com.example.beetles

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.beetles.databinding.ActivityMainBinding
import com.example.beetles.model.Gender
import com.example.beetles.model.Player
import com.example.beetles.model.ZodiacSign
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }

    private fun updateDifficultyLabel(progress: Int) {
        binding.tvDifficultyValue.text = getString(R.string.difficulty_value, progress + 1)
    }

    private fun updateZodiacPreview() {
        val sign = ZodiacSign.from(selectedMonth, selectedDay)
        binding.ivZodiac.setImageResource(sign.drawableRes())
        binding.tvZodiacName.setText(sign.labelRes())
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

        binding.ivZodiac.setImageResource(player.zodiacSign.drawableRes())
        binding.tvZodiacName.setText(player.zodiacSign.labelRes())
        binding.tvSummary.text = formatPlayer(player)
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
