package com.duoc.notix.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.TipoFirma
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotixDaoTest {

    private lateinit var db: NotixDatabase

    @Before
    fun crearBaseEnMemoria() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NotixDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun insertaYConsultaTramiteConDocumentosEHistorial() = runBlocking {
        val dao = db.tramiteDao()
        dao.insertTramiteCompleto(
            tramiteEntity("TX-1"),
            listOf(documento("TX-1")),
            listOf(evento("TX-1"))
        )

        val detalle = dao.getTramiteConDetalles("TX-1").first()

        assertNotNull(detalle)
        assertEquals("TX-1", detalle?.tramite?.id)
        assertEquals(1, detalle?.documentos?.size)
        assertEquals(1, detalle?.historial?.size)
    }

    @Test
    fun upsertRemotoReemplazaDocumentosEHistorialSinDuplicar() = runBlocking {
        val dao = db.tramiteDao()
        dao.insertTramiteCompleto(
            tramiteEntity("TX-1"),
            listOf(documento("TX-1")),
            listOf(evento("TX-1"))
        )

        dao.upsertTramiteRemoto(
            tramiteEntity("TX-1", estado = EstadoTramite.FINALIZADO),
            listOf(documento("TX-1", "nuevo.pdf")),
            listOf(evento("TX-1", "Aprobado"))
        )

        val detalle = dao.getTramiteConDetalles("TX-1").first()
        assertEquals(EstadoTramite.FINALIZADO, detalle?.tramite?.estado)
        assertEquals(1, detalle?.documentos?.size)
        assertEquals("nuevo.pdf", detalle?.documentos?.single()?.nombre)
        assertEquals(1, detalle?.historial?.size)
        assertEquals("Aprobado", detalle?.historial?.single()?.descripcion)
    }

    @Test
    fun actualizaEstadoConHistorialYAviso() = runBlocking {
        val dao = db.tramiteDao()
        dao.insertTramiteCompleto(
            tramiteEntity("TX-1"),
            listOf(documento("TX-1")),
            listOf(evento("TX-1"))
        )

        dao.actualizarEstadoCompleto(
            tramiteEntity(
                "TX-1",
                estado = EstadoTramite.DEVUELTO,
                observacion = "Anexo ilegible"
            ),
            evento("TX-1", "Devuelto", TipoCuenta.NOTARIO),
            aviso("TX-1")
        )

        val detalle = dao.getTramiteConDetalles("TX-1").first()
        assertEquals(EstadoTramite.DEVUELTO, detalle?.tramite?.estado)
        assertEquals("Anexo ilegible", detalle?.tramite?.observacion)
        assertEquals(2, detalle?.historial?.size)
        assertEquals("Devuelto", detalle?.historial?.last()?.descripcion)
        assertEquals(TipoCuenta.NOTARIO, detalle?.historial?.last()?.rol)
        assertEquals(1, db.avisoDao().getAvisosPorDestinatario("USR-1").first().size)
    }

    @Test
    fun reenviarReemplazaDocumentosYConservaHistorial() = runBlocking {
        val dao = db.tramiteDao()
        dao.insertTramiteCompleto(
            tramiteEntity("TX-1", estado = EstadoTramite.DEVUELTO, observacion = "obs"),
            listOf(documento("TX-1")),
            listOf(evento("TX-1"))
        )

        dao.reenviarTramiteCompleto(
            tramiteEntity("TX-1"),
            listOf(documento("TX-1", "reemplazo.pdf")),
            evento("TX-1", "Corregido"),
            aviso("TX-1")
        )

        val detalle = dao.getTramiteConDetalles("TX-1").first()
        assertEquals(EstadoTramite.ENVIADO, detalle?.tramite?.estado)
        assertNull(detalle?.tramite?.observacion)
        assertEquals("reemplazo.pdf", detalle?.documentos?.single()?.nombre)
        assertEquals(2, detalle?.historial?.size)
    }

    @Test
    fun eliminarTramiteBorraAvisosYHijos() = runBlocking {
        val dao = db.tramiteDao()
        dao.insertTramiteCompleto(
            tramiteEntity("TX-1"),
            listOf(documento("TX-1")),
            listOf(evento("TX-1"))
        )
        db.avisoDao().insertAviso(aviso("TX-1"))

        dao.eliminarTramiteLocal("TX-1")

        assertNull(dao.getTramiteConDetalles("TX-1").first())
        assertTrue(db.avisoDao().getAvisosPorDestinatario("USR-1").first().isEmpty())
        assertEquals(0, contar("documentos"))
        assertEquals(0, contar("historial_eventos"))
    }

    @Test
    fun usuarioDuplicadoFallaYSePuedeConsultarPorCorreo() = runBlocking {
        val dao = db.usuarioDao()
        dao.insertUsuario(usuarioEntity())

        assertEquals("Ana Demo", dao.getUsuarioPorCorreo("ana@test.cl")?.nombreCompleto)
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                dao.insertUsuario(usuarioEntity(id = "USR-2"))
            }
        }
        Unit
    }

    @Test
    fun marcaAvisoComoLeido() = runBlocking {
        val dao = db.avisoDao()
        dao.insertAviso(aviso("TX-1"))

        dao.marcarAvisoLeido("AVS-1")

        val aviso = dao.getAvisosPorDestinatario("USR-1").first().single()
        assertTrue(aviso.leido)
    }

    private fun contar(tabla: String): Int {
        return db.query("SELECT COUNT(*) FROM $tabla", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
    }

    private fun tramiteEntity(
        id: String,
        estado: EstadoTramite = EstadoTramite.ENVIADO,
        observacion: String? = null
    ) = TramiteEntity(
        id = id,
        formatoId = "F01",
        formatoNombre = "Poder simple",
        formatoCategoria = "Poderes",
        solicitanteId = "USR-1",
        solicitanteNombre = "Ana Demo",
        tipoFirma = TipoFirma.SIMPLE,
        estado = estado,
        antecedentesNombreCompleto = "Ana Demo",
        antecedentesDocumentoIdentidad = "111111111",
        antecedentesCorreoContacto = "ana@test.cl",
        antecedentesComuna = "Ñuñoa",
        observacion = observacion,
        firmaEvidencia = "SIMPLE_CANVAS"
    )

    private fun documento(tramiteId: String, nombre: String = "poder.pdf") = DocumentoEntity(
        tramiteId = tramiteId,
        nombre = nombre,
        tamanoKb = 100,
        estado = EstadoDocumento.ACEPTADO
    )

    private fun evento(
        tramiteId: String,
        descripcion: String = "Enviado",
        rol: TipoCuenta = TipoCuenta.PERSONA
    ) = EventoHistorialEntity(
        tramiteId = tramiteId,
        descripcion = descripcion,
        fechaHora = "12/10/2026 10:00",
        rol = rol
    )

    private fun aviso(tramiteId: String) = AvisoEntity(
        id = "AVS-1",
        destinatarioId = "USR-1",
        tramiteId = tramiteId,
        mensaje = "Mensaje",
        fechaHora = "12/10/2026 10:00",
        leido = false
    )

    private fun usuarioEntity(id: String = "USR-1") = UsuarioEntity(
        id = id,
        tipoCuenta = TipoCuenta.PERSONA,
        nombreCompleto = "Ana Demo",
        correo = "ana@test.cl",
        contrasena = "demo123",
        run = "111111111",
        numeroDocumento = "123456789",
        telefono = "912345678",
        comuna = "Ñuñoa"
    )
}
