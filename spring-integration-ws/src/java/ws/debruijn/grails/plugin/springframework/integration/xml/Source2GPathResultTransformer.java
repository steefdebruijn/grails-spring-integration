package ws.debruijn.grails.plugin.springframework.integration.xml;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class Source2GPathResultTransformer {
  public GPathResult transform(Source message) throws Exception {
    StringWriter sw = new StringWriter();
    TransformerFactory.newInstance().newTransformer().transform(message, new StreamResult(sw));
    return new XmlSlurper().parseText(sw.toString());
  }
}
