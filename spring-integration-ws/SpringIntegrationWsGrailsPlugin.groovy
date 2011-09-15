import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.router.HeaderValueRouter
import org.springframework.integration.transformer.MessageTransformingHandler
import org.springframework.integration.ws.SimpleWebServiceInboundGateway
import org.springframework.integration.xml.selector.XmlValidatingMessageSelector
import org.springframework.integration.xml.transformer.XPathHeaderEnricher
import org.springframework.integration.xml.transformer.XPathHeaderEnricher.XPathExpressionEvaluatingHeaderValueMessageProcessor
import org.springframework.ws.server.endpoint.mapping.UriEndpointMapping
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection
import ws.debruijn.grails.plugin.springframework.integration.xml.Source2GPathResultTransformer
import org.springframework.integration.config.*
import grails.util.GrailsNameUtils

class SpringIntegrationWsGrailsPlugin {
  static final String PACKAGE_NAME = 'ws.debruijn.grails.plugin.springframework.integration.ws'
  static final String SERVLET_NAME = 'spring-ws'
  static final String SERVICE_URL = '/services'
  static final String MESSAGE_TYPE_HEADER_PROPERTY_NAME = 'siWsType'
  static final String ENDPOINT_SERVICE_NAMESPACE_PROPERTY = 'serviceNamespace'
  static final String ENDPOINT_WSDL_FILENAME_PROPERTY = 'wsdlFilename'
  static final String ENDPOINT_REQUEST_NAMESPACE_PROPERTY = 'requestNamespace'
  static final String ENDPOINT_REQUEST_XSD_FILENAME_PROPERTY = 'requestXsdFilename'
  static final String ENDPOINT_REQUEST_SUFFIX_PROPERTY = 'requestSuffix'
  static final String ENDPOINT_VALIDATE_REQUEST_PROPERTY = 'validateRequest'
  static final String ENDPOINT_RESPONSE_XSD_FILENAME_PROPERTY = 'responseXsdFilename'
  static final String ENDPOINT_RESPONSE_SUFFIX_PROPERTY = 'responseSuffix'
  static final String ENDPOINT_VALIDATE_RESPONSE_PROPERTY = 'validateResponse'
  static final String ENDPOINT_FAULT_SUFFIX_PROPERTY = 'faultSuffix'
  static final String ENDPOINT_METHOD_NAME = 'invoke'
  static final String DEFAULT_REQUEST_SUFFIX = 'Request'
  static final String DEFAULT_RESPONSE_SUFFIX = 'Response'
  static final String DEFAULT_FAULT_SUFFIX = 'Fault'
  static final Log log = LogFactory.getLog("$PACKAGE_NAME.${this.simpleName}")

  def version = "2.0.5.1.RELEASE"
  def grailsVersion = "1.3.7 > *"
  def dependsOn = [springIntegrationCore: '2.0.5 > 2.0.6']
  def pluginExcludes = [
          'grails-app/services/ws/debruijn/grails/plugin/springframework/integration/ws/test/ServiceClassService.groovy',
          'grails-app/views/error.gsp',
          'src/java/ws/debruijn/grails/plugin/springframework/integration/ws/test/service.xsd'
  ]

  def author = "Steef de Bruijn"
  def authorEmail = "steef@debruijn.ws"
  def title = "Spring Integration support for Grails - WebServices"
  def description = '''\\
This plugin provides support for Spring Integration WebServices.

It creates a spring-ws servlet on /services, and creates an UriEndpointMapping
that publishes the message to the spring-integration-ws-requests channel.

All service classes are scanned for "requestNamespace" static property. For all these classes an endpoint
will be configured and a generated WSDL will be exported as /services/<serviceClass.logicalPropertyName>.wsdl.
The provided XSDs will be scanned for request, response and fault messages for this. This scan can be configured
for non-standard Request, Response and Fault suffixes with static properties "requestSuffix", "responseSuffix"
and "faultSuffix". The service namespace in the exported WSDL will be defaulted to the given "requestNamespace",
but can be customized with the static property "serviceNamespace".

Required static attributes are:
- requestNamespace : indicates namespace of request, triggers plugin
- requestXsdFilename : Filename of XSD defining request, located on classpath

Required method is
- String invoke (message), where the message is a to a GPathResult converted payload

Optional static attributes are:
- wsdlFilename : if given, must be located on classpath and will be exported as
                 /services/<serviceClass.scriptName>-formal.wsdl.
- serviceNamespace : used for dynamic WSDL generation, defaults to requestNamespace
- responseXsdFilename : Filename of XSD defining response, located on classpath
- requestSuffix, responseSuffix, faultSuffix : change defaults when scanning XSDs for WSDL generation
- validateRequest, validateResponse : when set to true, the request and/or response messages will be validated
                                      against the given "requestXsdFilename" and "responseXsdFilename"

TIP: Put wsdl files in <your-grails-app>/src/java/wsdl and xsd files in <your-grails-app>/src/java/xsd.
     Refer to these files as 'wsdl/blah.wsdl' and/or 'xsd/blah.xsd' in your service classes.
     This way these files can be served from your own application plugins instead of being hard-wired
     in your WEB-INF directory of your Grails application.

Please post your issues on GitHub
'''

