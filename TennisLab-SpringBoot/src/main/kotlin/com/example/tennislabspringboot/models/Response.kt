package com.example.tennislabspringboot.models

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase sellada es la base de las respuestas,
 * que seran devueltas por el Controller.
 */
sealed class Response<T>

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta clase hereda de Response y la usara el Controller para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
class ResponseSuccess<T: Any>(val code: Int, val data: T) : Response<T>()

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta clase hereda de Response y la usara el Controller para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
class ResponseError(val code: Int, val message: String?) : Response<Nothing>()

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta clase esta hecha para que el deserializador pueda deserializar los tokens,
 * puesto que hay que pasarle la clase a deserializar.
 */
class ResponseToken(var code: Int, var data: String? = null, var message: String? = null)