package com.example.mapsdemo.data.repository

import com.example.mapsdemo.data.model.Bump

interface DataSource {
    suspend fun saveBump(bump: Bump)
    suspend fun saveAllBumps (bumps : Bump)
    suspend fun getAllBumps() : List<Bump>
}