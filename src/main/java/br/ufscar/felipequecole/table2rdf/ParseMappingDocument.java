/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufscar.felipequecole.table2rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 *
 * @author felipequecole
 */
public class ParseMappingDocument {
    
    static String exampleNs = "http://www.example.com/ns#"; 
    static String rmlNs = "http://semweb.mmlab.be/ns/rml#";
    static String rrNs = "http://www.w3.org/ns/r2rml#";
    
    public static HashMap<String,String> parseDocument(String doc, Map parameters) throws Exception{
        HashMap<String,String> params = (HashMap) parameters;
        
        // for now, using example namespace
        
        
        Model model = ModelFactory.createDefaultModel();
        model.read(doc, "TURTLE");
        
        Resource subject = null; 
        Property type = model.
                getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#" ,"type");
        
        ResIterator resIter = model.listSubjectsWithProperty(type);
        
        while (resIter.hasNext()){
            Resource aux = resIter.nextResource();
            String value = aux.getProperty(type).getObject().toString();
            if (aux.getProperty(type).getObject().toString()
                    .equals(exampleNs + "tableMapping")){
                // for now, only one mapping for doc
                subject = aux;
            }
        }
        
        if (subject == null){
            throw new Exception("Not a valid document");
        } else {
            if (!subject.hasProperty(ResourceFactory.createProperty(rmlNs, "logicalSource"))){
                throw new Exception("No source defined");
            } else {
                StmtIterator iter = subject.listProperties();
                RDFNode subj, obj; 
                Statement aux; 
                while (iter.hasNext()){
                    aux = iter.nextStatement();                    
                    subj = aux.getPredicate();
                    obj = aux.getObject();                    
                    if (((Property) subj).getLocalName().equals("logicalSource")) {
                        params.putAll(parseSource(obj));
                    } else if (((Property) subj).getLocalName().equals("subjectMap")){
                        params.putAll(parseSubject(obj));
                    } else if (((Property) subj).getLocalName().equals("predicateObjectMap")){
                        params.putAll(parsePredicateObject(obj));
                    }
                    else if (((Property) subj).getLocalName().equals("reification")){
                        params.put("reification", obj.isLiteral()? obj.toString() : "none");
                    }
                }
            }
        }
        return params;
    }
    
    private static HashMap parseSource(RDFNode object) throws Exception{
        HashMap<String,String> ret = new HashMap();
        boolean sourceDefined = false; 
        if (object.isResource()){
            StmtIterator iter = ((Resource) object).listProperties();
            Statement aux ;
            List<String> tp = new ArrayList<String>();
            while (iter.hasNext()){
                aux = iter.nextStatement();
                if (aux.getPredicate().equals(ResourceFactory.createProperty(rmlNs, "source"))){
                    ret.put("source", aux.getObject().toString());
                    sourceDefined = true; 
                } else if (aux.getPredicate().getLocalName().equals("tablePosition")){
                    tp.add(aux.getObject().toString());
                } else if (aux.getPredicate().getLocalName().equals("CSSselector")) {
                    ret.put("tableSelector", aux.getObject().toString());
                } else {
                    ret.put(aux.getPredicate().getLocalName(), aux.getObject().toString());
                }
            }
            // doing this in order to work either with integer or with string
            ret.put("tablePosition", tp.isEmpty() ? "all" : tp.toString()
                    .replace("^^http://www.w3.org/2001/XMLSchema#integer", ""));

//            if (aux.hasProperty(ResourceFactory.createProperty(rmlNs, "source"))){
//                ret.put("source", aux.getProperty(ResourceFactory
//                        .createProperty(rmlNs, "source")).getObject().toString());
//                sourceDefined = true; 
//            }
            
        } 
        if (!sourceDefined)
            throw new Exception("No source defined");
        
        return ret; 
        
    }
    
    private static HashMap parseSubject(RDFNode object){
        HashMap<String,String> ret = new HashMap();
        if (object.isResource()){
            StmtIterator iter = ((Resource) object).listProperties();
            Statement aux ;
            while(iter.hasNext()){
                aux = iter.nextStatement();
                ret.put(aux.getPredicate().getLocalName(), aux.getObject().toString());
            }
        }
        return ret;
    }
    
    private static HashMap parsePredicateObject(RDFNode object){
        HashMap<String,String> ret = new HashMap();
        if (object.isResource()){
            StmtIterator iter = ((Resource) object).listProperties();
            Statement aux ;
            while(iter.hasNext()){
                aux = iter.nextStatement();
                ret.put(aux.getPredicate().getLocalName(), aux.getObject().toString());
            }
        }
        return ret;
    }
}
