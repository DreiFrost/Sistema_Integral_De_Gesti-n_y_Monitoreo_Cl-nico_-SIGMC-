package dao;

import modelo.paciente;

import servicio.repositorio;

import java.sql.SQLException;
import java.util.ArrayList;

public class pacientedao {
    public ArrayList<paciente> obtenertodoslospacientes() throws SQLException {
        ArrayList<paciente> lista = new ArrayList<>();
        for (Object[] fila : repositorio.listar(repositorio.modulos[0], "")) {
            lista.add(
                    new paciente(
                            ((Number) fila[0]).intValue(), fila[1].toString(), fila[2].toString()));
        }
        return lista;
    }
}
