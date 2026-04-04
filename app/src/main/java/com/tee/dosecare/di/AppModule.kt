package com.tee.dosecare.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tee.dosecare.data.local.DoseCareDatabase
import com.tee.dosecare.data.local.DoseLogDao
import com.tee.dosecare.data.local.MedicationDao
import com.tee.dosecare.data.repository.MedicationRepository
import com.tee.dosecare.utils.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DoseCareDatabase =
        DoseCareDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideMedicationDao(database: DoseCareDatabase): MedicationDao =
        database.medicationDao()

    @Provides
    @Singleton
    fun provideDoseLogDao(database: DoseCareDatabase): DoseLogDao =
        database.doseLogDao()

    @Provides
    @Singleton
    fun provideMedicationRepository(
        medicationDao: MedicationDao,
        doseLogDao: DoseLogDao
    ): MedicationRepository = MedicationRepository(medicationDao, doseLogDao)
}