  def documentation = "http://steefdebruijn.github.com/grails-spring-integration"

  def doWithWebDescriptor = { xml ->
    getAllEndpointClasses.delegate = delegate
    if (getAllEndpointClasses().size() > 0) {
      configureSpringWsServlet(xml)
    }
  }

  private void configureSpringWsServlet(xml) {
    createSpringWsServlet(xml)
    createSpringWsServletMapping(xml)
  }

  private void createSpringWsServlet(xml) {
    log.info "Creating $SERVLET_NAME servlet..."
    def servlets = xml.servlet
    servlets[servlets.size() - 1] + {
      servlet {
        'display-name'("web-services")
        'servlet-name'(SERVLET_NAME)
        'servlet-class'("org.springframework.ws.transport.http.MessageDispatcherServlet")
      }
    }
  }

  private void createSpringWsServletMapping(xml) {
    log.info "Mapping $SERVLET_NAME servlet to $SERVICE_URL..."
    def servletMappings = xml.'servlet-mapping'
    servletMappings[servletMappings.size() - 1] + {
      'servlet-mapping' {
        'servlet-name'(SERVLET_NAME)
        'url-pattern'("$SERVICE_URL/*")
      }
    }
  }

  def doWithSpring = {
    getAllEndpointClasses.delegate = delegate
    if (getAllEndpointClasses().size() > 0) {
      createGenericBeans.delegate = delegate
      createGenericBeans()

      configureBeansForAllEndpoints.delegate = delegate
      configureBeansForAllEndpoints()

      createNamespaceChannelMap.delegate = delegate
      createNamespaceChannelMap()
    }
  }

  private def createGenericBeans = {
    createChannels.delegate = delegate
    createChannels()

    createServiceEndpoint.delegate = delegate
    createServiceEndpoint()

    createMessageTypeLabeler.delegate = delegate
    createMessageTypeLabeler()

    createMessageRouter.delegate = delegate
    createMessageRouter()

    createPayloadTransformer.delegate = delegate
    createPayloadTransformer()
  }

  private def createChannels = {
    log.info "Creating channels..."
    'siWsRequestsChannel'(DirectChannel)
    'siWsTypedRequestsChannel'(DirectChannel)
  }

  private def createServiceEndpoint = {
    log.info "Creating service endpoint..."
    'siWsEndpoint'(UriEndpointMapping) {
      defaultEndpoint = ref('siWsInboundGateway')
    }
    'siWsInboundGateway'(SimpleWebServiceInboundGateway) {
      requestChannel = ref('siWsRequestsChannel')
    }
  }

  private def createMessageTypeLabeler = {
    log.info "Creating message type labeler..."
    'siWsRequestTypeLabeler'(ConsumerEndpointFactoryBean) {
      inputChannelName = 'siWsRequestsChannel'
      handler = ref('siWsRequestTypeLabelerHandler')
    }
    'siWsRequestTypeLabelerHandler'(MessageTransformingHandler, ref('siWsRequestTypeLabelerHeaderEnricher')) {
      outputChannel = ref('siWsTypedRequestsChannel')
    }
    Map map = new HashMap()
    map.put(MESSAGE_TYPE_HEADER_PROPERTY_NAME, new XPathExpressionEvaluatingHeaderValueMessageProcessor('namespace-uri()'))
    'siWsRequestTypeLabelerHeaderEnricher'(XPathHeaderEnricher, map)
  }

