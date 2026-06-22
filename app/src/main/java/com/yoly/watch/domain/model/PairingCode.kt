package com.yoly.watch.domain.model

data class PairingCode(
    val value: String,
    val validForSeconds: Long,
) {
    init {
        require(value.length == 6 && value.all { it.isDigit() }) {
            "Un code de jumelage doit contenir exactement 6 chiffres, reçu: \"$value\""
        }
        require(validForSeconds > 0) {
            "La durée de validité doit être strictement positive, reçu: $validForSeconds"
        }
    }
}
