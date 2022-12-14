package com.planner.util.wta;

import com.kairos.planner.vrp.taskplanning.model.LocationPair;
import com.kairos.planner.vrp.taskplanning.model.LocationPairDifference;
import com.kairos.planner.vrp.taskplanning.solution.VrpTaskPlanningSolution;
import com.kairos.shiftplanning.solution.ShiftPlanningSolution;
import com.kairos.shiftplanning.utils.LocalDateConverter;
import com.kairos.shiftplanning.utils.LocalTimeConverter;
import com.kairos.shiftplanning.utils.ZonedDateTimeConverter;
import com.planner.service.taskPlanningService.PlannerService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.optaplanner.persistence.xstream.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreXStreamConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class FileIOUtil {
    private static Logger log= LoggerFactory.getLogger(PlannerService.class);
    public static void writeShiftPlanningXMLToFile(ShiftPlanningSolution solution, String fileName) {
        try {
            XStream xstream = new XStream(new PureJavaReflectionProvider());
            //xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
            xstream.setMode(XStream.ID_REFERENCES);
            xstream.registerConverter(new ZonedDateTimeConverter());
            xstream.registerConverter(new LocalTimeConverter());
            xstream.registerConverter(new LocalDateConverter());
            // xstream.registerConverter(new JodaTimeConverterNoTZ());
            xstream.registerConverter(new HardMediumSoftLongScoreXStreamConverter());
            String xmlString = xstream.toXML(solution);
            writeXml(xmlString, fileName);
        }catch(Throwable e){
            log.error("soe:",e);
            throw e;
        }
    }
    public static void writeVrpPlanningXMLToFile(VrpTaskPlanningSolution solution, String fileName) {
        try {
            XStream xstream = new XStream(new PureJavaReflectionProvider());
            //xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
            xstream.setMode(XStream.ID_REFERENCES);
            /*xstream.registerConverter(new JodaTimeConverter());
            xstream.registerConverter(new JodaLocalTimeConverter());
            xstream.registerConverter(new JodaLocalDateConverter());*/
            // xstream.registerConverter(new JodaTimeConverterNoTZ());

            xstream.processAnnotations(LocationPair.class);
            xstream.processAnnotations(LocationPairDifference.class);

            xstream.registerConverter(new HardMediumSoftLongScoreXStreamConverter());
            String xmlString = xstream.toXML(solution);
            writeXml(xmlString, fileName);
        }catch(Throwable e){
            log.error("soe:",e);
            throw e;
        }
    }
    public static void writeXMLDocumentToFile(Document document, String fileName) {
        try {
            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(new File(fileName));
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        }catch(Throwable e){
            log.error("soe:",e);
        }
    }
    public static  void writeXml(String xmlString,String fileName){
        try(PrintWriter out=new PrintWriter(new File("" +fileName+".xml"))) {
            out.write(xmlString);
        } catch (FileNotFoundException | NullPointerException e) {
            log.error(e.getMessage());
        }
    }

    public static void copyFileContent(File baseFile, File file) {
        try(FileReader fr=new FileReader(baseFile); FileWriter fw=new FileWriter(file)) {
            int b=0;
            while ((b=fr.read())!=-1){
                fw.write(b);
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }
}
