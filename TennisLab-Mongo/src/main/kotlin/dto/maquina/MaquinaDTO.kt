package dto.maquina

import kotlinx.serialization.Serializable
import models.maquina.Maquina
import models.maquina.TipoMaquina
import serializers.LocalDateSerializer
import serializers.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable sealed interface MaquinaDTO
@Serializable sealed interface MaquinaDTOcreate : MaquinaDTO { fun fromDTO() : Maquina }
@Serializable sealed interface MaquinaDTOvisualize : MaquinaDTO

@Serializable data class EncordadoraDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val modelo: String,
    val marca: String,
    @Serializable(with = LocalDateSerializer::class)
    val fechaAdquisicion: LocalDate = LocalDate.now(),
    val numeroSerie: String,
    val activa: Boolean = true,

    val isManual: Boolean,
    val maxTension: Double = 0.0,
    val minTension: Double = 0.0
) : MaquinaDTOcreate {
    override fun fromDTO() = Maquina (
        uuid = uuid,
        modelo = modelo,
        marca = marca,
        fechaAdquisicion = fechaAdquisicion,
        numeroSerie = numeroSerie,
        tipo = TipoMaquina.ENCORDADORA,
        activa = activa,
        isManual = isManual,
        maxTension = maxTension,
        minTension = minTension,
        measuresManeuverability = null,
        measuresRigidity = null,
        measuresBalance = null
    )
}

@Serializable data class PersonalizadoraDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val modelo: String,
    val marca: String,
    @Serializable(with = LocalDateSerializer::class)
    val fechaAdquisicion: LocalDate = LocalDate.now(),
    val numeroSerie: String,
    val activa: Boolean = true,

    val measuresManeuverability: Boolean,
    val measuresRigidity: Boolean,
    val measuresBalance: Boolean
) : MaquinaDTOcreate {
    override fun fromDTO() = Maquina (
        uuid = uuid,
        modelo = modelo,
        marca = marca,
        fechaAdquisicion = fechaAdquisicion,
        numeroSerie = numeroSerie,
        tipo = TipoMaquina.ENCORDADORA,
        activa = activa,
        isManual = null,
        maxTension = null,
        minTension = null,
        measuresManeuverability = measuresManeuverability,
        measuresRigidity = measuresRigidity,
        measuresBalance = measuresBalance
    )
}

@Serializable data class EncordadoraDTOvisualize (
    val modelo: String,
    val marca: String,
    @Serializable(with = LocalDateSerializer::class)
    val fechaAdquisicion: LocalDate,
    val numeroSerie: String,
    val activa: Boolean,

    val isManual: Boolean,
    val maxTension: Double,
    val minTension: Double
) : MaquinaDTOvisualize

@Serializable data class PersonalizadoraDTOvisualize (
    val modelo: String,
    val marca: String,
    @Serializable(with = LocalDateSerializer::class)
    val fechaAdquisicion: LocalDate,
    val numeroSerie: String,
    val activa: Boolean,

    val measuresManeuverability: Boolean,
    val measuresRigidity: Boolean,
    val measuresBalance: Boolean
) : MaquinaDTOvisualize