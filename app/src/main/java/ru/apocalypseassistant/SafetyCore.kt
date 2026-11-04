package ru.apocalypseassistant

enum class CostStatus { FREE_CONFIRMED, PAID, UNKNOWN }
enum class ActivityStatus { READY, COOLDOWN, LOCKED, UNKNOWN }

data class GameActivity(
    val id: String,
    val title: String,
    val cost: CostStatus,
    val status: ActivityStatus,
    val note: String
)

data class BalanceSnapshot(
    val diamonds: Long? = null,
    val gold: Long? = null,
    val otherCurrencies: Map<String, Long> = emptyMap()
)

data class PlannedAction(val activityId: String, val explanation: String)

object CurrencyGuard {
    /**
     * Fail-closed policy: only explicitly marked FREE_CONFIRMED + READY activities
     * are shown as candidates. This does not itself authorize or perform game input.
     */
    fun mayEnter(activity: GameActivity): Boolean =
        activity.cost == CostStatus.FREE_CONFIRMED &&
            activity.status == ActivityStatus.READY

    /**
     * Returns true when a known balance decreases OR a previously observed currency
     * disappears from the next snapshot. Missing data must be treated as unsafe.
     */
    fun balanceDidDecrease(before: BalanceSnapshot, after: BalanceSnapshot): Boolean {
        if (before.diamonds != null &&
            (after.diamonds == null || after.diamonds < before.diamonds)
        ) return true

        if (before.gold != null &&
            (after.gold == null || after.gold < before.gold)
        ) return true

        return before.otherCurrencies.any { (name, oldValue) ->
            val newValue = after.otherCurrencies[name]
            newValue == null || newValue < oldValue
        }
    }
}

object ActivityPlanner {
    fun plan(activities: List<GameActivity>): List<PlannedAction> =
        activities.filter(CurrencyGuard::mayEnter).map {
            PlannedAction(it.id, "Отмечено как бесплатное; активность готова")
        }
}
