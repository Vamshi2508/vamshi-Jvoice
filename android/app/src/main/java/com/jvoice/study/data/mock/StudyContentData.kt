package com.jvoice.study.data.mock

import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.lt
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.QuestionSource
import com.jvoice.study.data.model.QuestionType
import com.jvoice.study.data.model.Quiz
import com.jvoice.study.data.model.StudyArticle

/**
 * ---------------------------------------------------------------------------
 *  DUMMY / DEMO STUDY CONTENT
 *  Articles and questions written as realistic competitive-exam preparation
 *  material for the J Voice Module 2 prototype. Local data only.
 * ---------------------------------------------------------------------------
 */
internal object StudyContentData {

    private val NOW = System.currentTimeMillis()
    private const val DAY = 24 * 60 * 60 * 1000L

    // ================================================================ articles

/**
     * Builds a study page from its English copy plus the matching Telugu.
     *
     * The Telugu parameters default to blank so a page can land in one language
     * and be translated later - which is how the desk actually works. A blank
     * side is not a broken page: [LocalizedText.get] falls back, so the student
     * reads the English while the desk's translation queue shows the debt.
     */
    private fun art(
        id: String,
        subjectId: String,
        topicId: String,
        title: String,
        description: String,
        minutes: Int,
        content: String,
        points: List<String>,
        examples: List<String> = emptyList(),
        formulas: List<String> = emptyList(),
        ageDays: Int = 3,
        status: ContentStatus = ContentStatus.PUBLISHED,
        author: String = "Sunitha Rao",
        titleTe: String = "",
        descriptionTe: String = "",
        contentTe: String = "",
        pointsTe: List<String> = emptyList(),
        examplesTe: List<String> = emptyList(),
        formulasTe: List<String> = emptyList()
    ) = StudyArticle(
        id = id,
        subjectId = subjectId,
        topicId = topicId,
        title = LocalizedText(en = title, te = titleTe),
        description = LocalizedText(en = description, te = descriptionTe),
        content = LocalizedText(en = content.trimIndent(), te = contentTe.trimIndent()),
        importantPoints = pair(points, pointsTe),
        examples = pair(examples, examplesTe),
        formulas = pair(formulas, formulasTe),
        readingMinutes = minutes,
        status = status,
        authorName = author,
        createdAt = NOW - ageDays * DAY
    )

    /**
     * Zips two same-language lists into bilingual entries, by position.
     *
     * Position is the contract: the third Telugu bullet is the translation of the
     * third English one. A shorter Telugu list leaves the tail untranslated
     * rather than misaligning the pairs, which is the safer failure.
     */
    private fun pair(en: List<String>, te: List<String>): List<LocalizedText> =
        en.mapIndexed { index, value ->
            LocalizedText(en = value, te = te.getOrElse(index) { "" })
        }

