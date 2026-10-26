package modelo;

public class consulta {
    private int idconsulta;
    private String fecha;
    private double peso;
    private double talla;
    private String diagnostico;

    public consulta(int idconsulta, String fecha, double peso, double talla, String diagnostico) {
        this.idconsulta = idconsulta;
        this.fecha = fecha;
        this.peso = peso;
        this.talla = talla;
        this.diagnostico = diagnostico;
    }

    public int getidconsulta() {
        return idconsulta;
    }

    public String getfecha() {
        return fecha;
    }

    public double getpeso() {
        return peso;
    }

    public double gettalla() {
        return talla;
    }

    public String getdiagnostico() {
        return diagnostico;
    }

    public void setidconsulta(int idconsulta) {
        this.idconsulta = idconsulta;
    }

    public void setfecha(String fecha) {
        this.fecha = fecha;
    }

    public void setpeso(double peso) {
        this.peso = peso;
    }

    public void settalla(double talla) {
        this.talla = talla;
    }

    public void setdiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }
}
