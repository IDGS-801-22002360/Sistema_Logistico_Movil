package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.models.User

/**
 * Simple in-memory session holder for the currently authenticated user.
 * This keeps the logged-in user available across composable destinations that
 * may create new ViewModel instances per NavBackStackEntry.
 */
object SessionManager {
    @Volatile
    var currentUser: User? = null
}
