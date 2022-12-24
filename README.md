# How to use it
First, you need to include the repository in your `pom.xml` or `build.gradle`:
```xml
<repositories>
    <repository>
        <id>luiz-otavio</id>
        <!--You will need to include the credentials in somewhere-->
        <url>https://maven.pkg.github.com/luiz-otavio/ptero-bukkit/url>
    </repository>
</repositories>

<dependencies>
    <dependency>
      <groupId>net.luxcube.pterodactyl</groupId>
      <artifactId>bukkit</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>net.luxcube.pterodactyl</groupId>
      <artifactId>binder</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <scope>runtime</scope>
    </dependency>
</dependencies>
```

```groovy
repositories {
    maven {
        name = "luiz-otavio"
        url = "https://maven.pkg.github.com/luiz-otavio/ptero-bukkit"
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation 'net.luxcube.pterodactyl:bukkit:0.0.1-SNAPSHOT'
    runtimeOnly 'net.luxcube.pterodactyl:binder:0.0.1-SNAPSHOT'
}
```

Then, you can retrieve the pterodactyl instance using the `PteroBukkit`:
```java
PteroBukkit ptero = PteroBukkit.createInstance(URL, APP_KEY, CLIENT_KEY, N_THREADS, PLUGIN);
```