  private def createMessageRouter = {
    log.info "Creating message router..."
    'siWsMessageRouter'(ConsumerEndpointFactoryBean) {
      inputChannelName = 'siWsTypedRequestsChannel'
      handler = ref('siWsMessageRouterHandler')
    }
    'siWsMessageRouterHandler'(RouterFactoryBean) {
      resolutionRequired = true
      targetObject = ref('siWsMessageRouterHeaderValue')
    }
  }

  private def createPayloadTransformer = {
    log.info "Creating payload transformer..."
    'siWsPayloadToGPathResultTransformer'(Source2GPathResultTransformer)
  }

  private def configureBeansForAllEndpoints = {
    getAllEndpointClasses.delegate = delegate
    for (serviceClass in getAllEndpointClasses()) {
      configureBeansForEndpoint.delegate = delegate
      configureBeansForEndpoint(serviceClass)
    }
  }

  def getAllEndpointClasses = {
    application.serviceClasses.findAll { it.hasProperty(ENDPOINT_REQUEST_NAMESPACE_PROPERTY) }
  }

  private def configureBeansForEndpoint = { endpointClass ->
    String beanPrefix = endpointClass.logicalPropertyName
    String staticWsdlFilename = getWsdlFilenameForEndpoint(endpointClass)
    Boolean staticWsdlMustBeExported = getStaticWsdlMustBeExportedForEndpoint(endpointClass)
    Boolean requestMustBeValidated = getRequestValidationIndicatorForEndpoint(endpointClass)
    Boolean responseMustBeValidated = getResponseValidationIndicatorForEndpoint(endpointClass)
    String inputChannelNameForTransformer = "${beanPrefix}RequestChannel"
    String outputChannelNameForEndpoint = "${beanPrefix}ResponseChannel"

    if (staticWsdlMustBeExported) {
      exportStaticWsdlWithBeanPrefix.delegate = delegate
      exportStaticWsdlWithBeanPrefix(staticWsdlFilename, beanPrefix)
    }

    exportDynamicWsdlForEndpointWithBeanPrefix.delegate = delegate
    exportDynamicWsdlForEndpointWithBeanPrefix(endpointClass, beanPrefix)

    if (requestMustBeValidated) {
      createRequestValidatorForEndpointWithBeanPrefix.delegate = delegate
      createRequestValidatorForEndpointWithBeanPrefix(endpointClass, beanPrefix)
      inputChannelNameForTransformer = "${beanPrefix}RequestValidatedChannel"
    }

    createEndpointTransformerWithBeanPrefixAndInputChannelName.delegate = delegate
    createEndpointTransformerWithBeanPrefixAndInputChannelName(endpointClass, beanPrefix, inputChannelNameForTransformer)

    if (responseMustBeValidated) {
      createEndpointHandlerWithBeanPrefixAndOutputChannelName.delegate = delegate
      createEndpointHandlerWithBeanPrefixAndOutputChannelName(endpointClass, beanPrefix, outputChannelNameForEndpoint)

      createResponseValidatorForEndpointWithBeanPrefixAndInputChannelName.delegate = delegate
      createResponseValidatorForEndpointWithBeanPrefixAndInputChannelName(endpointClass, beanPrefix, outputChannelNameForEndpoint)
    } else {
      createEndpointHandlerWithBeanPrefix.delegate = delegate
      createEndpointHandlerWithBeanPrefix(endpointClass, beanPrefix)
    }
  }

  private def exportStaticWsdlWithBeanPrefix = { wsdlFilename, beanPrefix ->
    String wsdlName = GrailsNameUtils.getScriptName(beanPrefix)
    log.info "Exporting ${wsdlName}-formal.wsdl..."
    "${wsdlName}-formal"(SimpleWsdl11Definition) {
      wsdl = wsdlFilename
    }
  }

  private def exportDynamicWsdlForEndpointWithBeanPrefix = { endpointClass, beanPrefix ->
    String wsdlName = GrailsNameUtils.getScriptName(beanPrefix)
    log.info "Exporting ${wsdlName}.wsdl..."
    String serviceLocation = "${application.config.grails.serverURL}$SERVICE_URL"
    String endpointServiceNamespace = getServiceNamespaceForEndpoint(endpointClass)
    String endpointRequestSuffix = getRequestSuffixForEndpoint(endpointClass)
    String endpointResponseSuffix = getResponseSuffixForEndpoint(endpointClass)
    String endpointFaultSuffix = getFaultSuffixForEndpoint(endpointClass)
    List xsdFilenames = getXsdFilenamesForEndpoint(endpointClass)
    "$wsdlName"(DefaultWsdl11Definition) {
      targetNamespace = endpointServiceNamespace
      portTypeName = beanPrefix
      requestSuffix = endpointRequestSuffix
      responseSuffix = endpointResponseSuffix
      faultSuffix = endpointFaultSuffix
      schemaCollection = ref("$beanPrefix-xsds")
      locationUri = serviceLocation
    }
    "$beanPrefix-xsds"(CommonsXsdSchemaCollection) {
      xsds = xsdFilenames
      inline = true
    }
  }

