package EOFMCtoPRISMwUberError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.Hashtable;
import java.util.regex.Pattern;
import javax.script.ScriptException;
import org.w3c.dom.NamedNodeMap;
import svetlana.similaritycheching.Var;
import svetlana.similaritycheching.boolTransformer;
import svetlana.similaritycheching.parser;

public class EOFMParser {
    
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String EOFM_SCHEMA = "OFMr7.xsd";

    private Document doc;
    private Hashtable<String, EOFMElement> names = new Hashtable<String, EOFMElement>();
    private int suffixCounter = 0;
    
    //members for process condition logic
    private parser conditionParser = new parser();
    private boolTransformer boolMaker = new boolTransformer();
    private ArrayList<String> booleanVars = new ArrayList(); 
    
    public void SetDocument(String fileName) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            //docFactory.setValidating(true);
            //docFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            //docFactory.setAttribute(JAXP_SCHEMA_SOURCE, EOFM_SCHEMA);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(fileName);
        } catch (SAXException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(2);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(EOFMParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Element GetDocumentRoot() {
        if (doc != null)
            return  doc.getDocumentElement();
        else
            return null;
    }
    
    public ArrayList<String> extractMultipleElementAttributes(String tagName, Element callerElement, String attributeName) {
        ArrayList<String> attributeData = new ArrayList<String>();
        NodeList elementNodes = callerElement.getElementsByTagName(tagName);
        for (int i = 0; i < elementNodes.getLength(); i++) {
            attributeData.add(((Element)elementNodes.item(i)).getAttribute(attributeName));
        }
        return attributeData;
    }
    
    public ArrayList extractMultipleElements(String tagName, Element callerElement, Class targetClass) {
        NodeList elementNodes = callerElement.getChildNodes();
        ArrayList<EOFMElement> elementObjects = new ArrayList<EOFMElement>();
        if(elementNodes != null && elementNodes.getLength() > 0) {
            for(int i = 0; i < elementNodes.getLength(); i++) {
                try {
                    if (elementNodes.item(i).getNodeName().equals(tagName)) {
                        EOFMElement elementObject = (EOFMElement)targetClass.newInstance();
                        elementObject.setParser(this);
                        elementObject.setElement((Element)elementNodes.item(i));
                        elementObjects.add(elementObject);
                    }
                } catch (InstantiationException ex) {
                    Logger.getLogger(EOFMParser.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(EOFMParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return elementObjects;
    }
    
    public EOFMElement extractSingleElement(String tagName, Element callerElement, Class targetClass) {
        ArrayList returnedArray = extractMultipleElements(tagName, callerElement, targetClass);
        return (EOFMElement)returnedArray.get(0);
    }
    
    public ArrayList extractMultipleElementTypes(Hashtable<String, Class> tagToClass, Element callerElement) {
        NodeList elementNodes = callerElement.getChildNodes();
        ArrayList<EOFMElement> elementObjects = new ArrayList<EOFMElement>();
        if(elementNodes != null && elementNodes.getLength() > 0) {
            for(int i = 0; i < elementNodes.getLength(); i++) {
                Class targetClass = (Class)tagToClass.get(elementNodes.item(i).getNodeName());
                if (targetClass != null) {
                    try {
                        EOFMElement elementObject = (EOFMElement)targetClass.newInstance();
                        elementObject.setParser(this);
                        elementObject.setElement((Element)elementNodes.item(i));
                        elementObjects.add(elementObject);
                    } catch (InstantiationException ex) {
                        Logger.getLogger(EOFMParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(EOFMParser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return elementObjects;
    }
    
    public String extractTagName(Element callerElement) {
        return callerElement.getNodeName();
    }
    
    public String extractElementData(Element callerElement) {
        if (callerElement.getFirstChild() == null)
            return "";
        else
            return callerElement.getFirstChild().getNodeValue();
    }
    
    public String extractChildElementData(Element callerElement, String tagName) {
        NodeList elementNodes = callerElement.getChildNodes();
        if (elementNodes != null) {
            for(int i = 0; i < elementNodes.getLength(); i++) {
                if (elementNodes.item(i).getNodeName().equals(tagName))
                    return elementNodes.item(i).getFirstChild().getNodeValue();
            }
        }
        return "";
    }
    
    public String extractAttributeData(Element callerElement, String attributeName) {
        String returnString = callerElement.getAttribute(attributeName);
        if (returnString == null)
            returnString = "";
        return returnString;
    }
    
    public Element getXMLChildXMLElment(Element callerElement, String tagName) {
        NodeList elementNodes = callerElement.getChildNodes();
        if(elementNodes != null && elementNodes.getLength() > 0) {
            for(int i = 0; i < elementNodes.getLength(); i++) {
                if (elementNodes.item(i).getNodeName().equals(tagName)) {
                    return (Element)elementNodes.item(i);
                }
            }
        }
        return null;
    }
    
    public void registerEOFMElement(String key, EOFMElement ofmElementObject) {
        names.put(key, ofmElementObject);
    }
    
    public EOFMElement getEOFMElement(String key) {
        return names.get(key);
    }
    
    public ArrayList<EOFMElement> getElementsOfType(Class cls) {
        ArrayList<EOFMElement> clsObjects = new ArrayList<EOFMElement>();
        for (EOFMElement el: names.values()) {
            if (cls.isInstance(el)) {
                clsObjects.add(el);
            }
        }
        return clsObjects;
    }
    
    public String getUniqueSuffix() {
        suffixCounter++;
        return "_" + Integer.toString(suffixCounter);
    }
    
    //condition processign functions
    public void addVariable(String name, int low, int high) {
        conditionParser.addVar(name, low, high);
    }
    
    public void addVariable(String name, String low, String high) {
        conditionParser.addVar(name, low, high);
    }
    
    public void addVariable(String name, String prismTypeConstruction) {
        String vals[] = prismTypeConstruction.replace("[", "").replace("]", "").split(Pattern.quote(".."));
        addVariable(name, vals[0].trim(), vals[1].trim());
    }
    
    public void addBooleanVariable(String name) {
        conditionParser.addVar(name, 0, 1);
        booleanVars.add(name);
    }
    
    public void addConstant(String name, int val) {
        conditionParser.addConst(name, val);
    }
    
    public void addConstant(String name, String val) {
        conditionParser.addConst(name, val);
    }
    
    public void addConstant(String name, boolean val) {
        conditionParser.addConst(name, val ? 1 : 0);
        booleanVars.add(name);
    }
    
    public void processVariablesAndConstants() {
        try {
            conditionParser.evalConsts();
            conditionParser.evalVars(); //added
            /*HashMap<String, Var> vars = conditionParser.getVars();
            HashMap<String, Integer> consts =  conditionParser.getConsts();
            System.out.println("Consts:");
            System.out.println("Vars:");
            for (String k : vars.keySet()) {
                System.out.println("    " + k  + " : " + Integer.toString(vars.get(k).getLower()) + ", " + Integer.toString(vars.get(k).getUpper()));
            }*/
        } catch (ScriptException ex) {
            Logger.getLogger(EOFMParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getCNFCondition(String condition) {
        try {
            /*parser myParser = new parser();
            HashMap<String, Integer> oldConsts = conditionParser.getConsts();
            HashMap<String, Var> oldVars = conditionParser.getVars();
            HashMap<String, Integer> consts = new HashMap();
            HashMap<String, Var> vars = new HashMap();
            
            for (String k : oldConsts.keySet()) {
                if (condition.contains(k) && !k.equals("Done")) {
                    System.out.println("Const: " + k);
                    myParser.addConst(k, oldConsts.get(k));
                }
            }
            for (String k : oldVars.keySet()) {
                if (condition.contains(k)) {
                    System.out.println("Var: " + k);
                    myParser.addVar(oldVars.get(k));
                }
            }*/
            
            return boolMaker.toCNForigVars(condition, conditionParser);
        } catch (ScriptException ex) {
            System.out.println("error");
            Logger.getLogger(EOFMParser.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    public String getSimilarityConstruction(String condition) {
        //this cleans the condition to make it easy to replace boolean variables with their integer equivalents
        if (condition.isBlank()) {
            return "1";
        }
        else {
            String cleanCondition = condition;
            int length;
            do {
                length = cleanCondition.length();
                cleanCondition = cleanCondition.replace("  ", " ");
            }while (cleanCondition.length() < length);
            cleanCondition = cleanCondition.replace("TRUE",  "1").replace("true",  "1")
                                           .replace("FALSE", "1").replace("false", "1")
                                           .replace(" =",    "=").replace("= ",    "=")
                                           .replace("(", " ( ").replace(")", " ) ");
            //remaining boolean variables (that aren;t already integer equalities) should be isolated with spaces on either side at this point " varname "
            //now replace isolated boolean vars with integer equalities
            for (String booleanName: booleanVars) {
                cleanCondition = cleanCondition.replace(" " + booleanName + " ", " " + booleanName + "=1 ")
                                               .replace(" " + booleanName + " ", " " + booleanName + "=0 ");
            }
            cleanCondition = cleanCondition.replace("=", " = ").replace("/ =", "!=").replace("! =", "!=");
            int conLength = cleanCondition.length() + 1;
            while (conLength > cleanCondition.length()) {
                conLength = cleanCondition.length();
                cleanCondition = cleanCondition.replace("  ", " ")
                                               .replace("( ", "(")
                                               .replace(" )", ")").trim();
            }
            
            System.out.println("cleanCondition: " + cleanCondition);
            String clauseString = getCNFCondition(cleanCondition);
            System.out.println("clauseString: " + clauseString);
            String noSurrounding = clauseString.substring(1, clauseString.length() - 1);
            if (noSurrounding.contains("(") || noSurrounding.contains(")")) {
                clauseString = clauseString.replace("((", "(").replace("))", ")");
            }
            else {
                clauseString = clauseString.replace(PRISMWriter.BOOLEAN_AND, ") " + PRISMWriter.BOOLEAN_AND + " (");
            }
            String [] clauses = clauseString.split(PRISMWriter.BOOLEAN_AND);
            String construction = "";
            for (int i = 0; i < clauses.length; i++) {
                construction += "(" + PRISMWriter.getIfElseConstruction(clauses[i].trim(), "1", "0") + ")";
                if (i < clauses.length - 1) {
                    construction += PRISMWriter.PLUS;
                }
            }
            return "(" + construction + ")" + PRISMWriter.DIVIDE + Integer.toString(clauses.length);
        }
    }
}
