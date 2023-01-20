package dto

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import models.tarea.TipoTarea
import java.util.UUID
import kotlin.properties.Delegates

class TareaDTO() {
    lateinit var id: UUID
    lateinit var raquetaId: UUID
    var precio by Delegates.notNull<Double>()
    lateinit var tipo: TipoTarea
    var finalizada: Boolean = false
    lateinit var pedidoId: UUID

    var productoAdquiridoId: UUID? = null

    var peso: Int? = null
    var balance: Double? = null
    var rigidez: Int? = null

    var tensionHorizontal: Double? = null
    var cordajeHorizontalId: UUID? = null
    var tensionVertical: Double? = null
    var cordajeVerticalId: UUID? = null
    var dosNudos: Boolean = false

    constructor(id: UUID?,
        raquetaId:UUID,
        precio: Double,
        tipo: TipoTarea,
        finalizada: Boolean,
        pedidoId: UUID,
        productoAdquiridoId: UUID?,
        peso: Int?,
        balance: Double?,
        rigidez: Int?,
        tensionHorizontal: Double?,
        cordajeHorizontalId: UUID?,
        tensionVertical: Double?,
        cordajeVerticalId: UUID?,
        dosNudos: Boolean
    ) : this(){
        this.id = id ?: UUID.randomUUID()
        this.raquetaId = raquetaId
        this.precio = precio
        this.tipo = tipo
        this.finalizada = finalizada
        this.pedidoId = pedidoId
        this.productoAdquiridoId = productoAdquiridoId
        this.peso = peso
        this.balance = balance
        this.rigidez = rigidez
        this.tensionHorizontal = tensionHorizontal
        this.tensionVertical = tensionVertical
        this.cordajeHorizontalId = cordajeHorizontalId
        this.cordajeVerticalId = cordajeVerticalId
        this.dosNudos = dosNudos
    }

    // constructor de adquisiciones
    constructor(id: UUID?,
                raquetaId:UUID,
                precio: Double,
                tipo: TipoTarea,
                finalizada: Boolean,
                pedidoId: UUID,
                productoAdquiridoId: UUID?,
    ) : this(){
        this.id = id ?: UUID.randomUUID()
        this.raquetaId = raquetaId
        this.precio = precio
        this.tipo = tipo
        this.finalizada = finalizada
        this.pedidoId = pedidoId
        this.productoAdquiridoId = productoAdquiridoId
    }

//    constructor de personalizaciones
    constructor(id: UUID?,
                raquetaId:UUID,
                precio: Double,
                tipo: TipoTarea,
                finalizada: Boolean,
                pedidoId: UUID,
                peso: Int?,
                balance: Double?,
                rigidez: Int?
    ) : this(){
        this.id = id ?: UUID.randomUUID()
        this.raquetaId = raquetaId
        this.precio = precio
        this.tipo = tipo
        this.finalizada = finalizada
        this.pedidoId = pedidoId
        this.peso = peso
        this.balance = balance
        this.rigidez = rigidez
    }

    //    constructor de encordados
    constructor(id: UUID?,
                raquetaId:UUID,
                precio: Double,
                tipo: TipoTarea,
                finalizada: Boolean,
                pedidoId: UUID,
                tensionHorizontal: Double?,
                cordajeHorizontalId: UUID?,
                tensionVertical: Double?,
                cordajeVerticalId: UUID?,
                dosNudos: Boolean
    ) : this(){
        this.id = id ?: UUID.randomUUID()
        this.raquetaId = raquetaId
        this.precio = precio
        this.tipo = tipo
        this.finalizada = finalizada
        this.pedidoId = pedidoId
        this.tensionHorizontal = tensionHorizontal
        this.tensionVertical = tensionVertical
        this.cordajeHorizontalId = cordajeHorizontalId
        this.cordajeVerticalId = cordajeVerticalId
        this.dosNudos = dosNudos
    }

    fun fromJSON(json: String): TurnoDTO? {
        return Gson().fromJson(json, TurnoDTO::class.java)
    }

    fun toJSON(): String {
        return GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create().toJson(this)
    }

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create().toJson(this)
    }
}