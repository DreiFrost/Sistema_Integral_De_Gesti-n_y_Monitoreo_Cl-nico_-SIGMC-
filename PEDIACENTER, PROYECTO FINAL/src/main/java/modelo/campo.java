package modelo;

public class campo {
    private String nombre;
    private String titulo;
    private String tipo;
    private boolean obligatorio;

    public campo(String nombre, String titulo, String tipo, boolean obligatorio) {
        this.nombre = nombre;
        this.titulo = titulo;
        this.tipo = tipo;
        this.obligatorio = obligatorio;
    }

    public String getnombre() {
        return nombre;
    }

    public String gettitulo() {
        return titulo;
    }

    public String gettipo() {
        return tipo;
    }

    public boolean isobligatorio() {
        return obligatorio;
    }
}
