package dao;

import java.io.InputStream;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public class conexion {
    private static final Properties p = new Properties();

    static {
        try (InputStream in = conexion.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                p.load(in);
            }
            if (Files.exists(Path.of("config.properties"))) {
                try (InputStream externo = Files.newInputStream(Path.of("config.properties"))) {
                    p.load(externo);
                }
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection conectar() throws SQLException {
        String host =
                System.getenv()
                        .getOrDefault("PEDIA_DB_HOST", p.getProperty("db.host", "localhost"));
        String port =
                System.getenv().getOrDefault("PEDIA_DB_PORT", p.getProperty("db.port", "3306"));
        String name =
                System.getenv()
                        .getOrDefault("PEDIA_DB_NAME", p.getProperty("db.name", "pediacenter_app"));
        String user =
                System.getenv().getOrDefault("PEDIA_DB_USER", p.getProperty("db.user", "root"));
        String pass =
                System.getenv().getOrDefault("PEDIA_DB_PASSWORD", p.getProperty("db.password", ""));
        return DriverManager.getConnection(
                "jdbc:mysql://"
                        + host
                        + ":"
                        + port
                        + "/"
                        + name
                        + "?useSSL=false&serverTimezone=America/Guatemala&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=15000",
                user,
                pass);
    }
}
