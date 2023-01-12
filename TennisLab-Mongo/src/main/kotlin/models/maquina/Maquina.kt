package models.maquina

import kotlinx.serialization.Serializable
import serializers.LocalDateSerializer
import serializers.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable
data class Maquina(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val modelo: String,
    val marca: String,
    @Serializable(with = LocalDateSerializer::class)
    val fechaAdquisicion: LocalDate,
    val numeroSerie: String,
    val tipo: TipoMaquina,
    // esto es data para encordadoras
    val isManual: Boolean?,
    val maxTension: Double?,
    val minTension: Double?,
    // esto es data para personalizadoras
    val measuresManeuverability: Boolean?,
    val measuresRigidity: Boolean?,
    val measuresBalance: Boolean?
)