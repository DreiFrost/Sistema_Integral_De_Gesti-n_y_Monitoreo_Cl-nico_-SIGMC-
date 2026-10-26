package servicio;

import dao.conexion;

import modelo.campo;
import modelo.modulo;

import java.sql.*;
import java.util.*;

public class repositorio {
    private static String sesion;

    public static void salir() {
        sesion = null;
    }

    public static void exigirmedico() {
        permiso(true);
    }

    private static void permiso(boolean clinico) {
        if (sesion == null || (clinico && !"MEDICO".equals(sesion))) {
            throw new SecurityException("No tiene permiso para esta operación.");
        }
    }

    private static void validar(modulo m) {
        boolean encontrado = false;
        for (modulo item : modulos) {
            if (item == m) {
                encontrado = true;
            }
        }
        if (!encontrado) {
            throw new IllegalArgumentException("Módulo inválido.");
        }
        permiso(m.isclinico());
    }

    public static campo c(String n, String t, String tipo, boolean req) {
        return new campo(n, t, tipo, req);
    }

    public static final modulo[] modulos = {
        new modulo(
                "pacientes",
                "Pacientes",
                new campo[] {
                    c("nombre", "Nombre completo", "text", true),
                    c("fecha_nacimiento", "Nacimiento (AAAA-MM-DD)", "date", true),
                    c("sexo", "Sexo", "sexo", true),
                    c("responsable", "Responsable", "text", false),
                    c("telefono", "Teléfono", "text", false),
                    c("direccion", "Dirección", "text", false)
                },
                false),
        new modulo(
                "citas",
                "Citas médicas",
                new campo[] {
                    c("paciente_id", "ID paciente", "paciente", true),
                    c("fecha", "Fecha y hora (AAAA-MM-DD HH:MM)", "datetime", true),
                    c("motivo", "Motivo", "text", true),
                    c("estado", "Estado", "estado", true)
                },
                false),
        new modulo(
                "consultas",
                "Historial clínico",
                new campo[] {
                    c("paciente_id", "ID paciente", "paciente", true),
                    c("fecha", "Fecha y hora (AAAA-MM-DD HH:MM)", "datetime", true),
                    c("motivo", "Motivo de consulta", "text", true),
                    c("diagnostico", "Diagnóstico", "text", false),
                    c("tratamiento", "Tratamiento", "text", false),
                    c("observaciones", "Observaciones", "text", false)
                },
                true),
        new modulo(
                "crecimiento",
                "Peso y talla",
                new campo[] {
                    c("paciente_id", "ID paciente", "paciente", true),
                    c("fecha", "Fecha (AAAA-MM-DD)", "date", true),
                    c("peso_kg", "Peso (kg)", "decimal", true),
                    c("talla_cm", "Talla (cm)", "decimal", true),
                    c("observaciones", "Observaciones", "text", false)
                },
                true),
        new modulo(
                "examenes",
                "Estudios y exámenes",
                new campo[] {
                    c("paciente_id", "ID paciente", "paciente", true),
                    c("fecha", "Fecha (AAAA-MM-DD)", "date", true),
                    c("nombre", "Estudio o examen", "text", true),
                    c("resultado", "Resultado", "text", false),
                    c("observaciones", "Observaciones", "text", false)
                },
                true)
    };

