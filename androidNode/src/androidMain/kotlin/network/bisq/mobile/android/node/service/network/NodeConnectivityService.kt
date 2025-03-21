package network.bisq.mobile.android.node.service.network

import network.bisq.mobile.android.node.AndroidApplicationService
import network.bisq.mobile.domain.service.network.ConnectivityService

class NodeConnectivityService(private val applicationService: AndroidApplicationService.Provider): ConnectivityService() {

    companion object {
        const val SLOW_PEER_QUANTITY_THRESHOLD = 2
    }

    override fun isConnected(): Boolean {
        val connections = currentConnections()
        log.d { "Connected peers = $connections"}
        return connections > 0
    }

    override suspend fun isSlow(): Boolean {
        // TODO improve impl using ConnectivityService#newRequestRoundTripTime() call needs to be applied when a P2P roundtrip call is done for the
        // parent isSlow impl to work
        return currentConnections() < SLOW_PEER_QUANTITY_THRESHOLD
    }

    private fun currentConnections(): Int {
        var connections = 0
        applicationService.findAllServiceNodes().forEach {
            connections += it.peerGroupManager.get().node.numConnections
        }
        return connections
    }
}