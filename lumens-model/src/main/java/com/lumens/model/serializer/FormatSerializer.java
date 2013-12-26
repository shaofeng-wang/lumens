/*
 * Copyright Lumens Team, Inc. All Rights Reserved.
 */
package com.lumens.model.serializer;

import com.lumens.io.JsonSerializer;
import com.lumens.io.StringUTF8Writer;
import com.lumens.io.XmlSerializer;
import com.lumens.model.Format;
import com.lumens.model.Value;
import com.lumens.model.serializer.parser.FormatHandlerImpl;
import com.lumens.model.serializer.parser.FormatParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.InputSource;

/**
 *
 * @author shaofeng wang
 */
public class FormatSerializer implements XmlSerializer, JsonSerializer {

    private Format format;
    private boolean useIndent;
    private boolean careProperties;
    private String INDENT_OFFSET = "  ";
    private String INDENT = "";

    public FormatSerializer(Format format, boolean careProps, boolean indent) {
        this.format = format;
        this.careProperties = careProps;
        this.useIndent = indent;
    }

    public FormatSerializer(Format format, boolean careProps) {
        this.format = format;
        this.careProperties = careProps;
    }

    public FormatSerializer(Format format) {
        this.format = format;
        this.careProperties = true;
    }

    public void initIndent(String indent) {
        this.INDENT = indent;
    }

    @Override
    public void readFromXml(InputStream in) throws Exception {
        List<Format> formatList = new ArrayList<>();
        FormatParser.parse(new InputSource(in), new FormatHandlerImpl(formatList));
        for (Format child : formatList) {
            format.addChild(child);
        }
    }

    @Override
    public void writeToXml(OutputStream out) throws Exception {
        StringUTF8Writer dataOut = new StringUTF8Writer(out);
        writeFormatToXml(format, INDENT, dataOut);
    }

    private void writePropertyListToXml(Map<String, Value> properties, String indent, StringUTF8Writer out) throws IOException {
        Set<Map.Entry<String, Value>> props = properties.entrySet();
        for (Map.Entry<String, Value> en : props) {
            out.print(indent).print("<property name=\"").print(en.getKey()).print("\" type=\"").print(en.getValue().type().toString()).print("\">");
            out.print(en.getValue().getString()).println("</property>");
        }
    }

    private void writeFormatToXml(Format format, String indent, StringUTF8Writer out) throws Exception {
        boolean closeTag = false;
        out.print(indent).print("<format name=\"").print(format.
        getName()).print("\" ").
        print("form=\"").print(format.getForm().toString()).print("\" ").print("type=\"").
        print(format.getType().toString()).print("\" ");
        if (format.getPropertyList() != null && careProperties) {
            closeTag = true;
            out.println(">");
            writePropertyListToXml(format.getPropertyList(), indent + INDENT_OFFSET, out);
        }
        List<Format> children = format.getChildren();
        if (children != null && children.size() > 0) {
            if (!closeTag) {
                out.println(">");
            }
            closeTag = true;
            for (Format child : children) {
                if (child.isArray()) {
                    writeFormatToXml(child, indent + INDENT_OFFSET, out);
                } else if (child.isStruct()) {
                    writeFormatToXml(child, indent + INDENT_OFFSET, out);
                } else if (child.isField()) {
                    writeFormatToXml(child, indent + INDENT_OFFSET, out);
                }
            }
        }
        if (closeTag) {
            out.print(indent).println("</format>");
        } else {
            out.println("/>");
        }
    }

    @Override
    public void readFromJson(InputStream in) throws Exception {
    }

    @Override
    public void writeToJson(OutputStream out) throws Exception {
        ObjectMapper om = new ObjectMapper();
        JsonGenerator jGenerator = om.getJsonFactory().createJsonGenerator(out, JsonEncoding.UTF8);
        writeRootFormatToJson(format, jGenerator);
        jGenerator.flush();
    }

    public void writeToJson(JsonGenerator jGenerator) throws Exception {
        jGenerator.writeObjectFieldStart("format");
        writeFormatToJson(format, jGenerator);
        jGenerator.writeEndObject();
        jGenerator.flush();
    }

    private void writeRootFormatToJson(Format format, JsonGenerator jGenerator) throws IOException {
        jGenerator.writeStartObject();
        jGenerator.writeObjectFieldStart("format");
        writeFormatToJson(format, jGenerator);
        jGenerator.writeEndObject();
        jGenerator.writeEndObject();
    }

    private void writeFormatToJson(Format format, JsonGenerator jGenerator) throws IOException {
        jGenerator.writeStringField("name", format.getName());
        jGenerator.writeStringField("form", format.getForm().toString());
        jGenerator.writeStringField("type", format.getType().toString());
        if (format.getPropertyList() != null && careProperties) {
            writePropertyListToJson(format.getPropertyList(), jGenerator);
        }
        List<Format> children = format.getChildren();
        if (children != null && children.size() > 0) {
            jGenerator.writeArrayFieldStart("format");
            for (Format child : children) {
                jGenerator.writeStartObject();
                writeFormatToJson(child, jGenerator);
                jGenerator.writeEndObject();
            }
            jGenerator.writeEndArray();
        }
    }

    private void writePropertyListToJson(Map<String, Value> propertyList, JsonGenerator jGenerator) throws IOException {
        jGenerator.writeArrayFieldStart("property");
        for (Map.Entry<String, Value> en : propertyList.entrySet()) {
            jGenerator.writeStartObject();
            jGenerator.writeStringField("name", en.getKey());
            jGenerator.writeStringField("type", en.getValue().type().toString());
            jGenerator.writeStringField("value", en.getValue().isNull() ? "" : en.getValue().toString());
            jGenerator.writeEndObject();
        }
        jGenerator.writeEndArray();
    }
}