package dto.tarea

import dto.producto.ProductoDTOcreate
import dto.producto.ProductoDTOvisualize
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.tarea.Tarea
import models.tarea.TipoTarea
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import serializers.UUIDSerializer
import java.util.*

@Serializable sealed interface TareaDTO
@Serializable sealed interface TareaDTOcreate : TareaDTO { fun fromDTO() : Tarea }
@Serializable sealed interface TareaDTOvisualize : TareaDTO

@Serializable data class AdquisicionDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    var precio: Double,
    val finalizada: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val productoAdquirido: ProductoDTOcreate,
) : TareaDTOcreate {
    override fun fromDTO() = Tarea (
        uuid = uuid,
        raquetaId = raqueta.uuid,
        precio = precio,
        tipo = TipoTarea.ADQUISICION,
        finalizada = finalizada,
        pedidoId = pedidoId,
        productoAdquiridoId = productoAdquirido.uuid,
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null
    )
}

@Serializable data class EncordadoDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    val finalizada: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val tensionHorizontal: Double,
    val cordajeHorizontal: ProductoDTOcreate,
    val tensionVertical: Double,
    val cordajeVertical: ProductoDTOcreate,
    val dosNudos: Boolean
) : TareaDTOcreate {
    val precio: Double = 15+cordajeHorizontal.precio+cordajeVertical.precio
    override fun fromDTO() = Tarea (
        uuid = uuid,
        raquetaId = raqueta.uuid,
        precio = precio,
        tipo = TipoTarea.ENCORDADO,
        finalizada = finalizada,
        pedidoId = pedidoId,
        productoAdquiridoId = null,
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = tensionHorizontal,
        cordajeHorizontalId = cordajeHorizontal.uuid,
        tensionVertical = tensionVertical,
        cordajeVerticalId = cordajeVertical.uuid,
        dosNudos = dosNudos
    )
}

@Serializable data class PersonalizacionDTOcreate (
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val raqueta: ProductoDTOcreate,
    val finalizada: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val peso: Int,
    val balance: Double,
    val rigidez: Int
) : TareaDTOcreate {
    val precio: Double = 60.0
    override fun fromDTO() = Tarea (
        uuid = uuid,
        raquetaId = raqueta.uuid,
        precio = precio,
        tipo = TipoTarea.PERSONALIZACION,
        finalizada = finalizada,
        pedidoId = pedidoId,
        productoAdquiridoId = null,
        peso = peso,
        balance = balance,
        rigidez = rigidez,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null
    )
}

@Serializable data class AdquisicionDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val productoAdquirido: ProductoDTOvisualize?
) : TareaDTOvisualize

@Serializable data class EncordadoDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val tensionHorizontal: Double,
    val cordajeHorizontal: ProductoDTOvisualize?,
    val tensionVertical: Double,
    val cordajeVertical: ProductoDTOvisualize?,
    val dosNudos: Boolean
) : TareaDTOvisualize

@Serializable data class PersonalizacionDTOvisualize (
    val raqueta: ProductoDTOvisualize?,
    var precio: Double,
    val finalizada: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val pedidoId: UUID,

    val peso: Int,
    val balance: Double,
    val rigidez: Int
) : TareaDTOvisualize

@Serializable data class TareaDTOFromApi (
    @SerialName("mongo_id")
    val id: String?,
    val uuid: String?,
    val raquetaId: String?,
    var precio: Double?,
    val tipo: TipoTarea?,
    @SerialName("completed")
    val finalizada: Boolean?,
    val pedidoId: String?,
    val productoAdquiridoId: String?,
    val peso: Int?,
    val balance: Double?,
    val rigidez: Int?,
    val tensionHorizontal: Double?,
    val cordajeHorizontalId: String?,
    val tensionVertical: Double?,
    val cordajeVerticalId: String?,
    val dosNudos: Boolean?
)