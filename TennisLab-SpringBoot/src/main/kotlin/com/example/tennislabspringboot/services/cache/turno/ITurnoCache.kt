package com.example.tennislabspringboot.services.cache.turno

import com.example.tennislabspringboot.models.turno.Turno
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

interface ITurnoCache : ICache<UUID, Turno>