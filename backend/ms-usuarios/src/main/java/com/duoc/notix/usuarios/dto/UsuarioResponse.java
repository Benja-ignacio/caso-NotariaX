package com.duoc.notix.usuarios.dto;

import com.duoc.notix.usuarios.model.TipoCuenta;
import com.duoc.notix.usuarios.model.Usuario;

/**
 * Respuesta pública de usuario. Nunca incluye la contraseña.
 */
public record UsuarioResponse(
        String id,
        TipoCuenta tipoCuenta,
        String nombreCompleto,
        String correo,
        String run,
        String numeroDocumento,
        String telefono,
        String comuna,
        String razonSocialEmpresa,
        String rutEmpresa,
        String giroEmpresa,
        Integer numeroNotaria
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getTipoCuenta(),
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getRun(),
                usuario.getNumeroDocumento(),
                usuario.getTelefono(),
                usuario.getComuna(),
                usuario.getRazonSocialEmpresa(),
                usuario.getRutEmpresa(),
                usuario.getGiroEmpresa(),
                usuario.getNumeroNotaria()
        );
    }
}
