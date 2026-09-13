package com.example.beetles.model

enum class ZodiacSign {
    ARIES,
    TAURUS,
    GEMINI,
    CANCER,
    LEO,
    VIRGO,
    LIBRA,
    SCORPIO,
    SAGITTARIUS,
    CAPRICORN,
    AQUARIUS,
    PISCES;

    companion object {
        fun from(month: Int, day: Int): ZodiacSign {
            require(month in 1..12) { "month must be 1..12" }
            require(day in 1..31) { "day must be 1..31" }
            return when (month) {
                1 -> if (day <= 19) CAPRICORN else AQUARIUS
                2 -> if (day <= 18) AQUARIUS else PISCES
                3 -> if (day <= 20) PISCES else ARIES
                4 -> if (day <= 19) ARIES else TAURUS
                5 -> if (day <= 20) TAURUS else GEMINI
                6 -> if (day <= 20) GEMINI else CANCER
                7 -> if (day <= 22) CANCER else LEO
                8 -> if (day <= 22) LEO else VIRGO
                9 -> if (day <= 22) VIRGO else LIBRA
                10 -> if (day <= 22) LIBRA else SCORPIO
                11 -> if (day <= 21) SCORPIO else SAGITTARIUS
                12 -> if (day <= 21) SAGITTARIUS else CAPRICORN
                else -> error("Invalid month")
            }
        }
    }
}
