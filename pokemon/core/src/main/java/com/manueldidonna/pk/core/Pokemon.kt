package com.manueldidonna.pk.core

/**
 * [Pokemon] represents a read-only entity. To edit pokemon data use [PokemonWriter]
 */
interface Pokemon {
    val position: Position

    data class Position(
        val box: Int,
        val slot: Int
    )

    val trainerId: UInt
    val trainerName: String

    val speciesId: Int
    val nickname: String
    val level: Int
    val experiencePoints: Int
    val natureId: Int

    val iV: IndividualValues

    interface IndividualValues {
        val health: Int
        val attack: Int
        val defense: Int
        val specialAttack: Int
        val specialDefense: Int
        val speed: Int
    }
}