package com.example.engine

import com.example.data.NushuGlyph

/**
 * Historical Nüshu (女书) Script Dictionary & Glyph Catalog.
 * Nüshu is a syllabic script developed and used exclusively by peasant women in
 * Jiangyong County, Hunan Province, China. Characters are diamond-shaped, elongated,
 * and traditionally inscribed vertically on bamboo slips, folded paper fans, and cloth.
 */
object NushuDictionary {

    val GLYPHS = listOf(
        NushuGlyph(
            id = "ns_001",
            unicodeChar = "𛰀",
            nameZh = "女",
            pinyin = "nü",
            englishMeaning = "Woman / Female",
            historicalContext = "Core symbolic glyph representing sisterhood and female identity in Jiangyong culture.",
            strokeCount = 5,
            category = "Kinship & Identity"
        ),
        NushuGlyph(
            id = "ns_002",
            unicodeChar = "𛰁",
            nameZh = "姊",
            pinyin = "zi",
            englishMeaning = "Elder Sister",
            historicalContext = "Used in Sandaoshu (Third Day Letters) between sworn sisters (jiebai jiemei).",
            strokeCount = 7,
            category = "Kinship & Identity"
        ),
        NushuGlyph(
            id = "ns_003",
            unicodeChar = "𛰂",
            nameZh = "妹",
            pinyin = "mei",
            englishMeaning = "Younger Sister",
            historicalContext = "Denotes sworn younger sisterhood in traditional female friendship networks.",
            strokeCount = 8,
            category = "Kinship & Identity"
        ),
        NushuGlyph(
            id = "ns_004",
            unicodeChar = "𛰃",
            nameZh = "结",
            pinyin = "jie",
            englishMeaning = "Bond / To Swear",
            historicalContext = "Refers to the sacred bonding pact between sworn sisters who vow lifelong companionship.",
            strokeCount = 9,
            category = "Kinship & Identity"
        ),
        NushuGlyph(
            id = "ns_005",
            unicodeChar = "𛰄",
            nameZh = "书",
            pinyin = "shu",
            englishMeaning = "Script / Letter",
            historicalContext = "Signifies the written Nüshu characters and epistolary communication.",
            strokeCount = 4,
            category = "Literature & Art"
        ),
        NushuGlyph(
            id = "ns_006",
            unicodeChar = "𛰅",
            nameZh = "花",
            pinyin = "hua",
            englishMeaning = "Flower / Blossom",
            historicalContext = "Metaphor for youth, beauty, embroidery, and spring songs.",
            strokeCount = 7,
            category = "Nature & Emotion"
        ),
        NushuGlyph(
            id = "ns_007",
            unicodeChar = "𛰆",
            nameZh = "歌",
            pinyin = "ge",
            englishMeaning = "Song / Ballad",
            historicalContext = "Nüshu characters were almost always chanted or sung in rhymed heptasyllabic verse.",
            strokeCount = 10,
            category = "Literature & Art"
        ),
        NushuGlyph(
            id = "ns_008",
            unicodeChar = "𛰇",
            nameZh = "天",
            pinyin = "tian",
            englishMeaning = "Heaven / Sky",
            historicalContext = "Cosmological reference frequently opening wedding laments and seasonal chronicles.",
            strokeCount = 4,
            category = "Cosmology"
        ),
        NushuGlyph(
            id = "ns_009",
            unicodeChar = "𛰈",
            nameZh = "地",
            pinyin = "di",
            englishMeaning = "Earth / Ground",
            historicalContext = "Paired with Heaven; represents mortal existence and farming landscapes.",
            strokeCount = 6,
            category = "Cosmology"
        ),
        NushuGlyph(
            id = "ns_010",
            unicodeChar = "𛰉",
            nameZh = "月",
            pinyin = "yue",
            englishMeaning = "Moon / Month",
            historicalContext = "Symbol of feminine endurance, nocturnal weaving, and calendar cycles.",
            strokeCount = 4,
            category = "Cosmology"
        ),
        NushuGlyph(
            id = "ns_011",
            unicodeChar = "𛰊",
            nameZh = "日",
            pinyin = "ri",
            englishMeaning = "Sun / Day",
            historicalContext = "Denotes dawn, daylight labor, and auspicious solar dates.",
            strokeCount = 4,
            category = "Cosmology"
        ),
        NushuGlyph(
            id = "ns_012",
            unicodeChar = "𛰋",
            nameZh = "心",
            pinyin = "xin",
            englishMeaning = "Heart / Soul",
            historicalContext = "Expresses inner sorrow (suku), yearning, and heartfelt vows of empathy.",
            strokeCount = 4,
            category = "Nature & Emotion"
        ),
        NushuGlyph(
            id = "ns_013",
            unicodeChar = "𛰌",
            nameZh = "情",
            pinyin = "qing",
            englishMeaning = "Affection / Feeling",
            historicalContext = "Deep emotional bond between companions sharing hardship and embroidery craft.",
            strokeCount = 11,
            category = "Nature & Emotion"
        ),
        NushuGlyph(
            id = "ns_014",
            unicodeChar = "𛰍",
            nameZh = "福",
            pinyin = "fu",
            englishMeaning = "Blessing / Good Fortune",
            historicalContext = "Woven into marriage quilts and bamboo scrolls to bestow protection.",
            strokeCount = 13,
            category = "Auspicious Vows"
        ),
        NushuGlyph(
            id = "ns_015",
            unicodeChar = "𛰎",
            nameZh = "爱",
            pinyin = "ai",
            englishMeaning = "Love / Devotion",
            historicalContext = "Sacred mutual care and maternal devotion recorded in oral-script ballads.",
            strokeCount = 10,
            category = "Nature & Emotion"
        ),
        NushuGlyph(
            id = "ns_016",
            unicodeChar = "𛰏",
            nameZh = "家",
            pinyin = "jia",
            englishMeaning = "Home / Family",
            historicalContext = "Represents ancestral homes and the poignant transition of bridal departures.",
            strokeCount = 10,
            category = "Kinship & Identity"
        ),
        NushuGlyph(
            id = "ns_017",
            unicodeChar = "𛰐",
            nameZh = "梦",
            pinyin = "meng",
            englishMeaning = "Dream / Vision",
            historicalContext = "Poetic yearning for reunion across distant mountainous villages.",
            strokeCount = 11,
            category = "Nature & Emotion"
        ),
        NushuGlyph(
            id = "ns_018",
            unicodeChar = "𛰑",
            nameZh = "竹",
            pinyin = "zhu",
            englishMeaning = "Bamboo",
            historicalContext = "Bamboo slats (简牍) were dried and carved with calligraphy pens for lasting historical records.",
            strokeCount = 6,
            category = "Material Culture"
        ),
        NushuGlyph(
            id = "ns_019",
            unicodeChar = "𛰒",
            nameZh = "简",
            pinyin = "jian",
            englishMeaning = "Bamboo Slip / Letter",
            historicalContext = "Individual vertical slat bound with flaxen or silk cords.",
            strokeCount = 13,
            category = "Material Culture"
        ),
        NushuGlyph(
            id = "ns_020",
            unicodeChar = "𛰓",
            nameZh = "音",
            pinyin = "yin",
            englishMeaning = "Voice / Melodic Sound",
            historicalContext = "The phonetic basis of Nüshu, matching the Chengguan local dialect of Jiangyong.",
            strokeCount = 9,
            category = "Literature & Art"
        )
    )

    private val glyphMap = GLYPHS.associateBy { it.id }

    fun getById(id: String): NushuGlyph {
        return glyphMap[id] ?: GLYPHS.first()
    }

    fun getRandomGlyph(seed: Int): NushuGlyph {
        val index = (seed % GLYPHS.size).coerceAtLeast(0)
        return GLYPHS[index]
    }
}
