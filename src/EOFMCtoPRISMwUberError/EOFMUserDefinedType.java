package EOFMCtoPRISMwUberError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author mlb4b
 */
class EOFMUserDefinedType implements EOFMElement {

    private String name;
    private String construction;
    private Element element;
    private EOFMParser parser;
    private String prismTypeConstruction;
    private boolean isEnumerated;
    private ArrayList<String> enumerations;
    //private String defaultVal = "";
    
    public EOFMUserDefinedType() {
    }
    
    public String GetPrismTypeConstruction () {
        return prismTypeConstruction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getConstruction() {
        return construction;
    }

    public void setConstruction(String construction) {
        this.construction = construction;
    }
    
    public Element getElement() {
        return element;
    }

    public void setElement(Element xmlElement) {
        this.element = xmlElement;
        if (parser != null)
            extractElementData();
    }

    public void setParser(EOFMParser parser) {
        this.parser = parser;
        if (element != null)
            extractElementData();
    }

    public EOFMParser getParser() {
        return parser;
    }
    
    public void writeType(PRISMWriter prismWriter) {
        //prismWriter.writeType(prefix, name, construction);
        if (isEnumerated) {
            for (String e : enumerations) {
                if (!isNumeric(e)) {
                    prismWriter.writeConstant(e, PRISMWriter.VariableType.INTEGER, Integer.toString(enumerations.indexOf(e)));
                }
            }
            //prismWriter.writeConstant(name, PRISMWriter.VariableType.INTEGER, construction);
        }
    }
    
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
    public ArrayList<String> getEnumerations() {
        if (isEnumerated) {
            return enumerations;
        }
        else if (construction.substring(0, 1).startsWith("["))  {
            String[] parts = construction.replace("[", "").replace("]", "").split("..");
            String oldString = "";
            while (!parts[0].equals(oldString)) {
                oldString = parts[0];
                for (EOFMElement con: parser.getElementsOfType(EOFMConstant.class)) {
                    parts[0] = parts[0].replace(((EOFMConstant)con).getName(), ((EOFMConstant)con).getConstuction());
                }
            }
            while (!parts[1].equals(oldString)) {
                oldString = parts[1];
                for (EOFMElement con: parser.getElementsOfType(EOFMConstant.class)) {
                    parts[1] = parts[1].replace(((EOFMConstant)con).getName(), ((EOFMConstant)con).getConstuction());
                }
            }
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            enumerations = null;
            try {
                int last = Integer.parseInt(engine.eval(parts[1]).toString());
                int first = Integer.parseInt(engine.eval(parts[0]).toString());
                enumerations = new ArrayList<String>();
                for (int i = first; i <= last; i++) {
                    enumerations.add(Integer.toString(i));
                }
            } catch (ScriptException ex) {
                Logger.getLogger(EOFMUserDefinedType.class.getName()).log(Level.SEVERE, null, ex);
            }
            return enumerations;
        }
        else {
            return null;
        }
    }
    
    private void extractElementData() {
        name = parser.extractAttributeData(element, EOFM_NAME);
        parser.registerEOFMElement(getName(), this);
        construction = parser.extractElementData(element).trim();
        if (construction.startsWith("{")) {
            isEnumerated = true;
            enumerations = new ArrayList<String>(Arrays.asList((construction.replace("{","").replace("}","")).split(",")));
            for(int i = 0; i < enumerations.size(); i++) {
                enumerations.set(i, enumerations.get(i).trim());
            }
            prismTypeConstruction = "[" + enumerations.get(0)  + ".." + enumerations.get(enumerations.size() - 1) + "]";
        }
        else {
            isEnumerated = false;
            prismTypeConstruction = construction;
        }
        
        if (isEnumerated) {
            for (String e : enumerations) {
                if (!isNumeric(e)) {
                    parser.addConstant(e, enumerations.indexOf(e));
                }
            }
            //prismWriter.writeConstant(name, PRISMWriter.VariableType.INTEGER, construction);
        }
    }
    
    public Element BuildXMLDoc(Document doc) {
        Element returnElement = doc.createElement(EOFM_USERDEFINEDTYPE);
        returnElement.setAttribute(EOFM_NAME, name);
        returnElement.setTextContent(construction);
        return returnElement;
    }
    
    /*public String getDefaultValue() {
        return defaultVal;
    }*/
}