    val articles: List<StudyArticle> = listOf(

        // ------------------------------------------- second pieces per topic
        // A topic carries several articles, not one — a concept piece, then a
        // shortcuts or revision piece the student reads after it.
        art(
            "a_num_system_2", "sub_math", "t_num_system",
            "Number System — shortcuts for the exam hall",
            "Divisibility tricks, unit-digit patterns and remainder shortcuts worth memorising.", 5,
            """
            Most number-system questions in a preliminary paper are solved in under thirty seconds if you
            know the standard shortcuts. This piece collects the ones that come up most often.

            Unit digit of a power repeats in a cycle of at most four, so 7^103 has the same unit digit as
            7^3, which is 3. Divisibility by 11 is the alternating sum of digits. A number is divisible by
            8 when its last three digits are.
            """,
            listOf(
                "Unit digits repeat in cycles of 1, 2 or 4",
                "Divisible by 11: alternating digit sum is 0 or a multiple of 11",
                "Divisible by 8: check the last three digits only"
            ),
            author = "Divya Sree"
        ),
        art(
            "a_percentage_2", "sub_math", "t_percentage",
            "Percentages — the fraction table",
            "Learn the common percentage-to-fraction conversions and most sums become one line.", 4,
            """
            Converting a percentage to its fraction removes almost all the arithmetic. 12.5% is 1/8, 16.67%
            is 1/6, 33.33% is 1/3. Once the table is memorised, a question like "find 37.5% of 512" becomes
            3/8 of 512, which is 192, done mentally.
            """,
            listOf(
                "12.5% = 1/8, 16.67% = 1/6, 20% = 1/5",
                "37.5% = 3/8, 62.5% = 5/8",
                "Successive change: net = a + b + ab/100"
            ),
            author = "Divya Sree"
        ),
        art(
            "a_frights_2", "sub_polity", "t_frights",
            "Fundamental Rights — case law that gets asked",
            "The handful of judgments examiners return to, in one page.", 6,
            """
            Kesavananda Bharati (1973) established the basic structure doctrine. Maneka Gandhi (1978) read
            "procedure established by law" as requiring a fair, just and reasonable procedure. Puttaswamy
            (2017) held privacy to be a fundamental right under Article 21.
            """,
            listOf(
                "Kesavananda Bharati — basic structure doctrine",
                "Maneka Gandhi — due process read into Article 21",
                "Puttaswamy — privacy is a fundamental right"
            ),
            author = "Divya Sree"
        ),
        art(
            "a_freedom_2", "sub_history", "t_freedom",
            "Freedom movement — dates worth memorising",
            "A compact timeline from 1885 to 1947 for last-minute revision.", 5,
            """
            1885 Indian National Congress founded. 1905 Partition of Bengal. 1919 Jallianwala Bagh and the
            Rowlatt Act. 1930 Dandi March. 1942 Quit India. 1947 Independence and Partition.
            """,
            listOf(
                "1905 — Partition of Bengal, Swadeshi begins",
                "1930 — Dandi March, Civil Disobedience",
                "1942 — Quit India, 'Do or Die'"
            ),
            author = "Divya Sree"
        ),
        art(
            "a_rivers_2", "sub_geography", "t_rivers",
            "Rivers — tributaries and the towns on them",
            "Which tributary joins which river, and the city that sits on each bank.", 5,
            """
            The Godavari rises at Trimbakeshwar and is joined by the Pravara, Manjira and Indravati. The
            Krishna rises at Mahabaleshwar with the Bhima and Tungabhadra as its major tributaries.
            Vijayawada sits on the Krishna, Rajahmundry on the Godavari.
            """,
            listOf(
                "Godavari — Pravara, Manjira, Indravati",
                "Krishna — Bhima, Tungabhadra, Musi",
                "Rajahmundry on Godavari, Vijayawada on Krishna"
            ),
            author = "Divya Sree"
        ),

        // ---------------------------------------------------------- mathematics
        art(
            "a_num_system", "sub_math", "t_num_system",
            "Number System", "Classification of numbers, divisibility rules and remainder basics.", 6,
            """
            The number system is the foundation of the quantitative aptitude section. Numbers are classified as natural numbers, whole numbers, integers, rational and irrational numbers. Almost every arithmetic chapter builds on these definitions, so clarity here saves time later.

            In exams the most productive areas are divisibility rules, unit-digit questions, and finding remainders. A number is divisible by 3 when its digit sum is divisible by 3, and by 9 when the digit sum is divisible by 9. For 11, take the difference between the sum of digits in odd and even positions.

            Prime numbers deserve special attention: 2 is the only even prime, and there are 25 primes below 100. Learning them by heart removes a whole category of mistakes.
            """,
            listOf(
                "Natural numbers start at 1; whole numbers include 0.",
                "Divisible by 4 if the last two digits form a multiple of 4.",
                "Divisible by 8 if the last three digits form a multiple of 8.",
                "There are 25 prime numbers below 100.",
                "LCM x HCF = product of the two numbers."
            ),
            listOf(
                "Is 3,168 divisible by 9? Digit sum = 3+1+6+8 = 18, which is divisible by 9, so yes.",
                "HCF of 24 and 36 is 12, LCM is 72. Check: 12 x 72 = 864 = 24 x 36."
            ),
            listOf("LCM(a,b) x HCF(a,b) = a x b", "Number of factors: if N = p^a x q^b then factors = (a+1)(b+1)")
        ),
        art(
            "a_percentage", "sub_math", "t_percentage",
            "Percentage", "The single most useful chapter - it feeds profit & loss, interest and data interpretation.", 5,
            """
            A percentage is simply a fraction with denominator 100. Converting between fractions and percentages quickly is the real exam skill: 1/8 = 12.5%, 1/6 = 16.67%, 1/3 = 33.33%, 3/8 = 37.5%. Memorising this table turns two-minute calculations into ten-second ones.

            Two ideas cover most questions. First, percentage change = (change / original) x 100, always on the original value. Second, successive percentage changes do not add up: a 10% increase followed by a 10% decrease leaves you at 99%, not 100%.

            Watch the direction of comparison. "A is 25% more than B" is not the same as "B is 25% less than A" - if A is 25% more than B, then B is 20% less than A.
            """,
            listOf(
                "x% of y = (x/100) x y.",
                "Percentage change is always calculated on the original value.",
                "Successive changes of +a% and +b% give a net a + b + ab/100 percent.",
                "If A is x% more than B, then B is 100x/(100+x) percent less than A.",
                "Learn fraction equivalents up to 1/12 by heart."
            ),
            listOf(
                "What is 20% of 250? (20/100) x 250 = 50.",
                "A price rises 20% then falls 20%. Net change = 20 - 20 - 400/100 = -4%, so the price is 96% of the original.",
                "A number increased by 20% becomes 96. Original = 96/1.2 = 80."
            ),
            listOf(
                "Percentage change = (New - Old) / Old x 100",
                "Net successive change = a + b + (ab/100)"
            )
        ),
        art(
            "a_profit_loss", "sub_math", "t_profit_loss",
            "Profit & Loss", "Cost price, selling price, marked price and discount chains.", 6,
            """
            Profit and loss is percentage applied to trade. Everything is measured on the cost price unless the question says otherwise, and this single habit prevents most errors. Profit = SP - CP, and profit percent = profit / CP x 100.

            Discount, by contrast, is always calculated on the marked price. A shopkeeper who marks goods 40% above cost and then offers a 20% discount still earns a profit: SP = 1.40 x 0.80 x CP = 1.12 CP, which is a 12% gain.

            The classic trap is the false-weight question. A trader who sells at cost price but uses a 900 g weight for a kilogram earns 100/9 = 11.11% profit, because the effective cost is lower than claimed.
            """,
            listOf(
                "Profit % and loss % are calculated on cost price.",
                "Discount % is calculated on marked price.",
                "Selling at the same percentage above and below cost always gives a net loss.",
                "If two articles sell at the same price with +x% and -x%, net loss = x^2/100 percent."
            ),
            listOf(
                "CP = 400, SP = 500. Profit = 100, profit % = 100/400 x 100 = 25%.",
                "Marked price 1,000 with 20% discount gives SP = 800."
            ),
            listOf(
                "Profit % = (SP - CP) / CP x 100",
                "SP = CP x (100 + profit%) / 100",
                "Net loss on equal +x% / -x% deals = x^2 / 100 percent"
            )
        ),
        art(
            "a_time_work", "sub_math", "t_time_work",
            "Time & Work", "Work rates, combined work and the efficiency method.", 6,
            """
            Treat work as a quantity and speed as work per day. If A finishes a job in 10 days, A's one-day work is 1/10. Adding rates is what makes combined-work questions easy: A and B together do 1/10 + 1/15 = 1/6 of the job daily, so they take 6 days.

            The LCM method is faster than fractions. For 10 and 15 days, assume the total work is 30 units; then A does 3 units a day and B does 2, giving 5 units a day together and 30/5 = 6 days.

            Efficiency questions follow the same logic in reverse: if A is twice as efficient as B, A takes half the time B does.
            """,
            listOf(
                "One day's work = 1 / (days taken).",
                "Rates add when people work together; they subtract when one empties a tank.",
                "Use LCM of the given days as total work to avoid fractions.",
                "Efficiency and time taken are inversely proportional."
            ),
            listOf(
                "A takes 10 days, B takes 15 days. Together = 1/10 + 1/15 = 1/6, so 6 days.",
                "A pipe fills a tank in 6 hours, a leak empties it in 12. Net = 1/6 - 1/12 = 1/12, so 12 hours."
            ),
            listOf("Together time = (a x b) / (a + b)", "Work = Rate x Time")
        ),
        art(
            "a_ratio", "sub_math", "t_ratio",
            "Ratio & Proportion", "Comparing quantities, dividing amounts and using proportion.", 5,
            """
            A ratio compares two quantities of the same kind, written a : b. Multiplying or dividing both terms by the same non-zero number does not change a ratio, which is why 6 : 10 and 3 : 5 are identical.

            To divide an amount in a given ratio, add the ratio terms and give each share its fraction. Dividing 64 in the ratio 3 : 5 means eight parts in total, so the shares are 24 and 40.

            In a proportion a : b = c : d the product of the extremes equals the product of the means, so ad = bc. This cross-multiplication is the workhorse of most proportion questions.
            """,
            listOf(
                "A ratio has no units; a proportion is an equality of two ratios.",
                "Divide amount N in ratio a : b as Na/(a+b) and Nb/(a+b).",
                "In a : b = c : d, ad = bc.",
                "Compounded ratio of a:b and c:d is ac : bd."
            ),
            listOf(
                "Divide 64 in the ratio 3 : 5. Parts = 8, so shares are 24 and 40.",
                "If 3 : 5 = x : 20, then x = 12."
            ),
            listOf("If a : b = c : d then ad = bc")
        ),
        art(
            "a_average", "sub_math", "t_average",
            "Average", "Arithmetic mean, weighted average and the deviation shortcut.", 4,
            """
            The average of a set is its total divided by the number of items. Almost every exam question is really about the total: if the average of 5 numbers is 20, the total is 100, and that total is what you manipulate.

            When one value is replaced, the change in average multiplied by the count gives the change in total. If the average of 10 students' marks rises by 2 after correcting one score, that score was understated by 20.

            For consecutive numbers the average is simply the middle term, so the average of the first n natural numbers is (n + 1) / 2.
            """,
            listOf(
                "Average = sum of observations / number of observations.",
                "Sum = average x count. Convert to totals first.",
                "Average of first n natural numbers = (n+1)/2.",
                "Weighted average = (n1a1 + n2a2) / (n1 + n2)."
            ),
            listOf(
                "Average of the first 10 natural numbers = 11/2 = 5.5.",
                "Average of 5 numbers is 20; adding a sixth number 26 gives a new average of 21."
            ),
            listOf("Average = Sum / Count", "Weighted average = (n1a1 + n2a2)/(n1 + n2)")
        ),

        // ------------------------------------------------------- general science
        art(
            "a_physics", "sub_science", "t_physics",
            "Physics Basics", "Motion, force, energy and the SI units examiners keep asking about.", 6,
            """
            Physics questions in general studies papers stay close to definitions and units. Force is measured in newtons, work and energy in joules, power in watts, and pressure in pascals. A quick revision of units alone answers a surprising share of questions.

            Newton's three laws describe inertia, the relation F = ma, and action-reaction pairs. Applications matter more than derivations: a passenger lurching forward when a bus brakes is inertia; a rocket rising is action-reaction.

            Energy conservation ties the chapter together. Energy changes form - potential to kinetic, electrical to light - but the total remains constant in an isolated system.
            """,
            listOf(
                "SI unit of force is the newton; 1 N = 1 kg m/s^2.",
                "Work is done only when a force produces displacement.",
                "Power is the rate of doing work, measured in watts.",
                "Speed of light in vacuum is about 3 x 10^8 m/s.",
                "Energy can neither be created nor destroyed."
            ),
            listOf(
                "A 10 kg mass accelerating at 2 m/s^2 needs F = ma = 20 N.",
                "Lifting 50 J of work in 10 s means a power of 5 W."
            ),
            listOf("F = m x a", "Work = Force x Displacement", "Power = Work / Time")
        ),
        art(
            "a_chemistry", "sub_science", "t_chemistry",
            "Chemistry Basics", "Atoms, common compounds and everyday chemical names.", 5,
            """
            An atom has protons and neutrons in its nucleus with electrons around it. The atomic number is the proton count and decides the element; the mass number is protons plus neutrons.

            Exams love the common names of compounds. Common salt is NaCl, baking soda is sodium bicarbonate, washing soda is sodium carbonate, quicklime is calcium oxide and marble is calcium carbonate.

            Acids taste sour and turn blue litmus red; bases taste bitter and turn red litmus blue. On the pH scale 7 is neutral, below 7 acidic and above 7 basic.
            """,
            listOf(
                "Atomic number = number of protons.",
                "Common salt is sodium chloride, NaCl.",
                "Baking soda is NaHCO3; washing soda is Na2CO3.",
                "pH 7 is neutral, below 7 acidic, above 7 basic.",
                "Water has the formula H2O."
            ),
            listOf(
                "Vinegar is dilute acetic acid, so it turns blue litmus red.",
                "Lime water turns milky with CO2 because calcium carbonate forms."
            )
        ),
        art(
            "a_human_body", "sub_science", "t_human_body",
            "Human Body", "Organ systems, blood, vitamins and deficiency diseases.", 6,
            """
            The human body is organised into systems. The heart has four chambers and pumps blood through arteries and veins; red blood cells carry oxygen using haemoglobin, while white blood cells defend against infection.

            Digestion begins in the mouth with saliva and is completed in the small intestine, where most absorption occurs. The liver is the largest internal organ, and skin is the largest organ overall.

            Vitamin deficiencies are a reliable exam topic: vitamin A protects vision, C prevents scurvy, D comes from sunlight and prevents rickets, and B12 deficiency causes anaemia.
            """,
            listOf(
                "The human heart has four chambers.",
                "Red blood cells transport oxygen through haemoglobin.",
                "Skin is the largest organ; the liver is the largest internal organ.",
                "Vitamin D is synthesised with the help of sunlight.",
                "Insulin is produced by the pancreas."
            ),
            listOf(
                "Scurvy is caused by a lack of vitamin C, found in citrus fruits.",
                "Rickets in children indicates vitamin D deficiency."
            )
        ),
        art(
            "a_plants_animals", "sub_science", "t_plants_animals",
            "Plants & Animals", "Photosynthesis, plant parts and animal classification.", 5,
            """
            Plants make their own food by photosynthesis, absorbing carbon dioxide and releasing oxygen in the presence of sunlight and chlorophyll. This single process supports almost every food chain on the planet.

            Roots absorb water and minerals, stems transport them, and leaves are the site of photosynthesis. Transpiration - water loss through stomata - helps pull water up the plant.

            Animals are classified as vertebrates and invertebrates. Mammals are warm-blooded and feed their young with milk; reptiles and amphibians are cold-blooded.
            """,
            listOf(
                "Photosynthesis needs sunlight, chlorophyll, water and carbon dioxide.",
                "Plants absorb CO2 and release O2 during photosynthesis.",
                "Stomata control gas exchange and transpiration.",
                "Mammals are warm-blooded; reptiles are cold-blooded."
            ),
            listOf("A potted plant kept in the dark turns pale because chlorophyll needs light.")
        ),
        art(
            "a_environment", "sub_science", "t_environment",
            "Environment & Ecology", "Ecosystems, pollution and conservation.", 5,
            """
            An ecosystem is a community of organisms interacting with their physical surroundings. Producers make food, consumers eat it, and decomposers return nutrients to the soil, keeping the cycle going.

            Greenhouse gases such as carbon dioxide and methane trap heat and drive global warming. Ozone in the stratosphere shields the earth from ultraviolet radiation, and chlorofluorocarbons damage it.

            Conservation appears in exams through protected areas, national parks and international days. India's Project Tiger is a standard example of species-focused conservation.
            """,
            listOf(
                "Producers, consumers and decomposers form the food chain.",
                "CO2 and methane are major greenhouse gases.",
                "The ozone layer blocks harmful ultraviolet radiation.",
                "World Environment Day is 5 June."
            ),
            listOf("A pond ecosystem: algae are producers, fish are consumers, bacteria are decomposers.")
        ),

        // ---------------------------------------------------------------- history
        art(
            "a_ancient", "sub_history", "t_ancient",
            "Ancient India", "Indus Valley, Vedic period, Mauryas and Guptas.", 7,
            """
            The Indus Valley or Harappan civilisation flourished around 2500 BCE with planned cities, drainage systems and standardised bricks. Mohenjo-daro stood on the Indus and Harappa on the Ravi; the Great Bath and granaries are its best known structures.

            The Vedic period follows, giving us the four Vedas, with the Rigveda the oldest. Later came the rise of Buddhism and Jainism, founded by Gautama Buddha and Mahavira respectively, both reacting to ritual orthodoxy.

            The Mauryan empire, founded by Chandragupta Maurya with Chanakya as adviser, peaked under Ashoka, who spread Buddhism after the Kalinga war. The Gupta period that followed is called the golden age for its achievements in mathematics, astronomy and literature.
            """,
            listOf(
                "Mohenjo-daro is on the Indus; Harappa on the Ravi.",
                "The Rigveda is the oldest of the four Vedas.",
                "Chandragupta Maurya founded the Mauryan empire; Chanakya wrote the Arthashastra.",
                "Ashoka embraced Buddhism after the Kalinga war.",
                "Aryabhata and Kalidasa belong to the Gupta age."
            ),
            listOf("Ashoka's edicts were inscribed on rocks and pillars in Prakrit using the Brahmi script.")
        ),
        art(
            "a_medieval", "sub_history", "t_medieval",
            "Medieval India", "Delhi Sultanate, Vijayanagara and the Mughals.", 7,
            """
            The Delhi Sultanate spans five dynasties - Slave, Khilji, Tughlaq, Sayyid and Lodi - from the thirteenth to the sixteenth century. Alauddin Khilji is remembered for market control and Muhammad bin Tughlaq for shifting his capital and issuing token currency.

            In the south, the Vijayanagara empire reached its height under Krishnadevaraya, while the Bahmani kingdom later split into five Deccan sultanates including Golconda, whose Qutb Shahi rulers founded Hyderabad.

            Babur defeated Ibrahim Lodi at the first battle of Panipat in 1526 and founded the Mughal empire. Akbar consolidated it, Shah Jahan built the Taj Mahal, and Aurangzeb's long wars strained it before its decline.
            """,
            listOf(
                "First battle of Panipat, 1526: Babur defeated Ibrahim Lodi.",
                "Krishnadevaraya was the greatest Vijayanagara ruler.",
                "Qutb Shahi rulers of Golconda founded Hyderabad.",
                "Akbar introduced the mansabdari system and abolished jizya.",
                "The Taj Mahal was built by Shah Jahan at Agra."
            ),
            listOf("Second battle of Panipat, 1556: Akbar's forces defeated Hemu.")
        ),
        art(
            "a_modern", "sub_history", "t_modern",
            "Modern India", "Company rule, revolts, reform movements and colonial policy.", 8,
            """
            The Battle of Plassey in 1757 and Buxar in 1764 established East India Company power in Bengal. Land revenue experiments followed - the Permanent Settlement in Bengal, the Ryotwari system in Madras and Bombay, and the Mahalwari system in the north.

            The Revolt of 1857, beginning at Meerut, was the first large challenge to Company rule. Its suppression transferred power to the Crown under the Government of India Act, 1858, and Queen Victoria's proclamation.

            Reform movements ran in parallel. Raja Ram Mohan Roy founded the Brahmo Samaj and campaigned against sati, Ishwar Chandra Vidyasagar promoted widow remarriage, and Jyotiba Phule worked for education and caste equality.
            """,
            listOf(
                "Battle of Plassey, 1757; Battle of Buxar, 1764.",
                "The Revolt of 1857 started at Meerut.",
                "The Government of India Act, 1858 moved power to the Crown.",
                "Raja Ram Mohan Roy founded the Brahmo Samaj.",
                "The Indian National Congress was founded in 1885."
            ),
            listOf("The Permanent Settlement of 1793 made zamindars the owners of land in Bengal.")
        ),
        art(
            "a_freedom", "sub_history", "t_freedom",
            "Indian Freedom Movement", "From moderates to independence: the key dates and movements.", 8,
            """
            The Indian National Congress, founded in 1885, moved from moderate petitions to assertive politics. The partition of Bengal in 1905 triggered the Swadeshi movement, and the Lucknow Pact of 1916 brought Congress and the Muslim League together briefly.

            Gandhi's era begins after the Jallianwala Bagh massacre of 1919. The Non-Cooperation movement (1920) was withdrawn after Chauri Chaura, the Civil Disobedience movement opened with the Dandi March in 1930, and the Quit India movement followed in 1942.

            Meanwhile revolutionary nationalism ran alongside: Bhagat Singh, Chandrasekhar Azad and later Subhas Chandra Bose with the Indian National Army. Independence came on 15 August 1947 under the Indian Independence Act.
            """,
            listOf(
                "Indian National Congress founded in 1885 in Bombay.",
                "Jallianwala Bagh massacre: 13 April 1919, Amritsar.",
                "Dandi March, 1930, launched Civil Disobedience.",
                "Quit India movement began in August 1942.",
                "India became independent on 15 August 1947."
            ),
            listOf("Gandhi withdrew Non-Cooperation in 1922 after the violence at Chauri Chaura.")
        ),

        // -------------------------------------------------------------- geography
        art(
            "a_rivers", "sub_geography", "t_rivers",
            "Indian Rivers", "Himalayan and peninsular river systems.", 6,
            """
            Indian rivers fall into two families. Himalayan rivers - the Indus, Ganga and Brahmaputra - are snow-fed and perennial, cutting deep gorges and forming large plains. Peninsular rivers are rain-fed and seasonal.

            The Ganga is India's longest river at about 2,525 km, rising at Gangotri and joined by the Yamuna at Prayagraj. The Godavari, the longest peninsular river, is called the Dakshina Ganga.

            The Narmada and Tapi are unusual in flowing west into the Arabian Sea through rift valleys, which is why they form estuaries rather than deltas.
            """,
            listOf(
                "Ganga is the longest river within India.",
                "Godavari is the longest peninsular river, called Dakshina Ganga.",
                "Narmada and Tapi flow westward and form estuaries.",
                "The Brahmaputra is called the Tsangpo in Tibet.",
                "Sundarbans is the delta of the Ganga-Brahmaputra system."
            ),
            listOf("The Krishna rises near Mahabaleshwar and flows through Karnataka, Telangana and Andhra Pradesh.")
        ),
        art(
            "a_climate", "sub_geography", "t_climate",
            "Indian Climate & Monsoon", "Monsoon mechanism, seasons and rainfall distribution.", 6,
            """
            India has a tropical monsoon climate driven by the seasonal reversal of winds. The south-west monsoon arrives in Kerala around 1 June, advances across the country by mid-July, and retreats from September.

            The monsoon splits into the Arabian Sea and Bay of Bengal branches. The windward Western Ghats receive very heavy rain while the leeward Deccan interior lies in a rain-shadow, which explains the sharp rainfall contrast over short distances.

            The retreating monsoon brings rain to Tamil Nadu's coast in October and November. Mawsynram and Cherrapunji in Meghalaya are among the wettest places on earth.
            """,
            listOf(
                "The south-west monsoon reaches Kerala around 1 June.",
                "Western Ghats windward slopes get heavy orographic rain.",
                "Tamil Nadu gets most rain from the retreating north-east monsoon.",
                "Mawsynram receives the world's highest rainfall."
            ),
            listOf("Pune receives far less rain than Mumbai because it lies in the rain-shadow of the Ghats.")
        ),
        art(
            "a_world_geo", "sub_geography", "t_world_geo",
            "World Geography", "Continents, oceans, latitudes and important lines.", 6,
            """
            The earth has seven continents and five oceans, the Pacific being the largest and deepest. Latitudes run parallel to the equator, and longitudes converge at the poles; the prime meridian at 0 degrees passes through Greenwich.

            The Tropic of Cancer at 23.5 degrees north passes through eight Indian states. The International Date Line roughly follows 180 degrees longitude, deviating to keep island groups on one side.

            Climate belts follow latitude: equatorial rainforests near the equator, deserts around 30 degrees, temperate zones in the mid-latitudes and tundra near the poles.
            """,
            listOf(
                "The Pacific is the largest and deepest ocean.",
                "The prime meridian passes through Greenwich.",
                "The Tropic of Cancer passes through 8 Indian states.",
                "Asia is the largest continent by area and population."
            ),
            listOf("Local time changes by 4 minutes for every degree of longitude.")
        ),
        art(
            "a_physical", "sub_geography", "t_physical",
            "Physical Features of India", "Mountains, plains, plateau, coast and islands.", 6,
            """
            India has six physical divisions: the Himalayas, the northern plains, the peninsular plateau, the Thar desert, the coastal plains and the islands. The Himalayas run about 2,400 km and are the youngest fold mountains.

            The peninsular plateau is the oldest landmass, and the Deccan trap region is formed of basalt from ancient volcanic activity. The Western Ghats are continuous and higher; the Eastern Ghats are broken by river valleys.

            The eastern coastal plains are wider and hold the deltas of the Mahanadi, Godavari, Krishna and Kaveri, while the narrow western coast has estuaries and lagoons.
            """,
            listOf(
                "The Himalayas are the youngest fold mountains in India.",
                "Kangchenjunga is the highest peak in India.",
                "The Deccan plateau is made of basaltic lava.",
                "Eastern coastal plains are wider than the western plains."
            ),
            listOf("Anai Mudi in the Western Ghats is the highest peak in peninsular India.")
        ),

        // ------------------------------------------------------------ indian polity
        art(
            "a_constitution", "sub_polity", "t_constitution",
            "Constitution Basics", "Making of the Constitution, Preamble and its schedules.", 7,
            """
            The Constituent Assembly was formed in 1946, with Dr B R Ambedkar chairing the drafting committee. The Constitution was adopted on 26 November 1949 and came into force on 26 January 1950.

            The Preamble declares India a sovereign, socialist, secular, democratic republic; the words socialist and secular were added by the 42nd Amendment in 1976. The Preamble is not enforceable in court but guides interpretation.

            Structurally the Constitution has parts, schedules and articles. Part III covers Fundamental Rights, Part IV the Directive Principles and Part IVA the Fundamental Duties, added by the 42nd Amendment.
            """,
            listOf(
                "Constitution adopted 26 November 1949; effective 26 January 1950.",
                "Dr B R Ambedkar chaired the drafting committee.",
                "Socialist and secular were added by the 42nd Amendment, 1976.",
                "Part III - Fundamental Rights; Part IV - Directive Principles.",
                "Fundamental Duties were added as Part IVA."
            ),
            listOf("The idea of the Directive Principles was borrowed from the Irish Constitution.")
        ),
        art(
            "a_frights", "sub_polity", "t_frights",
            "Fundamental Rights", "Articles 12 to 35 and the writs that protect them.", 7,
            """
            Fundamental Rights are contained in Part III, Articles 12 to 35, and are enforceable in court. There are six: equality, freedom, against exploitation, freedom of religion, cultural and educational rights, and constitutional remedies.

            Article 14 guarantees equality before law, Article 19 lists six freedoms including speech and expression, and Article 21 protects life and personal liberty - the article courts have read most expansively. Article 21A added the right to education.

            Article 32 is the right to constitutional remedies, which Ambedkar called the heart and soul of the Constitution. The Supreme Court can issue five writs: habeas corpus, mandamus, prohibition, certiorari and quo warranto.
            """,
            listOf(
                "Fundamental Rights: Part III, Articles 12-35.",
                "Article 14 - equality before law; Article 21 - life and liberty.",
                "Article 32 - right to constitutional remedies.",
                "Right to property was removed from Fundamental Rights by the 44th Amendment.",
                "Five writs: habeas corpus, mandamus, prohibition, certiorari, quo warranto."
            ),
            listOf("Habeas corpus is issued to produce a detained person before the court.")
        ),
        art(
            "a_parliament", "sub_polity", "t_parliament",
            "Parliament", "Lok Sabha, Rajya Sabha, bills and the budget process.", 7,
            """
            Parliament consists of the President, the Lok Sabha and the Rajya Sabha. The Lok Sabha is directly elected for five years; the Rajya Sabha is a permanent house with one third of its members retiring every two years.

            A money bill can be introduced only in the Lok Sabha, and only on the President's recommendation. The Rajya Sabha may keep it for a maximum of fourteen days and can only recommend changes.

            The Speaker presides over the Lok Sabha and decides whether a bill is a money bill. The Vice-President is the ex-officio chairman of the Rajya Sabha.
            """,
            listOf(
                "Money bills can be introduced only in the Lok Sabha.",
                "Rajya Sabha can delay a money bill by at most 14 days.",
                "The Vice-President chairs the Rajya Sabha.",
                "A joint sitting is convened by the President for ordinary bills.",
                "Question Hour and Zero Hour are parliamentary devices for oversight."
            ),
            listOf("The Speaker's decision on whether a bill is a money bill is final.")
        ),
        art(
            "a_judiciary", "sub_polity", "t_judiciary",
            "Judiciary", "Supreme Court, High Courts and judicial review.", 6,
            """
            India has a single integrated judiciary with the Supreme Court at the top, High Courts in the states and subordinate courts below. Judges of the Supreme Court are appointed by the President and retire at 65; High Court judges retire at 62.

            The Supreme Court has original, appellate and advisory jurisdiction. Judicial review lets it strike down laws that violate the Constitution, and public interest litigation has widened access to it considerably.

            The basic structure doctrine, laid down in the Kesavananda Bharati case, holds that Parliament cannot amend the essential features of the Constitution.
            """,
            listOf(
                "Supreme Court judges retire at 65; High Court judges at 62.",
                "Article 32 allows direct petition to the Supreme Court.",
                "The basic structure doctrine came from Kesavananda Bharati (1973).",
                "Judicial review allows courts to test laws against the Constitution."
            ),
            listOf("A law violating Fundamental Rights can be declared void under judicial review.")
        ),
        art(
            "a_panchayat", "sub_polity", "t_panchayat",
            "Panchayati Raj", "The 73rd and 74th Amendments and local self-government.", 5,
            """
            The 73rd Amendment Act of 1992 gave constitutional status to panchayats, creating a three-tier structure of Gram Panchayat, Panchayat Samiti and Zilla Parishad, with a five-year term. The 74th Amendment did the same for urban local bodies.

            One third of seats are reserved for women, along with reservation for scheduled castes and tribes in proportion to population. The Gram Sabha, made up of all registered voters of the village, is the foundation of the system.

            The Eleventh Schedule lists 29 subjects that states may devolve to panchayats, covering areas from agriculture to primary education.
            """,
            listOf(
                "The 73rd Amendment (1992) gave panchayats constitutional status.",
                "Three tiers: Gram Panchayat, Panchayat Samiti, Zilla Parishad.",
                "At least one third of seats are reserved for women.",
                "The Eleventh Schedule lists 29 subjects for panchayats."
            ),
            listOf("The Gram Sabha approves the panchayat's annual plan and accounts.")
        ),

        // ---------------------------------------------------------------- economy
        art(
            "a_eco_basics", "sub_economy", "t_eco_basics",
            "Basic Economic Concepts", "GDP, sectors, inflation and national income.", 6,
            """
            Gross Domestic Product is the market value of all final goods and services produced within a country in a year. GDP at market prices minus indirect taxes plus subsidies gives GDP at factor cost.

            The economy is divided into three sectors: primary covers agriculture and mining, secondary manufacturing, and tertiary services. India's tertiary sector contributes the largest share of GDP while agriculture still employs the most people.

            Inflation is a sustained rise in the general price level, measured in India by the Consumer Price Index and the Wholesale Price Index. Moderate inflation is normal; very high inflation erodes savings.
            """,
            listOf(
                "GDP counts only final goods and services produced within the country.",
                "Primary, secondary and tertiary are the three sectors.",
                "Agriculture belongs to the primary sector.",
                "Inflation in India is tracked using CPI and WPI."
            ),
            listOf("Value added avoids double counting: only the final price of bread, not the wheat and flour separately.")
        ),
        art(
            "a_banking", "sub_economy", "t_banking",
            "Banking in India", "RBI, monetary policy tools and types of banks.", 6,
            """
            The Reserve Bank of India, established in 1935 and nationalised in 1949, is the central bank. It issues currency, manages monetary policy and acts as banker to the government and lender of last resort.

            The Monetary Policy Committee sets the repo rate, the rate at which banks borrow from the RBI. Raising the repo rate makes borrowing costlier and cools inflation; the reverse repo, CRR and SLR are the other main tools.

            Commercial banks are classified as public sector, private sector, foreign, regional rural and cooperative banks. Small finance banks and payment banks were added to widen financial inclusion.
            """,
            listOf(
                "RBI was established in 1935 and nationalised in 1949.",
                "The repo rate is set by the Monetary Policy Committee.",
                "CRR is the share of deposits banks keep with the RBI.",
                "SLR is the share of deposits held in approved securities.",
                "The RBI is the lender of last resort."
            ),
            listOf("A repo rate cut usually lowers home loan interest rates over time.")
        ),
        art(
            "a_budget", "sub_economy", "t_budget",
            "Budget & Taxation", "Union Budget, deficits and the tax structure.", 6,
            """
            The Union Budget, presented by the Finance Minister under Article 112 as the annual financial statement, has a revenue account and a capital account. The financial year runs from 1 April to 31 March.

            Fiscal deficit is the gap between total expenditure and total receipts excluding borrowings; revenue deficit is the shortfall on the revenue account alone. Primary deficit is fiscal deficit minus interest payments.

            Taxes are direct - income tax and corporation tax - or indirect. GST, introduced on 1 July 2017, replaced a long list of indirect taxes and is administered through the GST Council.
            """,
            listOf(
                "The Union Budget is presented under Article 112.",
                "Fiscal deficit = total expenditure - total receipts excluding borrowings.",
                "Income tax is a direct tax; GST is indirect.",
                "GST was introduced on 1 July 2017."
            ),
            listOf("If expenditure is 40 lakh crore and non-borrowed receipts 30 lakh crore, the fiscal deficit is 10 lakh crore.")
        ),
        art(
            "a_agriculture", "sub_economy", "t_agriculture",
            "Indian Agriculture", "Cropping seasons, green revolution and support prices.", 5,
            """
            Indian agriculture follows three cropping seasons. Kharif crops are sown with the monsoon and harvested in autumn - rice, cotton and maize. Rabi crops are sown in winter and harvested in spring - wheat and mustard. Zaid crops grow in the short summer season.

            The Green Revolution of the 1960s raised wheat and rice output sharply through high-yielding varieties, irrigation and fertilisers, concentrated first in Punjab, Haryana and western Uttar Pradesh.

            Minimum Support Price is announced on the recommendation of the Commission for Agricultural Costs and Prices to assure farmers a floor price.
            """,
            listOf(
                "Kharif crops are sown with the south-west monsoon.",
                "Wheat is the main rabi crop.",
                "The Green Revolution focused on wheat and rice.",
                "MSP is recommended by CACP."
            ),
            listOf("Rice needs high rainfall and is a kharif crop; wheat needs cool weather and is rabi.")
        ),

        // --------------------------------------------------------- current affairs
        art(
            "a_national_af", "sub_ca", "t_national_af",
            "National Affairs Digest", "How to prepare the national current affairs section.", 5,
            """
            National current affairs rewards steady note-making rather than last-minute reading. Track government schemes, appointments to constitutional posts, major bills passed, and national reports and indices.

            For every scheme, record four things: the launching ministry, the year, the target group and the benefit. Examiners usually ask exactly these.

            This demo module carries a small illustrative set so the screens have data. In a live build this section would be refreshed from the newsroom side of the app.
            """,
            listOf(
                "Note the ministry, year, target group and benefit for every scheme.",
                "Track appointments to constitutional and statutory posts.",
                "Follow national indices and reports and their publishers.",
                "Revise weekly rather than in one long session."
            ),
            listOf("ISRO, headquartered in Bengaluru, is a standard source of national science current affairs.")
        ),
        art(
            "a_international_af", "sub_ca", "t_international_af",
            "International Affairs Digest", "Summits, organisations and India's global engagements.", 5,
            """
            International affairs questions concentrate on organisations and summits. For each body, learn the year of formation, headquarters and current head - the UN in New York, WHO in Geneva, IMF and World Bank in Washington DC.

            Groupings matter too: G20, BRICS, SAARC, ASEAN and QUAD. Remember membership and India's role, and note the host country of the most recent summit.

            Bilateral visits and defence exercises are the third area. Keep a one-line entry per exercise with the participating countries.
            """,
            listOf(
                "WHO is headquartered in Geneva; the UN in New York.",
                "BRICS: Brazil, Russia, India, China, South Africa and later additions.",
                "India hosted the G20 summit in 2023.",
                "Track host country and theme for each major summit."
            ),
            listOf("SAARC has eight members and is headquartered in Kathmandu.")
        ),
        art(
            "a_sports_awards", "sub_ca", "t_sports_awards",
            "Sports & Awards", "Tournaments, trophies and national honours.", 4,
            """
            Sports questions cluster around trophies and venues: the Ranji Trophy and Duleep Trophy in cricket, the Durand Cup in football, and the Thomas Cup in badminton. Learn which sport each trophy belongs to.

            National honours run in order: Bharat Ratna, Padma Vibhushan, Padma Bhushan and Padma Shri. The highest sporting honour is the Major Dhyan Chand Khel Ratna Award, followed by the Arjuna Award.

            Also note the Olympic and Asian Games cycles with host cities, and India's medal winners in recent editions.
            """,
            listOf(
                "Bharat Ratna is the highest civilian award.",
                "Khel Ratna is the highest sporting award; Arjuna comes next.",
                "The Ranji Trophy is a domestic cricket tournament.",
                "National Sports Day is observed on 29 August."
            ),
            listOf("National Sports Day marks the birth anniversary of hockey player Dhyan Chand.")
        ),

        // ------------------------------------------------------- general knowledge
        art(
            "a_culture", "sub_gk", "t_culture",
            "Indian Culture & Heritage", "Dances, festivals, monuments and UNESCO sites.", 6,
            """
            India's eight classical dance forms are tied to regions: Bharatanatyam to Tamil Nadu, Kuchipudi to Andhra Pradesh, Kathak to north India, Kathakali and Mohiniyattam to Kerala, Odissi to Odisha, Manipuri to Manipur and Sattriya to Assam.

            Heritage monuments come up constantly. The Ajanta and Ellora caves are in Maharashtra, Khajuraho in Madhya Pradesh, Konark Sun Temple in Odisha and Charminar in Hyderabad.

            National symbols are easy marks: the tiger is the national animal, the peacock the national bird, the mango the national fruit and the banyan the national tree.
            """,
            listOf(
                "Kuchipudi belongs to Andhra Pradesh; Bharatanatyam to Tamil Nadu.",
                "Ajanta and Ellora caves are in Maharashtra.",
                "The tiger is the national animal; the peacock the national bird.",
                "Charminar was built in Hyderabad by the Qutb Shahi rulers."
            ),
            listOf("Bathukamma and Bonalu are festivals associated with Telangana.")
        ),
        art(
            "a_books", "sub_gk", "t_books",
            "Books & Authors", "Famous works and their writers.", 4,
            """
            Author questions are pure memory, so group them. Jawaharlal Nehru wrote The Discovery of India and Glimpses of World History; Dr B R Ambedkar wrote Annihilation of Caste; Kautilya wrote the Arthashastra.

            Among literary classics, Kalidasa wrote Abhijnana Shakuntalam, Rabindranath Tagore wrote Gitanjali, and R K Narayan created Malgudi.

            Also track autobiographies and their subjects, since exams often pair a title with the wrong author as a distractor.
            """,
            listOf(
                "The Discovery of India was written by Jawaharlal Nehru.",
                "Gitanjali was written by Rabindranath Tagore.",
                "The Arthashastra is attributed to Kautilya or Chanakya.",
                "Abhijnana Shakuntalam was written by Kalidasa."
            ),
            listOf("Tagore also wrote the national anthem, Jana Gana Mana.")
        ),
        art(
            "a_days", "sub_gk", "t_days",
            "Important Days", "National and international observances.", 4,
            """
            Important days are best learned month by month. January has Republic Day on the 26th, and National Youth Day on the 12th. May has World Environment Day just after, on 5 June.

            August carries Independence Day on the 15th and National Sports Day on the 29th. September has Teachers' Day on the 5th and Hindi Diwas on the 14th.

            October begins with Gandhi Jayanti on the 2nd, also observed as the International Day of Non-Violence.
            """,
            listOf(
                "Republic Day: 26 January. Independence Day: 15 August.",
                "World Environment Day: 5 June.",
                "Teachers' Day: 5 September. National Sports Day: 29 August.",
                "Gandhi Jayanti, 2 October, is the International Day of Non-Violence."
            ),
            listOf("World Health Day is observed on 7 April, marking the founding of the WHO.")
        ),

        // ---------------------------------------------------------------- english
        art(
            "a_grammar", "sub_english", "t_grammar",
            "Grammar Basics", "Tenses, subject-verb agreement and articles.", 6,
            """
            Grammar questions test a small set of rules repeatedly. Subject-verb agreement is first: a singular subject takes a singular verb, and phrases like "one of the students" remain singular.

            Tense consistency is second. "Since" and "for" with a period of time signal the perfect continuous: "He has been working here since 2019." Mixing tenses within a sentence is the most common error planted in exam questions.

            Articles follow sound, not spelling: "an hour" because the h is silent, but "a university" because it begins with a y sound.
            """,
            listOf(
                "A singular subject takes a singular verb.",
                "Since and for with a time period signal perfect tenses.",
                "Use 'an' before a vowel sound, not merely a vowel letter.",
                "Each, every and either are always singular."
            ),
            listOf(
                "Correct: One of my friends is a doctor.",
                "Correct: He has been working here since 2019."
            )
        ),
        art(
            "a_vocabulary", "sub_english", "t_vocabulary",
            "Vocabulary Building", "One-word substitutions, idioms and usage.", 5,
            """
            Vocabulary is best built through roots and repetition. Knowing that "biblio" means book and "phile" means lover gives you bibliophile without memorising it separately.

            One-word substitutions are a standard section: a person who loves books is a bibliophile, one who talks to himself is a soliloquist, and a government by officials is a bureaucracy.

            Idioms must be learned whole. "Once in a blue moon" means very rarely, "to let the cat out of the bag" means to reveal a secret, and "a blessing in disguise" is an apparent misfortune that turns out well.
            """,
            listOf(
                "Learn roots and prefixes to decode unfamiliar words.",
                "Bibliophile = a lover of books.",
                "Once in a blue moon = very rarely.",
                "Record every new word with a sentence of your own."
            ),
            listOf("Let the cat out of the bag = to disclose a secret unintentionally.")
        ),
        art(
            "a_synonyms", "sub_english", "t_synonyms",
            "Synonyms & Antonyms", "High-frequency word pairs for objective papers.", 4,
            """
            Synonym and antonym questions reward a curated list over broad reading. Focus on words that recur: abundant means plentiful, benevolent means kind, candid means frank and diligent means hardworking.

            For antonyms, learn in pairs: ancient and modern, scarce and abundant, transparent and opaque, frugal and extravagant.

            When unsure, use the word in a sentence and test each option in the same slot. The option that preserves the meaning is usually the answer.
            """,
            listOf(
                "Abundant means plentiful; scarce is its antonym.",
                "Benevolent means kind and generous.",
                "Candid means frank and straightforward.",
                "Test options by substitution in a sentence."
            ),
            listOf("Antonym of ancient is modern; synonym of abundant is plentiful.")
        ),
        art(
            "a_sentence", "sub_english", "t_sentence",
            "Sentence Correction", "Spotting errors and improving sentences.", 5,
            """
            Error-spotting questions split a sentence into parts and ask which contains the mistake. Scan in a fixed order: subject-verb agreement, tense, preposition, article, then word choice. A fixed order stops you from missing the obvious.

            Voice conversion appears regularly. "She writes a letter" becomes "A letter is written by her": the object moves to the front, the verb takes the past participle, and the subject follows "by".

            Redundancy is the last check. "Return back", "repeat again" and "more better" are all incorrect because the extra word adds nothing.
            """,
            listOf(
                "Scan in order: agreement, tense, preposition, article, word choice.",
                "Passive voice: object + form of be + past participle + by + subject.",
                "Avoid redundancy such as 'return back' or 'more better'.",
                "Plural of crisis is crises; of criterion is criteria."
            ),
            listOf("Active: She writes a letter. Passive: A letter is written by her.")
        ),

        // -------------------------------------------------------------- reasoning
        art(
            "a_series", "sub_reasoning", "t_series",
            "Number & Letter Series", "Finding the pattern and the missing term.", 5,
            """
            Series questions ask for the rule behind a sequence. Check differences first: if they are constant the series is arithmetic; if the differences themselves form a pattern, look one level deeper.

            Then test ratios for geometric series, and squares or cubes for the classic 1, 4, 9, 16 and 1, 8, 27, 64 patterns. Alternating series interleave two rules, so separate the odd and even positions.

            Letter series use position values: A is 1 and Z is 26. Pairs such as AZ, BY, CX move forward from the start and backward from the end simultaneously.
            """,
            listOf(
                "Check first differences, then second differences.",
                "Constant ratio means a geometric series.",
                "Watch for squares, cubes and prime sequences.",
                "In letter series, convert letters to position numbers."
            ),
            listOf(
                "2, 6, 12, 20, ? - differences are 4, 6, 8, so the next difference is 10 and the answer is 30.",
                "AZ, BY, CX, ? - the next pair is DW."
            )
        ),
        art(
            "a_coding", "sub_reasoning", "t_coding",
            "Coding & Decoding", "Letter shifting, position values and pattern codes.", 5,
            """
            Coding questions replace letters with other letters or numbers by a rule. The most common rule is a fixed shift: if CAT becomes DBU, every letter has moved forward by one.

            The second family uses position values, so CAT becomes 3-1-20. Learn the alphabet positions in both directions, since some codes count backwards from Z.

            A third type reverses the word or swaps letters in pairs. When a code makes no sense, write the word and its code one above the other and mark the movement of each letter.
            """,
            listOf(
                "A = 1 and Z = 26; learn positions both ways.",
                "Write the word above its code and mark each letter's shift.",
                "Check for reversal before assuming a shift.",
                "Opposite-letter pairs: A-Z, B-Y, C-X."
            ),
            listOf("If CAT is coded 3-1-20, then DOG is coded 4-15-7.")
        ),
        art(
            "a_blood", "sub_reasoning", "t_blood",
            "Blood Relations", "Family trees and generation logic.", 5,
            """
            The reliable method is to draw a diagram. Put each generation on its own line, use a plus sign for a married couple and vertical lines for children. Marking gender as you read prevents the classic mistake of assuming a name's gender.

            Read the statement backwards when it is chained: "A is the father of B, B is the mother of C" places A two generations above C, making A the grandfather of C.

            Coded blood relations replace relationships with symbols. Translate each symbol into a diagram element before answering.
            """,
            listOf(
                "Always draw a generation diagram.",
                "Do not assume gender from a name; use only what is stated.",
                "Resolve chained statements from the end backwards.",
                "Paternal means father's side, maternal the mother's."
            ),
            listOf("A is B's father and B is C's mother, so A is C's maternal grandfather.")
        ),
        art(
            "a_direction", "sub_reasoning", "t_direction",
            "Direction Sense", "Turns, displacement and shortest distance.", 4,
            """
            Direction questions become easy once you fix north at the top of your page and draw every movement to rough scale. A right turn while facing north points you east; two right turns face you south.

            Net displacement often needs Pythagoras. Walking 3 km east and then 4 km north puts you 5 km from the start, because 3 squared plus 4 squared is 25.

            Watch for turns described relative to the walker rather than the compass: "turn left" depends on the direction currently faced.
            """,
            listOf(
                "Draw the path with north at the top of the page.",
                "Two consecutive right turns reverse your direction.",
                "Use Pythagoras for shortest distance from the start.",
                "Left and right are relative to the current facing."
            ),
            listOf("3 km east then 4 km north leaves you 5 km from the starting point.")
        ),

        // a couple of items in the content pipeline, for the Content Creator screens
        art(
            "a_draft_si", "sub_math", "t_percentage",
            "Simple & Compound Interest (draft)", "Interest formulas - draft awaiting completion.", 6,
            """
            Draft notes. Simple interest is calculated only on the principal, while compound interest is calculated on the principal plus accumulated interest. Worked examples and the practice set are still to be added before review.
            """,
            listOf("SI = PRT/100.", "CI compounds on principal plus interest."),
            emptyList(),
            listOf("SI = P x R x T / 100", "A = P(1 + R/100)^T"),
            ageDays = 1,
            status = ContentStatus.DRAFT,
            author = "Kiran Kumar"
        ),
        art(
            "a_pending_ca", "sub_ca", "t_national_af",
            "Government Schemes Revision (pending review)", "One-page revision of major schemes - submitted for review.", 7,
            """
            Submitted for editorial review. This revision sheet lists major central schemes with their launch year, ministry and beneficiary group, arranged so that a student can revise the entire set in one sitting.
            """,
            listOf(
                "Record ministry, year, target group and benefit for each scheme.",
                "Group schemes by ministry for faster recall."
            ),
            ageDays = 2,
            status = ContentStatus.PENDING_REVIEW,
            author = "Divya Sree"
        )
    )

