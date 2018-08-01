/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufscar.felipequecole.table2rdf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.parser.Tag;

/**
 *
 * @author felipequecole
 */
public class Preprocessing {

    public static void preProcess(String url,
            String tableLocation,
            String tablePosition,
            String headerRow,
            String reificationType) {
        try {
            Document page = Jsoup.connect(url).get();
            // Todo: give user the option to set the table (not hardcoded)
            Elements tables = page.select(".wikitable");
            
            Whitelist clean_list = Whitelist.none();
                clean_list.addTags("tables", "table", "tr", "td", "th", "span");
                clean_list.addAttributes("td", "column");
                clean_list.addAttributes("th", "column");
                clean_list.addAttributes("td", "group");
                clean_list.addAttributes("th", "group");
                // it's optional (just for visualizing the resulting table)
                clean_list.addAttributes("span", "style");
                clean_list.addAttributes("td", "datatype");
                clean_list.addAttributes("table", "title");
            
            Element table_parent = new Element(Tag.valueOf("tables"), "");
            if (!tablePosition.equals("all")){
                tablePosition = tablePosition.replace("[", "")
                        .replace("]", "");
                List <String> indexes = 
                        new ArrayList<String>(Arrays.asList(tablePosition.split(",")));
                
                Elements aux = tables.clone();
                tables.clear();
                for (String ind : indexes){
                    tables.add(aux.get(Integer.parseInt(ind.trim())-1));
                }
                
            }
            for (Element table : tables) {
//            Element table = tables.get(1);
                Elements title = page.select("title");
                table.attr("title", title.text());

                Elements trs = table.select("tr");
                
                // label for <th>
                int column = 0;

                Elements ths = table.select("tr:nth-child("
                        + String.valueOf(Integer.parseInt(headerRow) - 1)
                        + ") th");
                int group = 0;
                List<Integer> grouping = new ArrayList();
                for (Element th : ths) {
                    // it means that it's not a grouping [hopefully]
                    if (!th.hasAttr("colspan")) {
                        th.attr("column", String.valueOf(column++));
                        if (reificationType.equals("grouped")) {
//                       th.attr("group", String.valueOf(group++));
                            group++;
                            if (grouping.isEmpty()) {
                                grouping.add(1);
                            } else {
                                grouping.add(grouping.get(grouping.size() - 1) + 1);
                            }
                        }
                    } else if (reificationType.equals("grouped")) {
                        th.attr("group", String.valueOf(group++));
                        if (grouping.isEmpty()) {
                            grouping.add(Integer.parseInt(th.attr("colspan")));
                        } else {
                            grouping.add(grouping.get(grouping.size() - 1)
                                    + Integer.parseInt(th.attr("colspan")));
                        }

                    }
                }
                ths = table.select("tr:nth-child(" + headerRow + ") th");

                for (Element th : ths) {
                    th.attr("column", String.valueOf(column++));                  
                }
                
                // remove trailling spaces 
                // important for generating the predicate correctly
                for (Element th : table.select("th")){
                    th.select("sup").remove();
                    th.text(th.text().trim());
                }
                
                table.select("sup").remove();
                table.select("sup").remove();

                // workaround for jsoup bug (when there are duplicated elements)
                ths = table.select("tr:nth-child(" + headerRow + ") th:not([column])");
                for (Element th : ths) {
                    th.attr("column", String.valueOf(column++));
                }

                // label for <td>
                for (Element tr : trs) {
                    column = 0;
                    group = 0;
                    Elements tds = tr.select("td");
                    for (Element td : tds) {
                        td.attr("column", String.valueOf(column));
                        if (reificationType.equals("grouped")) {
                            while (group < grouping.size() && (column + 1 > grouping.get(group))) {
                                group++;
                            }
                            td.attr("group", String.valueOf(group));
                            td.text(td.text().trim());
                        }
                        column += 1;
                    }
                }

                table.select("span[style=display:none]").remove();

                for (Element td : table.select("td")) {
                    for (Element span : td.select("span[style=display:none]")) {
                        span.remove();
                    }
                }

                for (Element tr : trs) {
                    Elements tds = tr.select("td");
                    String text;
                    for (Element td : tds) {
                        td.select("span[style=display:none]").remove();
                        text = td.text();
                        text = Utils.removeSpecialCharacters(text);
                        td.text(text);
                        td.attr("dataType", Utils.getDataType(text));
                    }
                }
                
                table_parent.prepend(table.toString());
            }
            
            File fout = new File("tmp/");
                if (!fout.exists()) {
                    fout.mkdir();
                }
            
            
                PrintWriter out = new PrintWriter("tmp/table.html");
                String outputString = table_parent.toString();
                outputString = outputString.replace("&nbsp;", "");
                outputString = outputString.replace("<br>", "");
                outputString = Jsoup.clean(outputString, clean_list);
                outputString = Utils.removeAccents(outputString);
                out.write(outputString);
                out.flush();
                out.close();

        } catch (IOException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
