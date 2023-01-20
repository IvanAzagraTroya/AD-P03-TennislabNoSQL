package dto

import models.maquina.TipoMaquina
import java.time.LocalDate
import java.util.*

class MaquinaDTO(){
    lateinit var  uuid: UUID
    lateinit var modelo: String
    lateinit var  marca: String
    lateinit var  fechaAdquisicion: LocalDate
    lateinit var  numeroSerie: String
    lateinit var  tipo: TipoMaquina
    var  activa: Boolean = false

    // esto es data para encordadoras
    var  isManual: Boolean? = false
    var  maxTension: Double? = null
    var  minTension: Double? = null

    // esto es data para personalizadoras
    var  measuresManeuverability: Boolean? = false
    var  measuresRigidity: Boolean? = false
    var  measuresBalance: Boolean? = false

    constructor(
        uuid: UUID,
        modelo: String,
        marca: String,
        fechaAdquisicion: LocalDate,
        numeroSerie: String,
        tipo: TipoMaquina,
        activa: Boolean,
        isManual: Boolean,
        maxTension: Double?,
        minTension: Double?,
        measuresManeuverability: Boolean?,
        measuresRigidity: Boolean?,
        measuresBalance: Boolean?
    ): this(){
        this.uuid = uuid
        this.modelo = modelo
        this.marca = marca
        this.fechaAdquisicion = fechaAdquisicion
        this.numeroSerie = numeroSerie
        this.tipo = tipo
        this.activa = activa
        this.isManual = isManual
        this.maxTension = maxTension
        this.minTension = minTension
        this.measuresManeuverability =measuresManeuverability
        this.measuresRigidity = measuresRigidity
        this.measuresBalance = measuresBalance
    }

    //constructor encordadores
    constructor(
        uuid: UUID,
        modelo: String,
        marca: String,
        fechaAdquisicion: LocalDate,
        numeroSerie: String,
        tipo: TipoMaquina,
        activa: Boolean,
        isManual: Boolean,
        maxTension: Double?,
        minTension: Double?,
    ): this(){

        this.isManual = isManual
        this.maxTension = maxTension
        this.minTension = minTension
    }

    // constructor personalizadoras
    constructor(
        uuid: UUID,
        modelo: String,
        marca: String,
        fechaAdquisicion: LocalDate,
        numeroSerie: String,
        tipo: TipoMaquina,
        activa: Boolean,
        measuresManeuverability: Boolean?,
        measuresRigidity: Boolean?,
        measuresBalance: Boolean?
    ): this(){

        this.measuresManeuverability =measuresManeuverability
        this.measuresRigidity = measuresRigidity
        this.measuresBalance = measuresBalance
    }
}