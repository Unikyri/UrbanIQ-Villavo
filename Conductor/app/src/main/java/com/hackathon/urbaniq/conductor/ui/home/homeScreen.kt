package com.hackathon.urbaniq.conductor.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isAvailable by viewModel.isAvailable.collectAsState()
    val conductorName by viewModel.conductorName.collectAsState()
    val vehiculoPlaca by viewModel.vehiculoPlaca.collectAsState()
    val pagos by viewModel.pagos.collectAsState()

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadConductorAndVehicle()
        viewModel.loadPagos()
    }

    // Iniciar ubicación si está disponible y permisos dados
    LaunchedEffect(isAvailable) {
        if (isAvailable) {
            val fine = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarse = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fine || coarse) {
                viewModel.startLocationUpdates(context)
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Info conductor y vehículo
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Conductor: $conductorName", color = Color.Black)
                    Text("Vehículo: $vehiculoPlaca", color = Color.Black)
                    Text(
                        text = if (isAvailable) "Estado: En Ruta ✅" else "Estado: No Disponible ❌",
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón cambiar estado
            Button(
                onClick = { viewModel.toggleAvailability() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107023))
            ) {
                Text(
                    text = if (isAvailable) "Finalizar Ruta" else "Iniciar Ruta",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón cerrar sesión
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
            ) {
                Text("Cerrar Sesión", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Historial de pagos
            Text(
                text = "Historial de Pagos",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(pagos) { pago ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monto: \$${pago.monto}", color = Color.Black)
                            Text("Fecha: ${pago.fecha}", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
