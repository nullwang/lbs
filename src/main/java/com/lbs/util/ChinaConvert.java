package com.lbs.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.noggit.JSONUtil;
import org.noggit.ObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * @author nullwang@hotmail.com
 *         created at 2014/5/21
 */

public class ChinaConvert {

    private Logger logger = LoggerFactory.getLogger(ChinaConvert.class);

    protected HttpSolrServer getSolrServer() {
        String url = System.getProperty("url", "http://localhost:8983/solr/cityCollection");
        logger.info("using url " + url);

        final HttpSolrServer server = new HttpSolrServer(url);
        server.setMaxRetries(1);
        server.setSoTimeout(1000 * 60 * Integer.getInteger("solrTimeout", 10));  // socket read timeout 10 Minutes
        server.setDefaultMaxConnectionsPerHost(100);
        server.setMaxTotalConnections(100);
        server.setFollowRedirects(false);
        server.setAllowCompression(true);
        return server;
    }

    protected Collection<File> getFiles() {
        String directory = System.getProperty("dir", ".");
        logger.info("using directory " + directory);
        return FileUtils.listFiles(new File(directory), new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                Pattern pattern = Pattern.compile(getFilePattern());
                return pattern.matcher(file.getName()).find();
                //return file.getName().endsWith(".txt") &&
            }

            @Override
            public boolean accept(File dir, String name) {
                return false;
            }
        }, null);
    }

    public String getFilePattern() {
        String p = System.getProperty("file", "*.txt");
        p = StringUtils.replace(p, ".", "\\.");
        p = StringUtils.replace(p, "*", ".*");
        return p + "$";
    }

    protected Collection convert(File f) throws IOException {
        //need convert
        if (Boolean.getBoolean("convert")) {
            List<String> list = IOUtils.readLines(new FileInputStream(f), System.getProperty("inputCharSet", "UTF-8"));
            List retLst = new ArrayList();
            for (String str : list) {
                retLst.add(convertToMap(str));
            }
            if (Boolean.getBoolean("genConvert")) {
                File jsonFile = new File(f.getName() + ".json");
                IOUtils.write(JSONUtil.toJSON(Collections.unmodifiableCollection(retLst)), new FileOutputStream(jsonFile),
                        System.getProperty("outputCharSet", "UTF-8"));
            }
            return retLst;

        } else {
            Object obj = ObjectBuilder.fromJSON(IOUtils.toString(new FileInputStream(f), System.getProperty("inputCharSet", "UTF-8")));
            if (!(obj instanceof List)) return Collections.singleton(obj);
            else return (Collection) obj;
        }
    }

    protected Map convertToMap(String v) {
        int polyStart = StringUtils.indexOf(v, "MULTIPOLYGON");
        String polygon = StringUtils.substring(v, polyStart);

        if (polygon.contains(";")) {
            polygon = StringUtils.replace(polygon, ",", " ");
            polygon = StringUtils.replace(polygon, ";", ",");
            polygon = StringUtils.replace(polygon, ")) ((", ")),((");
        }

        Map map = new HashMap();
        map.put(DISTRICT_BOUNDARY, polygon);

        String[] results = StringUtils.split(StringUtils.substring(v, 0, polyStart), ",");

        map.put(CODE, results[0]);
        map.put(ID, map.get(CODE));
        map.put(COUNTRY_NAME, "中国");
        map.put(PROVINCE_NAME, results[5]);
        map.put(CITY_NAME, results[6]);
        map.put(DISTRICT_NAME, results[7]);
        return map;
    }


    protected SolrInputDocument convertToDocument(Map map) {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(DISTRICT_BOUNDARY, map.get(DISTRICT_BOUNDARY));
        solrDocument.addField(CODE, map.get(CODE));
        solrDocument.addField(ID, map.get(CODE));
        solrDocument.addField(COUNTRY_NAME, map.get(COUNTRY_NAME));
        solrDocument.addField(PROVINCE_NAME, map.get(PROVINCE_NAME));
        solrDocument.addField(CITY_NAME, map.get(CITY_NAME));
        solrDocument.addField(DISTRICT_NAME, map.get(DISTRICT_NAME));

        return solrDocument;
    }

    protected Collection commitToSolr(Collection maps) {

        if (!Boolean.getBoolean("commit")) return Collections.emptyList();
        if (maps.isEmpty()) return Collections.emptyList();
        Collection<Map> errors = new ArrayList<Map>();
        HttpSolrServer solrServer = getSolrServer();
        UpdateRequest req = new UpdateRequest();
        req.setAction(UpdateRequest.ACTION.COMMIT, false, false, true);

        int totalSize = maps.size();

        for (Object o : maps) {
            Map m = (Map) o;
            SolrInputDocument solrDocument = convertToDocument(m);
            req.add(solrDocument);
            totalSize--;
            if (!Boolean.getBoolean("batch") || totalSize <= 0) {
                try {
                    UpdateResponse resp = req.process(solrServer);
                    if (resp.getStatus() != 0) {
                        errors.add(m);
                    }
                } catch (SolrServerException e) {
                    logger.info("solr exception", e);
                    errors.add(m);
                } catch (Exception e) {
                    logger.info("exception", e);
                    errors.add(m);
                }
            }
        }

        if (!errors.isEmpty() && Boolean.getBoolean("batch")) {
            errors.clear();
            errors.addAll(maps);
        }
        return errors;
    }


    public void process() {
        Collection<File> files = getFiles();
        logger.info(files.size() + " files need to process ");

        final AtomicLong totalCount = new AtomicLong(0);
        final AtomicLong totalSuccess = new AtomicLong(0);

        ExecutorService executor = Executors.newFixedThreadPool(Integer.getInteger("threads", 5));
        for (File file : files) {
            final File f = file;
            executor.execute(new Runnable() {
                                 @Override
                                 public void run() {
                                     try {
                                         logger.info(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " process file " + f + " start");

                                         Collection records = convert(f);
                                         totalCount.getAndAdd(records.size());

                                         Collection<String> errors = commitToSolr(records);
                                         totalSuccess.getAndAdd(records.size() - errors.size());

                                         if (!errors.isEmpty()) {
                                             logger.info(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " process file " + f + " end, errCount = " + errors.size());
                                             File jsonFile = new File(f.getName() + ".err");
                                             //IOUtils.writeLines(errors, null, new FileOutputStream(jsonFile),));
                                             IOUtils.write(JSONUtil.toJSON(Collections.unmodifiableCollection(errors)), new FileOutputStream(jsonFile), System.getProperty("outputCharSet", "UTF-8"));
                                         } else
                                             logger.info(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " process file " + f + " end");

                                     } catch (Exception e) {
                                         logger.error("convert exception", e);
                                     }
                                 }
                             }
            );
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.getInteger("timeout", 120), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("wait exception", e);
        }

        logger.info("totalCount = " + totalCount);
        logger.info("totalSuccess  = " + totalSuccess);
    }


    public static void main(String[] args) throws IOException {
        ChinaConvert jsonConvert = new ChinaConvert();
        jsonConvert.process();
    }

    static final String DISTRICT_BOUNDARY = "DISTRICT_BOUNDARY";
    static final String CODE = "CODE";
    static final String COUNTRY_NAME = "COUNTRY_NAME";
    static final String PROVINCE_NAME = "PROVINCE_NAME";
    static final String CITY_NAME = "CITY_NAME";
    static final String DISTRICT_NAME = "DISTRICT_NAME";
    static final String ID = "id";
}
