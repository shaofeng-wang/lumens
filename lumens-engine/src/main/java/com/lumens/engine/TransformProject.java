/*
 * Copyright Lumens Team, Inc. All Rights Reserved.
 */
package com.lumens.engine;

import com.lumens.engine.component.TransformRuleEntry;
import com.lumens.engine.component.resource.DataSource;
import com.lumens.engine.component.instrument.DataTransformator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shaofeng wang (shaofeng.cjpw@gmail.com)
 */
public class TransformProject {

    private List<DataSource> datasourceList = new ArrayList<>();
    private List<DataTransformator> transformatorList = new ArrayList<>();
    private String name;
    private String description;
    private boolean isOpen;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataSource> getDatasourceList() {
        return datasourceList;
    }

    public void setDatasourceList(List<DataSource> datasourceList) {
        this.datasourceList = datasourceList;
    }

    public void setTransformatorList(List<DataTransformator> transformatorList) {
        this.transformatorList = transformatorList;
    }

    public List<DataTransformator> getDataTransformatorList() {
        return transformatorList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StartEntry> getStartEntryList() {
        List<StartEntry> startList = new ArrayList<>();
        for (DataTransformator dt : transformatorList) {
            // build start point list
            for (TransformRuleEntry tr : dt.getTransformRuleList())
                if (tr.getSourceName().equals(dt.getName()))
                    startList.add(new StartEntry(tr.getSourceFormatName(), dt));
        }
        return startList;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() throws Exception {
        try {
            for (DataSource ds : datasourceList)
                ds.open();
            for (DataTransformator dt : transformatorList)
                dt.open();
            isOpen = true;
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    public void close() throws Exception {
        if (isOpen()) {
            for (DataSource ds : datasourceList)
                ds.close();
            for (DataTransformator dt : transformatorList)
                dt.close();
        }
        isOpen = false;
    }
}
