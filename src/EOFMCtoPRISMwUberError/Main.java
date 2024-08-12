package EOFMCtoPRISMwUberError;

import com.bpodgursky.jbool_expressions.*;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
//import com.bpodgursky.jbool_expressions.Expression;
//import com.bpodgursky.jbool_expressions.Variable;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import  svetlana.similaritycheching.*;

/*import com.bpodgursky.jbool_expressions.*;
import com.bpodgursky.jbool_expressions.rules.RuleSet;*/

/**
 *
 * @author mlb4b
 */
public class Main {
    
    private static EOFMParser parser;
    private static EOFMs ofms;
    private static CommandExecuter executer;
    
    
    //public static int TRANSITION_ERROR_NUMBER;
    //public static boolean ALLOW_SPECIFIC = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        setLookAndFeel();
        
        /*Expression<String> expr = And.of(Variable.of("A"),
        Variable.of("B"),
        Or.of(Variable.of("C"), Not.of(Variable.of("C"))));
        System.out.println(expr);
        System.out.println(RuleSet.toDNF(expr));*/

        CmdLineParser clparser = new CmdLineParser();

        CmdLineParser.Option ifile = clparser.addStringOption('i', "input-file");
        CmdLineParser.Option ofile = clparser.addStringOption('o', "output-file");
        //CmdLineParser.Option runsal = clparser.addBooleanOption('s', "run-sal");
        //CmdLineParser.Option verbosity = clparser.addIntegerOption('v', "verbosity");

        try {
            clparser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
        }

        //Boolean runsalValue = (Boolean)clparser.getOptionValue(runsal);
        //Integer verbosityValue = (Integer)clparser.getOptionValue(verbosity);
        String ifileValue = (String)clparser.getOptionValue(ifile);
        String ofileValue = (String)clparser.getOptionValue(ofile);

        GenericFileFilter fileFilter = new GenericFileFilter("xml", "(*.xml) EOFM XML Files");
        final JFileChooser fileChooser = new JFileChooser();
        if (ifileValue == null) {
            fileChooser.setFileFilter(fileFilter);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                ifileValue = fileChooser.getSelectedFile().getAbsolutePath();
            }
            if (ifileValue != null && ofileValue == null) {
                fileFilter.setExtension("pm");
                fileFilter.setDescription("(*.pm) PRISM Files");
                fileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().getName().replace(".xml", ".pm")));
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    ofileValue = fileChooser.getSelectedFile().getAbsolutePath();
                }
            } 
        }
        else if (ofileValue == null) { 
            ofileValue = ifileValue.replace(".xml", ".pm");
        }
        
        if (ifileValue != null && ofileValue != null) {
            parser = new EOFMParser();
            parser.SetDocument(ifileValue);

            ofms = new EOFMs();
            ofms.setElement(parser.GetDocumentRoot());
            ofms.setParser(parser);

            writeUserModel(ofileValue, ifileValue);
            //writeParsedXML(ofileValue.replace(".pm", "_Out.xml"));
            //if (verbosityValue == null)
            //    verbosityValue = 0;
            //if (runsalValue != null && runsalValue)
            //    verifySalModel(ofileValue, verbosityValue);
            //parser.prepForConditionProcessing();
            //parser.processVariablesAndConstants();
            //System.out.println(parser.getSimilarityConstruction("(aEnterDrug = actExecuting) & (aReadDrug = actDone) & (iDrugName != lDrugName)"));
            //System.out.println(parser.getSimilarityConstruction("(hRarrow_2 = actReady) & (aSelect = actExecuting)"));
            //System.out.println(parser.getSimilarityConstruction("(hEnter_3 = actReady) & (aFill = actExecuting)"));
            //System.out.println(parser.getSimilarityConstruction("(hTab_4 = actReady) & (aEmpty = actExecuting) | (aEmpty = actReady)"));
            //System.out.println(parser.getSimilarityConstruction("(iSelected = Bananas AND iPointed /= Bananas) OR (iSelected = Pears AND iPointed /= Pears) OR (iSelected = Cherries AND iPointed /= Cherries) OR (iSelected = Strawberries AND iPointed /= Strawberries)"));

            //System.out.println(parser.getSimilarityConstruction("iState = SelectingState AND iSelected /= iPointed"));
            //System.out.println(parser.getSimilarityConstruction("iState = FillingEmptyingState AND iSelected = iPointed"));
        }
    }
    
    private static void writeUserModel(String fileName, String inFile) {
        PRISMWriter prismWriter = new PRISMWriter(fileName);
        try {
            prismWriter.writeComment("File created by the EOFM to PRISM Translator with Error Generation");
            prismWriter.writeComment("Version Compiled On :" + getCompileTimeStamp(Main.class).toString());
            prismWriter.writeComment("Translation Date: " + (new Date()).toString());
            prismWriter.writeComment("Input File: " + inFile);
            prismWriter.writeComment("Output File: " + fileName);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        prismWriter.writeBlankLine();
        prismWriter.writeStartOfFile(PRISMWriter.ModelType.MDP);
        prismWriter.writeBlankLine();
        
        ofms.writeConstantsAndTypes(prismWriter, true);
        
        prismWriter.writeBlankLine();
        
        ofms.writeModulesAndInitializations(prismWriter);
        //prismWriter.writeEndOfFile("");
        prismWriter.closeFile();
    }
    
    private static Date getCompileTimeStamp( Class<?> cls ) throws IOException
    {
       ClassLoader loader = cls.getClassLoader();
       String filename = cls.getName().replace('.', '/') + ".class";
       URL resource=( loader!=null ) ?
                    loader.getResource( filename ) :
                    ClassLoader.getSystemResource( filename );
       URLConnection connection = resource.openConnection();
       long time = connection.getLastModified();
       return( time != 0L ) ? new Date( time ) : null;
    }

    
    private static void writeParsedXML(String fileName) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.newDocument();
            Element e = ofms.BuildXMLDoc(doc);
            //doc.appendChild(e);

            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutputCharStream(new java.io.FileWriter(fileName));
            serializer.serialize(doc);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*private static void verifySalModel(String fileName, int verbosityValue) {
        File salFile = new File(fileName);
        executer = new CommandExecuter();
        executer.executeCommand("sal-smc -v " + verbosityValue + " " + salFile.getAbsolutePath());
        TextDataDisplay tdd = new TextDataDisplay(null, true);
        tdd.showTextData(executer.getOutputAsString() + "\n" + executer.getErrorOutputAsString());
    }*/
    
    //sets the look and feel to the system default
    public static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
