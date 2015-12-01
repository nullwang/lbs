package com.lbs.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nullwang@hotmail.com
 *         created at 2014/5/28
 */
public class ExchangeLatLon {

    public static void main(String[] args) throws IOException {

        //List<String> list = IOUtils.readLines(new FileInputStream(new File("C:\\Users\\www\\Desktop\\lbse_peformance\\0528_gis.txt")), "UTF-8");
        List<String> list = new ArrayList<String>();
        list.add("41.934977 109.866028,41.931145 109.872551,41.878508 109.846115,41.822502 109.880104,41.822246 109.895554,41.81457 109.926453,41.816361 109.949112,41.822502 109.982414,41.821734 110.001297,41.815082 110.044212,41.812267 110.057602,41.802287 110.056915,41.790769 110.048676,41.780017 110.044212,41.770544 110.044556,41.767215 110.047302,41.745957 110.039749,41.742627 110.02533,41.742627 110.018806,41.73571 110.01709,41.734173 110.014,41.715724 110.00473,41.680605 109.987907,41.680605 109.987907,41.650597 109.975891,41.656497 109.961472,41.671117 109.926796,41.684194 109.904137,41.693937 109.904137,41.714955 109.892464,41.725718 109.885941,41.733917 109.872551,41.74442 109.859505,41.755434 109.858131,41.767983 109.854698,41.812779 109.842682,41.883876 109.830322");


        List<String> ret = new ArrayList<String>();
        for (String line : list) {
            StringBuilder sb = new StringBuilder();
            String[] words = StringUtils.split(line, ",");
            for (String word : words) {
                String[] result = StringUtils.split(word);
                if (result.length > 2) {
                    sb.append(result[0]);
                    sb.append(" ").append(result[2]).append(" ").append(result[1]).append(",");
                } else if (result.length > 1) {
                    sb.append(result[1]).append(" ").append(result[0]).append(",");
                }
            }
            ret.add(sb.toString());
        }

        IOUtils.writeLines(ret, null, new FileOutputStream("t.json"));
    }


}
