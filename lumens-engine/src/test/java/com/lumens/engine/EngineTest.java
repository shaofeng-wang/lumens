package com.lumens.engine;

import com.lumens.connector.Connector;
import com.lumens.connector.Direction;
import com.lumens.connector.webservice.WebServiceConnector;
import com.lumens.engine.component.DataSource;
import com.lumens.engine.component.DataTransformation;
import com.lumens.engine.component.TransformRuleEntry;
import com.lumens.engine.run.TransformEngine;
import com.lumens.engine.serializer.ProjectSerializer;
import com.lumens.engine.serializer.ProjectJsonParser;
import com.lumens.model.Format;
import com.lumens.model.Value;
import com.lumens.model.serializer.FormatSerializer;
import com.lumens.processor.transform.TransformRule;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertTrue;
import org.apache.commons.io.IOUtils;

public class EngineTest extends TestCase {

    public EngineTest(String testName) {
        super(testName);
    }

    public void testEngine1() throws Exception {
        int nameCounter = 1;
        // Create ws connector to read data
        HashMap<String, Value> props = new HashMap<>();
        props.put(WebServiceConnector.WSDL, new Value(getClass().getResource("/wsdl/ChinaOpenFundWS.asmx").toString()));
        props.put(WebServiceConnector.PROXY_ADDR, new Value("web-proxy.atl.hp.com"));
        props.put(WebServiceConnector.PROXY_PORT, new Value(8080));
        DataSource datasource = new DataSource(WebServiceConnector.class.getName());
        datasource.setName("ChinaMobile-WebService-SOAP");
        datasource.setPropertyList(props);
        datasource.open();
        datasource.setDescription("this is testing demo datasource for web service");

        // Expand format tree
        //*******************************************************************************************
        Map<String, Format> consumes = datasource.getFormatList(Direction.IN);
        Connector connector = datasource.getConnector();
        Format getOpenFundStringRequest = connector.getFormat(consumes.get("getOpenFundString"), "getOpenFundString.userID", Direction.IN);
        Map<String, Format> produces = datasource.getFormatList(Direction.OUT);
        Format getOpenFundStringResponse = connector.getFormat(produces.get("getOpenFundString"), "getOpenFundStringResponse.getOpenFundStringResult.string", Direction.OUT);
        new FormatSerializer(getOpenFundStringRequest).writeToXml(System.out);
        // Register format
        String targetName = getOpenFundStringRequest.getName() + (nameCounter++);
        // The code is used to create a format copy for registered request
        getOpenFundStringRequest = getOpenFundStringRequest.recursiveClone();
        getOpenFundStringResponse = getOpenFundStringResponse.recursiveClone();
        datasource.registerFormat(targetName, getOpenFundStringRequest, Direction.IN);
        datasource.registerFormat(targetName, getOpenFundStringResponse, Direction.OUT);

        String targetName2 = getOpenFundStringRequest.getName() + (nameCounter++);
        datasource.registerFormat(targetName2, getOpenFundStringRequest, Direction.IN);
        datasource.registerFormat(targetName2, getOpenFundStringResponse, Direction.OUT);

        //******************************************************************************************
        // Create transformation to a data source
        DataTransformation callGetOpenFundString = new DataTransformation();
        callGetOpenFundString.setName("GetOpenFundString-WS-Transform");
        callGetOpenFundString.setDescription("Test DT 1");

        DataTransformation callGetOpenFundString2 = new DataTransformation();
        callGetOpenFundString2.setName("GetOpenFundString2-WS-Transform");
        callGetOpenFundString2.setDescription("Test DT 2");

        // Link transform call to datasource webservice getOpenFundString
        callGetOpenFundString.targetTo(datasource);
        datasource.targetTo(callGetOpenFundString2);
        callGetOpenFundString2.targetTo(datasource);

        // Create start point transformation
        String startPoint = "startWS";
        TransformRule rule1 = new TransformRule(getOpenFundStringRequest);
        rule1.getRuleItem("getOpenFundString.userID").setScript("\"123\"");
        callGetOpenFundString.registerRule(new TransformRuleEntry(startPoint, targetName, rule1));

        // Create the loop transformation datasource->transformation->datasource
        TransformRule rule2 = new TransformRule(getOpenFundStringRequest);
        rule2.getRuleItem("getOpenFundString.userID").setScript("@getOpenFundStringResponse.getOpenFundStringResult.string");
        callGetOpenFundString2.registerRule(new TransformRuleEntry(targetName, targetName2, rule2));

        //*************Test project********************************************
        TransformProject project = new TransformProject();
        project.setName("The demo project");
        project.setDescription("test project description demo");
        project.getStartEntryList().add(new StartEntry("startWS", callGetOpenFundString));
        List<DataSource> dsList = project.getDatasourceList();
        List<DataTransformation> dtList = project.getDataTransformationList();
        dsList.add(datasource);
        dtList.add(callGetOpenFundString);
        dtList.add(callGetOpenFundString2);
        TransformProject newProject = doTestProjectSerialize(project);
        //**********************************************************************
        // Execute all start rules to drive the ws connector
        TransformEngine stEngine = new TransformEngine();
        stEngine.execute(newProject);
    }

    private TransformProject doTestProjectSerialize(TransformProject project) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ProjectSerializer(project).writeToXml(System.out);
        new ProjectSerializer(project).writeToJson(System.out);
        System.out.println();
        InputStream in = null;
        try {
            in = EngineTest.class.getResourceAsStream("/xml/project.xml");
            // Read project and write it again
            TransformProject newProject = new TransformProject();
            ProjectSerializer projXml = new ProjectSerializer(newProject);
            projXml.readFromXml(in);
            //projXml.write(System.out);
            return newProject;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void TtestSerializeJson() throws Exception {
        TransformProject project = new TransformProject();
        new ProjectJsonParser(project).parse(getResourceAsByteArrayInputStream("/json/project.json"));
        assertTrue(project.getDatasourceList().size() == 1);
        assertTrue(project.getDataTransformationList().size() == 2);
    }

    public InputStream getResourceAsByteArrayInputStream(String url) throws IOException {
        InputStream in = null;
        try {
            in = EngineTest.class.getResourceAsStream(url);
            ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(in));
            return in;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void testEmpty() {
        assertTrue(true);
    }
}
