package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class imagen extends JPanel {
    private BufferedImage foto;

    public imagen() {
        setBackground(new Color(21, 65, 110));
        try (InputStream archivo = getClass().getResourceAsStream("/imagenes/pediacenter.jpg")) {
            if (archivo != null) {
                foto = ImageIO.read(archivo);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    protected void paintComponent(Graphics dibujo) {
        super.paintComponent(dibujo);
        if (foto == null) {
            return;
        }

        double escala =
                Math.min(
                        (double) getWidth() / foto.getWidth(),
                        (double) getHeight() / foto.getHeight());
        int ancho = (int) (foto.getWidth() * escala);
        int alto = (int) (foto.getHeight() * escala);
        int x = (getWidth() - ancho) / 2;
        int y = (getHeight() - alto) / 2;

        Graphics2D g = (Graphics2D) dibujo.create();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(foto, x, y, ancho, alto, null);
        g.dispose();
    }
}
