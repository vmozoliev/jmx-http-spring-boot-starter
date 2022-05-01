# jmx-http-spring-boot-starter

This starter looks for mBeans in the spring application and sets up controller to call mbean methods.

`<dependency>
<groupId>com.belka</groupId>
<artifactId>jmx-http-starter</artifactId>
<version>1.0</version>
</dependency>
`

application.properties:
spring.jmxHttp.enabled=true
spring.jmxHttp.endpointName=jmx

`GET host/contextpath/jmx` will return all information about mbeans

`GET host/contextpath/jmx/testMBean` will return all information about testMBean mbean


```
POST host/contextpath/jmx/testMBean/method
[
    {
        "type": "int",
        "value": "5"
    }
]
```
will call method of testMBean with argument int type and value "5"