    public static void creartablas() throws SQLException {
        String[] sql = {
            "CREATE TABLE IF NOT EXISTS usuarios (id INT AUTO_INCREMENT PRIMARY KEY, usuario"
                + " VARCHAR(60) NOT NULL UNIQUE, clave_hash VARCHAR(180) NOT NULL, rol VARCHAR(20)"
                + " NOT NULL)",
            "CREATE TABLE IF NOT EXISTS pacientes (id INT AUTO_INCREMENT PRIMARY KEY, nombre"
                + " VARCHAR(160) NOT NULL, fecha_nacimiento DATE NOT NULL, sexo VARCHAR(20) NOT"
                + " NULL, responsable VARCHAR(160), telefono VARCHAR(40), direccion VARCHAR(250),"
                + " creado TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS citas (id INT AUTO_INCREMENT PRIMARY KEY, paciente_id INT"
                + " NOT NULL, fecha DATETIME NOT NULL, motivo VARCHAR(250) NOT NULL, estado"
                + " VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA', FOREIGN KEY (paciente_id) REFERENCES"
                + " pacientes(id), INDEX(fecha))",
            "CREATE TABLE IF NOT EXISTS consultas (id INT AUTO_INCREMENT PRIMARY KEY, paciente_id"
                    + " INT NOT NULL, fecha DATETIME NOT NULL, motivo VARCHAR(250) NOT NULL,"
                    + " diagnostico TEXT, tratamiento TEXT, observaciones TEXT, FOREIGN KEY"
                    + " (paciente_id) REFERENCES pacientes(id))",
            "CREATE TABLE IF NOT EXISTS crecimiento (id INT AUTO_INCREMENT PRIMARY KEY, paciente_id"
                + " INT NOT NULL, fecha DATE NOT NULL, peso_kg DECIMAL(7,2) NOT NULL, talla_cm"
                + " DECIMAL(7,2) NOT NULL, observaciones TEXT, FOREIGN KEY (paciente_id) REFERENCES"
                + " pacientes(id))",
            "CREATE TABLE IF NOT EXISTS examenes (id INT AUTO_INCREMENT PRIMARY KEY, paciente_id"
                + " INT NOT NULL, fecha DATE NOT NULL, nombre VARCHAR(160) NOT NULL, resultado"
                + " TEXT, observaciones TEXT, FOREIGN KEY (paciente_id) REFERENCES pacientes(id))"
        };
        try (Connection cn = conexion.conectar();
                Statement st = cn.createStatement()) {
            for (String q : sql) {
                st.execute(q);
            }
            st.execute(
                    "CREATE TABLE IF NOT EXISTS archivos (id INT AUTO_INCREMENT PRIMARY KEY,"
                        + " examen_id INT NOT NULL UNIQUE, nombre VARCHAR(255) NOT NULL, contenido"
                        + " MEDIUMBLOB NOT NULL, FOREIGN KEY (examen_id) REFERENCES examenes(id) ON"
                        + " DELETE CASCADE)");
        }
    }

    public static int usuarios() throws SQLException {
        try (Connection c = conexion.conectar();
                Statement s = c.createStatement();
                ResultSet r = s.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            r.next();
            return r.getInt(1);
        }
    }

