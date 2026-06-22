package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.CreatePairingCodeRequest
import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingStatusDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException

class RetrofitPairingCodeApi(
    private val service: PairingService,
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val json: Json,
) : PairingCodeApi {

    override suspend fun fetchPairingCode(watchId: String): PairingCodeDto =
        service.createPairingCode(CreatePairingCodeRequest(watchId))

    override fun observeStatus(pairingId: String): Flow<PairingStatusDto> = callbackFlow {
        val request = Request.Builder()
            .url("${baseUrl}pairing/$pairingId/events")
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String,
            ) {
                trySend(json.decodeFromString<PairingStatusDto>(data))
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?,
            ) {
                close(t ?: IOException("SSE stream failed: HTTP ${response?.code}"))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val eventSource = EventSources.createFactory(client).newEventSource(request, listener)
        awaitClose { eventSource.cancel() }
    }
}
