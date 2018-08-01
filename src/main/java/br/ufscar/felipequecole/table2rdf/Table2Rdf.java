/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufscar.felipequecole.table2rdf;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import com.github.thesmartenergy.sparql.generate.jena.engine.PlanFactory;
import com.github.thesmartenergy.sparql.generate.jena.engine.RootPlan;
import com.github.thesmartenergy.sparql.generate.jena.query.SPARQLGenerateQuery;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author felipequecole
 */
public class Table2Rdf {

    static final HashMap<String, String> PARAMS = new HashMap<String, String>();
    // default parameters
    static {
        PARAMS.put("reification", "none");
        PARAMS.put("namespace", "example::http://www.example.com/");
        PARAMS.put("subjectIndex", "1");
        PARAMS.put("headerRow", "1");
        PARAMS.put("subject", "none");
        PARAMS.put("predicate", "none");
        PARAMS.put("tableLocation", "table.wikitable");
        PARAMS.put("mapping", "none");
        PARAMS.put("tablePosition", "all");
    }

    public static Model runQuery(String[] paramsString) throws Exception {
        HashMap<String, String> params = parseParams(paramsString);
        SPARQLGenerateQuery query = prepareQuery(params);
        RootPlan plan = PlanFactory.create(query);
        return plan.exec();
    }

    /*
    TODO: Maybe give the option to ignore some columns (or use them as reification)
     */
    private static SPARQLGenerateQuery prepareQuery(HashMap params) throws IOException, Exception {
        //todo: table location not in use        
        String reificationType = (String) params.get("reification");
        Preprocessing.preProcess((String) params.get("source"),
                (String) params.get("tableLocation"),
                (String) params.get("tablePosition"),
                (String) params.get("headerRow"),
                reificationType
        );
        String queryString;

        if (reificationType.equals("grouped")) {
            InputStream queryInput = Table2Rdf.class.getClassLoader().
                    getResourceAsStream("generic_grouped.rqg");
            queryString = Utils.readBuffer(queryInput);
            queryInput.close();
        } else if (reificationType.equals("context")) {
            InputStream queryInput = Table2Rdf.class.getClassLoader().
                    getResourceAsStream("generic_context.rqg");
            queryString = Utils.readBuffer(queryInput);
            queryInput.close();
            if (((String) params.get("subject")).equals("none")
                    || ((String) params.get("predicate")).equals("none")) {
                throw new Exception("Context reification needs subject and predicate");
            }
        } else {
            InputStream queryInput = Table2Rdf.class.getClassLoader().
                    getResourceAsStream("generic_noreification.rqg");
            queryString = Utils.readBuffer(queryInput);
            queryInput.close();
        }

        String namespace = (String) params.get("namespace");
        queryString = queryString.replace("<namespace>", namespace.split("::")[1]);
        queryString = queryString.replace("<custom_prefix>", "PREFIX "
                + namespace.split("::")[0] + ": <" + namespace.split("::")[1] + ">");
        queryString = queryString.replace("<headerRow>", (String) params.get("headerRow"));
        queryString = queryString.replace("<subjectIndex>", (String) params.get("subjectIndex"));
        queryString = queryString.replace("<start_value>", (String) params.get("subjectIndex"));
        if (reificationType.equals("context")) {
            queryString = queryString.replace("<subject>", (String) params.get("subject"));
            queryString = queryString.replace("<predicate>", (String) params.get("predicate")); 
            queryString = queryString.replace("<columnPredicate>", (String) params.get("columnPredicate"));
            
        }
        SPARQLGenerateQuery query = (SPARQLGenerateQuery) QueryFactory.create(queryString, SPARQLGenerate.SYNTAX);
        PrintWriter out = new PrintWriter("tmp/query.rqg");
        out.write(queryString);
        out.flush();
        out.close();
        return query;
    }

    private static HashMap<String, String> parseParams(String[] params) throws Exception {
        HashMap<String, String> ret = new HashMap();
        List<String> paramList = new ArrayList<String>(Arrays.asList(params));
        int index;

        if (paramList.contains("--mapping")) {
            return ParseMappingDocument.parseDocument(
                    paramList.get(paramList.indexOf("--mapping") + 1),
                    PARAMS
            );
        } else {

            if (!paramList.contains("--source")) {
                helpText();
                // todo: better exception 
                throw new Exception("No source defined");
            }

            ret.put("source", paramList.get(paramList.indexOf("--source") + 1));

            for (String p : PARAMS.keySet()) {
                if (paramList.contains("--" + p)) {
                    index = paramList.indexOf("--" + p);
                    ret.put(p, paramList.get(index + 1));
                } else {
                    ret.put(p, (String) PARAMS.get(p));
                }
            }
        }

        return ret;
    }

    private static void helpText() {
        System.out.println("Mandatory fields:\n\t--source <url>: page's url");
        System.out.println("Optional fields:\n\t--namespace <prefix::uri>[default: example]");
        System.out.println("\t--tableLocation <XPath path to table>");
        System.out.println("\t--subjectIndex <number>: which column to look for subject");
        System.out.println("\t--headerRow <number>: row containing the table header");
        System.out.println("\t--reification <none,grouped,context>:[default:none]");
        System.out.println("\t--subject <uri>: Mandatory in case reification=context");
        System.out.println("\t--mappping <document.ttl>: RDF file defining"
                + " mapping properties");
    }

}
