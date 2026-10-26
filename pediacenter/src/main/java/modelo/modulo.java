package modelo;

public class modulo {
    private String tabla;
    private String titulo;
    private campo[] campos;
    private boolean clinico;

    public modulo(String tabla, String titulo, campo[] campos, boolean clinico) {
        this.tabla = tabla;
        this.titulo = titulo;
        this.campos = campos;
        this.clinico = clinico;
    }

    public String gettabla() {
        return tabla;
    }

    public String gettitulo() {
        return titulo;
    }

    public campo[] getcampos() {
        return campos;
    }

    public boolean isclinico() {
        return clinico;
    }
}
