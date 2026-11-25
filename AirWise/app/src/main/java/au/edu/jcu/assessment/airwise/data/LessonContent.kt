package au.edu.jcu.assessment.airwise.data

import au.edu.jcu.assessment.airwise.model.Lesson
import au.edu.jcu.assessment.airwise.model.LessonSection
import au.edu.jcu.assessment.airwise.model.Pollutant
import au.edu.jcu.assessment.airwise.model.QuizQuestion
import au.edu.jcu.assessment.airwise.model.Topic

/**
 * Built-in learning content. Facts are based on the US EPA AirNow "AQI Basics"
 * guidance and EPA particle pollution resources.
 */
object LessonContent {

    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "aqi-basics",
            topic = Topic.AQI_BASICS,
            title = "What is the Air Quality Index?",
            summary = "How one number from 0 to 500 sums up the air you breathe.",
            minutes = 3,
            sections = listOf(
                LessonSection(
                    "One number for many pollutants",
                    "The Air Quality Index (AQI) turns measurements of several pollutants into a single number from 0 to 500. The higher the number, the more polluted the air and the greater the health concern."
                ),
                LessonSection(
                    "Six categories",
                    "The US AQI has six categories: Good (0–50), Moderate (51–100), Unhealthy for sensitive groups (101–150), Unhealthy (151–200), Very unhealthy (201–300) and Hazardous (301 and above). Each has a colour, but the name is what matters."
                ),
                LessonSection(
                    "Why 100 matters",
                    "An AQI of 100 matches the national health standard for a pollutant. Above 100, air quality is first unhealthy for sensitive groups, then for everyone as values rise."
                )
            )
        ),
        Lesson(
            id = "pm25",
            topic = Topic.PARTICLES,
            title = "PM2.5: fine particles",
            summary = "Tiny particles that travel deep into your lungs.",
            minutes = 3,
            sections = listOf(
                LessonSection(
                    "How small is small?",
                    "PM2.5 means particles 2.5 micrometres across or smaller, about 30 times thinner than a human hair. They are too small to see one by one."
                ),
                LessonSection(
                    "Where it comes from",
                    "Common sources include vehicle exhaust, bushfire smoke, wood heaters, industry and power generation."
                ),
                LessonSection(
                    "Why it matters",
                    "Because they are so small, fine particles can travel deep into the lungs and some can enter the bloodstream. They are linked to heart and lung problems."
                )
            ),
            livePollutant = Pollutant.PM25
        ),
        Lesson(
            id = "pm10",
            topic = Topic.PARTICLES,
            title = "PM10: coarse particles",
            summary = "Dust, pollen and other larger particles.",
            minutes = 2,
            sections = listOf(
                LessonSection(
                    "What counts as PM10",
                    "PM10 means particles 10 micrometres or smaller. It includes the fine PM2.5 particles plus larger ones such as dust, pollen and mould."
                ),
                LessonSection(
                    "Sources and effects",
                    "Roads, construction, farming and windblown dust are common sources. Coarse particles mostly affect the nose, throat and upper airways."
                )
            ),
            livePollutant = Pollutant.PM10
        ),
        Lesson(
            id = "ozone",
            topic = Topic.GASES,
            title = "Ground-level ozone",
            summary = "Good up high, harmful near the ground.",
            minutes = 3,
            sections = listOf(
                LessonSection(
                    "Made by sunlight",
                    "Ground-level ozone is not released directly. It forms when nitrogen oxides and volatile organic compounds react in sunlight, so levels are often highest on hot, sunny afternoons."
                ),
                LessonSection(
                    "Good up high, bad nearby",
                    "Ozone high in the atmosphere protects us from UV radiation. Near the ground, it irritates the airways and can trigger asthma symptoms."
                )
            ),
            livePollutant = Pollutant.OZONE
        ),
        Lesson(
            id = "no2",
            topic = Topic.GASES,
            title = "Nitrogen dioxide",
            summary = "A traffic pollutant that is highest near busy roads.",
            minutes = 2,
            sections = listOf(
                LessonSection(
                    "Where it comes from",
                    "Nitrogen dioxide (NO₂) comes mainly from burning fuel, especially in cars, trucks and power plants. Levels are usually highest near busy roads."
                ),
                LessonSection(
                    "Health effects",
                    "NO₂ inflames the airways and can make asthma worse. It also helps form ground-level ozone and fine particles."
                )
            ),
            livePollutant = Pollutant.NITROGEN_DIOXIDE
        ),
        Lesson(
            id = "protect",
            topic = Topic.HEALTH,
            title = "Protecting yourself",
            summary = "Simple steps when the air is bad.",
            minutes = 3,
            sections = listOf(
                LessonSection(
                    "Who is most sensitive",
                    "People with asthma or heart and lung disease, children, older adults and people who work or exercise outdoors are most at risk from air pollution."
                ),
                LessonSection(
                    "What you can do",
                    "Check the AQI before outdoor activities. When it is high, choose lighter activities, exercise indoors, keep windows closed during smoke events and use an air purifier with a HEPA filter if you have one."
                ),
                LessonSection(
                    "Not medical advice",
                    "This app gives general information only. If you have a health condition, follow your doctor's plan."
                )
            )
        )
    )

    val questions: List<QuizQuestion> = listOf(
        // AQI basics
        QuizQuestion("q-aqi-1", Topic.AQI_BASICS, "What does a higher AQI value mean?",
            listOf("Cleaner air", "More polluted air", "Higher temperature", "More humidity"), 1,
            "The higher the AQI, the more polluted the air and the greater the health concern."),
        QuizQuestion("q-aqi-2", Topic.AQI_BASICS, "Which AQI range is \"Moderate\"?",
            listOf("0–50", "51–100", "101–150", "151–200"), 1,
            "Moderate is 51–100. Unusually sensitive people may be affected."),
        QuizQuestion("q-aqi-3", Topic.AQI_BASICS, "Above which AQI value does air become unhealthy for sensitive groups?",
            listOf("50", "100", "200", "300"), 1,
            "An AQI of 100 matches the national health standard. Above 100, sensitive groups are affected first."),
        // Particles
        QuizQuestion("q-pm-1", Topic.PARTICLES, "What does the 2.5 in PM2.5 refer to?",
            listOf("Particles 2.5 micrometres or smaller", "2.5 grams of dust", "A 2.5-hour average", "2.5% of the air"), 0,
            "PM2.5 means particles 2.5 micrometres across or smaller."),
        QuizQuestion("q-pm-2", Topic.PARTICLES, "Why are fine particles especially harmful?",
            listOf("They smell strongly", "They reach deep into the lungs", "They are always visible", "They only affect plants"), 1,
            "Fine particles are small enough to travel deep into the lungs, and some enter the bloodstream."),
        QuizQuestion("q-pm-3", Topic.PARTICLES, "Which is a common source of coarse PM10 particles?",
            listOf("Windblown dust", "Mobile phones", "Ozone layer", "Rainwater"), 0,
            "Roads, construction, farming and windblown dust are common PM10 sources."),
        // Gases
        QuizQuestion("q-gas-1", Topic.GASES, "How does ground-level ozone form?",
            listOf("It is released from car exhausts directly", "Pollutants react in sunlight", "It comes from the ocean", "It forms only at night"), 1,
            "Ozone forms when nitrogen oxides and volatile organic compounds react in sunlight."),
        QuizQuestion("q-gas-2", Topic.GASES, "When are ozone levels often highest?",
            listOf("Cold, cloudy mornings", "Hot, sunny afternoons", "Rainy nights", "During snowfall"), 1,
            "Sunlight drives ozone formation, so hot, sunny afternoons often have the highest levels."),
        QuizQuestion("q-gas-3", Topic.GASES, "Where is nitrogen dioxide usually highest?",
            listOf("Near busy roads", "In national parks", "Over the open ocean", "High in the atmosphere"), 0,
            "NO₂ comes mainly from burning fuel, especially traffic, so it peaks near busy roads."),
        // Health
        QuizQuestion("q-health-1", Topic.HEALTH, "Which group is most sensitive to air pollution?",
            listOf("People with asthma", "People who wear glasses", "People who drink coffee", "Nobody is more sensitive"), 0,
            "People with asthma or heart and lung disease, children and older adults are most sensitive."),
        QuizQuestion("q-health-2", Topic.HEALTH, "What should you do during a bushfire smoke event?",
            listOf("Open all windows", "Go for a long run", "Keep windows closed and stay indoors", "Ignore the AQI"), 2,
            "Keeping windows closed and staying indoors reduces how much smoke you breathe."),
        QuizQuestion("q-health-3", Topic.HEALTH, "Which device can help clean indoor air?",
            listOf("A HEPA air purifier", "A ceiling light", "A phone charger", "A clock"), 0,
            "Air purifiers with HEPA filters can remove fine particles from indoor air.")
    )
}
