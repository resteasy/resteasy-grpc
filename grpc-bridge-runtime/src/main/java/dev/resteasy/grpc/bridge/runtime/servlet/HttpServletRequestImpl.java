/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.resteasy.grpc.bridge.runtime.servlet;

import static dev.resteasy.grpc.bridge.runtime.Constants.ANY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletResponseWrapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.ResteasyUriInfo;

import dev.resteasy.grpc.bridge.runtime.i18n.Messages;
import io.undertow.util.DateUtils;
import io.undertow.util.LocaleUtils;

/*
 * The http servlet request implementation. This class is not thread safe.
 *
 * Much of it is borrowed from io.undertow.servlet.spec.HttpServletRequestImpl,
 * written by Stuart Douglas.
 *
 * NOT IMPLEMENTED: Currently, methods related to sessions and security are not implemented.
 */
public class HttpServletRequestImpl implements HttpServletRequest {

    public static final String LOCATOR = "LOCATOR";

    private ServletResponse servletResponse;
    private String uri;
    private String contextPath;
    private String path;
    private UriInfo uriInfo;
    private String servletPath;
    private String method;
    private ServletInputStream inputStream;
    private String entityType;
    private Map<String, List<String>> headers;
    private Cookie[] cookies;
    private ServletContext servletContext;
    private volatile boolean asyncStarted;
    private volatile AsyncContext asyncContext;
    private boolean gotInputStream = false;
    private boolean gotReader = false;
    private boolean readStarted;

    // servlet info
    private String characterEncoding;
    private String clientAddr;
    private String clientHost;
    private int clientPort;

    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Map<String, String[]> formParameters;
    private Map<String, String[]> parameters;

    public HttpServletRequestImpl(final ServletResponse servletResponse, final ServletContext servletContext,
            final String uri, final String path, final String method, final ServletInputStream sis, final String entity,
            final Map<String, List<String>> headers,
            final Cookie[] cookies, final Map<String, String[]> formParameters) throws URISyntaxException {
        this.servletResponse = servletResponse;
        this.servletContext = servletContext;
        setUri(uri);
        this.contextPath = servletContext.getContextPath();
        this.path = path;
        this.method = method;
        this.inputStream = sis;
        this.entityType = entity;
        this.headers = headers == null ? new HashMap<String, List<String>>() : headers;
        this.cookies = cookies;
        this.formParameters = formParameters;
        List<String> acceptList = new ArrayList<String>();
        acceptList.add("application/grpc-jaxrs");
        acceptList.add("*/*;grpc-jaxrs=true");
        headers.put("Accept", acceptList);
        List<String> contentTypeList = new ArrayList<String>();
        contentTypeList.add("*/*;grpc-jaxrs=true");
        headers.put("Content-Type", contentTypeList);
        if ("com.google.protobuf.Any".equals(entity)) {
            acceptList = new ArrayList<String>();
            acceptList.add("true");
            headers.put(ANY, acceptList);
        }
        this.cookies = cookies;
        this.formParameters = formParameters;
    }

    public HttpServletRequestImpl() {
        List<String> acceptList = new ArrayList<String>();
        acceptList.add("application/grpc-jaxrs");
        acceptList.add("*/*;grpc-jaxrs=true");
        headers = new HashMap<String, List<String>>();
        headers.put("Accept", acceptList);
        List<String> contentTypeList = new ArrayList<String>();
        contentTypeList.add("*/*;grpc-jaxrs=true");
        headers.put("Content-Type", contentTypeList);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        if (characterEncoding != null) {
            return characterEncoding;
        }
        String characterEncodingFromHeader = getCharacterEncodingFromHeader();
        if (characterEncodingFromHeader != null) {
            return characterEncodingFromHeader;
        }
        // next check, web-app context level default request encoding
        if (servletContext.getRequestCharacterEncoding() != null) {
            servletContext.getRequestCharacterEncoding();
        }
        //     // now check the container level default encoding (??)
        //     if (servletContext.getDeployment().getDeploymentInfo().getDefaultEncoding() != null) {
        //         return servletContext.getDeployment().getDeploymentInfo().getDefaultEncoding();
        //     }
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        if (readStarted) {
            return;
        }
        this.characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        return -1;
    }

