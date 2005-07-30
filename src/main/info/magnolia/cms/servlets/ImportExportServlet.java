package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Bootstrapper.VersionFilter;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Simple servlet used to import/export data from jcr using the standard jcr import/export features.
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class ImportExportServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * request parameter: repository name.
     */
    private static final String PARAM_REPOSITORY = "mgnlRepository"; //$NON-NLS-1$

    /**
     * request parameter: path.
     */
    private static final String PARAM_PATH = "mgnlPath"; //$NON-NLS-1$

    /**
     * request parameter: keep versions.
     */
    private static final String PARAM_KEEPVERSIONS = "mgnlKeepVersions"; //$NON-NLS-1$

    /**
     * request parameter: format
     */
    private static final String PARAM_FORMAT = "mgnlFormat"; //$NON-NLS-1$

    /**
     * request parameter: imported file.
     */
    private static final String PARAM_FILE = "mgnlFileImport"; //$NON-NLS-1$

    /**
     * request parameter: UUID behavior for import.
     */
    private static final String PARAM_UUID_BEHAVIOR = "mgnlUuidBehavior"; //$NON-NLS-1$

    /**
     * request parameter: redirect page after import.
     */
    private static final String PARAM_REDIRECT = "mgnlRedirect"; //$NON-NLS-1$

    /**
     * request parameter: export requested.
     */
    private static final String PARAM_EXPORT_ACTION = "exportxml"; //$NON-NLS-1$

    /**
     * Number of space for indentation
     */
    private static final int INDENT_VALUE = 2; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ImportExportServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        try {
            request.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        }
        catch (IllegalStateException e) {
            // ignore
        }

        String repository = request.getParameter(PARAM_REPOSITORY);
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }
        String basepath = request.getParameter(PARAM_PATH);
        if (StringUtils.isEmpty(basepath)) {
            basepath = "/"; //$NON-NLS-1$
        }

        boolean keepVersionHistory = BooleanUtils.toBoolean(request.getParameter(PARAM_KEEPVERSIONS));
        boolean format = BooleanUtils.toBoolean(request.getParameter(PARAM_FORMAT));

        if (request.getParameter(PARAM_EXPORT_ACTION) != null) {

            if (checkPermissions(request, repository, basepath, Permission.WRITE)) {
                executeExport(response, repository, basepath, format, keepVersionHistory);
                return;
            }

            throw new ServletException(new AccessDeniedException(
                "Write permission needed for export. User not allowed to WRITE path [" //$NON-NLS-1$
                    + basepath + "]")); //$NON-NLS-1$
        }

        if (StringUtils.contains(request.getRequestURI(), "import")) { //$NON-NLS-1$
            displayImportForm(request, response.getWriter(), repository, basepath);
        }
        else {
            displayExportForm(request, response.getWriter(), repository, basepath);
        }
    }

    /**
     * Display a simple form for importing/exporting data.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository (extracted from request parameter or default)
     */
    private void displayExportForm(HttpServletRequest request, PrintWriter out, String repository, String basepath) {

        out.println("<html><head><title>Magnolia</title>"); //$NON-NLS-1$
        out.println(new Sources(request.getContextPath()).getHtmlCss());
        out.println("</head><body class=\"mgnlBgLight mgnlImportExport\">"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "importexport.export")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        out.println("<form method=\"get\" action=\"\">"); //$NON-NLS-1$

        writeRepositoryField(request, out, repository);
        writeBasePathField(request, out, basepath);
        writeKeepVersionField(request, out);
        writeFormatField(request, out);

        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
            + PARAM_EXPORT_ACTION + "\" value=\"" //$NON-NLS-1$
            + MessagesManager.get(request, "importexport.export") //$NON-NLS-1$
            + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    /**
     * Display a simple form for importing/exporting data.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository (extracted from request parameter or default)
     */
    private void displayImportForm(HttpServletRequest request, PrintWriter out, String repository, String basepath) {

        out.println("<html><head><title>Magnolia</title>"); //$NON-NLS-1$
        out.println(new Sources(request.getContextPath()).getHtmlCss());
        out.println("</head><body class=\"mgnlBgLight mgnlImportExport\">"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "importexport.import")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        out.println("<form method=\"post\" action=\"\" enctype=\"multipart/form-data\">"); //$NON-NLS-1$

        writeRepositoryField(request, out, repository);
        writeBasePathField(request, out, basepath);
        writeKeepVersionField(request, out);
        out.println(MessagesManager.get(request, "importexport.file") //$NON-NLS-1$
            + " <input type=\"file\" name=\"" + PARAM_FILE + "\" /><br/>"); //$NON-NLS-1$//$NON-NLS-2$

        out.println("<input type=\"radio\" name=\"" //$NON-NLS-1$
            + PARAM_UUID_BEHAVIOR + "\" value=\"" //$NON-NLS-1$
            + ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW + "\">"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "importexport.createnew")); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$

        out.println("<input type=\"radio\" name=\"" //$NON-NLS-1$
            + PARAM_UUID_BEHAVIOR + "\" value=\"" //$NON-NLS-1$
            + ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING + "\">"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "importexport.removeexisting")); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$

        out.println("<input type=\"radio\" name=\"" //$NON-NLS-1$
            + PARAM_UUID_BEHAVIOR + "\" value=\"" //$NON-NLS-1$
            + ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING + "\">"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "importexport.replaceexisting")); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$

        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
            + PARAM_EXPORT_ACTION + "\" value=\"" //$NON-NLS-1$
            + MessagesManager.get(request, "importexport.import") //$NON-NLS-1$
            + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param basepath
     */
    private void writeBasePathField(HttpServletRequest request, PrintWriter out, String basepath) {
        out.println(MessagesManager.get(request, "importexport.basepath") //$NON-NLS-1$
            + " <input name=\"" //$NON-NLS-1$
            + PARAM_PATH + "\" value=\"" //$NON-NLS-1$
            + basepath + "\" /><br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     */
    private void writeKeepVersionField(HttpServletRequest request, PrintWriter out) {
        out.println(MessagesManager.get(request, "importexport.keepversions") //$NON-NLS-1$
            + " <input name=\"" //$NON-NLS-1$
            + PARAM_KEEPVERSIONS + "\" value=\"true\" type=\"checkbox\"/><br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     */
    private void writeFormatField(HttpServletRequest request, PrintWriter out) {
        out.println(MessagesManager.get(request, "importexport.format") //$NON-NLS-1$
            + " <input name=\"" //$NON-NLS-1$
            + PARAM_FORMAT + "\" value=\"true\" type=\"checkbox\"/><br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param repository
     */
    private void writeRepositoryField(HttpServletRequest request, PrintWriter out, String repository) {
        out.println(MessagesManager.get(request, "importexport.repository") //$NON-NLS-1$
            + " <select name=\"" //$NON-NLS-1$
            + PARAM_REPOSITORY + "\">"); //$NON-NLS-1$

        String[] repositories = ContentRepository.getAllRepositoryNames();
        for (int j = 0; j < repositories.length; j++) {
            out.print("<option"); //$NON-NLS-1$
            if (repository.equals(repositories[j])) {
                out.print(" selected=\"selected\""); //$NON-NLS-1$
            }
            out.print(">"); //$NON-NLS-1$
            out.print(repositories[j]);
            out.print("</option>"); //$NON-NLS-1$
        }
        out.println("</select>"); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$
    }

    /**
     * A post request is usually an import request.
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        log.debug("Import request received."); //$NON-NLS-1$

        MultipartForm form = Resource.getPostedForm(request);
        if (form == null) {
            log.error("Missing form."); //$NON-NLS-1$
            return;
        }

        String basepath = form.getParameter(PARAM_PATH);
        if (StringUtils.isEmpty(basepath)) {
            basepath = "/"; //$NON-NLS-1$
        }

        boolean keepVersionHistory = BooleanUtils.toBoolean(form.getParameter(PARAM_KEEPVERSIONS));

        String repository = form.getParameter(PARAM_REPOSITORY);
        Document xmlFile = form.getDocument(PARAM_FILE);
        if (StringUtils.isEmpty(repository) || xmlFile == null) {
            throw new RuntimeException("Wrong parameters"); //$NON-NLS-1$
        }

        String uuidBehaviorString = form.getParameter(PARAM_UUID_BEHAVIOR);

        int uuidBehavior = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;
        if (NumberUtils.isNumber(uuidBehaviorString)) {
            uuidBehavior = Integer.parseInt(uuidBehaviorString);
        }

        if (checkPermissions(request, repository, basepath, Permission.WRITE)) {
            executeImport(basepath, repository, xmlFile, keepVersionHistory, uuidBehavior);
        }
        else {
            throw new ServletException(new AccessDeniedException(
                "Write permission needed for import. User not allowed to WRITE path [" //$NON-NLS-1$
                    + basepath + "]")); //$NON-NLS-1$
        }

        String redirectPage = form.getParameter(PARAM_REDIRECT);
        if (StringUtils.isNotBlank(redirectPage)) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Redirecting to [{0}]", //$NON-NLS-1$
                    new Object[]{redirectPage}));
            }
            response.sendRedirect(redirectPage);
        }
        else {
            doGet(request, response);
        }
    }

    /**
     * Actually perform export. The generated file is sent to the client.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository
     * @param format should we format the resulting xml
     * @param keepVersionHistory if <code>false</code> version info will be stripped from the exported document
     * @throws IOException for errors while accessing the servlet output stream
     */
    private void executeExport(HttpServletResponse response, String repository, String basepath, boolean format,
        boolean keepVersionHistory) throws IOException {
        HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();
        OutputStream stream = response.getOutputStream();
        response.setContentType("text/xml"); //$NON-NLS-1$
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        String pathName = StringUtils.replace(basepath, "/", "."); //$NON-NLS-1$ //$NON-NLS-2$
        if (".".equals(pathName)) { //$NON-NLS-1$
            // root node
            pathName = StringUtils.EMPTY;
        }
        response.setHeader("content-disposition", "attachment; filename=" + repository + pathName + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Session session = ws.getSession();

        try {
            if (keepVersionHistory) {
                // use exportSystemView in order to preserve property types
                // http://issues.apache.org/jira/browse/JCR-115
                if (!format) {
                    session.exportSystemView(basepath, stream, false, false);
                }
                else {
                    parseAndFormat(stream, null, repository, basepath, format, session);
                }
            }
            else {
                // use XMLSerializer and a SAXFilter in order to rewrite the file
                XMLReader reader = new VersionFilter(XMLReaderFactory
                    .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));
                parseAndFormat(stream, reader, repository, basepath, format, session);
            }
        }
        catch (Exception e) {
            throw new NestableRuntimeException(e);
        }

        stream.flush();
        stream.close();
    }

    /**
     * This export the content of the repository, and format it if necessary
     * @param stream the stream to write the content to
     * @param reader the reader to use to parse the xml content (so that we can perform filtering), if null instanciate
     * a default one
     * @param repository the repository to export
     * @param basepath the basepath in the repository
     * @param format should we format the xml
     * @param session the session to use to export the data from the repository
     * @throws Exception if anything goes wrong ...
     */
    private void parseAndFormat(OutputStream stream, XMLReader reader, String repository, String basepath,
        boolean format, Session session) throws Exception {

        if (reader == null) {
            reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());
        }

        // write to a temp file and then re-read it to remove version history
        File tempFile = File.createTempFile("export-" + repository + session.getUserID(), "xml"); //$NON-NLS-1$ //$NON-NLS-2$
        tempFile.deleteOnExit();
        OutputStream fileStream = new FileOutputStream(tempFile);

        session.exportSystemView(basepath, fileStream, false, false);

        try {
            fileStream.close();
        }
        catch (IOException e) {
            // ignore
        }

        InputStream fileInputStream = new FileInputStream(tempFile);

        OutputFormat forma = new OutputFormat();
        if (format) {
            forma.setIndenting(true);
            forma.setIndent(INDENT_VALUE);
        }
        reader.setContentHandler(new XMLSerializer(stream, forma));

        reader.parse(new InputSource(fileInputStream));

        try {
            fileInputStream.close();
        }
        catch (IOException e) {
            // ignore
        }

        if (!tempFile.delete()) {
            log.error("Could not delete temporary export file..." + tempFile.getAbsolutePath()); //$NON-NLS-1$
        }
    }

    /**
     * Perform import.
     * @param repository selected repository
     * @param basepath base path in repository
     * @param xmlFile uploaded file
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode a valid value for ImportUUIDBehavior
     * @see ImportUUIDBehavior
     */
    private void executeImport(String basepath, String repository, Document xmlFile, boolean keepVersionHistory,
        int importMode) {
        HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("About to import file into the [{0}] repository", new Object[]{repository})); //$NON-NLS-1$
        }

        InputStream stream = xmlFile.getStream();
        Session session = ws.getSession();

        try {
            if (keepVersionHistory) {
                session.importXML(basepath, stream, importMode);
            }
            else {

                // create a temporary file and save the trimmed xml
                File strippedFile = File.createTempFile("import", "xml"); //$NON-NLS-1$ //$NON-NLS-2$
                strippedFile.deleteOnExit();

                FileOutputStream outstream = new FileOutputStream(strippedFile);

                // use XMLSerializer and a SAXFilter in order to rewrite the file
                XMLReader reader = new VersionFilter(XMLReaderFactory
                    .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));
                reader.setContentHandler(new XMLSerializer(outstream, new OutputFormat()));

                try {
                    reader.parse(new InputSource(stream));
                }
                finally {
                    stream.close();
                }

                // return the filtered file as an input stream
                InputStream filteredStream = new FileInputStream(strippedFile);
                try {
                    session.importXML(basepath, filteredStream, importMode);
                }
                finally {
                    try {
                        filteredStream.close();
                    }
                    catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        catch (Exception e) {
            throw new NestableRuntimeException(e);
        }
        try {
            stream.close();
        }
        catch (IOException e) {
            // ignore
        }

        try {
            session.save();
        }
        catch (RepositoryException e) {
            log.error(MessageFormat.format(
                "Unable to save changes to the [{0}] repository due to a {1} Exception: {2}.", //$NON-NLS-1$
                new Object[]{repository, e.getClass().getName(), e.getMessage()}), e);
        }

        log.info("Import done"); //$NON-NLS-1$
    }

    /**
     * Uses access manager to authorise this request.
     * @param request HttpServletRequest as received by the service method
     * @return boolean true if read access is granted
     */
    protected boolean checkPermissions(HttpServletRequest request, String repository, String basePath,
        long permissionType) {
        if (SessionAccessControl.getAccessManager(request, repository) != null) {
            if (!SessionAccessControl.getAccessManager(request).isGranted(basePath, permissionType)) {
                return false;
            }
        }
        return true;
    }

}