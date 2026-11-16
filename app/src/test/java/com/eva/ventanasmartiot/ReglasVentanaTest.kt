package com.eva.ventanasmartiot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasVentanaTest {
    @Test fun humedadAltaCierra() {
        assertFalse(aplicarHisteresis(EstadoVentana(abierta = true), 70.0))
        assertFalse(aplicarHisteresis(EstadoVentana(abierta = true), 85.0))
    }
    @Test fun humedadBajaAbre() {
        assertTrue(aplicarHisteresis(EstadoVentana(abierta = false), 60.0))
        assertTrue(aplicarHisteresis(EstadoVentana(abierta = false), 45.0))
    }
    @Test fun zonaMuertaMantieneEstado() {
        assertTrue(aplicarHisteresis(EstadoVentana(abierta = true), 65.0))
        assertFalse(aplicarHisteresis(EstadoVentana(abierta = false), 65.0))
    }
}
