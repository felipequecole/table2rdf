/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufscar.felipequecole.table2rdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author felipequecole
 */
public class Utils {

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    static String readBuffer(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static String removeAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return s;
    }

    public static String removeSpecialCharacters(String s) {
        s = s.replace("(", "");
        s = s.replace(")", "");
        s = s.replace("[", "");
        s = s.replace("]", "");
        s = s.replace(",", "");
        return s;
    }
    
    public static String getDataType(String s){
         if (s.contains("%")){
            return getDataType(s.replace("%", ""));
        }
        Scanner scanner = new Scanner(s);
        scanner.useLocale(Locale.US);       
        if (scanner.hasNextInt()) {
            return "http://www.w3.org/2001/XMLSchema#integer";
        } else if (scanner.hasNextDouble()) {
            return "http://www.w3.org/2001/XMLSchema#decimal";
        } else if (scanner.hasNextFloat()) {
            return "http://www.w3.org/2001/XMLSchema#decimal";
        } else {
            return "en";
        }
    }

}
