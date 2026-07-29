package our.bunny.julie.ui.screens.pet

object PetData {
    val species = listOf("Dog", "Cat", "Rabbit", "Bunny", "Guinea Pig", "Mouse", "Bird", "Other")
    
    fun getBreedsForSpecies(species: String): List<String> {
        val list = when (species) {
            "Dog" -> listOf("Labrador", "Poodle", "Bulldog", "Beagle", "Rottweiler", "Golden Retriever", "German Shepherd")
            "Cat" -> listOf("Persian", "Maine Coon", "Siamese", "Ragdoll", "Sphynx", "British Shorthair")
            "Rabbit", "Bunny" -> listOf("Holland Lop", "Rex", "Lionhead", "Flemish Giant", "Netherland Dwarf")
            "Guinea Pig" -> listOf("American", "Abyssinian", "Peruvian", "Silkie", "Teddy")
            "Bird" -> listOf("Parrot", "Canary", "Finch", "Cockatiel", "Lovebird", "Macaw")
            else -> emptyList()
        }
        return list + "Undetermined"
    }
    
    val sexes = listOf("Male", "Female", "Undetermined")
}
