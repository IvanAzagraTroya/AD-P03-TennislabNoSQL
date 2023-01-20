package mappers

import dto.maquina.EncordadoraDTOvisualize
import dto.maquina.MaquinaDTOcreate
import dto.maquina.MaquinaDTOvisualize
import dto.maquina.PersonalizadoraDTOvisualize
import models.maquina.Maquina
import models.maquina.TipoMaquina

fun Maquina.toDTO() : MaquinaDTOvisualize {
    return when (tipo) {
        TipoMaquina.ENCORDADORA -> {
            EncordadoraDTOvisualize (
                modelo = modelo,
                marca = marca,
                fechaAdquisicion = fechaAdquisicion,
                numeroSerie = numeroSerie,
                activa = activa,
                isManual = isManual!!,
                maxTension = maxTension!!,
                minTension = minTension!!
            )
        }
        TipoMaquina.PERSONALIZADORA -> {
            PersonalizadoraDTOvisualize (
                modelo = modelo,
                marca = marca,
                fechaAdquisicion = fechaAdquisicion,
                numeroSerie = numeroSerie,
                activa = activa,
                measuresManeuverability = measuresManeuverability!!,
                measuresRigidity = measuresRigidity!!,
                measuresBalance = measuresBalance!!
            )
        }
    }
}

fun toDTO(list: List<Maquina>) : List<MaquinaDTOvisualize> {
    val res = mutableListOf<MaquinaDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun fromDTO(list: List<MaquinaDTOcreate>) : List<Maquina> {
    val res = mutableListOf<Maquina>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}