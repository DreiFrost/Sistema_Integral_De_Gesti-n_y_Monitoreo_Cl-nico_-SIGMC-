package servicio;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class pdfbasico {
    private static String escapar(String t) {
        return t.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    public static void escribir(File archivo, String texto) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (String linea : texto.split("\\R")) {
            String clean = linea.replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]", " ");
            while (clean.length() > 90) {
                lineas.add(clean.substring(0, 90));
                clean = clean.substring(90);
            }
            lineas.add(clean);
        }
        List<String> streams = new ArrayList<>();
        for (int ini = 0; ini < lineas.size(); ini += 49) {
            StringBuilder s = new StringBuilder("BT /F1 9 Tf 45 785 Td 13 TL ");
            for (int i = ini; i < Math.min(ini + 49, lineas.size()); i++) {
                s.append('(').append(escapar(lineas.get(i))).append(") Tj T* ");
            }
            s.append("ET");
            streams.add(s.toString());
        }
        if (streams.isEmpty()) {
            streams.add("BT ET");
        }
        int pages = streams.size(), fontid = 3 + pages * 2;
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pages; i++) {
            kids.append(3 + i * 2).append(" 0 R ");
        }
        objects.add("<< /Type /Pages /Kids [ " + kids + "] /Count " + pages + " >>");
        for (int i = 0; i < pages; i++) {
            int pageid = 3 + i * 2, contentid = pageid + 1;
            objects.add(
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font <<"
                            + " /F1 "
                            + fontid
                            + " 0 R >> >> /Contents "
                            + contentid
                            + " 0 R >>");
            byte[] b = streams.get(i).getBytes(StandardCharsets.ISO_8859_1);
            objects.add(
                    "<< /Length " + b.length + " >>\nstream\n" + streams.get(i) + "\nendstream");
        }
        objects.add(
                "<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding"
                        + " >>");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            out.write(
                    (i + 1 + " 0 obj\n" + objects.get(i) + "\nendobj\n")
                            .getBytes(StandardCharsets.ISO_8859_1));
        }
        int xref = out.size();
        out.write(
                ("xref\n0 " + (objects.size() + 1) + "\n0000000000 65535 f \n")
                        .getBytes(StandardCharsets.ISO_8859_1));
        for (int i = 1; i < offsets.size(); i++) {
            out.write(
                    String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i))
                            .getBytes(StandardCharsets.ISO_8859_1));
        }
        out.write(
                ("trailer\n<< /Size "
                                + (objects.size() + 1)
                                + " /Root 1 0 R >>\nstartxref\n"
                                + xref
                                + "\n%%EOF")
                        .getBytes(StandardCharsets.ISO_8859_1));
        try (FileOutputStream f = new FileOutputStream(archivo)) {
            out.writeTo(f);
        }
    }
}
