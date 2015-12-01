package com.lbs.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class BoundaryExtracter {

    public static String baseUrl = "http://nominatim.openstreetmap.org/search/?&format=xml&polygon_text=1&limit=1&q=";
    public static String outputName = "D:\\test\\output\\country-boundary.txt";
    public static String noResName = "D:\\test\\output\\country-noResult.txt";
    public static String inputName = "D:\\test\\country.txt";

    /**
     * @param args
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        FileWriter outputWriter = new FileWriter(outputName, false);
        FileWriter noRes = new FileWriter(noResName, false);
        BoundaryExtracter test = new BoundaryExtracter();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputName), "GB2312"));
        String line = reader.readLine();
        while (line != null) {
            String[] items = line.split("\t");
            Date nowTime = new Date();
            System.out.println(items[0] + " " + nowTime.toString());
            try {
                test.getBoundary(items[0], items[9], outputWriter, noRes);
            } catch (Exception e) {
                noRes.write(line + "\r\n");
                e.printStackTrace();
            }

            line = reader.readLine();
        }

        outputWriter.close();
        noRes.close();
    }


    public void getBoundary(String code, String name, FileWriter outputWriter, FileWriter noRes) throws IOException, ParserConfigurationException, SAXException {

        String nameEncode = URLEncoder.encode(name, "UTF-8");
        URL url = new URL(baseUrl + nameEncode);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        //urlConnection.setConnectTimeout(1000 * 10);
        urlConnection.setRequestProperty("Accept", "application/xml");
        urlConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        InputStream in = urlConnection.getInputStream();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document document = db.parse(in);

        Node place = document.getFirstChild().getFirstChild().getNextSibling();
        NamedNodeMap attrs = place.getAttributes();
        Node geoTextNode = attrs.getNamedItem("geotext");
        Node displayNameNode = attrs.getNamedItem("display_name");
        Node omsIdNode = attrs.getNamedItem("osm_id");

        String geoText = geoTextNode.getNodeValue();
        if (geoText == null || geoText.isEmpty()) {
            noRes.write(name + "," + displayNameNode.getNodeValue() + "," + omsIdNode.getNodeValue() + "\r\n");
        } else {
            outputWriter.write(code + "," + name + "," + displayNameNode.getNodeValue() + "," + omsIdNode.getNodeValue() + "," + geoText);
        }

        outputWriter.write("\r\n");
        outputWriter.flush();
        noRes.flush();
    }
}

