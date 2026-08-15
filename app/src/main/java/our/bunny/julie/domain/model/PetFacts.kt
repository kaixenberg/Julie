package our.bunny.julie.domain.model

object PetFacts {
    val facts: Map<String, List<String>> = mapOf(
        "Dog" to listOf(
            "Dogs can see more colors than you might think — they mainly see shades of blue and yellow.",
            "A dog's nose is incredibly powerful — their sense of smell is vastly better than ours.",
            "Dogs communicate with more than barking — body posture, ears, tail, and facial expressions all matter.",
            "Dogs are social animals — regular interaction and mental stimulation are important for their wellbeing.",
            "Every dog is different — breed, age, size, and lifestyle can greatly affect their needs."
        ),
        "Cat" to listOf(
            "Cats sleep a lot — many cats spend around 12–16 hours a day sleeping or resting.",
            "Cats use scent to communicate — rubbing their face against things helps leave their scent.",
            "Purring doesn't always mean happiness — cats can also purr when stressed, frightened, or uncomfortable.",
            "Cats are natural hunters — stalking, chasing, and pouncing are normal behaviors.",
            "Cats often hide signs of illness — knowing your cat's normal behavior makes changes easier to notice."
        ),
        "Rabbit" to listOf(
            "Rabbit teeth never stop growing — they need constant chewing to naturally wear them down.",
            "Hay is a big deal — hay and grass should make up most of a rabbit's diet and help support both digestion and dental health.",
            "Rabbits eat special droppings — caecotrophs are re-ingested as part of their normal digestive process.",
            "Rabbits are social animals — they generally benefit from having a compatible rabbit companion.",
            "A \"binky\" means happiness — rabbits may leap and twist in the air when they're excited or happy.",
            "Rabbits are most active around dawn and dusk — they're naturally crepuscular."
        ),
        "Guinea Pig" to listOf(
            "Guinea pigs can't make their own vitamin C — they need to get it from their diet.",
            "They need hay available all the time — their high-fibre diet supports digestion and helps wear down their continuously growing teeth.",
            "Guinea pigs are very social — they're generally happiest with another compatible guinea pig rather than living alone.",
            "They can be active for much of the day — guinea pigs may be awake and active for up to around 20 hours.",
            "They're not great climbers — their environment should emphasize floor space, tunnels, hiding places, and safe exploration.",
            "Guinea pigs are cavies — that's another common name for them."
        ),
        "Mouse" to listOf(
            "Mice are highly curious animals — exploring, climbing, digging, and investigating are natural behaviors.",
            "Their teeth keep growing — gnawing helps keep their incisors worn down.",
            "Mice are social animals — compatible companionship can be important for their wellbeing.",
            "Mice are most active at night — expect much of their exploring and exercise to happen after dark.",
            "A mouse's whiskers are important sensory tools — they help it navigate and sense its surroundings."
        ),
        "Bird" to listOf(
            "Birds have hollow, lightweight bones — their skeletons are adapted for flight.",
            "Birds have incredibly efficient lungs — their respiratory system is very different from ours.",
            "Many pet birds are highly intelligent — some can learn sounds, words, tricks, and routines.",
            "Birds communicate constantly — vocalizations, body language, feathers, and posture can all carry meaning.",
            "Birds need mental stimulation — foraging, toys, exploration, and interaction help satisfy natural behaviors.",
            "A bird's beak is more than a mouth — it is used for eating, climbing, exploring, grooming, and manipulating objects."
        )
    )

    fun getFactsForSpecies(species: String): List<String> {
        val normalizedSpecies = when (species.lowercase(java.util.Locale.ROOT).trim()) {
            "dog", "puppy" -> "Dog"
            "cat", "kitten" -> "Cat"
            "rabbit" -> "Rabbit"
            "guinea pig" -> "Guinea Pig"
            "mouse", "mice" -> "Mouse"
            "bird", "parrot" -> "Bird"
            else -> "" // Unknown species no longer maps to "Other", just return empty
        }
        return facts[normalizedSpecies] ?: emptyList()
    }

    fun getSpeciesEmoji(species: String) = when (species) {
        "Rabbit" -> "🐰"
        "Dog" -> "🐶"
        "Cat" -> "🐱"
        "Bird" -> "🐦"
        "Guinea Pig" -> "🐹"
        "Mouse" -> "🐭"
        else -> "🐾"
    }

    data class SpeciesImage(val resourceId: Int, val authorName: String, val source: String = "Unsplash")

    fun getSpeciesImage(species: String): SpeciesImage {
        return when (species) {
            "Dog" -> SpeciesImage(our.bunny.julie.R.drawable.fact_dog, "Charles Deluvio")
            "Cat" -> SpeciesImage(our.bunny.julie.R.drawable.fact_cat, "Manja Vitolic")
            "Rabbit" -> SpeciesImage(our.bunny.julie.R.drawable.fact_rabbit, "Satyabratasm")
            "Bird" -> SpeciesImage(our.bunny.julie.R.drawable.fact_bird, "David Clode")
            "Guinea Pig" -> SpeciesImage(our.bunny.julie.R.drawable.fact_guinea_pig, "Bonnie Kittle")
            "Mouse" -> SpeciesImage(our.bunny.julie.R.drawable.fact_mouse, "Ricky Kharawala")
            else -> SpeciesImage(our.bunny.julie.R.drawable.fact_other, "Mikhail Vasilyev")
        }
    }
}