    // ================================================================ questions

    private fun q(
        id: String,
        subjectId: String,
        topicId: String,
        text: String,
        options: List<String>,
        correctIndex: Int,
        explanation: String,
        difficulty: Difficulty = Difficulty.MEDIUM,
        type: QuestionType = QuestionType.MCQ,
        textTe: String = "",
        optionsTe: List<String> = emptyList(),
        explanationTe: String = ""
    ) = Question(
        id = id,
        subjectId = subjectId,
        topicId = topicId,
        text = LocalizedText(en = text, te = textTe),
        // Options stay positional across languages - that is what lets
        // correctIndex be a single number rather than one per language.
        options = pair(options, optionsTe),
        correctIndex = correctIndex,
        explanation = LocalizedText(en = explanation, te = explanationTe),
        difficulty = difficulty,
        type = type
    )

    /** A question a real exam already asked — carries the paper it came from. */
    private fun pyq(
        id: String,
        subjectId: String,
        topicId: String,
        text: String,
        options: List<String>,
        correctIndex: Int,
        explanation: String,
        paperName: String,
        year: String,
        difficulty: Difficulty = Difficulty.MEDIUM,
        textTe: String = "",
        optionsTe: List<String> = emptyList(),
        explanationTe: String = ""
    ) = Question(
        id = id,
        subjectId = subjectId,
        topicId = topicId,
        text = LocalizedText(en = text, te = textTe),
        options = pair(options, optionsTe),
        correctIndex = correctIndex,
        explanation = LocalizedText(en = explanation, te = explanationTe),
        difficulty = difficulty,
        source = QuestionSource.PREVIOUS,
        paperName = paperName,
        year = year
    )

