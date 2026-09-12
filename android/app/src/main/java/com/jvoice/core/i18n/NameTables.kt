package com.jvoice.core.i18n

/**
 * Telugu renderings for the proper nouns the demo data is keyed on.
 *
 * Place names and people's names are content - a Telugu reader wants
 * "హైదరాబాద్", not "Hyderabad" - but they are also *keys*: articles are filtered
 * by `location == "Hyderabad"`, reporters are looked up by name, and the exam
 * leaderboard sorts on them. Turning those fields into [LocalizedText] would mean
 * every equality check had to pick a language first, which is how filters quietly
 * start missing rows.
 *
 * So the field stays a plain English [String] and is the single identity, and the
 * translation is looked up here only at the moment it is drawn. Anything not in
 * the table falls through unchanged, which is the right behaviour for a name
 * nobody has transliterated yet.
 */
object Places {

    private val telugu = mapOf(
        // Telangana
        "Hyderabad" to "హైదరాబాద్",
        "Warangal" to "వరంగల్",
        "Karimnagar" to "కరీంనగర్",
        "Nizamabad" to "నిజామాబాద్",
        "Khammam" to "ఖమ్మం",
        "Secunderabad" to "సికింద్రాబాద్",
        "Nalgonda" to "నల్గొండ",
        "Mahbubnagar" to "మహబూబ్‌నగర్",
        "Adilabad" to "ఆదిలాబాద్",
        // Andhra Pradesh
        "Vijayawada" to "విజయవాడ",
        "Visakhapatnam" to "విశాఖపట్నం",
        "Guntur" to "గుంటూరు",
        "Tirupati" to "తిరుపతి",
        "Amaravati" to "అమరావతి",
        "Nellore" to "నెల్లూరు",
        "Kurnool" to "కర్నూలు",
        "Rajahmundry" to "రాజమహేంద్రవరం",
        "Kakinada" to "కాకినాడ",
        "Anantapur" to "అనంతపురం",
        "Kadapa" to "కడప",
        // Elsewhere
        "New Delhi" to "న్యూఢిల్లీ",
        "Bengaluru" to "బెంగళూరు",
        "Chennai" to "చెన్నై",
        "Mumbai" to "ముంబై",
        "Kolkata" to "కోల్‌కతా",
        "Pune" to "పుణె"
    )

    /** The place name in [language]; the English key itself when untranslated. */
    fun render(name: String, language: AppLanguage): String =
        if (language == AppLanguage.TELUGU) telugu[name] ?: name else name

    /** As a bilingual value, for search and for the desk's both-languages views. */
    fun localized(name: String): LocalizedText =
        LocalizedText(en = name, te = telugu[name] ?: name)
}

/**
 * Telugu renderings for the demo people - reporters, editors, students.
 *
 * Same reasoning as [Places]: the English spelling stays the identity, because
 * bylines are matched and stats are grouped on it.
 */
object People {

    private val telugu = mapOf(
        // Reporters
        "Kiran Kumar" to "కిరణ్ కుమార్",
        "Lakshmi Prasanna" to "లక్ష్మి ప్రసన్న",
        "Lakshmi Priya" to "లక్ష్మి ప్రియ",
        "Suresh Babu" to "సురేష్ బాబు",
        "Divya Sree" to "దివ్య శ్రీ",
        "Imran Ali" to "ఇమ్రాన్ అలీ",
        "Anil Varma" to "అనిల్ వర్మ",
        // Editors and admins
        "Sridevi Naidu" to "శ్రీదేవి నాయుడు",
        "Mahesh Chandra" to "మహేష్ చంద్ర",
        "Farhana Begum" to "ఫర్హానా బేగం",
        "Anitha Reddy" to "అనిత రెడ్డి",
        "Naveen Kumar" to "నవీన్ కుమార్",
        "Ravi Teja Sharma" to "రవి తేజ శర్మ",
        // Readers and students
        "Sai Charan" to "సాయి చరణ్",
        "Pooja Rani" to "పూజా రాణి",
        "Vamsi Krishna" to "వంశీ కృష్ణ",
        "Zoya Khan" to "జోయా ఖాన్",
        "Harish Goud" to "హరీష్ గౌడ్",
        "Meghana Rao" to "మేఘన రావు",
        "Sunitha Rao" to "సునీత రావు",
        // Desk byline
        "J Voice Desk" to "J Voice డెస్క్"
    )

    fun render(name: String, language: AppLanguage): String =
        if (language == AppLanguage.TELUGU) telugu[name] ?: name else name

    fun localized(name: String): LocalizedText =
        LocalizedText(en = name, te = telugu[name] ?: name)
}
