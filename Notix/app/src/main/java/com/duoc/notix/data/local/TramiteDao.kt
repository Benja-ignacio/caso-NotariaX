package com.duoc.notix.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class TramiteConDetalles(
    @Embedded val tramite: TramiteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tramiteId"
    )
    val documentos: List<DocumentoEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "tramiteId"
    )
    val historial: List<EventoHistorialEntity>
)

@Dao
interface TramiteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTramite(tramite: TramiteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentos(documentos: List<DocumentoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: List<EventoHistorialEntity>)

    @Transaction
    suspend fun insertTramiteCompleto(
        tramite: TramiteEntity,
        documentos: List<DocumentoEntity>,
        historial: List<EventoHistorialEntity>
    ) {
        insertTramite(tramite)
        insertDocumentos(documentos)
        insertHistorial(historial)
    }

    @Transaction
    @Query("SELECT * FROM tramites WHERE id = :id")
    fun getTramiteConDetalles(id: String): Flow<TramiteConDetalles?>

    @Transaction
    @Query("SELECT * FROM tramites WHERE solicitanteId = :solicitanteId")
    fun getTramitesPorSolicitante(solicitanteId: String): Flow<List<TramiteConDetalles>>

    @Transaction
    @Query("SELECT * FROM tramites")
    fun getTodosLosTramites(): Flow<List<TramiteConDetalles>>

    @Update
    suspend fun updateTramite(tramite: TramiteEntity)

    @Query("DELETE FROM documentos WHERE tramiteId = :tramiteId")
    suspend fun deleteDocumentosDeTramite(tramiteId: String)

    @Query("DELETE FROM historial_eventos WHERE tramiteId = :tramiteId")
    suspend fun deleteHistorialDeTramite(tramiteId: String)

    @Query("DELETE FROM avisos WHERE tramiteId = :tramiteId")
    suspend fun deleteAvisosDeTramite(tramiteId: String)

    @Query("DELETE FROM tramites WHERE id = :tramiteId")
    suspend fun deleteTramite(tramiteId: String)

    @Transaction
    suspend fun eliminarTramiteLocal(tramiteId: String) {
        deleteAvisosDeTramite(tramiteId)
        deleteTramite(tramiteId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvisoTransaccional(aviso: AvisoEntity)

    /**
     * Guarda un trámite recibido del backend reemplazando sus documentos e
     * historial locales para evitar duplicados en cada sincronización.
     */
    @Transaction
    suspend fun upsertTramiteRemoto(
        tramite: TramiteEntity,
        documentos: List<DocumentoEntity>,
        historial: List<EventoHistorialEntity>
    ) {
        insertTramite(tramite)
        deleteDocumentosDeTramite(tramite.id)
        insertDocumentos(documentos)
        deleteHistorialDeTramite(tramite.id)
        insertHistorial(historial)
    }

    @Transaction
    suspend fun actualizarEstadoCompleto(
        tramite: TramiteEntity,
        historial: EventoHistorialEntity,
        aviso: AvisoEntity?
    ) {
        updateTramite(tramite)
        insertHistorial(listOf(historial))
        if (aviso != null) insertAvisoTransaccional(aviso)
    }

    @Transaction
    suspend fun reenviarTramiteCompleto(
        tramite: TramiteEntity,
        documentos: List<DocumentoEntity>,
        historial: EventoHistorialEntity,
        aviso: AvisoEntity?
    ) {
        updateTramite(tramite)
        deleteDocumentosDeTramite(tramite.id)
        insertDocumentos(documentos)
        insertHistorial(listOf(historial))
        if (aviso != null) insertAvisoTransaccional(aviso)
    }
}
@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)      // falla si el correo o el id ya existen
    suspend fun insertUsuario(usuario: UsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)    // para el seeder
    suspend fun upsertUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun getUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun getUsuarioPorId(id: String): Flow<UsuarioEntity?>
}

@Dao
interface AvisoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAviso(aviso: AvisoEntity)

    @Query("SELECT * FROM avisos WHERE destinatarioId = :destinatarioId ORDER BY fechaHora DESC")
    fun getAvisosPorDestinatario(destinatarioId: String): Flow<List<AvisoEntity>>

    @Query("UPDATE avisos SET leido = 1 WHERE id = :id")
    suspend fun marcarAvisoLeido(id: String)
}
