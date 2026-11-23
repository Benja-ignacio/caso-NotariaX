package com.duoc.notix.testutil

import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.DatosEmpresa
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.EventoHistorial
import com.duoc.notix.data.model.FormatoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.TipoFirma
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.remote.dto.AntecedentesDto
import com.duoc.notix.data.remote.dto.DocumentoDto
import com.duoc.notix.data.remote.dto.EventoHistorialDto
import com.duoc.notix.data.remote.dto.TramiteDto
import com.duoc.notix.data.remote.dto.UsuarioDto

fun usuario(
    id: String = "USR-001",
    tipo: TipoCuenta = TipoCuenta.PERSONA,
    nombre: String = "Ana Demo Prueba",
    correo: String = "ana.demo@notariax.test",
    contrasena: String = "demo123",
    run: String = "111111111",
    comuna: String = "Ñuñoa",
    empresa: DatosEmpresa? = null,
    numeroNotaria: Int? = null
) = Usuario(
    id = id,
    tipoCuenta = tipo,
    nombreCompleto = nombre,
    correo = correo,
    contrasena = contrasena,
    run = run,
    numeroDocumento = "123456789",
    telefono = "912345678",
    comuna = comuna,
    empresa = empresa,
    numeroNotaria = numeroNotaria
)

fun formato(
    id: String = "F01",
    nombre: String = "Poder simple",
    firma: TipoFirma = TipoFirma.SIMPLE
) = FormatoTramite(id, nombre, "Poderes", firma)

fun tramite(
    id: String = "TX-2026-0001",
    estado: EstadoTramite = EstadoTramite.ENVIADO,
    solicitanteId: String = "USR-001",
    documentos: List<Documento> = listOf(Documento("poder.pdf", 100, EstadoDocumento.ACEPTADO)),
    antecedentes: Antecedentes = Antecedentes("Ana Demo Prueba", "111111111", "ana.demo@notariax.test", "Ñuñoa"),
    observacion: String? = null,
    tipoFirma: TipoFirma = TipoFirma.SIMPLE,
    firmaEvidencia: String? = "SIMPLE_CANVAS"
) = Tramite(
    id = id,
    formato = formato(firma = tipoFirma),
    solicitanteId = solicitanteId,
    solicitanteNombre = "Ana Demo Prueba",
    tipoFirma = tipoFirma,
    estado = estado,
    documentos = documentos,
    antecedentes = antecedentes,
    observacion = observacion,
    firmaEvidencia = firmaEvidencia,
    historial = listOf(EventoHistorial("Enviado", "12/10/2026 10:00", TipoCuenta.PERSONA))
)

fun usuarioDto(
    id: String = "USR-001",
    tipo: String = "PERSONA",
    nombre: String = "Ana Demo Prueba",
    correo: String = "ana.demo@notariax.test"
) = UsuarioDto(
    id = id,
    tipoCuenta = tipo,
    nombreCompleto = nombre,
    correo = correo,
    run = "111111111",
    numeroDocumento = "123456789",
    telefono = "912345678",
    comuna = "Ñuñoa"
)

fun tramiteDto(
    id: String = "TX-2026-0042",
    estado: String = "DEVUELTO",
    tipoFirma: String = "SIMPLE",
    observacion: String? = "Anexo ilegible"
) = TramiteDto(
    id = id,
    formatoId = "F01",
    formatoNombre = "Poder simple",
    formatoCategoria = "Poderes",
    solicitanteId = "USR-001",
    solicitanteNombre = "Ana Demo Prueba",
    tipoFirma = tipoFirma,
    estado = estado,
    antecedentes = AntecedentesDto(
        nombreCompleto = "Ana Demo Prueba",
        documentoIdentidad = "111111111",
        correoContacto = "ana.demo@notariax.test",
        comuna = "Ñuñoa"
    ),
    documentos = listOf(
        DocumentoDto(nombre = "poder.pdf", tamanoKb = 100, estado = "ACEPTADO"),
        DocumentoDto(nombre = "anexo.jpg", tamanoKb = 200, estado = "OBSERVADO")
    ),
    observacion = observacion,
    firmaEvidencia = "SIMPLE_CANVAS",
    fechaCreacion = "2026-10-12T10:00:00",
    fechaActualizacion = "2026-10-13T09:30:00.123",
    historial = listOf(
        EventoHistorialDto(
            descripcion = "Devuelto",
            fechaHora = "2026-10-13T09:30:00",
            rol = "NOTARIO"
        )
    )
)
