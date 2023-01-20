package mappers

import dto.tarea.*
import models.tarea.Tarea
import models.tarea.TipoTarea
import repositories.producto.ProductoRepository

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