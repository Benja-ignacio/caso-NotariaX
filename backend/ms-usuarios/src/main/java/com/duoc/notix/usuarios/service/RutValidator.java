package com.duoc.notix.usuarios.service;

/**
 * Réplica acotada de la validación de RUN usada por la app Android.
 * No reemplaza una validación oficial; solo evita datos demo inconsistentes.
 */
public final class RutValidator {

    private RutValidator() {
    }

    public static String validar(String rutLimpio) {
        if (rutLimpio == null || rutLimpio.isBlank()) {
            return "Ingresa el RUN.";
        }
        String rut = rutLimpio.trim().toUpperCase();
        if (!rut.matches("\\d{7,8}[0-9K]")) {
            return "El RUN debe tener 7 u 8 dígitos y un dígito verificador.";
        }
        String cuerpo = rut.substring(0, rut.length() - 1);
        char dv = rut.charAt(rut.length() - 1);
        if (dv != calcularDv(cuerpo)) {
            return "El dígito verificador del RUN no coincide.";
        }
        return null;
    }

    public static char calcularDv(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.digit(cuerpo.charAt(i), 10) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return Character.forDigit(resto, 10);
    }
}
