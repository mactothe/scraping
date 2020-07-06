package kr.whoi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class Scraping {

    /**
     * JSON형태의 String을 Object형태로 변경해주는 메소드
     * @param obj           Map 또는 List 형태의 객체
     * @param jsonString    JSON형태의 String
     * @return java.lang.Object
     */
    public static Object jsonStringConverter(Object obj, String jsonString) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(jsonString, obj.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Object형태의 데이터를 JSON형태의 String으로 변경해주는 메소드
     * @param obj   Map 또는 List 형태의 객체
     * @return java.lang.String
     */
    public static String objectToJsonString(Object obj) {
        ObjectMapper om = new ObjectMapper();
        String rString = "";
        try {
            rString = om.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return rString;
    }

    /**
     * URL과 CSS Selector로 단순하게 원하는 부분을 Scraping하는 메소드
     * @param url       scraping을 하기위한 URL 주소
     * @param selector  CSS selctor 형태의 String
     * @return java.lang.Object
     */
    public static Object simpleScraping(String url, String selector) {

        List<String> result = new LinkedList();

        Document doc = getDom(url, "");

        Elements els = null;
        if (doc != null) {
            els = doc.select(selector);
        }

        if (els != null) {
            for (Element el : els) {
                result.add(el.text());
            }
        }

        return result;
    }

    /**
     * 연결된 URL에서 Document를 가져오는 메소드
     * @param url       scraping을 하기위한 URL 주소
     * @param method    Request method(get, post)
     * @return org.jsoup.nodes.Document
     */
    public static Document getDom(String url, String method) {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36";


        if (url.contains("https://")) {
            try {
                setSSL();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }
        Document doc = null;
        try {
            if(method == null || method.equals("") || method.toLowerCase().equals("get")){
                doc = Jsoup.connect(url).userAgent(userAgent).method(Connection.Method.GET).ignoreContentType(true).get();
            } else {
                doc = Jsoup.connect(url).userAgent(userAgent).method(Connection.Method.POST).ignoreContentType(true).post();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    /**
     * 연결된 URL에서 Document를 가져오는 메소드
     * @param url       scraping을 하기위한 URL 주소
     * @param method    Request method(get, post)
     * @param params    parameter
     * @return org.jsoup.nodes.Document
     */
    public static Document getDom(String url, String method, Map params) {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36";

        if (url.contains("https://")) {
            try {
                setSSL();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }
        Document doc = null;
        try {
            if(method == null || method.equals("") || method.toLowerCase().equals("get")){
                doc = Jsoup.connect(url).userAgent(userAgent).method(Connection.Method.GET).ignoreContentType(true).data(params).get();
            } else {
                doc = Jsoup.connect(url).userAgent(userAgent).method(Connection.Method.POST).ignoreContentType(true).data(params).post();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    /**
     * 신뢰할 수 없는 사이트에 대한 예외처리 메소드(인증서 무시)
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static void setSSL() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    }

    /**
     * scraping을 위한 정보를 가진 List 객체를 parse해주는 메소드
     * @param scrpInfoList   scraping을 위한 정보를 가진 List
     * @return java.util.List
     */
    public static List scrpInfoParse(List<Map<String, Object>> scrpInfoList) {
        List result = new ArrayList();
        for (Map scrpInfo : scrpInfoList) {
            result.add(scrpInfoParse(scrpInfo));
        }
        System.out.println(objectToJsonString(result));
        return result;
    }


    /**
     * scraping을 위한 정보를 가진 Map 객체를 parse해주는 메소드
     * @param scrpInfo   scraping을 위한 정보를 가진 Map
     * @return java.util.List
     */
    public static List scrpInfoParse(Map scrpInfo) {
        List result = new ArrayList();
        String url = null;
        if (scrpInfo.get("url") != null) {
            url = (String) scrpInfo.get("url");
            if (url.equals("")) {
                new Exception();
            }
        } else {
            new Exception();
        }

        Object selectors = null;
        if (scrpInfo.get("selectors") != null) {
            if (scrpInfo.get("selectors").getClass().getSimpleName().toLowerCase().indexOf("arraylist") >= 0) {
                selectors = (List) scrpInfo.get("selectors");
            } else if (scrpInfo.get("selectors").getClass().getSimpleName().toLowerCase().indexOf("string") >= 0) {
                selectors = (String) scrpInfo.get("selectors");
                if (selectors.equals("")) {
                    new Exception();
                }
            } else {
                new Exception();
            }
        } else {
            new Exception();
        }

        String method = "get";
        if (scrpInfo.get("method") != null) {
            method = (String) scrpInfo.get("method");
            if (method.equals("")) {
                method = "get";
            }
        }

        Map params = null;
        if(scrpInfo.get("params") != null) {
            params = (Map) scrpInfo.get("params");
        }

        String needAttr = null;
        if (scrpInfo.get("needAttr") != null) {
            needAttr = (String) scrpInfo.get("needAttr");
            if (needAttr.equals("")) {
                needAttr = null;
            }
        }

        String linkAttr = null;
        if (scrpInfo.get("linkAttr") != null) {
            linkAttr = (String) scrpInfo.get("linkAttr");
            if (linkAttr.equals("")) {
                linkAttr = null;
            }
        }

        Map child = null;
        if (scrpInfo.get("child") != null) {
            child = (Map) scrpInfo.get("child");
        }


        Document doc = null;
        if(params != null) {
            doc = getDom(url, method, params);
        } else {
            doc = getDom(url, method);
        }
        String hostLocation = doc.location().split("/")[0] + "//" + doc.location().split("/")[2];

        List<String> linkList = new ArrayList();

        if (doc != null) {
            if (selectors.getClass().getSimpleName().toLowerCase().indexOf("string") >= 0) {
                Elements els = doc.select((String) selectors);
                for (Element el : els) {
                    if (needAttr != null || linkAttr != null) {
                        if (linkAttr != null) {
                            linkList.add(el.attr(linkAttr));
                        } else {
                            result.add(el.attr(needAttr));
                        }
                    } else {
                        result.add(el.text());
                    }
                }
            } else {
                Map<String, String> map = new HashMap();
                for (Map selector : (List<Map>) selectors) {
                    Elements els = doc.select((String) selector.get("query"));
                    for (Element el : els) {
                        if (needAttr != null || linkAttr != null) {
                            if (linkAttr != null) {
                                linkList.add(el.attr(linkAttr));
                            } else {
                                if (selector.get("keyName") != null) {
                                    if (selector.get("keyName").equals("")) {
                                        result.add(el.attr(needAttr));
                                    } else {
                                        map.put((String) selector.get("keyName"), el.attr(needAttr));
                                    }
                                } else {
                                    result.add(el.attr(needAttr));
                                }
                            }
                        } else {
                            if (selector.get("keyName") != null) {
                                if (selector.get("keyName").equals("")) {
                                    result.add(el.text());
                                } else {
                                    map.put((String) selector.get("keyName"), el.text());
                                }
                            } else {
                                result.add(el.text());
                            }
                        }
                    }
                }
                if(map.size() > 0) {
                    result.add(map);
                }
            }
        }

        if (linkList.size() > 0) {
            for (String link : linkList) {
                if (link.indexOf("http://") < 0 && link.indexOf("https://") < 0) {
                    link = hostLocation + link;
                }
                child.put("url", link);
                result.addAll(scrpInfoParse(child));
            }
        }
        return result;
    }


    public static void main(String[] args) {
        List<Map<String, Object>> scrpInfoList = null;
        try {
            if(args[0].equals("") || args[0].equals("false")){
                String text = "";
                File file = new File(args[1]);
                FileReader filereader = new FileReader(file);
                BufferedReader bufReader = new BufferedReader(filereader);
                String line = "";
                while((line = bufReader.readLine()) != null){
                    text += line;
                }
                bufReader.close();
                scrpInfoList = (List) jsonStringConverter(new ArrayList(), text);
            } else {
                scrpInfoList = (List) jsonStringConverter(new ArrayList(), args[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(scrpInfoParse(scrpInfoList));
    }
}