  private def createRequestValidatorForEndpointWithBeanPrefix = { endpointClass, beanPrefix ->
    log.info "Creating request validator for $beanPrefix..."
    "${beanPrefix}RequestChannel"(DirectChannel)
    "${beanPrefix}RequestValidator"(ConsumerEndpointFactoryBean) {
      inputChannelName = "${beanPrefix}RequestChannel"
      handler = ref("${beanPrefix}RequestValidatorHandler")
    }
    "${beanPrefix}RequestValidatorHandler"(FilterFactoryBean) {
      throwExceptionOnRejection = true
      outputChannel = ref("${beanPrefix}RequestValidatedChannel")
      targetObject = ref("${beanPrefix}RequestValidatingMessageSelector")
    }
    "${beanPrefix}RequestValidatingMessageSelector"(XmlValidatingMessageSelector,
            getRequestXsdFilenameForEndpoint(endpointClass),
            "http://www.w3.org/2001/XMLSchema") {
      throwExceptionOnRejection = true
    }
  }

  private def createEndpointTransformerWithBeanPrefixAndInputChannelName = { endpointClass, beanPrefix, inputChannel ->
    log.info "Creating endpoint transformer for $beanPrefix..."
    "${inputChannel}"(DirectChannel)
    "${beanPrefix}Transformer"(ConsumerEndpointFactoryBean) {
      inputChannelName = "${inputChannel}"
      handler = ref("${beanPrefix}TransformerHandler")
    }
    "${beanPrefix}TransformerHandler"(TransformerFactoryBean) {
      targetObject = ref('siWsPayloadToGPathResultTransformer')
      targetMethodName = 'transform'
      outputChannel = ref("${beanPrefix}PayloadAsGPathResultChannel")
    }
  }

  private def createEndpointHandlerWithBeanPrefixAndOutputChannelName = { endpointClass, beanPrefix, outputChannelName ->
    String serviceBeanName = endpointClass.propertyName
    log.info "Creating endpoint handler for $beanPrefix..."
    "${beanPrefix}PayloadAsGPathResultChannel"(DirectChannel)
    "${beanPrefix}Endpoint"(ConsumerEndpointFactoryBean) {
      inputChannelName = "${beanPrefix}PayloadAsGPathResultChannel"
      handler = ref("${beanPrefix}EndpointHandler")
    }
    "${beanPrefix}EndpointHandler"(ServiceActivatorFactoryBean) {
      targetObject = ref(serviceBeanName)
      targetMethodName = ENDPOINT_METHOD_NAME
      outputChannel = ref(outputChannelName)
    }
  }

  private def createResponseValidatorForEndpointWithBeanPrefixAndInputChannelName = { endpointClass, beanPrefix, inputChannel ->
    log.info "Creating response validator for $beanPrefix..."
    "${inputChannel}"(DirectChannel)
    "${beanPrefix}ResponseValidator"(ConsumerEndpointFactoryBean) {
      inputChannelName = "${inputChannel}"
      handler = ref("${beanPrefix}ResponseValidatorHandler")
    }
    "${beanPrefix}ResponseValidatorHandler"(FilterFactoryBean) {
      throwExceptionOnRejection = true
      targetObject = ref("${beanPrefix}ResponseValidatingMessageSelector")
    }
    "${beanPrefix}ResponseValidatingMessageSelector"(XmlValidatingMessageSelector,
            getResponseXsdFilenameForEndpoint(endpointClass),
            "http://www.w3.org/2001/XMLSchema") {
      throwExceptionOnRejection = true
    }
  }

