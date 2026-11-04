package ru.apocalypseassistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyCoreTest {
    @Test
    fun unknownAndPaidActionsAreBlocked() {
        assertFalse(
            CurrencyGuard.mayEnter(
                GameActivity("x", "x", CostStatus.UNKNOWN, ActivityStatus.READY, "")
            )
        )
        assertFalse(
            CurrencyGuard.mayEnter(
                GameActivity("x", "x", CostStatus.PAID, ActivityStatus.READY, "")
            )
        )
    }

    @Test
    fun onlyConfirmedFreeReadyActionIsAllowed() {
        assertTrue(
            CurrencyGuard.mayEnter(
                GameActivity("x", "x", CostStatus.FREE_CONFIRMED, ActivityStatus.READY, "")
            )
        )
        assertFalse(
            CurrencyGuard.mayEnter(
                GameActivity("x", "x", CostStatus.FREE_CONFIRMED, ActivityStatus.COOLDOWN, "")
            )
        )
    }

    @Test
    fun currencyDecreaseTriggersStopSignal() {
        val before = BalanceSnapshot(diamonds = 100, gold = 500)
        val after = BalanceSnapshot(diamonds = 99, gold = 500)
        assertTrue(CurrencyGuard.balanceDidDecrease(before, after))
    }

    @Test
    fun missingPreviouslyKnownBalanceTriggersStopSignal() {
        val before = BalanceSnapshot(diamonds = 100, gold = 500)
        val after = BalanceSnapshot(diamonds = null, gold = 500)
        assertTrue(CurrencyGuard.balanceDidDecrease(before, after))
    }

    @Test
    fun missingPreviouslyKnownCurrencyTriggersStopSignal() {
        val before = BalanceSnapshot(otherCurrencies = mapOf("tickets" to 4))
        val after = BalanceSnapshot(otherCurrencies = emptyMap())
        assertTrue(CurrencyGuard.balanceDidDecrease(before, after))
    }
}
