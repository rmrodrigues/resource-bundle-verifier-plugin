# resource-bundle-verifier-plugin
A Maven Plugin to verify ResourceBundles. It verifies if there are missing and empty keys.

##Configuration
Here is an example of the configuration need.
```xml
<plugin>
		<groupId>com.rmrodrigues.maven.plugins.resource.bundle.verifier</groupId>
		<artifactId>resource-bundle-verifier-plugin</artifactId>
		<version>1.0.0</version>
		<executions>
			<execution>
				<phase>package</phase>
				<goals>
					<goal>check</goal>
				</goals>
			</execution>
		</executions>
		<configuration>
			<localeFile>${project.basedir}/src/main/resources/myMainLocale.properties</localeFile>
			<locales>
				<param>${project.basedir}/src/main/resources/myLocale_fr_FR.properties</param>
				<param>${project.basedir}/src/main/resources/myLocale_de_DE.properties</param>
			</locales>
		</configuration>
	</plugin>
```
###How does it work?
Simple. You just need to define your main locale file path on tag "<localeFile>". The other locale files are defined on tag "<locales>" as shows above.

###Validations
This simple plugin validates if there are keys whitour value, missing keys and keys with same value.
The build fails when there are keys without values or missing keys. When there are keys with same values it just write a log warn message.