    /** Previously asked questions, grouped by the topic they belong to. */
    private fun previousPapers(): List<Question> = listOf(
        pyq(
            "qy1", "sub_polity", "t_constitution",
            "The Right to Equality is guaranteed by which Articles?",
            listOf("Articles 12-13", "Articles 14-18", "Articles 19-22", "Articles 23-24"), 1,
            "Articles 14 to 18 together form the Right to Equality.",
            "TSPSC Group-2 General Studies", "2023"
        ),
        pyq(
            "qy2", "sub_polity", "t_frights",
            "Which Article deals with the Right to Life and Personal Liberty?",
            listOf("Article 19", "Article 20", "Article 21", "Article 22"), 2,
            "Article 21 — expanded by the courts to cover dignity, privacy and livelihood.",
            "TS Police Constable Preliminary", "2022", Difficulty.EASY
        ),
        pyq(
            "qy3", "sub_polity", "t_parliament",
            "The maximum strength of the Lok Sabha is",
            listOf("500", "543", "552", "560"), 2,
            "The Constitution fixes the maximum at 552 members.",
            "RRB NTPC CBT-1", "2022"
        ),
        pyq(
            "qy4", "sub_polity", "t_judiciary",
            "Who acts as President when both the President and Vice President posts are vacant?",
            listOf("Prime Minister", "Speaker of Lok Sabha", "Chief Justice of India", "Home Minister"), 2,
            "The Chief Justice of India acts as President — as happened in 1969.",
            "TSPSC Group-1 Prelims", "2022", Difficulty.HARD
        ),
        pyq(
            "qy5", "sub_polity", "t_panchayat",
            "The 73rd Constitutional Amendment relates to",
            listOf("Municipalities", "Panchayati Raj", "Anti-defection", "Education"), 1,
            "The 73rd Amendment, 1992 gave Panchayati Raj constitutional status.",
            "TS Police SI Preliminary", "2023"
        ),
        pyq(
            "qy6", "sub_history", "t_freedom",
            "The Quit India Movement was launched in the year",
            listOf("1930", "1935", "1942", "1945"), 2,
            "Launched on 8 August 1942 from the Bombay session of the Congress.",
            "SSC CGL Tier-1", "2023", Difficulty.EASY
        ),
        pyq(
            "qy7", "sub_science", "t_physics",
            "The SI unit of force is",
            listOf("Joule", "Newton", "Watt", "Pascal"), 1,
            "Force is measured in newtons — one newton accelerates 1 kg by 1 m/s².",
            "TS Police Constable Preliminary", "2022", Difficulty.EASY
        ),
        pyq(
            "qy8", "sub_geography", "t_rivers",
            "Which river is known as the Dakshina Ganga?",
            listOf("Krishna", "Godavari", "Kaveri", "Tungabhadra"), 1,
            "The Godavari — the largest peninsular river, called the Dakshina Ganga.",
            "TSPSC Group-2 General Studies", "2023"
        )
    )