    public static void crearusuario(String nombre, String clave, String rol) throws SQLException {
        if (usuarios() > 0) {
            permiso(true);
        } else if (!"MEDICO".equals(rol)) {
            throw new SecurityException("El primer usuario debe ser médico.");
        }
        if (nombre == null
                || nombre.isBlank()
                || nombre.trim().length() > 60
                || clave == null
                || clave.length() < 8) {
            throw new IllegalArgumentException(
                    "Usuario obligatorio y contraseña de al menos ocho caracteres.");
        }
        if (!rol.equals("MEDICO") && !rol.equals("SECRETARIA")) {
            throw new IllegalArgumentException("Rol inválido.");
        }
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement(
                                "INSERT INTO usuarios(usuario,clave_hash,rol) VALUES(?,?,?)")) {
            p.setString(1, nombre);
            p.setString(2, seguridad.hash(clave));
            p.setString(3, rol);
            p.executeUpdate();
        }
    }

    public static String autenticar(String usuario, String clave) throws SQLException {
        sesion = null;
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement("SELECT clave_hash,rol FROM usuarios WHERE usuario=?")) {
            p.setString(1, usuario);
            try (ResultSet r = p.executeQuery()) {
                if (r.next() && seguridad.verificar(clave, r.getString(1))) {
                    sesion = r.getString(2);
                    return sesion;
                }
            }
        }
        return null;
    }

    public static List<Object[]> listar(modulo m, String filtro) throws SQLException {
        validar(m);
        List<Object[]> filas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id");
        for (campo f : m.getcampos()) {
            sql.append(",").append(f.getnombre());
        }
        sql.append(" FROM ").append(m.gettabla());
        if (filtro != null && !filtro.isBlank()) {
            sql.append(" WHERE ")
                    .append(
                            m.gettabla().equals("pacientes")
                                    ? "(nombre LIKE ? OR responsable LIKE ? OR telefono LIKE ? OR"
                                            + " CAST(id AS CHAR)=?)"
                                    : "paciente_id IN (SELECT id FROM pacientes WHERE nombre LIKE ?"
                                            + " OR CAST(id AS CHAR)=?)");
        }
        sql.append(
                m.gettabla().equals("pacientes")
                        ? " ORDER BY nombre,id"
                        : " ORDER BY fecha DESC,id DESC");
        try (Connection c = conexion.conectar();
                PreparedStatement p = c.prepareStatement(sql.toString())) {
            if (filtro != null && !filtro.isBlank()) {
                p.setString(1, "%" + filtro.trim() + "%");
                if (m.gettabla().equals("pacientes")) {
                    p.setString(2, "%" + filtro.trim() + "%");
                    p.setString(3, "%" + filtro.trim() + "%");
                    p.setString(4, filtro.trim());
                } else {
                    p.setString(2, filtro.trim());
                }
            }
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Object[] row = new Object[m.getcampos().length + 1];
                    for (int i = 0; i < row.length; i++) {
                        row[i] = r.getObject(i + 1);
                    }
                    filas.add(row);
                }
            }
        }
        return filas;
    }

    public static void guardar(modulo m, Integer id, String[] datos) throws Exception {
        validar(m);
        comprobar(m, id, datos);
        if (datos.length != m.getcampos().length) {
            throw new IllegalArgumentException("Campos incompletos");
        }
        for (int i = 0; i < datos.length; i++) {
            if (m.getcampos()[i].isobligatorio() && (datos[i] == null || datos[i].isBlank())) {
                throw new IllegalArgumentException("Completa: " + m.getcampos()[i].gettitulo());
            }
        }
        String columnas = "";
        String valores = "";
        String cambios = "";

        for (int i = 0; i < m.getcampos().length; i++) {
            if (i > 0) {
                columnas = columnas + ",";
                valores = valores + ",";
                cambios = cambios + ",";
            }
            String nombre = m.getcampos()[i].getnombre();
            columnas = columnas + nombre;
            valores = valores + "?";
            cambios = cambios + nombre + "=?";
        }

        String sql;
        if (id == null) {
            sql = "INSERT INTO " + m.gettabla() + " (" + columnas + ") VALUES (" + valores + ")";
        } else {
            sql = "UPDATE " + m.gettabla() + " SET " + cambios + " WHERE id=?";
        }
        try (Connection c = conexion.conectar();
                PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < datos.length; i++) {
                String val = datos[i] == null ? "" : datos[i].trim();
                String tipo = m.getcampos()[i].gettipo();
                if (val.isEmpty()) {
                    p.setNull(i + 1, Types.NULL);
                } else if (tipo.equals("date")) {
                    p.setDate(i + 1, java.sql.Date.valueOf(val));
                } else if (tipo.equals("datetime")) {
                    p.setTimestamp(
                            i + 1,
                            Timestamp.valueOf(
                                    val.replace('T', ' ') + (val.length() == 16 ? ":00" : "")));
                } else if (tipo.equals("decimal")) {
                    double d = Double.parseDouble(val);
                    if (d <= 0 || d > 300) {
                        throw new IllegalArgumentException(
                                "Medida fuera de rango: " + m.getcampos()[i].gettitulo());
                    }
                    p.setDouble(i + 1, d);
                } else if (tipo.equals("paciente")) {
                    p.setInt(i + 1, Integer.parseInt(val));
                } else {
                    p.setString(i + 1, val);
                }
            }
            if (id != null) {
                p.setInt(datos.length + 1, id);
            }
            p.executeUpdate();
        }
    }

    public static void eliminar(modulo m, int id) throws SQLException {
        validar(m);
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement("DELETE FROM " + m.gettabla() + " WHERE id=?")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    public static List<Object[]> crecimiento(int id) throws SQLException {
        permiso(true);
        List<Object[]> out = new ArrayList<>();
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement(
                                "SELECT fecha,peso_kg,talla_cm FROM crecimiento WHERE paciente_id=?"
                                        + " ORDER BY fecha")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    out.add(new Object[] {r.getDate(1), r.getDouble(2), r.getDouble(3)});
                }
            }
        }
        return out;
    }

    private static void comprobar(modulo m, Integer id, String[] datos) throws Exception {
        if (datos.length != m.getcampos().length) {
            throw new IllegalArgumentException("Datos incompletos.");
        }
        for (int i = 0; i < datos.length; i++) {
            datos[i] = datos[i] == null ? "" : datos[i].trim();
        }
        try (Connection cn = conexion.conectar()) {
            if (m.gettabla().equals("pacientes")) {
                java.time.LocalDate fecha = java.time.LocalDate.parse(datos[1]);
                if (fecha.isAfter(java.time.LocalDate.now())) {
                    throw new IllegalArgumentException(
                            "El nacimiento no puede estar en el futuro.");
                }
                if (!datos[2].equals("FEMENINO")
                        && !datos[2].equals("MASCULINO")
                        && !datos[2].equals("OTRO")) {
                    throw new IllegalArgumentException("Sexo inválido.");
                }
                try (PreparedStatement q =
                        cn.prepareStatement(
                                "SELECT id FROM pacientes WHERE LOWER(TRIM(nombre))=LOWER(?) AND"
                                        + " fecha_nacimiento=? AND id<>?")) {
                    q.setString(1, datos[0]);
                    q.setDate(2, java.sql.Date.valueOf(fecha));
                    q.setInt(3, id == null ? 0 : id);
                    try (ResultSet r = q.executeQuery()) {
                        if (r.next()) {
                            throw new IllegalArgumentException(
                                    "Ya existe un paciente con ese nombre y nacimiento.");
                        }
                    }
                }
            } else {
                int paciente = Integer.parseInt(datos[0]);
                java.time.LocalDate fecha = java.time.LocalDate.parse(datos[1].substring(0, 10));
                try (PreparedStatement q =
                        cn.prepareStatement("SELECT fecha_nacimiento FROM pacientes WHERE id=?")) {
                    q.setInt(1, paciente);
                    try (ResultSet r = q.executeQuery()) {
                        if (!r.next()) {
                            throw new IllegalArgumentException("El paciente no existe.");
                        }
                        if (fecha.isBefore(r.getDate(1).toLocalDate())) {
                            throw new IllegalArgumentException(
                                    "La fecha es anterior al nacimiento.");
                        }
                    }
                }
                if (!m.gettabla().equals("citas") && fecha.isAfter(java.time.LocalDate.now())) {
                    throw new IllegalArgumentException(
                            "El control clínico no puede tener fecha futura.");
                }
                if (m.gettabla().equals("citas")) {
                    if (!datos[3].equals("PROGRAMADA")
                            && !datos[3].equals("REALIZADA")
                            && !datos[3].equals("CANCELADA")) {
                        throw new IllegalArgumentException("Estado inválido.");
                    }
                    if (!datos[3].equals("CANCELADA")) {
                        try (PreparedStatement q =
                                cn.prepareStatement(
                                        "SELECT id FROM citas WHERE fecha=? AND estado<>'CANCELADA'"
                                                + " AND id<>?")) {
                            q.setTimestamp(
                                    1,
                                    Timestamp.valueOf(
                                            datos[1].replace('T', ' ')
                                                    + (datos[1].length() == 16 ? ":00" : "")));
                            q.setInt(2, id == null ? 0 : id);
                            try (ResultSet r = q.executeQuery()) {
                                if (r.next()) {
                                    throw new IllegalArgumentException(
                                            "Ese horario ya tiene una cita.");
                                }
                            }
                        }
                    }
                }
                if (m.gettabla().equals("crecimiento")) {
                    for (int i = 2; i <= 3; i++) {
                        double valor = Double.parseDouble(datos[i]);
                        if (!Double.isFinite(valor) || valor <= 0 || valor > 300) {
                            throw new IllegalArgumentException("Peso o talla fuera de rango.");
                        }
                    }
                }
            }
            for (int i = 0; i < datos.length; i++) {
                campo f = m.getcampos()[i];
                if (f.gettipo().equals("text")) {
                    int limite = 160;
                    String nombre = f.getnombre();
                    if (nombre.equals("diagnostico")
                            || nombre.equals("tratamiento")
                            || nombre.equals("observaciones")
                            || nombre.equals("resultado")) {
                        limite = 16000;
                    } else if (nombre.equals("direccion") || nombre.equals("motivo")) {
                        limite = 250;
                    } else if (nombre.equals("telefono")) {
                        limite = 40;
                    }
                    if (datos[i].length() > limite) {
                        throw new IllegalArgumentException(
                                "Texto demasiado largo: " + f.gettitulo());
                    }
                }
            }
        }
    }

    public static List<Object[]> pacientes() throws SQLException {
        permiso(false);
        List<Object[]> lista = new ArrayList<>();
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement("SELECT id,nombre FROM pacientes ORDER BY nombre");
                ResultSet r = p.executeQuery()) {
            while (r.next()) {
                lista.add(new Object[] {r.getInt(1), r.getString(2)});
            }
        }
        return lista;
    }

    public static void adjuntar(int examen, java.io.File archivo) throws Exception {
        permiso(true);
        if (!archivo.isFile() || archivo.length() == 0 || archivo.length() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo debe tener entre 1 byte y 10 MB.");
        }
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement(
                                "INSERT INTO archivos(examen_id,nombre,contenido) VALUES(?,?,?) ON"
                                        + " DUPLICATE KEY UPDATE"
                                        + " nombre=VALUES(nombre),contenido=VALUES(contenido)")) {
            p.setInt(1, examen);
            p.setString(2, archivo.getName());
            p.setBytes(3, java.nio.file.Files.readAllBytes(archivo.toPath()));
            p.executeUpdate();
        }
    }

    public static void descargar(int examen, java.io.File destino) throws Exception {
        permiso(true);
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement("SELECT contenido FROM archivos WHERE examen_id=?")) {
            p.setInt(1, examen);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    throw new IllegalArgumentException("Este examen no tiene archivo adjunto.");
                }
                java.nio.file.Files.write(destino.toPath(), r.getBytes(1));
            }
        }
    }

    public static String nombrearchivo(int examen) throws SQLException {
        permiso(true);
        try (Connection c = conexion.conectar();
                PreparedStatement p =
                        c.prepareStatement("SELECT nombre FROM archivos WHERE examen_id=?")) {
            p.setInt(1, examen);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }
}
