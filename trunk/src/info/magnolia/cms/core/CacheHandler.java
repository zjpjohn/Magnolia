/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */







package info.magnolia.cms.core;


import info.magnolia.cms.beans.runtime.Cache;
import info.magnolia.cms.beans.runtime.SecureURI;
import info.magnolia.cms.Aggregator;
import info.magnolia.cms.util.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.net.URLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;


/**
 * User: sameercharles
 * Date: Jul 14, 2003
 * Time: 4:22:14 PM
 * @author Sameer Charles
 * @version 1.1
 */


/**
 * CacheHandle checks if the data is already cached, if yes it spools the data back to the
 * requester either compressed or as is.
 * If not it caching that request in default and optimized (only gzip for now) stores.
 *
 * */


public class CacheHandler {

    private static Logger log = Logger.getLogger(CacheHandler.class);

    public static final String CACHE_DIRECTORY = Path.getCacheDirectoryPath();
    private static final String DEFAULT_STORE = "/default";
    private static final String COMPRESSED_STORE = "/optimized";





    /**
     *
     * @param request
     */
    public static void cacheURI(HttpServletRequest request) throws IOException {
        if (CacheHandler.hasRedirect(request)) /* dont cache */
            return;
        String URI = Path.getURI(request);
        FileOutputStream out = null;
        int size = 0;
        int compressedSize = 0;
        try {
            if (!info.magnolia.cms.beans.config.Cache.isCacheable(request))
                return;
            File file = getDestinationFile(URI,DEFAULT_STORE);
            if (!file.exists()) {
                file.createNewFile();
                out = new FileOutputStream(file);
                streamURI(URI,out,request);
                out.flush();
                out.close();
            }
            size = (new Long(file.length())).intValue();
            if (info.magnolia.cms.beans.config.Cache.applyCompression(Path.getExtension(request))) {
                File gzipFile = getDestinationFile(URI,COMPRESSED_STORE);
                if (!gzipFile.exists()) {
                    gzipFile.createNewFile();
                    out = new FileOutputStream(gzipFile);
                    GZIPOutputStream gOut = new GZIPOutputStream(out);
                    streamURI(URI,gOut,request);
                    gOut.close();
                }
                compressedSize = (new Long(gzipFile.length())).intValue();
            }
            Cache.addToCachedURIList(URI,(new Date()).getTime(),size,compressedSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (out != null)
                out.close();
        }
    }




    private static boolean hasRedirect(HttpServletRequest request) {
        Object obj = request.getAttribute(Aggregator.ACTPAGE);
        if (obj == null)
            return false; /* some other resource */
        else {
            Content aPage = (Content)obj;
            if (aPage.getNodeData("redirectURL").getString().equals(""))
                return false;
            else
                return true;
        }
    }



    /**
     * @param URI
     * @param out
     */
    private static void streamURI(String URI, OutputStream out, HttpServletRequest request) throws IOException {
        InputStream in = null;
        try {
            URL url = new URL(info.magnolia.cms.beans.config.Cache.getDomain()+URI);
            URLConnection urlConnection = url.openConnection();
            if (SecureURI.isProtected(URI))
                urlConnection.setRequestProperty("Authorization",request.getHeader("Authorization"));
            byte[] buffer = new byte[8192];
            int read = 0;
            in = urlConnection.getInputStream();
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
        } catch (Exception e) {
            log.error("Failed to stream - "+URI);
            log.error(e.getMessage());
        } finally {
            if (in != null)
                in.close();
        }
    }


    /**
     * @deprecated
     * */
    private static int stripWhiteSpaces(InputStream in) {
        int read = 0;
        try {
            while ((read = in.read()) > -1) {
                if ((read != 32) && (read != 10))
                    break;
            }
        } catch (IOException e) {log.error(e.getMessage());}
        return read;
    }



    /**
     * @param URI
     */
    private static File getDestinationFile(String URI, String type) throws Exception {
        validatePath(CACHE_DIRECTORY);
        validatePath(CACHE_DIRECTORY+type);
        String[] items = URI.split("/");
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (; i<(items.length-1); i++) {
            if (items[i].equals(""))
                continue;
            buffer.append("/"+items[i]);
            validatePath(CACHE_DIRECTORY+type+buffer.toString());
        }
        buffer.append("/"+items[i]);
        return (new File(CACHE_DIRECTORY+type+buffer.toString()));
    }



    /**
     * @param path to the directory
     */
    public static void validatePath(String path) throws Exception {
        try {
            File file = new File(path);
            if (!file.isDirectory()) {
                if (!file.mkdir())
                    log.error("Can not create directory - "+path);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }



    /**
     * @param request
     * @param response
     * @throws IOException
     */
    public static boolean streamFromCache(HttpServletRequest request,HttpServletResponse response)
            throws IOException {
        /* make sure not to stream anything from cache if its a POST request or has ? in URL */
        if (request.getMethod().toLowerCase().equals("post"))
            return false;
        Enumeration paramList = request.getParameterNames();
        if (paramList.hasMoreElements())
            return false;
        boolean compress = canCompress(request);
        FileInputStream fin = null;
        try {
            File file;
            if (compress)
                file = new File(CACHE_DIRECTORY+COMPRESSED_STORE+Path.getURI(request));
            else
                file = new File(CACHE_DIRECTORY+DEFAULT_STORE+Path.getURI(request));
            if (!file.exists())
                return false;
            if (file.length() < 4)
                return false;
            fin = new FileInputStream(file);
            if (compress) {
                response.setContentLength(Cache.getCompressedSize(request));
                sendCompressed(fin, response);
            }
            else {
                response.setContentLength(Cache.getSize(request));
                send(fin, response);
            }
        } catch (Exception e) {
        } finally {
            if (fin != null)
                fin.close();
        }
        return true;
    }




    /**
     * <p>send data as GZIP output stream ;)</p>
     *
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws Exception
     */
    private static void sendCompressed(InputStream is, HttpServletResponse res)
            throws Exception {
        res.setHeader("Content-Encoding", "gzip");
        send(is,res);
    }



    /**
     * <p>send data as is</p>
     *
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws IOException
     */
    private static void send(InputStream is, HttpServletResponse res)
            throws Exception {
        ServletOutputStream os = res.getOutputStream();
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        os.flush();
        os.close();
    }


    /**
     * <p>returns true if the request sender accepts GZIP compressed data</p>
     *
     * @return boolean
     */
    private static boolean canCompress(HttpServletRequest req) {
        if (!info.magnolia.cms.beans.config.Cache.applyCompression(Path.getExtension(req)))
            return false;
        String encoding = req.getHeader("Accept-Encoding");
        if (encoding != null) {
            return (encoding.toLowerCase().indexOf("gzip") > -1);
        }
        return false;
    }


    /**
     * @param URI
     */
    public static void flushResource(String URI) throws Exception {
        File file = new File(URI);
        try {
            if (file.isDirectory()) {
                emptyDirectory(file);
                file.delete();
                Cache.clearCachedURIList();
            } else {
                log.info("Flushing - "+URI);
                file.delete();
                Cache.removeFromCachedURIList(URI);
            }
        } catch (Exception e) {
            log.error("Failed to flush - "+URI);
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }



    /**
     * <p>
     * recursively deletes all files under the specified directory
     * </p>
     *
     * @param directory
     * */
    private static void emptyDirectory(File directory) {
        File[] children = directory.listFiles();
        for (int i=0; i<children.length; i++) {
            if (children[i].isDirectory())
                emptyDirectory(children[i]);
            else {
                log.info("Flushing - "+children[i].getPath());
                children[i].delete();
            }
        }
    }



    /**
     * <p>flushes entire cache</p>
     */
    public static void flushCache() {
        log.info("Flushing entire cache");
        try {
            CacheHandler.flushResource(CACHE_DIRECTORY);
            /* this will create cache start directory again */
            CacheHandler.validatePath(CACHE_DIRECTORY);
            CacheHandler.validatePath(CACHE_DIRECTORY+DEFAULT_STORE);
            CacheHandler.validatePath(CACHE_DIRECTORY+COMPRESSED_STORE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }





}