    @Override
    public long getContentLengthLong() {
        return -1;
    }

    @Override
    public String getContentType() {
        return "*/*;grpc-jaxrs=true";
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (gotReader) {
            throw new IllegalStateException(Messages.MESSAGES.readerAlreadyReturned());
        }
        gotInputStream = true;
        readStarted = true;
        return inputStream;
    }

    @Override
    public String getParameter(String name) {
        extractParameters();
        if (parameters.containsKey(name)) {
            return parameters.get(name)[0];
        }
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        extractParameters();
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        extractParameters();
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        extractParameters();
        return parameters;
    }

    @Override
    public String getProtocol() {
        return "HTTP/2.0";
    }

    @Override
    public String getScheme() {
        return getUriInfo().getBaseUri().getScheme();
    }

    @Override
    public String getServerName() {
        return getUriInfo().getBaseUri().getHost();
    }

    @Override
    public int getServerPort() {
        return getUriInfo().getBaseUri().getPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (gotInputStream) {
            throw new IllegalStateException(Messages.MESSAGES.inputStreamAlreadyReturned());
        }
        gotReader = true;
        readStarted = true;
        return new BufferedReader(new InputStreamReader(this.inputStream));
    }

    @Override
    public String getRemoteAddr() {
        return clientAddr;
    }

    public void setRemoteAddr(String addr) {
        clientAddr = addr;
    }

    @Override
    public String getRemoteHost() {
        return clientHost;
    }

    public void setRemoteHost(String host) {
        clientHost = host;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        return getLocales().nextElement();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        final List<String> acceptLanguage = headers.get(HttpHeaders.ACCEPT_LANGUAGE);
        List<Locale> ret = LocaleUtils.getLocalesFromHeader(acceptLanguage);
        if (ret.isEmpty()) {
            return Collections.enumeration(Collections.singletonList(Locale.getDefault()));
        }
        return Collections.enumeration(ret);
    }

