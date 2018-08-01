# Tables2RDF

### Java Tool
#### How to use
- compile:  ```mvn clean package```
- example of use (using a definition rdf): ``` java -jar target/T2RDF.jar --mapping <mapping.ttl> [--output <outputfile>]```
  - **Examples for mapping documents can be found in mappings folder**
- testing other types of table: 
  - no-reification: ```java -jar target/T2RDF.jar --source https://en.wikipedia.org/wiki/Outline_of_Brazil --subjectIndex 2 [--output <output file>]```
  - columns grouped by context: ```java -jar target/T2RDF.jar --source https://en.wikipedia.org/wiki/List_of_sovereign_states_and_dependent_territories_by_mortality_rate --headerRow 2 --reification grouped``` 
  - columns that need information from outside the table: ```java -jar target/T2RDF.jar --source https://en.wikipedia.org/wiki/World_population_estimates --reification context --subject http://www.example.com/World --predicate http://www.example.com/Population```
  
- notes: 
  - if no output is defined, the result will be printed on the terminal
  - subjectIndex indicates in which column the subject is.
  - currently having issued w/ big tables.
  
  #### Predicates 
  
|      Predicate     |                                        Description                                       |             Values             |      Default      |
|:------------------:|:----------------------------------------------------------------------------------------:|:------------------------------:|:-----------------:|
| ex:reification     | Which type of table and thus reification                                                 | {"none", "grouped", "context"} | "none"            |
| ex:CSSselector     | Selector to find the table in the input HTML page                                        | Any valid css selector         | "table.wikitable" |
| ex:tablePosition   | Index of the specific target table (in that selector)                                    | {"all", numeric value}         | "all"             |
| ex:columnPredicate | Concatenates the value of the column in that index to the predicate [only for "context"] | Numeric value                  | -                 |
| ex:headerRow       | Indicates the row to look for the correct header for creating the predicates             | Numeric value                  | 1                 |
| ex:subjectIndex    | In which column the subject can be found                                                 | Numeric value                  | 1                 |

