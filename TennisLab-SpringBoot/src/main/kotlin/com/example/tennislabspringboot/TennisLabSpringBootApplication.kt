package com.example.tennislabspringboot

import com.example.tennislabspringboot.controllers.Controller
import com.example.tennislabspringboot.db.*
import com.example.tennislabspringboot.mappers.fromDTO
import com.example.tennislabspringboot.services.login.create
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TennisLabSpringBootApplication : CommandLineRunner {
    @Autowired
    lateinit var controller: Controller

    override fun run(vararg args: String?) = runBlocking {
        val job = launch { loadData(controller) }
        job.join()


    }

}

fun main(args: Array<String>) {
    runApplication<TennisLabSpringBootApplication>(*args)
}

private suspend fun loadData(controller: Controller) = withContext(Dispatchers.IO) {
    println("Deleting previous data...")
    val delete = launch { controller.deleteAll() }
    delete.join()
    println("Previous data deleted.")
    println("Loading data...")
    val users = getUsers()
    val admin = users[0]
    val adminToken = create(admin.fromDTO())

    val job1: Job = launch { users.forEach { println(controller.createUser(it, adminToken)) } }
    val job2: Job = launch { getProducts().forEach { println(controller.createProducto(it, adminToken)) } }
    val job3: Job = launch { getMaquinas().forEach { println(controller.createMaquina(it, adminToken)) } }
    joinAll(job1, job2, job3)
    val job4: Job = launch { getTareas().forEach { println(controller.createTarea(it, adminToken)) } }
    job4.join()
    val job5: Job = launch { getPedidos().forEach { println(controller.createPedido(it, adminToken)) } }
    val job6: Job = launch { getTurnos().forEach { println(controller.createTurno(it, adminToken)) } }
    joinAll(job5, job6)
    println("Data loaded.")
}
