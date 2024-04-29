package com.sena.lanraragi.database.statsData

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Stats")
data class Stats(
    @PrimaryKey
    var splicingText: String,
    val nameSpace: String,
    val text: String,
    val weight: Int,
) {
    override fun toString(): String {
        return "splicingText: $splicingText, nameSpace: $nameSpace, text: $text, weight: $weight"
    }
}
