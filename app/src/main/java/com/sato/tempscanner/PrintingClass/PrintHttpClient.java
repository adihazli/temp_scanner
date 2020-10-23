package com.sato.tempscanner.PrintingClass;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class PrintHttpClient {

    static final String prfx_boundary = "-----------------------------";
    static final String POST_METHOD = "POST";
    static final String GET_METHOD = "GET";
    static final String CONNECTION = "Connection";
    static final String CHARSET = "Charset";
    static final String CONTENT_TYPE = "Content-Type";
    static final String KEEP_ALIVE = "Keep-Alive";
    static final String UTF8 = "UTF-8";
    static final String MULTIPART_BOUNDARY = "multipart/form-data; boundary=";

    static final String twohyphens = "--";
    static final String end = "\r\n";
    static final String dq = "\"";

    public static Map<String, String> get(String url, Map<String, String> paramMap) throws HttpClientException {
        HttpURLConnection conn = null;
        StandardHandler handler = new StandardHandler();
        URL syncUrl;
        InputStream is = null;

        try {
            // form parameter
            String param = "";
            if (paramMap != null) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if(param.length() > 0){ param = param + "&";
                    } else{  param = "?"; }
                    param = param + URLEncoder.encode(entry.getKey(), String.valueOf(StandardCharsets.UTF_8)) + "=" + URLEncoder.encode(entry.getValue(), String.valueOf(StandardCharsets.UTF_8));
                }
            }
            // connection
            syncUrl = new URL(url + param);
            conn = (HttpURLConnection) syncUrl.openConnection();

            // header
            conn.setRequestMethod(GET_METHOD);
            conn.connect();

            // result code
            final int responceCode = conn.getResponseCode();
            if (responceCode != HttpURLConnection.HTTP_OK) {
                throw new HttpClientException("invalid responce code, " + responceCode);
            }
            // result parsing
            SAXParserFactory spfactory = SAXParserFactory.newInstance();
            SAXParser parser = spfactory.newSAXParser();
            is = conn.getInputStream();
            parser.parse(is, handler);

        } catch (MalformedURLException e) {
            throw new HttpClientException(e);
        } catch (IOException e) {
            throw new HttpClientException(e);
        } catch (ParserConfigurationException e) {
            throw new HttpClientException(e);
        } catch (SAXException e) {
            throw new HttpClientException(e);
        } finally {
            if(is != null){
                try { is.close();
                } catch (IOException e) {}
            }
            if(conn != null){
                conn.disconnect();
            }
        }
        return handler.getItem();
    }

    @SuppressWarnings("serial")
    public static class HttpClientException extends Throwable {
        /**
         *
         */
        public HttpClientException() {
            super();
        }

        /**
         * @param detailMessage
         * @param throwable
         */
        public HttpClientException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * @param detailMessage
         */
        public HttpClientException(String detailMessage) {
            super(detailMessage);
        }

        /**
         * @param throwable
         */
        public HttpClientException(Throwable throwable) {
            super(throwable);
        }
    }

}
