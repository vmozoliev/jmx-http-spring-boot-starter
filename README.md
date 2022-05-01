# jmx-http-spring-boot-starter

This starter looks for mBeans in the spring application and sets up controller to call mbean methods.

```
<dependency>
    <groupId>com.belka</groupId>
    <artifactId>jmx-http-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

application.properties:
```
spring.jmxHttp.enabled=true
spring.jmxHttp.endpointName=jmx
```

`GET host/contextpath/jmx` will return all information about mbeans

`GET host/contextpath/jmx/testMBean` will return all information about testMBean mbean


```
POST host/contextpath/jmx/testMBean/method
[
    {
        "type": "int",
        "value": "5"
    },
    {
        "type": "java.lang.Double",
        "value": "5.4"
    },
]
```
will call method of testMBean with argument int type and Double type

It is possible to use only types: 
```
java.lang.Integer, int, 
java.lang.Long, long, 
java.lang.Float, float,
java.lang.Double, double,
java.lang.Short, short,
java.lang.String, 
java.math.BigDecimal, 
java.lang.Byte, byte, 
java.lang.Character, char
java.lang.Boolean, boolean
```