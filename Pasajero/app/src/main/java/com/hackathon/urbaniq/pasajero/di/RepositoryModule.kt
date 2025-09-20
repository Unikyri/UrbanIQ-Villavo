package com.hackathon.urbaniq.pasajero.di

import com.hackathon.urbaniq.pasajero.data.repository.AuthRepositoryImpl
import com.hackathon.urbaniq.pasajero.data.repository.MockAuthRepositoryImpl
import com.hackathon.urbaniq.pasajero.data.repository.LocationRepositoryImpl
import com.hackathon.urbaniq.pasajero.data.repository.RouteRepositoryImpl
import com.hackathon.urbaniq.pasajero.data.repository.VehicleRepositoryImpl
import com.hackathon.urbaniq.pasajero.data.repository.MockVehicleRepositoryImpl
import com.hackathon.urbaniq.pasajero.data.repository.WalletRepositoryImpl
import com.hackathon.urbaniq.pasajero.domain.repository.AuthRepository
import com.hackathon.urbaniq.pasajero.domain.repository.LocationRepository
import com.hackathon.urbaniq.pasajero.domain.repository.RouteRepository
import com.hackathon.urbaniq.pasajero.domain.repository.VehicleRepository
import com.hackathon.urbaniq.pasajero.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proveer implementaciones de repositorios
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        routeRepositoryImpl: RouteRepositoryImpl
    ): RouteRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl // Usando Firebase Auth real
    ): AuthRepository
}