    /** Numeric question helper: options are sorted so the answer position varies naturally. */
    private fun numQ(
        id: String,
        subjectId: String,
        topicId: String,
        text: String,
        answer: Int,
        wrong: List<Int>,
        explanation: String,
        difficulty: Difficulty = Difficulty.MEDIUM,
        textTe: String = "",
        explanationTe: String = ""
    ): Question {
        val opts = (listOf(answer) + wrong.filter { it != answer }).distinct().sorted()
        return Question(
            id = id,
            subjectId = subjectId,
            topicId = topicId,
            text = LocalizedText(en = text, te = textTe),
            // Numeric options need no translation - a digit reads the same in
            // both scripts - so both sides carry the identical string.
            options = opts.map { LocalizedText.both(it.toString()) },
            correctIndex = opts.indexOf(answer),
            explanation = LocalizedText(en = explanation, te = explanationTe),
            difficulty = difficulty
        )
    }

    // ---------------------------------------------------------------- generated
    // Deterministic arithmetic items. Real exam-style questions whose answers are
    // computed, so the bank can be large without inventing unverifiable facts.

    private fun percentageQuestions(): List<Question> {
        val data = listOf(
            Triple(20, 250, 1), Triple(15, 800, 2), Triple(35, 400, 3), Triple(12, 1500, 4),
            Triple(45, 200, 5), Triple(60, 350, 6), Triple(25, 640, 7), Triple(18, 950, 8)
        )
        return data.map { (pct, num, n) ->
            val ans = pct * num / 100
            numQ(
                "q_gen_pct_$n", "sub_math", "t_percentage",
                "What is $pct% of $num?",
                ans,
                listOf(ans + ans / 5 + 1, ans - ans / 4 - 1, ans + 10, ans - 8),
                "$pct% of $num = ($pct/100) x $num = $ans.",
                if (n % 3 == 0) Difficulty.MEDIUM else Difficulty.EASY
            )
        }
    }

