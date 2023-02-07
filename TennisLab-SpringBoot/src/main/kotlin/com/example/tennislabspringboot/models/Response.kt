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

