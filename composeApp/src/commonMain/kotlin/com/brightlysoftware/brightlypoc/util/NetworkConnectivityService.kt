package com.brightlysoftware.brightlypoc.util

import dev.tmapps.konnection.ConnectionInfo
import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NetworkConnectivityService {

    private val konnection = Konnection.instance

    // Observe connection type: Wifi, Mobile, Ethernet, null (disconnected)
    val networkConnectionFlow = konnection.observeNetworkConnection()

    // Observe whether connected (boolean)
    val isConnected: Flow<Boolean> = konnection.observeHasConnection()

    // Convenience getter for IP info snapshot
    suspend fun getConnectionInfo(): ConnectionInfo? = konnection.getInfo()
    suspend fun isCurrentlyConnected(): Boolean = isConnected.first()
}