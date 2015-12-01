package com.lbs.util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nullwang@hotmail.com
 *         created at 2014/5/27
 */
public class CountryConvert extends ChinaConvert {

    @Override
    protected Collection<File> getFiles() {
        String directory = System.getProperty("dir", ".");
        System.out.println("using directory " + directory);
        File file = new File(new File(directory), "country-boundary.txt");
        if (file.exists()) {
            return Collections.singleton(file);
        }
        return Collections.emptySet();
    }

    @Override
    protected Map convertToMap(String v) {
        int polyStart = StringUtils.indexOf(v, "MULTIPOLYGON");
        if (polyStart < 0)
            polyStart = StringUtils.indexOf(v, "POLYGON");

        String polygon = StringUtils.substring(v, polyStart);

        Map map = new HashMap();
        map.put(DISTRICT_BOUNDARY, polygon);

        String[] results = StringUtils.split(StringUtils.substring(v, 0, polyStart), ",");
        map.put(CODE, results[0]);
        map.put(ID,results[0]);
        map.put(COUNTRY_NAME, results[1]);
        return map;
    }

    public static void main(String[] args) {
        CountryConvert countryConvert = new CountryConvert();
        countryConvert.process();
    }


}
