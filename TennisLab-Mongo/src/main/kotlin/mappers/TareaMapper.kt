package mappers

import dto.tarea.*
import models.tarea.Tarea
import models.tarea.TipoTarea
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import repositories.producto.ProductoRepository
import java.util.*

private val pRepo = ProductoRepository()

suspend fun Tarea.toDTO() : TareaDTOvisualize {
    return when (tipo) {
        TipoTarea.ADQUISICION -> {
            AdquisicionDTOvisualize (
                raqueta = pRepo.findByUUID(raquetaId)?.toDTO(),
                precio = precio,
                finalizada = finalizada,
                pedidoId = pedidoId,
                productoAdquirido = pRepo.findByUUID(productoAdquiridoId!!)?.toDTO()
            )
        }
        TipoTarea.ENCORDADO -> {
            EncordadoDTOvisualize (
                raqueta = pRepo.findByUUID(raquetaId)?.toDTO(),
                precio = precio,
                finalizada = finalizada,
                pedidoId = pedidoId,
                tensionHorizontal = tensionHorizontal!!,
                cordajeHorizontal = pRepo.findByUUID(cordajeHorizontalId!!)?.toDTO(),
                tensionVertical = tensionVertical!!,
                cordajeVertical = pRepo.findByUUID(cordajeVerticalId!!)?.toDTO(),
                dosNudos = dosNudos!!
            )
        }
        TipoTarea.PERSONALIZACION -> {
            PersonalizacionDTOvisualize (
                raqueta = pRepo.findByUUID(raquetaId)?.toDTO(),
                precio = precio,
                finalizada = finalizada,
                pedidoId = pedidoId,
                peso = peso!!,
                balance = balance!!,
                rigidez = rigidez!!
            )
        }
    }
}

fun TareaDTOFromApi.fromDTO() = Tarea (
    id = id?.toId() ?: newId(),
    uuid = UUID.fromString(uuid) ?: UUID.fromString("00000000-0000-0000-0000-000000000000"),
    raquetaId = UUID.fromString(raquetaId) ?: UUID.fromString("00000000-0000-0000-0000-000000000001"),
    precio = precio ?: 0.0,
    tipo = tipo ?: TipoTarea.ADQUISICION,
    finalizada = finalizada ?: true,
    pedidoId = UUID.fromString(pedidoId) ?: UUID.fromString("00000000-0000-0000-0000-000000000002"),
    productoAdquiridoId = UUID.fromString(pedidoId) ?: UUID.fromString("00000000-0000-0000-0000-000000000002"),
    peso = peso,
    balance = balance,
    rigidez = rigidez,
    tensionHorizontal = tensionHorizontal,
    cordajeHorizontalId = UUID.fromString(cordajeHorizontalId) ?: null,
    tensionVertical = tensionVertical,
    cordajeVerticalId = UUID.fromString(cordajeVerticalId) ?: null,
    dosNudos = dosNudos
)

suspend fun toDTO(list: List<Tarea>) : List<TareaDTOvisualize> {
    val res = mutableListOf<TareaDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun fromDTO(list: List<TareaDTOcreate>) : List<Tarea> {
    val res = mutableListOf<Tarea>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}

fun fromDTO(list: List<TareaDTOFromApi>) : List<Tarea> {
    val res = mutableListOf<Tarea>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}