package ui;

import modelo.campo;
import modelo.modulo;
import servicio.repositorio;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class pediaapp implements ActionListener {
    private final Color amarillo = new Color(245, 195, 55);
    private JFrame ventana;
    private JPanel contenido, grafico;
    private String rol;
    private modulo actual;
    private JTable tabla;
    private JTextField filtro, usuario;
    private JPasswordField clave;
    private JComboBox<String> pacientes, tipo;
    private JTextArea texto;

    public void iniciar() {
        try {
            repositorio.creartablas();
            JTextField nombre = new JTextField("medico");
            JPasswordField password = new JPasswordField();
            JPasswordField repetir = new JPasswordField();
            if (repositorio.usuarios() == 0) {
                JPanel alta = formulario(
                        new String[]{"Usuario médico", "Contraseña", "Confirmar"},
                        new JComponent[]{nombre, password, repetir});
                while (true) {
                    if (!confirmar(alta, "Crear médico")) return;
                    String valor = new String(password.getPassword());
                    if (nombre.getText().trim().isEmpty() || valor.length() < 8
                            || !valor.equals(new String(repetir.getPassword()))) {
                        aviso("Revisa el usuario y las contraseñas. Mínimo 8 caracteres.");
                        continue;
                    }
                    repositorio.crearusuario(nombre.getText().trim(), valor, "MEDICO");
                    break;
                }
            }
            login();
        } catch (Exception e) {
            aviso(e.getMessage());
        }
    }

    private void login() throws Exception {
        JTextField nombre = new JTextField();
        JPasswordField password = new JPasswordField();
        JPanel acceso = new JPanel(new GridLayout(1, 2, 15, 0));
        acceso.setBackground(amarillo);
        acceso.add(formulario(new String[]{"Usuario", "Contraseña"},
                new JComponent[]{nombre, password}));
        acceso.add(new imagen());
        acceso.setPreferredSize(new Dimension(650, 300));
        while (confirmar(acceso, "PediaCenter")) {
            rol = repositorio.autenticar(nombre.getText().trim(), new String(password.getPassword()));
            if (rol != null) {
                principal(nombre.getText().trim());
                return;
            }
            aviso("Credenciales incorrectas.");
            password.setText("");
        }
    }

    private JPanel formulario(String[] etiquetas, JComponent[] entradas) {
        JPanel panel = new JPanel(new GridLayout(etiquetas.length, 2, 10, 10));
        panel.setBackground(amarillo);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        for (int i = 0; i < etiquetas.length; i++) {
            panel.add(new JLabel(etiquetas[i]));
            panel.add(entradas[i]);
        }
        return panel;
    }

    private boolean confirmar(Object panel, String titulo) {
        return JOptionPane.showConfirmDialog(ventana, panel, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, "PediaCenter",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton boton(String nombre, String accion) {
        JButton boton = new JButton(nombre);
        boton.setActionCommand(accion);
        boton.addActionListener(this);
        return boton;
    }

    private JPanel barra(JComponent... elementos) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(amarillo);
        for (JComponent elemento : elementos) panel.add(elemento);
        return panel;
    }

    private void mostrar(JComponent arriba, JComponent centro, JComponent abajo) {
        contenido.removeAll();
        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(abajo, BorderLayout.SOUTH);
        contenido.revalidate();
        contenido.repaint();
    }

    private void principal(String nombre) {
        ventana = new JFrame("PediaCenter | " + nombre + " | " + rol);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        ventana.setSize(Math.min(1100, pantalla.width - 40), Math.min(740, pantalla.height - 60));
        ventana.setLocationRelativeTo(null);
        JPanel menu = new JPanel(new GridLayout(rol.equals("MEDICO") ? 2 : 1, 0, 5, 5));
        menu.setBackground(amarillo);
        menu.add(boton("Inicio", "inicio"));
        for (int i = 0; i < repositorio.modulos.length; i++) {
            modulo m = repositorio.modulos[i];
            if (!m.isclinico() || rol.equals("MEDICO")) menu.add(boton(m.gettitulo(), "modulo" + i));
        }
        if (rol.equals("MEDICO")) {
            menu.add(boton("Gráficas", "graficas"));
            menu.add(boton("Expediente / PDF", "expediente"));
            menu.add(boton("Usuarios", "usuarios"));
}
menu.add(boton("Página web", "web"));

        
        menu.add(boton("Cerrar sesión", "salir"));
        contenido = new JPanel(new BorderLayout(10, 10));
        contenido.setBackground(new Color(255, 252, 235));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        ventana.add(menu, BorderLayout.NORTH);
        ventana.add(contenido, BorderLayout.CENTER);
        inicio();
        ventana.setVisible(true);
    }

    private void inicio() {
        mostrar(barra(new JLabel("Bienvenido a PediaCenter")), new imagen(),
                barra(new JLabel("Su bienestar, nuestra mayor prioridad.")));
    }

    public void actionPerformed(ActionEvent evento) {
        try {
            String accion = evento.getActionCommand();
            if (accion.startsWith("modulo")) {
                actual = repositorio.modulos[Integer.parseInt(accion.substring(6))];
                if (actual.isclinico()) repositorio.exigirmedico();
                registros();
            } else if (accion.equals("inicio")) {
                inicio();
            } else if (accion.equals("salir")) {
                ventana.dispose();
                repositorio.salir();
                ventana = null;
                login();
            } else if (accion.equals("buscar")) {
                cargar();
            } else if (accion.equals("nuevo") || accion.equals("editar")) {
                editor(accion.equals("nuevo") ? -1 : seleccion());
            } else if (accion.equals("eliminar")) {
                int id = ((Number) tabla.getValueAt(seleccion(), 0)).intValue();
                if (confirmar("¿Eliminar el registro #" + id + "?", "Eliminar")) {
                    repositorio.eliminar(actual, id);
                    cargar();
                }
            } else if (accion.equals("adjuntar") || accion.equals("descargar")) {
                int id = ((Number) tabla.getValueAt(seleccion(), 0)).intValue();
                boolean guardar = accion.equals("descargar");
                String nombre = guardar ? repositorio.nombrearchivo(id) : null;
                if (guardar && nombre == null) {
                    aviso("No hay archivo adjunto.");
                    return;
                }
                File archivo = archivo(guardar, nombre, false);
                if (archivo != null) {
                    if (guardar) repositorio.descargar(id, archivo);
                    else repositorio.adjuntar(id, archivo);
                    aviso("Archivo guardado.");
                }
            } else {
                repositorio.exigirmedico();
                if (accion.equals("usuarios")) {
                    usuario = new JTextField();
                    clave = new JPasswordField();
                    tipo = new JComboBox<>(new String[]{"SECRETARIA", "MEDICO"});
                    mostrar(barra(new JLabel("Usuarios")), formulario(
                            new String[]{"Usuario", "Contraseña (mínimo 8 caracteres)", "Rol"},
                            new JComponent[]{usuario, clave, tipo}),
                            barra(boton("Crear usuario", "crear")));
                } else if (accion.equals("crear")) {
                    String password = new String(clave.getPassword());
                    if (usuario.getText().trim().isEmpty() || password.length() < 8) {
                        aviso("Escribe un usuario y una contraseña de al menos 8 caracteres.");
                        return;
                    }
                    repositorio.crearusuario(usuario.getText().trim(), password,
                            tipo.getSelectedItem().toString());
                    usuario.setText("");
                    clave.setText("");
                    aviso("Usuario creado.");
                } else if (accion.equals("graficas") || accion.equals("expediente")) {
                    pacientes = selector();
                    if (accion.equals("graficas")) {
                        tipo = new JComboBox<>(new String[]{"Peso", "Talla"});
                        grafico = new JPanel(new BorderLayout());
                        grafico.add(new JLabel("Selecciona un paciente y presiona Ver."));
                        mostrar(barra(pacientes, tipo, boton("Ver", "ver")), grafico, barra());
                    } else {
                        texto = new JTextArea();
                        texto.setEditable(false);
                        mostrar(barra(pacientes, boton("Consultar", "consultar"),
                                boton("PDF", "pdf")), new JScrollPane(texto), barra());
                    }
                } else if (accion.equals("ver")) {
                    List<Object[]> datos = repositorio.crecimiento(pacienteid());
                    grafico.removeAll();
                    grafico.add(new grafica(datos, tipo.getSelectedIndex()), BorderLayout.CENTER);
                    grafico.revalidate();
                    grafico.repaint();
                } else if (accion.equals("consultar") || accion.equals("pdf")) {
                    String reporte = expediente(pacienteid());
                    if (accion.equals("consultar")) {
                        texto.setText(reporte);
                        texto.setCaretPosition(0);
                    } else {
                        File archivo = archivo(true, "expediente_" + pacienteid() + ".pdf", true);
                        if (archivo != null) {
                            servicio.pdfbasico.escribir(archivo, reporte);
                            aviso("PDF guardado.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            aviso(e.getMessage());
        }
    }

    private void registros() throws Exception {
        filtro = new JTextField(25);
        filtro.setActionCommand("buscar");
        filtro.addActionListener(this);
        String[] columnas = new String[actual.getcampos().length + 1];
        columnas[0] = "ID";
        for (int i = 1; i < columnas.length; i++) columnas[i] = actual.getcampos()[i - 1].gettitulo();
        tabla = new JTable(new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int fila, int columna) { return false; }
        });
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < columnas.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 50 : 170);
        JPanel botones = barra(boton("Nuevo", "nuevo"), boton("Editar", "editar"),
                boton("Eliminar", "eliminar"));
        if (actual.gettabla().equals("examenes")) {
            botones.add(boton("Adjuntar", "adjuntar"));
            botones.add(boton("Descargar", "descargar"));
        }
        cargar();
        mostrar(barra(new JLabel(actual.gettitulo()), filtro, boton("Buscar", "buscar")),
                new JScrollPane(tabla), botones);
    }

    private void cargar() throws Exception {
        List<Object[]> datos = repositorio.listar(actual, filtro.getText());
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        for (Object[] fila : datos) modelo.addRow(fila);
    }

    private int seleccion() {
        if (tabla.getSelectedRow() < 0) throw new IllegalArgumentException("Selecciona un registro.");
        return tabla.getSelectedRow();
    }

    private JComboBox<String> selector() throws Exception {
        JComboBox<String> lista = new JComboBox<>();
        for (Object[] paciente : repositorio.pacientes()) lista.addItem(paciente[0] + " - " + paciente[1]);
        return lista;
    }

    private int pacienteid() {
        if (pacientes.getSelectedItem() == null) throw new IllegalArgumentException("Selecciona un paciente.");
        return Integer.parseInt(pacientes.getSelectedItem().toString().split(" - ", 2)[0]);
    }

    private void editor(int fila) throws Exception {
        campo[] campos = actual.getcampos();
        String[] etiquetas = new String[campos.length];
        JComponent[] entradas = new JComponent[campos.length];
        Integer id = fila < 0 ? null : ((Number) tabla.getValueAt(fila, 0)).intValue();
        for (int i = 0; i < campos.length; i++) {
            String tipo = campos[i].gettipo();
            String valor = fila < 0 ? "" : Objects.toString(tabla.getValueAt(fila, i + 1), "");
            etiquetas[i] = campos[i].gettitulo();
            if (tipo.equals("paciente") || tipo.equals("sexo") || tipo.equals("estado")) {
                JComboBox<String> lista = tipo.equals("paciente") ? selector()
                        : new JComboBox<>(tipo.equals("sexo")
                        ? new String[]{"FEMENINO", "MASCULINO", "OTRO"}
                        : new String[]{"PROGRAMADA", "REALIZADA", "CANCELADA"});
                if (lista.getItemCount() == 0) throw new IllegalArgumentException("Primero registra un paciente.");
                for (int j = 0; j < lista.getItemCount(); j++)
                    if (lista.getItemAt(j).equals(valor) || tipo.equals("paciente")
                            && lista.getItemAt(j).startsWith(valor + " - ")) lista.setSelectedIndex(j);
                entradas[i] = lista;
            } else {
                if (fila < 0 && tipo.equals("date")) valor = LocalDate.now().toString();
                if (fila < 0 && tipo.equals("datetime"))
                    valor = LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ');
                if (tipo.equals("text") && Arrays.asList("diagnostico", "tratamiento",
                        "observaciones", "resultado").contains(campos[i].getnombre())) {
                    JTextArea area = new JTextArea(valor, 3, 22);
                    area.setLineWrap(true);
                    area.setWrapStyleWord(true);
                    entradas[i] = new JScrollPane(area);
                } else entradas[i] = new JTextField(valor, 22);
            }
        }
        JPanel panel = formulario(etiquetas, entradas);
        while (confirmar(new JScrollPane(panel), actual.gettitulo())) {
            String[] valores = new String[entradas.length];
            for (int i = 0; i < entradas.length; i++) {
                JComponent entrada = entradas[i];
                if (entrada instanceof JTextField) valores[i] = ((JTextField) entrada).getText();
                else if (entrada instanceof JScrollPane)
                    valores[i] = ((JTextArea) ((JScrollPane) entrada).getViewport().getView()).getText();
                else valores[i] = Objects.toString(((JComboBox<?>) entrada).getSelectedItem(), "");
                if (campos[i].gettipo().equals("paciente")) valores[i] = valores[i].split(" - ", 2)[0];
            }
            try {
                repositorio.guardar(actual, id, valores);
                cargar();
                return;
            } catch (Exception e) {
                aviso(e.getMessage());
            }
        }
    }

    private File archivo(boolean guardar, String nombre, boolean pdf) {
        JFileChooser selector = new JFileChooser();
        if (nombre != null) selector.setSelectedFile(new File(nombre));
        int resultado = guardar ? selector.showSaveDialog(ventana) : selector.showOpenDialog(ventana);
        if (resultado != JFileChooser.APPROVE_OPTION) return null;
        File archivo = selector.getSelectedFile();
        if (pdf && !archivo.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))
            archivo = new File(archivo.getPath() + ".pdf");
        if (guardar && archivo.exists() && !confirmar("¿Reemplazar el archivo?", "Guardar")) return null;
        return archivo;
    }

    private String expediente(int id) throws Exception {
        repositorio.exigirmedico();
        StringBuilder reporte = new StringBuilder("PEDIACENTER\nExpediente: " + id
                + "\nGenerado: " + LocalDate.now() + "\n");
        LocalDate nacimiento;
        try (Connection conexion = dao.conexion.conectar()) {
            try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM pacientes WHERE id=?")) {
                consulta.setInt(1, id);
                try (ResultSet datos = consulta.executeQuery()) {
                    if (!datos.next()) throw new IllegalArgumentException("No existe el paciente.");
                    nacimiento = datos.getDate("fecha_nacimiento").toLocalDate();
                    for (campo dato : repositorio.modulos[0].getcampos())
                        reporte.append(dato.gettitulo()).append(": ")
                                .append(Objects.toString(datos.getObject(dato.getnombre()), "-")).append("\n");
                }
            }
            for (modulo m : repositorio.modulos) {
                if (m.gettabla().equals("pacientes")) continue;
                reporte.append("\n").append(m.gettitulo()).append("\n");
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT * FROM " + m.gettabla() + " WHERE paciente_id=? ORDER BY fecha,id")) {
                    consulta.setInt(1, id);
                    try (ResultSet datos = consulta.executeQuery()) {
                        while (datos.next()) {
                            for (campo dato : m.getcampos())
                                if (!dato.getnombre().equals("paciente_id"))
                                    reporte.append(dato.gettitulo()).append(": ")
                                            .append(Objects.toString(datos.getObject(dato.getnombre()), "-")).append("\n");
                            if (m.gettabla().equals("crecimiento"))
                                reporte.append("Edad al control: ")
                                        .append(java.time.temporal.ChronoUnit.MONTHS.between(
                                                nacimiento, datos.getDate("fecha").toLocalDate())).append(" meses\n");
                            if (m.gettabla().equals("examenes"))
                                reporte.append("Adjunto: ").append(Objects.toString(
                                        repositorio.nombrearchivo(datos.getInt("id")), "Sin archivo")).append("\n");
                            reporte.append("\n");
                        }
                    }
                }
            }
        }
        return reporte.toString();
    }

    private class grafica extends JPanel {
        private final List<Object[]> datos;
        private final int columna;

        grafica(List<Object[]> datos, int columna) {
            this.datos = datos;
            this.columna = columna;
            setBackground(Color.WHITE);
        }

        protected void paintComponent(Graphics dibujo) {
            super.paintComponent(dibujo);
            Graphics2D g = (Graphics2D) dibujo.create();
            int l = 65, r = getWidth() - 45, t = 55, b = getHeight() - 60;
            g.setColor(Color.DARK_GRAY);
            g.drawString(columna == 0 ? "Peso (kg)" : "Talla (cm)", l, 25);
            if (datos.isEmpty() || r <= l || b <= t) {
                g.drawString("Sin controles para mostrar.", l, t);
                g.dispose();
                return;
            }
            double maximo = 1;
            for (Object[] dato : datos) maximo = Math.max(maximo, ((Number) dato[columna + 1]).doubleValue());
            maximo *= 1.15;
            for (int i = 0; i <= 4; i++) {
                int y = b - (b - t) * i / 4;
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(l, y, r, y);
                g.setColor(Color.DARK_GRAY);
                g.drawString(String.format(Locale.US, "%.1f", maximo * i / 4), 10, y + 4);
            }
            long inicio = ((java.sql.Date) datos.get(0)[0]).toLocalDate().toEpochDay();
            long fin = ((java.sql.Date) datos.get(datos.size() - 1)[0]).toLocalDate().toEpochDay();
            int px = -1, py = -1;
            for (int i = 0; i < datos.size(); i++) {
                Object[] dato = datos.get(i);
                long fecha = ((java.sql.Date) dato[0]).toLocalDate().toEpochDay();
                double valor = ((Number) dato[columna + 1]).doubleValue();
                int x = fin == inicio ? (l + r) / 2 : l + (int) ((fecha - inicio) * (r - l) / (fin - inicio));
                int y = b - (int) (valor / maximo * (b - t));
                g.setColor(new Color(160, 110, 15));
                if (px >= 0) g.drawLine(px, py, x, y);
                g.fillOval(x - 4, y - 4, 8, 8);
                g.drawString(String.format(Locale.US, "%.1f", valor), x - 8, y - 12);
                if (i == 0 || i == datos.size() - 1 || i % Math.max(1, (datos.size() + 5) / 6) == 0)
                    g.drawString(dato[0].toString(), Math.max(0, x - 35), b + 23);
                px = x;
                py = y;
            }
            g.dispose();
        }
    }
}