    private fun profitLossQuestions(): List<Question> {
        val data = listOf(
            Pair(400, 500), Pair(250, 300), Pair(1200, 1500), Pair(800, 720),
            Pair(600, 690), Pair(150, 120)
        )
        return data.mapIndexed { i, (cp, sp) ->
            val pct = ((sp - cp) * 100) / cp
            val gainOrLoss = if (pct >= 0) "profit" else "loss"
            numQ(
                "q_gen_pl_${i + 1}", "sub_math", "t_profit_loss",
                "An article bought for Rs $cp is sold for Rs $sp. Find the $gainOrLoss percentage.",
                kotlin.math.abs(pct),
                listOf(kotlin.math.abs(pct) + 5, kotlin.math.abs(pct) - 5, kotlin.math.abs(pct) + 10, kotlin.math.abs(pct) + 2),
                "Difference = ${kotlin.math.abs(sp - cp)}. Percentage on CP = ${kotlin.math.abs(sp - cp)}/$cp x 100 = ${kotlin.math.abs(pct)}%.",
                Difficulty.MEDIUM
            )
        }
    }

    private fun averageQuestions(): List<Question> {
        val sets = listOf(
            listOf(12, 18, 24, 30, 36),
            listOf(45, 55, 65, 75),
            listOf(8, 16, 24, 32, 40, 48),
            listOf(100, 150, 200),
            listOf(21, 27, 33, 39),
            listOf(5, 15, 25, 35, 45)
        )
        return sets.mapIndexed { i, set ->
            val ans = set.sum() / set.size
            numQ(
                "q_gen_avg_${i + 1}", "sub_math", "t_average",
                "Find the average of ${set.joinToString(", ")}.",
                ans,
                listOf(ans + 3, ans - 3, ans + 6, ans - 6),
                "Sum = ${set.sum()}, count = ${set.size}, so average = ${set.sum()}/${set.size} = $ans.",
                Difficulty.EASY
            )
        }
    }

    private fun timeWorkQuestions(): List<Question> {
        val pairs = listOf(Pair(10, 15), Pair(12, 24), Pair(20, 30), Pair(6, 12), Pair(9, 18), Pair(16, 48))
        return pairs.mapIndexed { i, (a, b) ->
            val ans = (a * b) / (a + b)
            numQ(
                "q_gen_tw_${i + 1}", "sub_math", "t_time_work",
                "A can finish a piece of work in $a days and B in $b days. Working together, in how many days will they finish it?",
                ans,
                listOf(ans + 2, ans - 2, ans + 4, a + b),
                "Together = 1/$a + 1/$b. Days = ($a x $b)/($a + $b) = $ans.",
                Difficulty.MEDIUM
            )
        }
    }

    private fun ratioQuestions(): List<Question> {
        val data = listOf(Triple(3, 5, 64), Triple(2, 7, 90), Triple(4, 5, 108), Triple(5, 7, 144), Triple(1, 3, 80), Triple(3, 4, 140))
        return data.mapIndexed { i, (a, b, total) ->
            val ans = total * b / (a + b)
            numQ(
                "q_gen_ratio_${i + 1}", "sub_math", "t_ratio",
                "Rs $total is divided between two people in the ratio $a : $b. What is the larger share?",
                ans,
                listOf(total * a / (a + b), ans + 8, ans - 8, total / 2),
                "Total parts = ${a + b}. Larger share = $total x $b/${a + b} = $ans.",
                Difficulty.MEDIUM
            )
        }
    }

    private fun numberSystemQuestions(): List<Question> {
        val pairs = listOf(Pair(12, 18), Pair(24, 36), Pair(15, 25), Pair(8, 20), Pair(14, 21), Pair(30, 45))
        fun hcf(a: Int, b: Int): Int = if (b == 0) a else hcf(b, a % b)
        return pairs.mapIndexed { i, pair ->
            val (a, b) = pair
            val h = hcf(a, b)
            val l = a * b / h
            listOf(
                numQ(
                    "q_gen_ns_h${i + 1}", "sub_math", "t_num_system",
                    "Find the HCF of $a and $b.",
                    h,
                    listOf(h * 2, h + 1, h + 3, l),
                    "Using the division method, HCF($a, $b) = $h.",
                    Difficulty.EASY
                ),
                numQ(
                    "q_gen_ns_l${i + 1}", "sub_math", "t_num_system",
                    "Find the LCM of $a and $b.",
                    l,
                    listOf(l / 2, l + a, l - b, a * b),
                    "LCM x HCF = product, so LCM = ($a x $b)/$h = $l.",
                    Difficulty.MEDIUM
                )
            )
        }.flatten()
    }

    private fun seriesQuestions(): List<Question> {
        val series = listOf(
            Triple(listOf(2, 6, 12, 20), 30, "Differences are 4, 6, 8, so the next difference is 10."),
            Triple(listOf(5, 10, 20, 40), 80, "Each term is doubled."),
            Triple(listOf(1, 4, 9, 16), 25, "These are squares of 1, 2, 3, 4, so the next is 5 squared."),
            Triple(listOf(3, 6, 11, 18), 27, "Differences are 3, 5, 7, so the next difference is 9."),
            Triple(listOf(1, 8, 27, 64), 125, "These are cubes of 1, 2, 3, 4, so the next is 5 cubed."),
            Triple(listOf(7, 14, 28, 56), 112, "Each term is doubled."),
            Triple(listOf(2, 5, 10, 17), 26, "Differences are 3, 5, 7, so the next difference is 9."),
            Triple(listOf(4, 12, 36, 108), 324, "Each term is multiplied by 3.")
        )
        return series.mapIndexed { i, (seq, ans, why) ->
            numQ(
                "q_gen_series_${i + 1}", "sub_reasoning", "t_series",
                "Find the next term: ${seq.joinToString(", ")}, ?",
                ans,
                listOf(ans + 2, ans - 2, ans + 6, ans + seq.last() / 2),
                why,
                if (i % 3 == 0) Difficulty.EASY else Difficulty.MEDIUM
            )
        }
    }

    // -------------------------------------------------------------- handcrafted

