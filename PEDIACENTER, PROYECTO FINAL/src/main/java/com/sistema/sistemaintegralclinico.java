package com.sistema;

import dao.conexion;

import ui.pediaapp;

import java.sql.Connection;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class sistemaintegralclinico {

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("America/Guatemala"));

        try (Connection cn = conexion.conectar()) {

            if (cn != null) {
                System.out.println("Base de datos conectada.");

                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                pediaapp aplicacion = new pediaapp();
                                aplicacion.iniciar();
                            }
                        });
            } else {
                System.out.println("No se pudo conectar.");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(
                    null, "No se pudo conectar. Revisa config.properties.\n" + e.getMessage());
        }
    }
}
