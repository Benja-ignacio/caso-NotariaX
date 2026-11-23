package com.duoc.notix.data.remote

import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.TipoFirma
import com.duoc.notix.data.remote.dto.AvisoDto
import com.duoc.notix.data.remote.dto.DocumentoDto
import com.duoc.notix.testutil.tramite
import com.duoc.notix.testutil.tramiteDto
import com.duoc.notix.testutil.usuario
import com.duoc.notix.testutil.usuarioDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteMappersTest {

    @Test
    fun mapeaUsuarioYConservaLaClaveLocal() {
        val usuario = usuarioDto().toDomain(contrasena = "demo123")

        assertEquals("USR-001", usuario.id)
        assertEquals(TipoCuenta.PERSONA, usuario.tipoCuenta)
        assertEquals("demo123", usuario.contrasena)
        assertNull(usuario.empresa)
    }

    @Test
    fun mapeaEmpresaYSuRequestDeRegistro() {
        val dto = usuarioDto(tipo = "EMPRESA").copy(
            razonSocialEmpresa = "Notix SpA",
            rutEmpresa = "76543210-1",
            giroEmpresa = "Servicios"
        )
        val usuario = dto.toDomain("clave123")

        assertEquals("Notix SpA", usuario.empresa?.razonSocial)
        val request = usuario.toRegistroRequest()
        assertEquals("EMPRESA", request.tipoCuenta)
        assertEquals("Notix SpA", request.razonSocialEmpresa)
        assertEquals("ana.demo@notariax.test", request.correo)
        assertNull(request.numeroNotaria)
    }

    @Test
    fun mapeaTramiteCompletoConFechasYEnums() {
        val tramite = tramiteDto().toDomain()

        assertEquals(EstadoTramite.DEVUELTO, tramite.estado)
        assertEquals(TipoFirma.SIMPLE, tramite.tipoFirma)
        assertEquals(2, tramite.documentos.size)
        assertEquals(EstadoDocumento.OBSERVADO, tramite.documentos[1].estado)
        assertEquals("13/10/2026 09:30", tramite.historial.first().fechaHora)
        assertEquals(TipoCuenta.NOTARIO, tramite.historial.first().rol)
        assertEquals("Anexo ilegible", tramite.observacion)
    }

    @Test
    fun usaValoresPorDefectoAnteEnumsDesconocidos() {
        val tramite = tramiteDto(estado = "RARO", tipoFirma = "RARO").toDomain()

        assertEquals(EstadoTramite.ENVIADO, tramite.estado)
        assertEquals(TipoFirma.SIMPLE, tramite.tipoFirma)
    }

    @Test
    fun mapeaRequestsDeCreacionEstadoYReenvio() {
        val base = tramite()
        val creacion = base.toCrearRequest()
        assertEquals("F01", creacion.formatoId)
        assertEquals("SIMPLE_CANVAS", creacion.firmaEvidencia)
        assertEquals("Ñuñoa", creacion.antecedentes.comuna)

        val cambio = base.toCambioEstadoRequest(
            nuevoEstado = EstadoTramite.FINALIZADO,
            observacion = null,
            legibilidadConfirmada = true,
            rol = TipoCuenta.NOTARIO,
            comentario = "ok"
        )
        assertEquals("FINALIZADO", cambio.estado)
        assertEquals(true, cambio.legibilidadConfirmada)
        assertEquals("NOTARIO", cambio.rol)

        val reenvio = base.toReenvioRequest(
            antecedentes = base.antecedentes,
            documentos = base.documentos,
            comentario = " "
        )
        assertEquals(1, reenvio.documentos.size)
        assertNull(reenvio.comentario)
    }

    @Test
    fun mapeaAvisoYSuFecha() {
        val aviso = AvisoDto(
            id = "AVS-1",
            destinatarioId = "USR-001",
            tramiteId = "TX-2026-0001",
            mensaje = "Mensaje",
            fechaHora = "2026-10-13T09:30:00",
            leido = false
        ).toDomain()

        assertEquals("AVS-1", aviso.id)
        assertEquals("13/10/2026 09:30", aviso.fechaHora)
    }

    @Test
    fun mapeaDocumentoAceptadoPorDefecto() {
        val tramite = tramiteDto().copy(
            documentos = listOf(DocumentoDto(nombre = "x.pdf", tamanoKb = 1, estado = "RARO"))
        ).toDomain()

        assertEquals(EstadoDocumento.ACEPTADO, tramite.documentos.single().estado)
    }

    @Test
    fun mapeaPersonaNotariaYRequest() {
        val notario = usuario(
            tipo = TipoCuenta.NOTARIO,
            numeroNotaria = 2
        )
        val request = notario.toRegistroRequest()

        assertEquals("NOTARIO", request.tipoCuenta)
        assertEquals(2, request.numeroNotaria)
        assertNull(request.razonSocialEmpresa)
    }
}