    private val handcrafted: List<Question> = listOf(
        // ---- mathematics
        q("q_math_1", "sub_math", "t_percentage", "A number increased by 20% becomes 96. What is the original number?",
            listOf("76", "80", "84", "88"), 1, "Original x 1.2 = 96, so original = 96/1.2 = 80.", Difficulty.MEDIUM),
        q("q_math_2", "sub_math", "t_percentage", "A price rises by 20% and then falls by 20%. The net change is:",
            listOf("No change", "4% decrease", "4% increase", "2% decrease"), 1,
            "Net = 20 - 20 - (20x20)/100 = -4%, a 4% decrease.", Difficulty.MEDIUM),
        q("q_math_3", "sub_math", "t_profit_loss", "A shopkeeper marks goods 40% above cost and allows a 20% discount. His gain percent is:",
            listOf("8%", "12%", "16%", "20%"), 1,
            "SP = 1.40 x 0.80 x CP = 1.12 CP, so the gain is 12%.", Difficulty.HARD),
        q("q_math_4", "sub_math", "t_average", "The average of the first 10 natural numbers is:",
            listOf("5", "5.5", "6", "6.5"), 1, "Average of first n naturals = (n+1)/2 = 11/2 = 5.5.", Difficulty.EASY),
        q("q_math_5", "sub_math", "t_num_system", "How many prime numbers are there below 100?",
            listOf("23", "24", "25", "26"), 2, "There are 25 primes below 100.", Difficulty.MEDIUM),
        q("q_math_6", "sub_math", "t_num_system", "Which of the following is divisible by 9?",
            listOf("3,168", "3,167", "3,169", "3,170"), 0,
            "Digit sum of 3168 = 18, which is divisible by 9.", Difficulty.EASY),
        q("q_math_7", "sub_math", "t_time_work", "A pipe fills a tank in 6 hours and a leak empties it in 12 hours. With both open, the tank fills in:",
            listOf("8 hours", "10 hours", "12 hours", "18 hours"), 2,
            "Net rate = 1/6 - 1/12 = 1/12, so 12 hours.", Difficulty.MEDIUM),
        q("q_math_8", "sub_math", "t_ratio", "If a : b = 3 : 5 and b = 20, then a is:",
            listOf("9", "12", "15", "18"), 1, "a/b = 3/5, so a = 3/5 x 20 = 12.", Difficulty.EASY),

        // ---- general science
        q("q_sci_1", "sub_science", "t_physics", "The SI unit of force is:",
            listOf("Joule", "Newton", "Watt", "Pascal"), 1, "Force is measured in newtons; 1 N = 1 kg m/s^2.", Difficulty.EASY),
        q("q_sci_2", "sub_science", "t_physics", "A body of mass 10 kg accelerates at 2 m/s^2. The force applied is:",
            listOf("5 N", "12 N", "20 N", "40 N"), 2, "F = ma = 10 x 2 = 20 N.", Difficulty.EASY),
        q("q_sci_3", "sub_science", "t_chemistry", "The chemical formula of common salt is:",
            listOf("NaCl", "KCl", "CaCO3", "NaHCO3"), 0, "Common salt is sodium chloride, NaCl.", Difficulty.EASY),
        q("q_sci_4", "sub_science", "t_chemistry", "Washing soda is chemically:",
            listOf("Sodium bicarbonate", "Sodium carbonate", "Calcium oxide", "Calcium carbonate"), 1,
            "Washing soda is sodium carbonate, Na2CO3. Baking soda is the bicarbonate.", Difficulty.MEDIUM),
        q("q_sci_5", "sub_science", "t_human_body", "Which cells in blood carry oxygen?",
            listOf("White blood cells", "Red blood cells", "Platelets", "Plasma cells"), 1,
            "Red blood cells carry oxygen using haemoglobin.", Difficulty.EASY),
        q("q_sci_6", "sub_science", "t_human_body", "Deficiency of vitamin C causes:",
            listOf("Rickets", "Scurvy", "Night blindness", "Beriberi"), 1,
            "Scurvy results from a lack of vitamin C.", Difficulty.EASY),
        q("q_sci_7", "sub_science", "t_plants_animals", "During photosynthesis plants absorb:",
            listOf("Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"), 2,
            "Plants absorb carbon dioxide and release oxygen.", Difficulty.EASY),
        q("q_sci_8", "sub_science", "t_environment", "Which of these is a greenhouse gas?",
            listOf("Nitrogen", "Methane", "Argon", "Neon"), 1,
            "Methane is a potent greenhouse gas, as is carbon dioxide.", Difficulty.EASY),

        // ---- history
        q("q_hist_1", "sub_history", "t_ancient", "Mohenjo-daro is situated on the bank of which river?",
            listOf("Ravi", "Indus", "Ganga", "Sutlej"), 1, "Mohenjo-daro is on the Indus; Harappa is on the Ravi.", Difficulty.MEDIUM),
        q("q_hist_2", "sub_history", "t_ancient", "Ashoka belonged to which dynasty?",
            listOf("Gupta", "Maurya", "Kushana", "Satavahana"), 1, "Ashoka was a Mauryan emperor.", Difficulty.EASY),
        q("q_hist_3", "sub_history", "t_ancient", "Which is the oldest of the four Vedas?",
            listOf("Samaveda", "Yajurveda", "Rigveda", "Atharvaveda"), 2, "The Rigveda is the oldest Veda.", Difficulty.EASY),
        q("q_hist_4", "sub_history", "t_medieval", "The first battle of Panipat was fought in:",
            listOf("1526", "1556", "1761", "1498"), 0, "Babur defeated Ibrahim Lodi in 1526.", Difficulty.MEDIUM),
        q("q_hist_5", "sub_history", "t_medieval", "Who founded the Mughal empire in India?",
            listOf("Akbar", "Humayun", "Babur", "Aurangzeb"), 2, "Babur founded the Mughal empire after Panipat, 1526.", Difficulty.EASY),
        q("q_hist_6", "sub_history", "t_modern", "The Battle of Plassey was fought in:",
            listOf("1757", "1764", "1857", "1772"), 0, "Plassey was in 1757; Buxar followed in 1764.", Difficulty.MEDIUM),
        q("q_hist_7", "sub_history", "t_freedom", "The Jallianwala Bagh massacre took place in:",
            listOf("1909", "1919", "1929", "1942"), 1, "It occurred on 13 April 1919 at Amritsar.", Difficulty.EASY),
        q("q_hist_8", "sub_history", "t_freedom", "The Quit India Movement was launched in:",
            listOf("1930", "1935", "1942", "1945"), 2, "The Quit India Movement began in August 1942.", Difficulty.EASY),
        q("q_hist_9", "sub_history", "t_freedom", "The Indian National Congress was founded in:",
            listOf("1875", "1885", "1895", "1905"), 1, "The Congress was founded in 1885 in Bombay.", Difficulty.EASY),
        q("q_hist_10", "sub_history", "t_modern", "The Revolt of 1857 began at:",
            listOf("Delhi", "Meerut", "Kanpur", "Jhansi"), 1, "The revolt began at Meerut in May 1857.", Difficulty.MEDIUM),

        // ---- geography
        q("q_geo_1", "sub_geography", "t_rivers", "Which is the longest river flowing within India?",
            listOf("Godavari", "Ganga", "Krishna", "Narmada"), 1, "The Ganga is the longest river within India.", Difficulty.EASY),
        q("q_geo_2", "sub_geography", "t_rivers", "Which river is known as the Dakshina Ganga?",
            listOf("Krishna", "Kaveri", "Godavari", "Mahanadi"), 2, "The Godavari, the longest peninsular river, is called Dakshina Ganga.", Difficulty.MEDIUM),
        q("q_geo_3", "sub_geography", "t_rivers", "Which two major rivers flow westward into the Arabian Sea?",
            listOf("Ganga and Yamuna", "Narmada and Tapi", "Krishna and Kaveri", "Godavari and Mahanadi"), 1,
            "The Narmada and Tapi flow west through rift valleys and form estuaries.", Difficulty.MEDIUM),
        q("q_geo_4", "sub_geography", "t_physical", "The highest mountain peak located within India is:",
            listOf("Mount Everest", "Kangchenjunga", "Nanda Devi", "Anai Mudi"), 1,
            "Kangchenjunga is the highest peak within India.", Difficulty.MEDIUM),
        q("q_geo_5", "sub_geography", "t_physical", "The Deccan plateau is mainly composed of:",
            listOf("Basalt", "Granite", "Limestone", "Sandstone"), 0,
            "The Deccan traps are basaltic lava flows.", Difficulty.MEDIUM),
        q("q_geo_6", "sub_geography", "t_climate", "The south-west monsoon normally reaches Kerala around:",
            listOf("1 May", "1 June", "1 July", "15 July"), 1, "The monsoon onset over Kerala is around 1 June.", Difficulty.EASY),
        q("q_geo_7", "sub_geography", "t_climate", "Tamil Nadu receives most of its rainfall from:",
            listOf("South-west monsoon", "Retreating north-east monsoon", "Western disturbances", "Local thunderstorms"), 1,
            "The retreating monsoon brings October-November rain to the Tamil Nadu coast.", Difficulty.MEDIUM),
        q("q_geo_8", "sub_geography", "t_world_geo", "The Tropic of Cancer passes through how many Indian states?",
            listOf("6", "7", "8", "9"), 2, "It passes through 8 Indian states.", Difficulty.HARD),
        q("q_geo_9", "sub_geography", "t_world_geo", "Which is the largest and deepest ocean?",
            listOf("Atlantic", "Indian", "Pacific", "Arctic"), 2, "The Pacific is the largest and deepest ocean.", Difficulty.EASY),

        // ---- polity
        q("q_pol_1", "sub_polity", "t_frights", "Fundamental Rights are contained in which part of the Constitution?",
            listOf("Part II", "Part III", "Part IV", "Part V"), 1, "Part III, Articles 12 to 35.", Difficulty.EASY),
        q("q_pol_2", "sub_polity", "t_frights", "Which article guarantees equality before the law?",
            listOf("Article 14", "Article 19", "Article 21", "Article 32"), 0, "Article 14 guarantees equality before law.", Difficulty.MEDIUM),
        q("q_pol_3", "sub_polity", "t_frights", "The right to constitutional remedies is provided by:",
            listOf("Article 19", "Article 21", "Article 32", "Article 44"), 2,
            "Article 32 lets a citizen move the Supreme Court directly.", Difficulty.MEDIUM),
        q("q_pol_4", "sub_polity", "t_constitution", "The words 'socialist' and 'secular' were added to the Preamble by which amendment?",
            listOf("24th", "42nd", "44th", "73rd"), 1, "The 42nd Amendment, 1976, added them.", Difficulty.MEDIUM),
        q("q_pol_5", "sub_polity", "t_constitution", "The Constitution of India came into force on:",
            listOf("15 August 1947", "26 November 1949", "26 January 1950", "26 January 1949"), 2,
            "It was adopted on 26 November 1949 and came into force on 26 January 1950.", Difficulty.EASY),
        q("q_pol_6", "sub_polity", "t_parliament", "A money bill can be introduced only in:",
            listOf("Rajya Sabha", "Lok Sabha", "Either house", "A joint sitting"), 1,
            "Money bills originate only in the Lok Sabha.", Difficulty.MEDIUM),
        q("q_pol_7", "sub_polity", "t_judiciary", "A judge of the Supreme Court of India retires at the age of:",
            listOf("60", "62", "65", "70"), 2, "Supreme Court judges retire at 65; High Court judges at 62.", Difficulty.MEDIUM),
        q("q_pol_8", "sub_polity", "t_panchayat", "Panchayati Raj institutions received constitutional status through:",
            listOf("The 42nd Amendment", "The 44th Amendment", "The 73rd Amendment", "The 74th Amendment"), 2,
            "The 73rd Amendment Act, 1992.", Difficulty.MEDIUM),
        q("q_pol_9", "sub_polity", "t_panchayat", "The Eleventh Schedule lists how many subjects for panchayats?",
            listOf("18", "24", "29", "32"), 2, "The Eleventh Schedule lists 29 subjects.", Difficulty.HARD),

        // ---- economy
        q("q_eco_1", "sub_economy", "t_banking", "The Reserve Bank of India was established in:",
            listOf("1921", "1935", "1949", "1955"), 1, "The RBI was established in 1935 and nationalised in 1949.", Difficulty.MEDIUM),
        q("q_eco_2", "sub_economy", "t_banking", "The repo rate in India is decided by:",
            listOf("The Finance Ministry", "The Monetary Policy Committee of the RBI", "SEBI", "NITI Aayog"), 1,
            "The RBI's Monetary Policy Committee sets the repo rate.", Difficulty.MEDIUM),
        q("q_eco_3", "sub_economy", "t_eco_basics", "GDP stands for:",
            listOf("Gross Domestic Product", "Gross Development Product", "General Domestic Produce", "Gross Direct Product"), 0,
            "GDP is Gross Domestic Product.", Difficulty.EASY),
        q("q_eco_4", "sub_economy", "t_eco_basics", "Agriculture belongs to which sector of the economy?",
            listOf("Primary", "Secondary", "Tertiary", "Quaternary"), 0, "Agriculture is part of the primary sector.", Difficulty.EASY),
        q("q_eco_5", "sub_economy", "t_budget", "GST was introduced in India on:",
            listOf("1 April 2016", "1 July 2017", "1 April 2018", "1 January 2017"), 1,
            "GST came into effect on 1 July 2017.", Difficulty.MEDIUM),
        q("q_eco_6", "sub_economy", "t_budget", "Which of these is a direct tax?",
            listOf("GST", "Customs duty", "Income tax", "Excise duty"), 2, "Income tax is a direct tax.", Difficulty.EASY),
        q("q_eco_7", "sub_economy", "t_agriculture", "Wheat is mainly which type of crop?",
            listOf("Kharif", "Rabi", "Zaid", "Plantation"), 1, "Wheat is a rabi crop, sown in winter.", Difficulty.EASY),
        q("q_eco_8", "sub_economy", "t_eco_basics", "NITI Aayog replaced which body?",
            listOf("Finance Commission", "Planning Commission", "Election Commission", "UPSC"), 1,
            "NITI Aayog replaced the Planning Commission in 2015.", Difficulty.MEDIUM),

        // ---- current affairs
        q("q_ca_1", "sub_ca", "t_national_af", "The headquarters of ISRO is located at:",
            listOf("Hyderabad", "Bengaluru", "Thiruvananthapuram", "New Delhi"), 1, "ISRO is headquartered in Bengaluru.", Difficulty.EASY),
        q("q_ca_2", "sub_ca", "t_national_af", "India's Mars orbiter mission is popularly called:",
            listOf("Chandrayaan", "Mangalyaan", "Gaganyaan", "Aditya"), 1, "The Mars Orbiter Mission is called Mangalyaan.", Difficulty.EASY),
        q("q_ca_3", "sub_ca", "t_national_af", "Chandrayaan-3 achieved a soft landing near which part of the Moon?",
            listOf("North pole", "South pole", "Equator", "Far side centre"), 1,
            "Chandrayaan-3 landed in the lunar south polar region in 2023.", Difficulty.MEDIUM),
        q("q_ca_4", "sub_ca", "t_international_af", "Which country hosted the G20 summit in 2023?",
            listOf("Indonesia", "India", "Italy", "Brazil"), 1, "India hosted the 2023 G20 summit in New Delhi.", Difficulty.MEDIUM),
        q("q_ca_5", "sub_ca", "t_international_af", "The World Health Organization is headquartered in:",
            listOf("New York", "Geneva", "Paris", "Vienna"), 1, "The WHO is headquartered in Geneva.", Difficulty.EASY),
        q("q_ca_6", "sub_ca", "t_international_af", "SAARC is headquartered in:",
            listOf("New Delhi", "Colombo", "Kathmandu", "Dhaka"), 2, "The SAARC secretariat is in Kathmandu.", Difficulty.MEDIUM),
        q("q_ca_7", "sub_ca", "t_sports_awards", "National Sports Day in India is observed on:",
            listOf("29 August", "5 September", "2 October", "14 November"), 0,
            "It marks the birth anniversary of Dhyan Chand, 29 August.", Difficulty.MEDIUM),
        q("q_ca_8", "sub_ca", "t_sports_awards", "The Ranji Trophy is associated with:",
            listOf("Hockey", "Football", "Cricket", "Badminton"), 2, "The Ranji Trophy is domestic cricket.", Difficulty.EASY),

        // ---- general knowledge
        q("q_gk_1", "sub_gk", "t_culture", "The national animal of India is:",
            listOf("Lion", "Tiger", "Elephant", "Peacock"), 1, "The Bengal tiger is the national animal.", Difficulty.EASY),
        q("q_gk_2", "sub_gk", "t_culture", "Kuchipudi is a classical dance form of:",
            listOf("Tamil Nadu", "Kerala", "Andhra Pradesh", "Odisha"), 2, "Kuchipudi originated in Andhra Pradesh.", Difficulty.EASY),
        q("q_gk_3", "sub_gk", "t_culture", "The Ajanta caves are located in:",
            listOf("Madhya Pradesh", "Maharashtra", "Karnataka", "Odisha"), 1, "Ajanta and Ellora are in Maharashtra.", Difficulty.MEDIUM),
        q("q_gk_4", "sub_gk", "t_books", "Who wrote 'The Discovery of India'?",
            listOf("Mahatma Gandhi", "Jawaharlal Nehru", "B R Ambedkar", "Sarojini Naidu"), 1,
            "It was written by Jawaharlal Nehru.", Difficulty.EASY),
        q("q_gk_5", "sub_gk", "t_books", "The Arthashastra was written by:",
            listOf("Kalidasa", "Kautilya", "Panini", "Banabhatta"), 1, "The Arthashastra is attributed to Kautilya.", Difficulty.MEDIUM),
        q("q_gk_6", "sub_gk", "t_books", "The Indian national anthem was written by:",
            listOf("Bankim Chandra Chatterjee", "Rabindranath Tagore", "Sarojini Naidu", "Subramania Bharati"), 1,
            "Jana Gana Mana was written by Rabindranath Tagore.", Difficulty.EASY),
        q("q_gk_7", "sub_gk", "t_days", "World Environment Day is observed on:",
            listOf("22 April", "5 June", "16 September", "1 December"), 1, "World Environment Day is 5 June.", Difficulty.EASY),
        q("q_gk_8", "sub_gk", "t_days", "The highest civilian award of India is:",
            listOf("Padma Shri", "Padma Bhushan", "Padma Vibhushan", "Bharat Ratna"), 3,
            "Bharat Ratna is the highest civilian award.", Difficulty.EASY),

        // ---- english
        q("q_eng_1", "sub_english", "t_synonyms", "Choose the synonym of ABUNDANT:",
            listOf("Scarce", "Plentiful", "Fragile", "Hesitant"), 1, "Abundant means plentiful.", Difficulty.EASY),
        q("q_eng_2", "sub_english", "t_synonyms", "Choose the antonym of ANCIENT:",
            listOf("Old", "Modern", "Historic", "Classic"), 1, "The opposite of ancient is modern.", Difficulty.EASY),
        q("q_eng_3", "sub_english", "t_synonyms", "Choose the synonym of BENEVOLENT:",
            listOf("Cruel", "Kind", "Careless", "Rigid"), 1, "Benevolent means kind and generous.", Difficulty.MEDIUM),
        q("q_eng_4", "sub_english", "t_grammar", "Choose the grammatically correct sentence:",
            listOf(
                "One of my friends is a doctor.",
                "One of my friends are a doctor.",
                "One of my friend is a doctor.",
                "One of my friends were a doctor."
            ), 0, "'One of' takes a plural noun and a singular verb.", Difficulty.MEDIUM),
        q("q_eng_5", "sub_english", "t_grammar", "Fill in the blank: He ______ here since 2019.",
            listOf("works", "worked", "has been working", "is working"), 2,
            "'Since' with a point in time takes the present perfect continuous.", Difficulty.MEDIUM),
        q("q_eng_6", "sub_english", "t_sentence", "Change to passive voice: 'She writes a letter.'",
            listOf(
                "A letter is written by her.",
                "A letter was written by her.",
                "A letter is being written by her.",
                "A letter has written by her."
            ), 0, "Simple present passive: is/are + past participle.", Difficulty.MEDIUM),
        q("q_eng_7", "sub_english", "t_sentence", "The correct plural of CRISIS is:",
            listOf("Crisises", "Crises", "Crisies", "Crisis"), 1, "Crisis becomes crises in the plural.", Difficulty.MEDIUM),
        q("q_eng_8", "sub_english", "t_vocabulary", "'Once in a blue moon' means:",
            listOf("Very frequently", "Very rarely", "At night only", "Without warning"), 1,
            "The idiom means something that happens very rarely.", Difficulty.EASY),
        q("q_eng_9", "sub_english", "t_vocabulary", "One word for 'a person who loves books':",
            listOf("Bibliophile", "Bibliography", "Philatelist", "Numismatist"), 0,
            "Biblio means book and phile means lover.", Difficulty.MEDIUM),

        // ---- reasoning
        q("q_rea_1", "sub_reasoning", "t_coding", "If CAT is coded as 3-1-20, then DOG is coded as:",
            listOf("4-15-7", "4-14-7", "3-15-7", "4-15-8"), 0, "Letters are replaced by alphabet positions: D=4, O=15, G=7.", Difficulty.EASY),
        q("q_rea_2", "sub_reasoning", "t_coding", "If in a code MADRAS is written as NBESBT, then DELHI is written as:",
            listOf("EFMIJ", "EFMJI", "FEMIJ", "EMFIJ"), 0, "Each letter moves one step forward.", Difficulty.MEDIUM),
        q("q_rea_3", "sub_reasoning", "t_series", "Complete the letter series: AZ, BY, CX, ?",
            listOf("DV", "DW", "EW", "DX"), 1, "The first letter moves forward and the second moves backward, giving DW.", Difficulty.MEDIUM),
        q("q_rea_4", "sub_reasoning", "t_series", "Find the odd one out: 3, 5, 7, 9, 11",
            listOf("3", "7", "9", "11"), 2, "All are prime except 9, which is 3 x 3.", Difficulty.EASY),
        q("q_rea_5", "sub_reasoning", "t_blood", "A is the father of B, and B is the mother of C. How is A related to C?",
            listOf("Father", "Grandfather", "Uncle", "Brother"), 1, "A is two generations above C, so A is C's grandfather.", Difficulty.EASY),
        q("q_rea_6", "sub_reasoning", "t_blood", "P is Q's brother, R is Q's mother. How is P related to R?",
            listOf("Son", "Brother", "Father", "Nephew"), 0, "P is Q's brother, so R's child - her son.", Difficulty.MEDIUM),
        q("q_rea_7", "sub_reasoning", "t_direction", "Facing north, a person turns right twice. Which direction is he facing now?",
            listOf("North", "East", "South", "West"), 2, "Two right turns of 90 degrees each reverse the direction to south.", Difficulty.EASY),
        q("q_rea_8", "sub_reasoning", "t_direction", "A man walks 3 km east, then 4 km north. How far is he from the start?",
            listOf("5 km", "6 km", "7 km", "1 km"), 0, "By Pythagoras, the distance is the square root of 9 + 16 = 5 km.", Difficulty.MEDIUM)
    )

