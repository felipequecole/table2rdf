package br.ufscar.felipequecole.table2rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.BasicConfigurator;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
        // configure logger for sparql-generate
//        BasicConfigurator.configure();

        try {
            // run query
            Model queryResult = Table2Rdf.runQuery(args);
            OutputStream out;
            if (!ArrayUtils.contains(args, "--output")) {
                queryResult.write(System.out, "TURTLE");
            } else {
                int index = ArrayUtils.indexOf(args, "--output");
                File fout = new File(args[index + 1]);
                if (!fout.exists()) {
                    if ((fout = fout.getParentFile()) != null) {
                        if (!fout.exists()) {
                            fout.mkdirs();
                        }
                    }
                }

                out = new FileOutputStream(args[index + 1]);
                queryResult.write(out, "TURTLE");
            }
            // delete tmp folder (probably we should delete this tmp file)
            // keeping it for now for debugging purposes
            //Utils.deleteFolder(new File("tmp/"));

        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
