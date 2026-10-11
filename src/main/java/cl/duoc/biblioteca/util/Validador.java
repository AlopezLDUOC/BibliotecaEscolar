package cl.duoc.biblioteca.util;

import java.util.regex.Pattern;

/** Validaciones simples reutilizadas por los controladores. */
public final class Validador {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validador() { }

    public static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean correoValido(String correo) {
        return !vacio(correo) && CORREO.matcher(correo.trim()).matches();
    }

    /** Validador de rut */
    public static boolean rutValido(String rut) {
        if (vacio(rut)) {
            return false;
        }
        String limpio = rut.replace(".", "").replace("-", "").trim().toUpperCase();
        if (limpio.length() < 2 || !limpio.substring(0, limpio.length() - 1).matches("\\d+")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = (resto == 11) ? '0' : (resto == 10) ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }
}
