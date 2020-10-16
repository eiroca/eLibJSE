/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.library.diagnostics.actions;

import java.io.IOException;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import org.w3c.dom.Node;
import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.TypeDefinition;
import com.predic8.soamodel.Consts;
import com.predic8.wsdl.AbstractAddress;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Input;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.xml.util.ExternalResolver;
import net.eiroca.library.config.parameter.BooleanParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.IConverter;
import net.eiroca.library.diagnostics.actiondata.ActionData;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.system.IContext;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

public class WebServiceAction extends HTTPAction {

  private final class WsAuthenticator extends Authenticator {

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      final PasswordAuthentication pa = new PasswordAuthentication(pProxyUser.get(), pProxyPass.get().toCharArray());
      return pa;
    }
  }

  private final class WsProxySelector extends ProxySelector {

    @Override
    public List<Proxy> select(final URI uri) {
      // Setting up a new ProxySelector implementation
      final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(pProxyHost.get(), pProxyPort.get()));
      final ArrayList<Proxy> list = new ArrayList<>();
      list.add(proxy);
      context.debug("Inner ProxySelector: select method: created proxy: localhost, 6670");
      context.debug("Inner ProxySelector: select method: " + Arrays.toString(list.toArray()));
      return list;
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
      new RuntimeException("Inner ProxySelector:: connectFailed method: Connection to " + uri + " failed. Exception message is '" + ioe.getMessage() + "'");
    }
  }

  public static final int DEFAULT_EXTERNAL_RESOLVER_TIMEOUT = 30000;

  static final Map<String, String> BINDING_ID_MAP = new HashMap<>();
  static {
    WebServiceAction.BINDING_ID_MAP.put("SOAP11", SOAPBinding.SOAP11HTTP_BINDING);
    WebServiceAction.BINDING_ID_MAP.put("SOAP12", SOAPBinding.SOAP12HTTP_BINDING);
    WebServiceAction.BINDING_ID_MAP.put("HTTP", HTTPBinding.HTTP_BINDING);
  }

  final StringParameter pWSDL = new StringParameter(params, "wsdl", null, true, false);
  final StringParameter pOperation = new StringParameter(params, "wsOperation", null, true, false);
  final StringParameter pParameters = new StringParameter(params, "wsParameters", null, true, false);
  final BooleanParameter pUsePrefix = new BooleanParameter(params, "wsUsePrefix", true, true, false);
  final BooleanParameter pIsDotNet = new BooleanParameter(params, "isDotNET", true, true, false);

  private String wsOperationName;
  private String wsPortName;
  private Properties wsParmsSubstituter;
  private String wsProtocol;
  private String wsBindingId;
  private AbstractAddress wsAddress;
  private Definitions wsDefinitions;
  private String wsTargetNamespace;
  private String wsLocation;
  private WsOpParams wsOpParams;
  private Port wsPort;
  private Binding wsBinding;

  @Override
  public void setup(final IContext context) throws CommandException {
    super.setup(context);
    final Properties p = new Properties();
    try {
      p.load(new StringReader(pParameters.get()));
      wsParmsSubstituter = p;
    }
    catch (final IOException e) {
      CommandException.ConfigurationError(e.getMessage());
    }
    // WS data
    final WSDLParser parser = new WSDLParser();
    // setup external resolver if proxy is used
    if (pHasProxy.get()) {
      final ExternalResolver er = new ExternalResolver();
      er.setProxyHost(pProxyHost.get());
      er.setProxyPort(pProxyPort.get());
      er.setTimeout(WebServiceAction.DEFAULT_EXTERNAL_RESOLVER_TIMEOUT);
      parser.setResourceResolver(er);
    }
    wsDefinitions = parser.parse(pWSDL.get());
    wsTargetNamespace = wsDefinitions.getTargetNamespace();
    if (!WebServiceAction.isOperation(wsDefinitions, pOperation.get())) {
      CommandException.ConfigurationError("setup method: Operation '" + pOperation.get() + "' is not found");
    }
    wsPort = WebServiceAction.getPort(wsDefinitions, null);
    wsPortName = (wsPort != null) ? wsPort.getName() : null;
    wsBinding = (wsPort != null) ? wsPort.getBinding() : null;
    wsProtocol = (wsBinding != null) ? wsBinding.getProtocol().toString() : null;
    if ((wsProtocol == null) || wsProtocol.isEmpty()) {
      wsBinding = WebServiceAction.getBinding(wsDefinitions, wsPortName);
      wsProtocol = (wsBinding != null) ? wsBinding.getProtocol().toString() : null;
    }
    if ((wsProtocol == null) || wsProtocol.isEmpty()) {
      wsBindingId = SOAPBinding.SOAP11HTTP_BINDING;
    }
    else {
      wsBindingId = WebServiceAction.BINDING_ID_MAP.get(wsProtocol.toUpperCase());
    }
    wsAddress = (wsPort != null) ? wsPort.getAddress() : null;
    wsLocation = (wsAddress != null) ? wsAddress.getLocation() : null;
    // check if port name, binding id, and location are all populated
    if (LibStr.isEmptyOrNull(wsPortName) || LibStr.isEmptyOrNull(wsBindingId) || LibStr.isEmptyOrNull(wsLocation)) {
      CommandException.ConfigurationError("setup method: there is no port or binding id or location found in the wsdl");
    }
    context.info("setup method: PortName is '" + wsPortName + "', bindingId is '" + wsBindingId + "', location is '" + wsLocation + "'");
    wsOpParams = WebServiceAction.getRequestParms(wsDefinitions, wsPortName, wsOperationName);
    context.info(params);
  }

  @Override
  public ReturnObject execute(final ActionData action) throws CommandException {
    final ReturnObject result = new ReturnObject();
    try {
      // WS service call
      // setup default proxy selector and authenticator
      if (pHasProxy.get()) {
        ProxySelector.setDefault(new WsProxySelector()); // added proxy selector
        // setup default authenticator if proxy authentication is used
        if ((pProxyUser.get() != null) && !pProxyUser.get().isEmpty()) {
          AuthCacheValue.setAuthCache(new AuthCacheImpl());
          Authenticator.setDefault(new WsAuthenticator());
        }
      }

      // define service
      final javax.xml.namespace.QName operationQName = new javax.xml.namespace.QName(wsTargetNamespace, wsOperationName);
      final javax.xml.namespace.QName portQName = new javax.xml.namespace.QName(wsTargetNamespace, wsPortName);
      final javax.xml.ws.Service svc = javax.xml.ws.Service.create(operationQName);
      svc.addPort(portQName, wsBindingId, wsLocation);

      // create dispatch object from this service
      final Dispatch<SOAPMessage> dispatch = svc.createDispatch(portQName, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
      // The soapActionUri is set here. otherwise we get an error on .net based services.
      if (pIsDotNet.get()) {
        final String soapActionUri = new StringBuilder(wsTargetNamespace).append("/").append(wsOperationName).toString();
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, new Boolean(true));
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapActionUri);
      }
      // add SOAP message
      final MessageFactory factory = MessageFactory.newInstance();

      final SOAPMessage request = factory.createMessage();
      final SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();

      final SOAPBody soapBody = request.getSOAPBody();
      soapBody.addNamespaceDeclaration("q0", wsTargetNamespace);
      final SOAPBodyElement soapBodyElement = soapBody.addBodyElement(envelope.createName(wsOperationName, "q0", wsTargetNamespace));
      // add parameters
      for (final Element p : wsOpParams.getArgList()) {
        addElement(p, WebServiceAction.getComplexTypeStructure(p.getName(), wsOpParams), soapBodyElement, converter);
      }
      // invoke web service
      final SOAPMessage response = dispatch.invoke(request);
      Node wrapper = response.getSOAPBody().getFirstChild();
      while (wrapper.getNodeType() != Node.ELEMENT_NODE) {
        wrapper = wrapper.getNextSibling();
      }
      Node part = wrapper.getFirstChild();
      while (part.getNodeType() != Node.ELEMENT_NODE) {
        part = part.getNextSibling();
      }
      result.setOutput(part.getFirstChild().getNodeValue());
      result.setRetCode(0);
    }
    catch (final Exception e) {
      result.setRetCode(1);
      result.setOutput(new StringBuilder("execute method: exception is '").append(Helper.getExceptionAsString(e)).append("'").toString());
    }
    return result;
  }

  private static boolean isOperation(final Definitions defs, final String operationName) {
    for (final PortType pt : defs.getPortTypes()) {
      for (final Operation op : pt.getOperations()) {
        if (op.getName().equals(operationName)) { return true; }
      }
    }
    return false;
  }

  private static Port getPort(final Definitions defs, final String portName) throws CommandException {
    final List<Service> services = defs.getServices();
    Port port = null;
    if ((services != null) && !services.isEmpty()) {
      if ((portName != null) && portName.isEmpty()) {
        // check if port with portName exists
        for (final Service service : services) {
          final List<Port> ports = service.getPorts();
          if ((ports != null) && !ports.isEmpty()) {
            for (final Port p : ports) {
              if (p.getName().equals(portName)) {
                port = p;
              }
            }
          }
          else {
            CommandException.ConfigurationError("getPort method: Check wsdl. Service '" + service.getName() + "' has no ports");
          }
        }
      }
      else {
        final List<Port> ports = services.get(0).getPorts();
        if ((ports != null) && !ports.isEmpty()) {
          port = services.get(0).getPorts().get(0);
        }
        else {
          CommandException.ConfigurationError("getPort method: Check wsdl. Service '" + services.get(0).getName() + "' has no ports");
        }
      }
    }
    else {
      CommandException.ConfigurationError("getPort method: Check wsdl. Array of services is null or empty");
    }
    return port;
  }

  private static Binding getBinding(final Definitions defs, final String portName) {
    final List<Binding> bindings = defs.getBindings();
    if (bindings != null) {
      for (final Binding binding : bindings) {
        if (binding.getName().equalsIgnoreCase(portName)) { return binding; }
      }
    }
    return null;
  }

  private static boolean isParameterIn(final Map<Element, List<Element>> map, final Element parameter) {
    for (final Element p : map.keySet()) {
      if (p.getName().equals(parameter.getName())) { return true; }
    }
    return false;
  }

  private static List<Element> getComplexTypeStructure(final String name, final WsOpParams wsOpParams) {
    final LinkedHashMap<Element, List<Element>> complexTypes = wsOpParams.getComplexTypes();
    for (final Element p : complexTypes.keySet()) {
      if (p.getName().equals(name)) { return wsOpParams.getComplexTypes().get(p); }
    }
    return null;
  }

  private static void listParameters1(final Element element, final WsOpParams wsOpParams) throws CommandException {
    ComplexType ct = (ComplexType)element.getEmbeddedType();
    if (ct == null) {
      if (element.getType().getLocalPart().equals("complexType")) {
        ct = element.getSchema().getComplexType(element.getName());
      }
      else {
        ct = element.getSchema().getComplexType(element.getType().getLocalPart());
      }
    }
    if (ct == null) { return; }
    List<Element> list = WebServiceAction.getComplexTypeStructure(element.getName(), wsOpParams);
    if (list == null) {
      wsOpParams.getComplexTypes().put(element, list = new ArrayList<>());
    }
    for (final Element e : ct.getSequence().getElements()) {
      list.add(e);
      // Fix for invalid schema
      if (e.getType() == null) {
        CommandException.ConfigurationError("listParameters1 method: Check wsdl. Element '" + element.getName() + "' has no type");
      }
      if (!e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) || e.getType().getLocalPart().equals("complexType")) {
        // element is a complex type: let's drill into it
        if (!WebServiceAction.isParameterIn(wsOpParams.getComplexTypes(), e)) {
          WebServiceAction.listParameters(e, wsOpParams);
        }
      }
    }
  }

  private static void listParameters(final Element element, final WsOpParams wsOpParams) throws CommandException {
    final TypeDefinition tDef = element.getEmbeddedType();
    ComplexType ct = (ComplexType)tDef;
    if ((ct != null) && (ct.getSequence() != null) && (ct.getSequence().getElements() != null) && !ct.getSequence().getElements().isEmpty()) {
      for (final Element e : ct.getSequence().getElements()) {
        wsOpParams.getArgList().add(e);
        if (!e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) || (e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && e.getType().getLocalPart().equals("complexType"))) {
          // for complex types drill into their structure
          if (!WebServiceAction.isParameterIn(wsOpParams.getComplexTypes(), e)) {
            wsOpParams.getComplexTypes().put(e, new ArrayList<Element>());
            WebServiceAction.listParameters1(e, wsOpParams);
          }
        }
      }
    }
    else if ((element.getType() != null) && (element.getType().getLocalPart() != null) && element.getType().getLocalPart().equals("complexType")) {
      ct = element.getSchema().getComplexType(element.getName());
      if ((ct != null) && (ct.getSequence() != null) && (ct.getSequence().getElements() != null) && !ct.getSequence().getElements().isEmpty()) {
        List<Element> list;
        wsOpParams.getComplexTypes().put(element, (list = new ArrayList<>()));
        for (final Element e : ct.getSequence().getElements()) {
          list.add(e);
          if (e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && e.getType().getLocalPart().equals("complexType")) {
            WebServiceAction.listParameters1(e, wsOpParams);
          }
        }
      }
    }
    else {
      CommandException.ConfigurationError("listParameters method: Check wsdl. Complex type is null, or sequence is null, or elements list is null, or elements list is empty");
    }
  }

  private static WsOpParams getRequestParms(final Definitions defs, final String portName, final String operation) throws CommandException {
    final WsOpParams wsOpParams = new WsOpParams(new ArrayList<Element>(), new LinkedHashMap<Element, List<Element>>());
    Element e;
    for (final PortType pt : defs.getPortTypes()) {
      for (final Operation op : pt.getOperations()) {
        final String opName = op.getName();
        if (opName.equals(operation)) {
          // get request parameters
          final Input in = op.getInput();
          final Message message = (in != null) ? in.getMessage() : null;
          final List<Part> parts = (message != null) ? message.getParts() : null;
          if ((in != null) && (message != null) && (parts != null) && !parts.isEmpty()) {
            for (final Part part : parts) {
              final Element e1 = part.getElement();
              final groovy.xml.QName qName = (e1 != null) ? e1.getQname() : null;
              if ((e1 != null) && (qName != null)) {
                e = defs.getElement(qName);
                if (e != null) {
                  WebServiceAction.listParameters(e, wsOpParams);
                }
                else {
                  CommandException.ConfigurationError("getRequestParms method: Check wsdl. Element is null");
                }
              }
              else if (e1 == null) {
                if ((part.getType() != null) && (part.getType().getQname() != null) && (part.getType().getQname().getLocalPart() != null) && !part.getType().getQname().getLocalPart().equals("complexType")) {
                  final String name = (part != null) ? part.getName() : null;
                  final TypeDefinition type = (part != null) ? part.getType() : null;
                  final groovy.xml.QName qn = (type != null) ? type.getQname() : null;
                  final String s = (qn != null) ? qn.getLocalPart() : null;
                  if ((name != null) && !part.getName().isEmpty() && (type != null) && (qn != null) && (s != null) && !s.isEmpty()) {
                    final WSElement wsElement = new WSElement(name, qn);
                    wsOpParams.getArgList().add(wsElement);
                  }
                  else {
                    CommandException.ConfigurationError("getRequestParms method: Check wsdl. Element of Part is null and other available information is incomplete");
                  }
                }
                else {
                  CommandException.ConfigurationError("getRequestParms method: Check wsdl. Element e1 of Part is null and other available information is incomplete");
                }
              }
            }
          }
          else {
            CommandException.ConfigurationError("getRequestParms method: Check wsdl. Input is null or input message is null or parts is null or parts array is empty");
          }
        }
      }
    }
    return wsOpParams;
  }

  private String getStringValue(final Element element, final IConverter converter) {
    final String key = element.getName();
    final Properties customMap = wsParmsSubstituter;
    final String value = customMap.getProperty(key);
    if (value != null) {
      // process value string
      final String s = converter.convert(value);
      return s;
    }
    else {
      return null;
    }
  }

  private void addElement(final Element element, final List<Element> list, final SOAPElement soapElement, final IConverter converter) throws SOAPException {
    if (element.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && !element.getType().getLocalPart().equals("complexType")) {
      // add simple type
      final String value = getStringValue(element, converter);
      if ((value == null) || value.isEmpty()) {
        // element is not present in the WS Parameters list or its value is empty
        if (!element.getMinOccurs().equals("0")) { throw new RuntimeException("addElement method: element '" + element.getName() + "' should be present in the WS Parameters list as its minOccurs is '" + element.getMinOccurs() + "'"); }
      }
      else {
        if (pUsePrefix.get()) {
          soapElement.addChildElement(element.getName(), "q0").addTextNode(value);
        }
        else {
          soapElement.addChildElement(element.getName()).addTextNode(value);
        }
      }
    }
    else {
      // add complex type
      SOAPElement structureElement;
      if (pUsePrefix.get()) {
        structureElement = soapElement.addChildElement(element.getName(), "q0");
      }
      else {
        // added .addTextNode("") as workaround to avoid <... xmlns="">
        structureElement = soapElement.addChildElement(element.getName()).addTextNode("");
      }
      for (final Element e : list) {
        if (e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && !e.getType().getLocalPart().equals("complexType")) {
          // add simple type
          final String value = getStringValue(e, converter);
          if ((value == null) || value.isEmpty()) {
            // element is not present in the WS Parameters list or its value is empty
            if (!e.getMinOccurs().equals("0")) { throw new RuntimeException("addElement method: structure element '" + e.getName() + "' should be present in the WS Parameters list as its minOccurs is '" + e.getMinOccurs() + "'"); }
          }
          else {
            if (pUsePrefix.get()) {
              structureElement.addChildElement(e.getName(), "q0").addTextNode(value);
            }
            else {
              structureElement.addChildElement(e.getName()).addTextNode(value);
            }
          }
        }
        else {
          // add complex type
          addElement(e, WebServiceAction.getComplexTypeStructure(e.getName(), wsOpParams), structureElement, converter);
        }
      }
    }

  }

  protected static String getDefaultValue(final Element element) {
    if (element.getType().getLocalPart().equals("string")) {
      return "_UNDEFINED_" + element.getName() + "_";
    }
    else if (element.getType().getLocalPart().equals("int")) {
      return String.valueOf(Integer.MIN_VALUE);
    }
    else if (element.getType().getLocalPart().equals("boolean")) {
      return "false";
    }
    else if (element.getType().getLocalPart().equals("dateTime")) {
      final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
      return sdf.format(new Date());
    }
    else {
      return element.getName() + element.getType();
    }
  }

}
