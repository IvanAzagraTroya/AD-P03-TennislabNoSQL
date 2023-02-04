package com.example.tennislabspringboot.repositories.maquina

import com.example.tennislabspringboot.models.maquina.Maquina
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MaquinaRepository: CoroutineCrudRepository<Maquina, ObjectId> {
    fun findFirstByUuid(uuid: UUID) : Flow<Maquina>
}