package com.duoc.notix.usuarios.service;

import com.duoc.notix.usuarios.dto.LoginRequest;
import com.duoc.notix.usuarios.dto.RegistroUsuarioRequest;
import com.duoc.notix.usuarios.dto.UsuarioResponse;
import com.duoc.notix.usuarios.model.TipoCuenta;
import com.duoc.notix.usuarios.model.Usuario;
import com.duoc.notix.usuarios.repository.UsuarioRepository;
import com.duoc.notix.usuarios.web.ApiException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "nombreCompleto"))
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(String id) {
        return UsuarioResponse.from(buscar(id));
    }

    public UsuarioResponse registrar(RegistroUsuarioRequest request) {
        String correo = normalizarCorreo(request.correo());
        if (repository.existsByCorreo(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        validarTipoCuenta(request);

        Usuario usuario = new Usuario();
        usuario.setId("USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        aplicar(usuario, request, correo);
        try {
            return UsuarioResponse.from(repository.save(usuario));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponse login(LoginRequest request) {
        String correo = normalizarCorreo(request.correo());
        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe una cuenta con ese correo."));
        if (usuario.getTipoCuenta() != request.tipoCuenta()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El tipo de cuenta no coincide con el registrado.");
        }
        if (!usuario.getContrasena().equals(request.contrasena())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La contraseña es incorrecta.");
        }
        return UsuarioResponse.from(usuario);
    }

    public UsuarioResponse actualizar(String id, RegistroUsuarioRequest request) {
        Usuario usuario = buscar(id);
        String correo = normalizarCorreo(request.correo());
        repository.findByCorreo(correo)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
                });
        validarTipoCuenta(request);
        aplicar(usuario, request, correo);
        return UsuarioResponse.from(repository.save(usuario));
    }

    public void eliminar(String id) {
        Usuario usuario = buscar(id);
        repository.delete(usuario);
    }

    private Usuario buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró el usuario solicitado."));
    }

    private void aplicar(Usuario usuario, RegistroUsuarioRequest request, String correo) {
        String errorRun = RutValidator.validar(request.run());
        if (errorRun != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errorRun);
        }
        if (!request.numeroDocumento().trim().matches("\\d{9}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El número de documento debe tener 9 dígitos.");
        }

        usuario.setTipoCuenta(request.tipoCuenta());
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(request.contrasena());
        usuario.setRun(request.run().trim());
        usuario.setNumeroDocumento(request.numeroDocumento().trim());
        usuario.setTelefono(request.telefono().trim());
        usuario.setComuna(request.comuna().trim());
        usuario.setRazonSocialEmpresa(limpiar(request.razonSocialEmpresa()));
        usuario.setRutEmpresa(limpiar(request.rutEmpresa()));
        usuario.setGiroEmpresa(limpiar(request.giroEmpresa()));
        usuario.setNumeroNotaria(request.numeroNotaria());
    }

    private void validarTipoCuenta(RegistroUsuarioRequest request) {
        if (request.tipoCuenta() == TipoCuenta.EMPRESA) {
            if (esBlanco(request.razonSocialEmpresa())
                    || esBlanco(request.rutEmpresa())
                    || esBlanco(request.giroEmpresa())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "La cuenta de empresa requiere razón social, RUT y giro."
                );
            }
        }
        if (request.tipoCuenta() == TipoCuenta.NOTARIO && request.numeroNotaria() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La cuenta de notario requiere número de notaría.");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private String limpiar(String valor) {
        return esBlanco(valor) ? null : valor.trim();
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
