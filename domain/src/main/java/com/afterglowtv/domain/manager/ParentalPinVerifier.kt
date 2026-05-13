package com.afterglowtv.domain.manager

interface ParentalPinVerifier {
    suspend fun verifyParentalPin(pin: String): Boolean
}