    /** The complete question bank: handcrafted items plus generated arithmetic sets. */
    val questions: List<Question> = handcrafted +
        previousPapers() +
        percentageQuestions() +
        profitLossQuestions() +
        averageQuestions() +
        timeWorkQuestions() +
        ratioQuestions() +
        numberSystemQuestions() +
        seriesQuestions()

    // =================================================================== quizzes

    /**
     * One topic quiz per topic that has questions. Short topics are padded with
     * other questions from the same subject so every quiz is worth attempting.
     */
    /**
     * Quiz sets per topic. A topic does not get one fixed quiz — it gets as
     * many sets as its bank supports, and each set is whatever size falls out
     * of the split (they are deliberately uneven, like real practice sets).
     * Previously-asked questions are kept as their own set.
     */
    private val SET_SIZES = listOf(5, 3, 6, 4, 7)

    val quizzes: List<Quiz> = questions
        .groupBy { it.topicId }
        .flatMap { (topicId, topicQuestions) ->
            val subjectId = topicQuestions.first().subjectId
            val sample = topicQuestions.filter { it.source == QuestionSource.SAMPLE }
            val past = topicQuestions.filter { it.source == QuestionSource.PREVIOUS }

            // a thin topic borrows from its subject so a set is still worth sitting
            val filler = questions.filter {
                it.subjectId == subjectId && it.topicId != topicId && it.source == QuestionSource.SAMPLE
            }
            val pool = if (sample.size >= 3) sample else sample + filler.take(6 - sample.size)

            val sets = mutableListOf<Quiz>()
            var index = 0
            var cursor = 0
            while (cursor < pool.size && index < SET_SIZES.size) {
                val size = SET_SIZES[index % SET_SIZES.size]
                val slice = pool.drop(cursor).take(size)
                if (slice.isEmpty()) break
                // a trailing scrap of one question is folded into the previous set
                if (slice.size == 1 && sets.isNotEmpty()) {
                    val last = sets.removeAt(sets.lastIndex)
                    sets.add(last.copy(questionIds = last.questionIds + slice.map { it.id }))
                } else {
                    index++
                    sets.add(
                        Quiz(
                            id = "quiz_" + topicId + "_s" + index,
                            subjectId = subjectId,
                            topicId = topicId,
                            title = lt("Set " + index, "సెట్ " + index),
                            questionIds = slice.map { it.id },
                            durationMinutes = if (slice.size <= 4) 5 else 10
                        )
                    )
                }
                cursor += slice.size
            }

            if (past.isNotEmpty()) {
                sets.add(
                    Quiz(
                        id = "quiz_" + topicId + "_past",
                        subjectId = subjectId,
                        topicId = topicId,
                        title = lt("Previously asked", "ఇంతకుముందు అడిగినవి"),
                        questionIds = past.map { it.id },
                        durationMinutes = 5
                    )
                )
            }
            sets
        }
}
