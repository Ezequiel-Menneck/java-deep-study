Is an object responsible for loading classes. Class Loaders load Java classes dynamically yo the JVM during runtime. They are also part of the JRE (Java Runtime Environment).

Furthermore, the JVM doesn’t load these Java classes into memory all at once, but rather when an application requires them. This is where class loaders come into the picture. They’re responsible for loading classes into memory.

## What Are the Functions of a Class Loader?

A class loader has two main functions:

- **Load Classes -** Different built-in and custom class loaders load classes. We can extend the _java.lang.ClassLoader_ abstract class to create class loader implementations
- **Locale Resources -** A resouce is some data such as .class file. We typically package resources with an application or library.

## Types of Built-in Class Loaders

- **Bootstrap class loader -** The virtual machine's built-in class loader, is represented as null
- **Platform class loader -** Loads the platform classes, which includes the Java SE platform APIs, their implementation classes, and JDK-specfic run-time classes. The platform class loader is the parent of the system class loader.
- **System class loader -** Also known as application class loader, loads classes on the application class path, module path, and JDK-specific tools.

### Bootstrap Class Loader

Bootstrap class loader serves as the parent of all the other ClassLoader instances.

This bootstrap class loader is part of the core JVM and is written in native code. Different platforms might have different implementations of this particular class loader.

### Platform Class Loader

Is a child of bootstrap class loader and takes care of loading the standard core Java classes, so that they’re available to all applications running on the platform.

### System Class Loader

The system class loader in the other hand takes care of loading all the application-level classes into the JVM. It loads files found in the classpath environment variable, -classpath or -cp command line option. It's also a child of the platform class loader.

## How does Class Loaders work

Class loaders are part of the Java Runtime Environment. When JVM request a class, the class loader tries to locate the class and load the class definition into the runtime using the fully qualified class name. The _java.lang.ClassLoader.loadClass(String name, boolean resolve)_ is the responsible method for loading the class definition into runtime using its binary name.

### Delegation Model

The delegation model means that the _ClassLoader class_ delegates the search for a class or resource to its parent class loader before it tries to find the class or resource itself. The delegation model is hierarchical by default.

The system class loader first delegates the loading of that class to its parent platform class loader, which in turn delegates it to the bootstrap class loader.

Only if the bootstrap and then the platform class loader are unsuccessful in loading the class, does the system class loader try to load the class itself.

### Unique Classes

As a consequence of the delegation model, it’s easy to ensure **unique classes, as we always try to delegate upwards**.

If the parent class loader isn’t able to find the class, only then will the current instance attempt to do so itself.

### Visibility

Child class loaders are visible to classes loaded by their parent class loaders.

For instance, classes loaded by the system class loader have visibility into classes loaded by the platform and bootstrap class loaders, but not vice-versa

If **Class A** is loaded by the application class loader and **Class B** is loaded by the platform class loader, then both **A** and **B** classes are visible as far as other classes loaded by the application class loader are concerned.

**Class B** however is the only class visible to other classes loaded by the platform class loader.

## Custom _ClassLoader_ Use-Cases

- Helping to modify the existing bytecode, e.g. weaving agents
- Creating classes dynamically suited to the user’s needs, e.g. in JDBC, switching between different driver implementations is done through dynamic class loading.
- Implementing a class versioning mechanism while loading different bytecodes for classes with the same names and packages. This can be done either through a URL class loader (load JAR's via URLs) or custom class loaders.

Browsers, for instance, use a custom class loader to load executable content from a website. A browser can load applets from different web pages using separate class loaders. The applet viewer, which is used to run applets, contains a ClassLoader that accesses a website on a remove server instead of looking in the local file system.

It then loads the raw bytecode files via HTTP, and turns them into classes inside the JVM. Even if these applets have the same name, they're considered different components if loaded by different class loaders.

### Creating Our Custom Class Loader

```java
public class CClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = loadClassFromFile(name);
        return defineClass(name, b, 0, b.length);
    }

    private byte[] loadClassFromFile(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName.replace('.', File.separatorChar) + ".class");
        byte[] buffer;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = 0;
        try {
            while ((nextValue = inputStream.read()) != -1) {
                byteStream.write(nextValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteStream.toByteArray();
    }
}
```

In this example we defined a custom class loader that extents the default class loader and loads a byte array from the specified file.

## Understanding java.lang.ClassLoader

### The loadClass() method

This method is responsible for loading the class given a name parameter. The name parameter refers to the fully qualified class name

```java
public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
```

The JVM invokes the loadClass() method to resolve class references, setting resolve to true. However it isn’t always necessary to resolve a class. If we only need to determine if the class exists or not, then we set the resolve parameter to false.

This method serves as an entry point for the class loader.

### The defineClass() method

This method is responsible for the conversion of an array of bytes into an instance of a class.

```java
protected final Class<?> defineClass(
  String name, byte[] b, int off, int len) throws ClassFormatError
```

If the data doesn’t contain a valid class, it throws a _ClassFormatError._

### The findClass() method

This method finds the class with the fully qualified name as a parameter. We need to override this method in custom class loader implementations that follow the delegation model for loading classes:

```java
protected Class<?> findClass(
  String name) throws ClassNotFoundException
```

### The getParent() method

Return the parent class loader for delegation

```java
public final ClassLoader getParent()
```

### The getResource() method

This method try to find a resource with the given name:

```java
public URL getResource(String name)
```

It’ll first be delegated to the parent class loader for the resource. **If the parent is _null_, the path of the class loader built into the virtual machine is searched**.

If that fails, then the method will invoke _findResource(String)_ to find the resource. The resource name specified as an input can be relative or absolute to the classpath.

It returns a URL object for reading the resource, or null if the resource can’t be found or the invoker doesn’t have adequate privileges to return the resource.

Finally, **resource loading in Java is considered location-independent**, as it doesn’t matter where the code is running as long as the environment is set to find the resources.

## Context Class Loaders

In general, context class loaders provide an alternative method to the class-loading delegation scheme introduced in J2SE.

As we learned before, **classloaders in a JVM follow a hierarchical model, such that every class loader has a single parent except for the bootstrap class loader**.

However, sometimes when JVM core classes need to dynamically load classes or resources provided by application developers, we might encounter a problem.

For example, in JNDI, the core functionality is implemented by the bootstrap classes in _rt.jar._ But these JNDI classes may load JNDI providers implemented by independent vendors (deployed in the application classpath). This scenario calls for the bootstrap class loader (parent class loader) to load a class visible to the application loader (child class loader).

**J2SE delegation doesn’t work here, and to get around this problem, we need to find alternative ways of class loading. This can be achieved using thread context loaders**.

The _java.lang.Thread_ class has a method, **_getContextClassLoader(),_ that returns the _ContextClassLoader_ for the particular thread**. The _ContextClassLoader_ is provided by the creator of the thread when loading resources and classes. **As of Java SE 9, threads in the fork/join common pool always return the system class loader as their thread context class loader**.