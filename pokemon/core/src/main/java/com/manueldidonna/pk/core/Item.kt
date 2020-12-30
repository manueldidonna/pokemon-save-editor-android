package com.manueldidonna.pk.core

/**
 * An entity stored in the Inventory or held by a Pokemon.
 *
 * @see Inventory
 * @see Item.isEmpty
 */
interface Item {
    val id: Int
    val quantity: Int

    /**
     * Used to represent an immutable [Item].
     */
    data class Immutable(
        override val id: Int,
        override val quantity: Int,
    ) : Item


    /**
     * In this companion object are listed the items which are specific to a game
     * or should be treated specially.
     * Search them typing 'Generation' plus number in Roman Numerals (ex: Generation II)
     */
    companion object {
        /**
         * A Hidden Machine is an item that is used to teach a Pokemon a move.
         * HMs can be used an unlimited number of times and cannot be disposed of.
         */
        fun isHiddenMachine(itemId: Int): Boolean {
            return itemId == 737 || itemId in 420..427
        }

        /**
         * A Technical Machine is an item that can be used to teach a Pokemon a move.
         */
        fun isTechnicalMachine(itemId: Int): Boolean {
            return itemId == 1230 || itemId in 328..419 || itemId in 618..620 || itemId in 690..694
        }

        /**
         * A voucher for obtaining a bicycle from the Bike Shop.
         * Available in R/B/Y (Generation I) & FR/LG (Generation III)
         */
        const val BikeVoucherId = 1590

        /**
         * A held item exclusive to the Generation II games
         */
        const val PinkBowId = 1591

        /**
         * A mysterious ball. Its design appears to be a reference to Pokemon Gold and Silver.
         * It's exclusive to Pokemon Crystal (Generation II)
         */
        const val GSBallId = 1592

        /**
         * Received from the Day-Care Man through the Pokémon Mobile System GB.
         * It's exclusive to Pokemon Crystal JP (Generation II)
         */
        const val EggTicketId = 1593

        /**
         * A consumable held item exclusive to the Generation II games
         */
        const val BerserkGeneId = 1594

        /**
         * A held item exclusive to the Generation II games
         */
        const val PolkadotBowId = 1595

        /**
         * A valuable item exclusive to the Generation II games
         */
        const val BrickPieceId = 1596

        /**
         * It's a type of item exclusive to the Generation II games
         */
        const val NormalBoxId = 1597

        /**
         * It's a type of item exclusive to the Generation II games
         */
        const val GorgeousBoxId = 1598

        /**
         * Mails - Generation II
         */
        const val FlowerMailId = 1599
        const val SurfMailId = 1600
        const val LiteBlueMailId = 1601
        const val PortraitMailId = 1602
        const val LovelyMailId = 1603
        const val EonMailId = 1604
        const val MorphMailId = 1605
        const val BlueSkyMailId = 1606
        const val MusicMailId = 1607
        const val MirageMailId = 1608
    }
}

inline val Item.isEmpty: Boolean get() = quantity <= 0 || id <= 0
