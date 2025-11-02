package com.example.crm_logistico_movil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.ClientRepository

data class ClientDashboardUiState(
    val isLoading: Boolean = false,
    val clientSummary: ClientSummary? = null, // <-- Usa el ClientSummary correcto
    val recentOperations: List<OperationExtended> = emptyList(),
    val quoteRequests: List<SolicitudCotizacionExtended> = emptyList(),
    val recentInvoices: List<FacturaExtended> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ClientDashboardViewModel : ViewModel() {

    private val clientRepository = ClientRepository()

    private val _uiState = MutableStateFlow(ClientDashboardUiState())
    val uiState: StateFlow<ClientDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboardData(clientId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Cargar resumen del cliente
                val summaryResponse = clientRepository.getClientSummary(clientId)

                // Cargar operaciones recientes (máximo 3)
                val operationsResponse = clientRepository.getClientOperations(clientId, limit = 3)

                // Cargar solicitudes de cotización (máximo 5)
                val requestsResponse = clientRepository.getClientQuoteRequests(clientId, limit = 5) // <-- Este es el que se usa en UI

                // Cargar facturas recientes (máximo 3)
                val invoicesResponse = clientRepository.getClientInvoices(clientId, limit = 3)

                // Si necesitas cargar cotizaciones (CotizacionExtended) por separado, descomenta y úsalo
                // val quotesResponse = clientRepository.getClientQuotes(clientId, limit = 3)


                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    clientSummary = summaryResponse.data,
                    recentOperations = operationsResponse.data ?: emptyList(),
                    quoteRequests = requestsResponse.data ?: emptyList(), // Asignación corregida aquí
                    recentInvoices = invoicesResponse.data ?: emptyList(),
                    // Si tienes quotesResponse, asigna aquí:
                    // recentQuotes = quotesResponse.data ?: emptyList(),
                    errorMessage = if (summaryResponse.status != "OK") summaryResponse.message else null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    fun refreshData(clientId: String) {
        loadDashboardData(clientId)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}