    @Override
    public boolean isSecure() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("isSecure()"));
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        if (path == null) {
            return null;
        }
        String realPath;
        if (path.startsWith("/")) {
            realPath = path;
        } else {
            String current = getUriInfo().relativize(getUriInfo().getBaseUri()).toString();
            int lastSlash = current.lastIndexOf("/");
            if (lastSlash != -1) {
                current = current.substring(0, lastSlash + 1);
            }
            realPath = current + path;
        }
        return servletContext.getRequestDispatcher(realPath);
    }

    @Override
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return clientPort;
    }

    public void setRemotePort(int port) {
        clientPort = port;
    }

    @Override
    public String getLocalName() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getLocalName()"));
    }

    @Override
    public String getLocalAddr() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getLocalAddr()"));
    }

    @Override
    public int getLocalPort() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getLocalPort()"));
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        if (asyncStarted) {
            throw Messages.MESSAGES.asyncProcessingAlreadyStarted();
        }
        asyncStarted = true;
        return asyncContext = new AsyncContextImpl(this, servletResponse);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        if (this != servletRequest) {
            if (!(servletRequest instanceof ServletRequestWrapper)) {
                throw Messages.MESSAGES.requestWasNotOriginalOrWrapper(servletRequest);
            }
        }
        if (this.servletResponse != servletResponse) {
            if (!(servletResponse instanceof ServletResponseWrapper)) {
                throw Messages.MESSAGES.responseWasNotOriginalOrWrapper(servletResponse);
            }
        }
        if (asyncStarted) {
            throw Messages.MESSAGES.asyncAlreadyStarted();
        }
        return asyncContext = new AsyncContextImpl(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return asyncStarted;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public AsyncContext getAsyncContext() {
        if (!isAsyncStarted()) {
            throw Messages.MESSAGES.asyncNotStarted();
        }
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }

    @Override
    public String getAuthType() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getAuthType()"));
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public long getDateHeader(String name) {
        String header = headers.get(name).get(0);
        if (header == null) {
            return -1;
        }
        Date date = DateUtils.parseDate(header);
        if (date == null) {
            throw Messages.MESSAGES.headerCannotBeConvertedToDate(header);
        }
        return date.getTime();
    }

    @Override
    public String getHeader(String name) {
        List<String> list = headers.get(name);
        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(headers.get(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        String header = getHeader(name);
        if (header == null) {
            return -1;
        }
        return Integer.parseInt(header);
    }

    @Override
    public String getMethod() {
        return LOCATOR.equals(method) ? null : method;
    }

    @Override
    public String getPathInfo() {
        return path;
    }

    @Override
    public String getPathTranslated() {
        return getRealPath(getPathInfo());
    }

    @Override
    public String getContextPath() {
        return servletContext.getContextPath();
    }

    @Override
    public String getQueryString() {
        return getUriInfo().getRequestUri().getQuery();
    }

    @Override
    public String getRemoteUser() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getRemoteUser()"));
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("isUserInRole()"));
    }

    @Override
    public Principal getUserPrincipal() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getUserPrincipal()"));
    }

    @Override
    public String getRequestedSessionId() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getRequestedSessionId()"));
    }

    @Override
    public String getRequestURI() {
        return getUriInfo().getAbsolutePath().toString();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(getUriInfo().getAbsolutePath().toString());
    }

    @Override
    public String getServletPath() {
        if (servletPath != null) {
            return servletPath;
        }
        int i = uri.indexOf(contextPath);
        if (i > -1) {
            i += contextPath.length();
            int j = uri.substring(i).indexOf(path);
            servletPath = uri.substring(i, i + j);
        } else {
            String wholePath = getUriInfo().getPath();
            int j = wholePath.indexOf(path);
            if (j > -1) {
                servletPath = wholePath.substring(0, j);
            } else {
                servletPath = "";
            }
        }
        return servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getSession()"));
    }

    @Override
    public HttpSession getSession() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getSession()"));
    }

    @Override
    public String changeSessionId() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("changeSessionId()"));
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("isRequestedSessionIdValid()"));
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("isRequestedSessionIdFromCookie()"));
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("isRequestedSessionIdFromURL()"));
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("isRequestedSessionIdFromUrl()"));
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("authenticate()"));
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("login()"));
    }

    @Override
    public void logout() throws ServletException {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("logout()"));
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        List<Part> list = new ArrayList<Part>();
        for (Entry<String, String[]> entry : formParameters.entrySet()) {
            Part part = new PartImpl(entry.getKey(), entry.getValue()[0]);
            list.add(part);
        }
        return list;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("getPart()"));
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new NotSupportedException(Messages.MESSAGES.isNotImplemented("upgrade()"));
    }

    public ServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(ServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        uriInfo = adjustUriInfo(new ResteasyUriInfo(uri, servletContext.getContextPath()));
        this.uri = uriInfo.getRequestUri().toString();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setInputStream(ServletInputStream sis) {
        this.inputStream = sis;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers.putAll(headers);
    }

    public boolean isGotInputStream() {
        return gotInputStream;
    }

    public void setGotInputStream(boolean gotInputStream) {
        this.gotInputStream = gotInputStream;
    }

    public boolean isGotReader() {
        return gotReader;
    }

    public void setGotReader(boolean gotReader) {
        this.gotReader = gotReader;
    }

    public boolean isReadStarted() {
        return readStarted;
    }

    public void setReadStarted(boolean readStarted) {
        this.readStarted = readStarted;
    }

    public String getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String[]> getFormParameters() {
        return formParameters;
    }

    public void setFormParameters(Map<String, String[]> formParameters) {
        this.formParameters = formParameters;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public static String getGrpcReturnResponse() {
        return ANY;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = adjustUriInfo(uriInfo);
        this.uri = uriInfo.getRequestUri().toString();
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    public void setAsyncContext(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
        if ("com.google.protobuf.Any".equals(entityType)
                || "google.protobuf.Any".equals(entityType)
                || "Any".equals(entityType)) {
            List<String> list = new ArrayList<String>();
            list.add("true");
            headers.put(ANY, list);
        }
    }

    private String getCharacterEncodingFromHeader() {
        String contentType = headers.get(HttpHeaders.CONTENT_TYPE).get(0);
        if (contentType == null) {
            return null;
        }
        return extractQuotedValueFromHeader(contentType, "charset");
    }

    /**
     * Extracts a quoted value from a header that has a given key. For instance if the header is
     * <p>
     * content-disposition=form-data; name="my field"
     * and the key is name then "my field" will be returned without the quotes.
     *
     * @param header The header
     * @param key    The key that identifies the token to extract
     *
     * @return The token, or null if it was not found
     *
     * @author Stuart Douglas
     */
    private static String extractQuotedValueFromHeader(final String header, final String key) {

        int keypos = 0;
        int pos = -1;
        boolean whiteSpace = true;
        boolean inQuotes = false;
        for (int i = 0; i < header.length() - 1; ++i) { //-1 because we need room for the = at the end
            //TODO: a more efficient matching algorithm
            char c = header.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    inQuotes = false;
                }
            } else {
                if (key.charAt(keypos) == c && (whiteSpace || keypos > 0)) {
                    keypos++;
                    whiteSpace = false;
                } else if (c == '"') {
                    keypos = 0;
                    inQuotes = true;
                    whiteSpace = false;
                } else {
                    keypos = 0;
                    whiteSpace = c == ' ' || c == ';' || c == '\t';
                }
                if (keypos == key.length()) {
                    if (header.charAt(i + 1) == '=') {
                        pos = i + 2;
                        break;
                    } else {
                        keypos = 0;
                    }
                }
            }

        }
        if (pos == -1) {
            return null;
        }

        int end;
        int start = pos;
        if (header.charAt(start) == '"') {
            start++;
            for (end = start; end < header.length(); ++end) {
                char c = header.charAt(end);
                if (c == '"') {
                    break;
                }
            }
            return header.substring(start, end);

        } else {
            //no quotes
            for (end = start; end < header.length(); ++end) {
                char c = header.charAt(end);
                if (c == ' ' || c == '\t' || c == ';') {
                    break;
                }
            }
            return header.substring(start, end);
        }
    }

    private UriInfo getUriInfo() {
        return uriInfo;
    }

    private void extractParameters() {
        if (parameters != null) {
            return;
        }
        parameters = formParameters;
        MultivaluedMap<String, String> queryParams = getUriInfo().getQueryParameters();
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            if (parameters.containsKey(entry.getKey())) {
                int formLength = parameters.get(entry.getKey()).length;
                int queryLength = entry.getValue().size();
                String[] array = Arrays.copyOf(parameters.get(entry.getKey()), formLength + queryLength);
                System.arraycopy(entry.getValue().toArray(), 0, array, formLength, queryLength);
                parameters.put(entry.getKey(), array);
            } else {
                String[] array = new String[entry.getValue().size()];
                parameters.put(entry.getKey(), entry.getValue().toArray(array));
            }
        }
    }

    // Make sure uriInfo.getRequestUri() is absolute for opentracing
    private UriInfo adjustUriInfo(UriInfo uriInfo) {
        String scheme = uriInfo.getRequestUri().getScheme();
        if (scheme == null || "".equals(scheme)) {
            String u = uriInfo.getRequestUri().toString();
            if (u.charAt(0) != '/') {
                u = '/' + u;
            }
            u = "http://localhost:8080" + u;
            uriInfo = new ResteasyUriInfo(u, servletContext.getContextPath());
        }
        return uriInfo;
    }
}