  private def createEndpointHandlerWithBeanPrefix = { endpointClass, beanPrefix ->
    String serviceBeanName = endpointClass.propertyName
    log.info "Creating endpoint handler for $beanPrefix..."
    "${beanPrefix}PayloadAsGPathResultChannel"(DirectChannel)
    "${beanPrefix}Endpoint"(ConsumerEndpointFactoryBean) {
      inputChannelName = "${beanPrefix}PayloadAsGPathResultChannel"
      handler = ref("${beanPrefix}EndpointHandler")
    }
    "${beanPrefix}EndpointHandler"(ServiceActivatorFactoryBean) {
      targetObject = ref(serviceBeanName)
      targetMethodName = ENDPOINT_METHOD_NAME
    }
  }

  private def createNamespaceChannelMap = {
    getAllEndpointClasses.delegate = delegate
    Map map = new HashMap()
    getAllEndpointClasses().each {
      map.put(getRequestNamespaceForEndpoint(it),
              "${it.logicalPropertyName}RequestChannel".toString())
    }
    'siWsMessageRouterHeaderValue'(HeaderValueRouter, MESSAGE_TYPE_HEADER_PROPERTY_NAME) {
      channelIdentifierMap = map
    }
  }

  private String getServiceNamespaceForEndpoint(endpointClass) {
    getValueOfStaticPropertyForGrailsClass(ENDPOINT_SERVICE_NAMESPACE_PROPERTY, endpointClass) ?:
      getRequestNamespaceForEndpoint(endpointClass)
  }

  private String getWsdlFilenameForEndpoint(endpointClass) {
    "classpath:${getValueOfStaticPropertyForGrailsClass(ENDPOINT_WSDL_FILENAME_PROPERTY, endpointClass)}".toString()
  }

  private Boolean getStaticWsdlMustBeExportedForEndpoint(endpointClass) {
    !"classpath:null".equals(getWsdlFilenameForEndpoint(endpointClass))
  }

  private List getXsdFilenamesForEndpoint(endpointClass) {
    if ("classpath:null".equals(getResponseXsdFilenameForEndpoint(endpointClass)))
      [getRequestXsdFilenameForEndpoint(endpointClass)]
    else
      [getRequestXsdFilenameForEndpoint(endpointClass), getResponseXsdFilenameForEndpoint(endpointClass)]
  }

  private String getRequestXsdFilenameForEndpoint(endpointClass) {
    "classpath:${getValueOfStaticPropertyForGrailsClass(ENDPOINT_REQUEST_XSD_FILENAME_PROPERTY, endpointClass)}".toString()
  }

  private String getResponseXsdFilenameForEndpoint(endpointClass) {
    "classpath:${getValueOfStaticPropertyForGrailsClass(ENDPOINT_RESPONSE_XSD_FILENAME_PROPERTY, endpointClass)}".toString()
  }

  private String getRequestNamespaceForEndpoint(endpointClass) {
    return getValueOfStaticPropertyForGrailsClass(ENDPOINT_REQUEST_NAMESPACE_PROPERTY, endpointClass)
  }

  private Boolean getRequestValidationIndicatorForEndpoint(endpointClass) {
    return (Boolean) getValueOfStaticPropertyForGrailsClass(ENDPOINT_VALIDATE_REQUEST_PROPERTY, endpointClass)
  }

  private Boolean getResponseValidationIndicatorForEndpoint(endpointClass) {
    return (Boolean) getValueOfStaticPropertyForGrailsClass(ENDPOINT_VALIDATE_RESPONSE_PROPERTY, endpointClass)
  }

  private String getRequestSuffixForEndpoint(endpointClass) {
    return getValueOfStaticPropertyForGrailsClass(ENDPOINT_REQUEST_SUFFIX_PROPERTY, endpointClass) ?:
      DEFAULT_REQUEST_SUFFIX
  }

  private String getResponseSuffixForEndpoint(endpointClass) {
    return getValueOfStaticPropertyForGrailsClass(ENDPOINT_RESPONSE_SUFFIX_PROPERTY, endpointClass) ?:
      DEFAULT_RESPONSE_SUFFIX
  }

  private String getFaultSuffixForEndpoint(endpointClass) {
    return getValueOfStaticPropertyForGrailsClass(ENDPOINT_FAULT_SUFFIX_PROPERTY, endpointClass) ?:
      DEFAULT_FAULT_SUFFIX
  }

  private def getValueOfStaticPropertyForGrailsClass(String propertyName, GrailsClass clazz) {
    GrailsClassUtils.getStaticPropertyValue(clazz.clazz, propertyName)
  }
}
