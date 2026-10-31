package modelo;

import java.util.ArrayList;

public class paciente {
    private int idpaciente;
    private String nombrecompleto;
    private String fechanacimiento;

    private ArrayList<consulta> historialconsultas;

    public paciente(int idpaciente, String nombrecompleto, String fechanacimiento) {
        this.idpaciente = idpaciente;
        this.nombrecompleto = nombrecompleto;
        this.fechanacimiento = fechanacimiento;
        this.historialconsultas = new ArrayList<>();
    }

    public void agregarconsulta(consulta nuevaconsulta) {
        this.historialconsultas.add(nuevaconsulta);
    }

    public ArrayList<consulta> gehistorialconsultas() {
        return historialconsultas;
    }

    public int getidpaciente() {
        return idpaciente;
    }

    public String getnombrecompleto() {
        return nombrecompleto;
    }

    public String getfechanacimiento() {
        return fechanacimiento;
    }

    public ArrayList<consulta> gethistorialconsultas() {
        return historialconsultas;
    }

    public void setidpaciente(int idpaciente) {
        this.idpaciente = idpaciente;
    }

    public void setnombrecompleto(String nombrecompleto) {
        this.nombrecompleto = nombrecompleto;
    }

    public void setfechanacimiento(String fechanacimiento) {
        this.fechanacimiento = fechanacimiento;
    }

    public void sethistorialconsultas(ArrayList<consulta> historialconsultas) {
        this.historialconsultas = historialconsultas;
    }
}
