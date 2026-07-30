package our.bunny.julie.util

object SearchUtil {
    fun fuzzyMatches(query: String, target: String): Boolean {
        if (query.isBlank()) return true
        if (target.isBlank()) return false

        // Fast path: exact substring match (case-insensitive)
        if (target.contains(query, ignoreCase = true)) return true

        // Fall back to Levenshtein distance on words
        val queryWords = query.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val targetWords = target.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        
        if (queryWords.isEmpty() || targetWords.isEmpty()) return false

        // For each query word, see if it matches ANY target word within the distance threshold
        return queryWords.all { qWord ->
            targetWords.any { tWord ->
                val distance = levenshtein(qWord, tWord)
                val threshold = if (tWord.length <= 5) 1 else 2
                distance <= threshold
            }
        }
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        
        var cost = IntArray(rhsLength + 1) { it }
        var newCost = IntArray(rhsLength + 1)

        for (i in 1..lhsLength) {
            newCost[0] = i
            for (j in 1..rhsLength) {
                val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[rhsLength]
    }
}
