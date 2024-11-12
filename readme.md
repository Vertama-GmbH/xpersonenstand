# XPersonenstand

This Java library simplifies the process of generating XML files conforming to the specifications defined within the "XPersonenstand" standard. By providing Java classes directly derived from the XPersonenstand schema, this library eliminates the need for manual XML construction, reducing errors and streamlining development.

The current supported version of the XPersonenstand standard is 24.11

## Features

- **Schema-Based Class Generation:** Automatically generated Java classes representing the elements and structures defined in the XPersonenstandard schema.
- **XML Serialization:** Provides methods to seamlessly convert instances of generated classes into valid XML files.
- **XML Validation:** Validate XML files to according to the XPersonenstand standard.

## Installation

### Maven

Add this dependency to your project's POM file:

```xml
<dependency>
    <groupId>com.vertama</groupId>
    <artifactId>xpersonenstand-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add this to your dependencies:

```groovy
implementation "com.vertama:xpersonenstand:1.0.0"
```

### Gradle (Kotlin)

Add this to your dependencies:

```kts
implementation("com.vertama:xpersonenstand:1.0.0")
```

## Usage

To create a message simple create a new object - e.g. `Portal2StAGeburt081020` - and set the values.

Use
```java
XpersonenstandMarshaller.marshalToFile(message, file);
```
or
```java
XpersonenstandMarshaller.marshalToOutputStream(message, outputStream);
```
to create an xml file, and
```java
XpersonenstandMarshaller.unmarshalfromFile(file);
XpersonenstandMarshaller.unmarshalfromOutputStream(outputStream);
```
to create a message object from an xml file

and
```java
Xpersonenstand.validateOrThrow(message);
```
to validate a message against the XPersonenstand schema.
