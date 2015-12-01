package com.lbs.util;

import org.apache.commons.io.IOUtils;
import org.noggit.JSONUtil;
import org.noggit.ObjectBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author nullwang@hotmail.com
 *         created at 2014/5/29
 */
public class SplitFile {

    public static void main(String[] args) throws IOException {
        Logger logger = org.slf4j.LoggerFactory.getLogger(SplitFile.class);

        logger.info("main");
        File dir = new File("C:\\Users\\www\\Desktop\\lbse_peformance\\gis_data_ok_for_619\\json");
        String f = "country-boundary.txt.json";
        Object obj = ObjectBuilder.fromJSON(IOUtils.toString(new FileInputStream(new File(dir, f)), System.getProperty("inputCharSet", "UTF-8")));
        List lst = (List) obj;
        int totalFiles = 30;
        int fileZie = lst.size() / totalFiles;
        ArrayList al = new ArrayList();
        int i = 1;
        for (Object line : lst) {
            Map record = (Map) line;
            al.add(record);
            if (i++ % fileZie == 0) {
                String jsonFile = "country" + "_" + i + ".json";
                IOUtils.write(JSONUtil.toJSON(Collections.unmodifiableCollection(al)), new FileOutputStream(new File(dir, jsonFile)), System.getProperty("outputCharSet", "UTF-8"));
                al.clear();
            }
        }

        if (!al.isEmpty()) {
            String jsonFile = "country" + "_" + i + ".json";
            IOUtils.write(JSONUtil.toJSON(Collections.unmodifiableCollection(al)), new FileOutputStream(new File(dir, jsonFile)), System.getProperty("outputCharSet", "UTF-8"));
        }

    }
}
