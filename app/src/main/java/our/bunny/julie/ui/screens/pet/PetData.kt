package our.bunny.julie.ui.screens.pet

object PetData {
    val species = listOf("Dog", "Cat", "Rabbit", "Guinea Pig", "Mouse", "Bird")
    
    fun getBreedsForSpecies(species: String): List<String> {
        return when (species) {
            "Dog" -> listOf("Mixed", "Labrador Retriever", "German Shepherd", "Golden Retriever", "French Bulldog", "Bulldog", "Poodle", "Beagle", "Rottweiler", "Dachshund", "Undetermined")
            "Cat" -> listOf("Mixed", "Persian", "Maine Coon", "Ragdoll", "Siamese", "British Shorthair", "Sphynx", "Abyssinian", "Scottish Fold", "Bengal", "Undetermined")
            "Rabbit" -> listOf("Holland Lop", "Rex", "Lionhead", "Flemish Giant", "Netherland Dwarf", "Undetermined")
            "Guinea Pig" -> listOf("American", "Abyssinian", "Peruvian", "Silkie", "Teddy", "Undetermined")
            "Mouse" -> listOf("Fancy Mouse", "Spiny Mouse", "Zebra Mouse", "Deer Mouse", "Undetermined")
            "Bird" -> listOf("Parakeet (Budgie)", "Cockatiel", "Finch", "Canary", "Parrotlet", "Lovebird", "Cockatoo", "Undetermined")
            else -> listOf("Undetermined")
        }
    }
    
    val sexes = listOf("Male", "Female", "Undetermined")

    fun getEmojiForSpecies(species: String): String {
        return when (species) {
            "Dog" -> "🐶"
            "Cat" -> "🐱"
            "Rabbit" -> "🐰"
            "Guinea Pig" -> "🐹"
            "Mouse" -> "🐭"
            "Bird" -> "🐦"
            else -> "🐾"
        }
    }
}
