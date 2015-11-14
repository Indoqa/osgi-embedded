# Indoqa Solr Spring Client

This project offers a Spring based implementation of a FactoryBean for communicating with Apache Solr 5.x servers. 

The SolrClientFactory allows to communicate with Solr either embedded, via http or Apache ZooKeeper for SolrCloud installations.

The desired behavior is configured with the supplied url:

* file:// - uses the EmbeddedSolrClient
* http:// - uses the HttpSolrClient
* cloud:// - uses the CloudSolrClient


## Installation

### Requirements

  * Apache Solr 5.0+
  * Spring Beans 3.1+
  * Java 6+
  
### Build

  * Download the latest release via maven

```xml

    <dependency>
      <groupId>com.indoqa.solr</groupId>
      <artifactId>solr-spring-client</artifactId>
    </dependency>
    
```

  * Download source
  * run "maven clean install"

  ## Configuration

### Initialize the SolrClientFactory for tests with this snippet
```java

    SolrClientFactory solrClientFactory = new SolrClientFactory();
    solrClientFactory.setUrl("file:///tmp/solr-spring-server/embedded-test-core");
    solrClientFactory.setEmbeddedSolrConfigurationDir("./src/test/resources/solr/test-core");
    solrClientFactory.initialize();

    SolrServer solrServer = solrClientFactory.getObject();

    QueryResponse response = solrServer.query(new SolrQuery("*:*"));

    ...

    solrServer.shutdown();
    solrClientFactory.destroy();

```
The url can either be a relative or absolute directory, the embeddedSolrConfigurationDir must include the usual Solr configuration files:

* schema.xml
* solrconfig.xml

### To communicate with a single Solr server use this snippet

```java

    SolrClientFactory solrClientFactory = new SolrClientFactory();
    solrClientFactory.setUrl("http://localhost:8983/test-core");
    solrClientFactory.initialize();
```

### To communicate with a SolrCloud cluster use this snippet

 ```java

    SolrClientFactory solrClientFactory = new SolrClientFactory();
    solrClientFactory.setUrl("cloud://zkHost1:2181,zkHost2:2182?collection=test-collection");
    solrClientFactory.initialize();

```
The syntax uses zkHost1:2181,zkHost2:2182 for the ZooKeeper instances in your ZooKeeper ensemble, followed by the collection to be used. 
This ensures that the communication for update requests will be established against the leader of the collection to minimize communication overhead.

### Solr Spring server integration based on xml configuration

 ```xml

    <bean name="solrClientFactory" class="com.indoqa.solr.server.factory.SolrClientFactory">
      <property name="url" value="file://./target/solr/embedded-test-core" />
      <property name="embeddedSolrConfigurationDir" value="./src/test/resources/solr/test-core" />
    </bean>

 ```
  