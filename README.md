# SAML-Authentication-example-Digital-Factory

SAML Authentication example in Digital Factory


For this implementation we used a custom Tomcat Authenticator in order to implement SAML authentication. 

On the Digital Factory side we created a module named “JahiaAuthSAML”.  In this module there is:

-	JahiaSAMLValve:  a java class that extends AutoRegisteredBaseAuthValve and implements the ContainerAuthValve code.  It also implements LogoutUrlProvider in order to provide logout from SAML.
-	JahiaAuthSAML.xml:  spring configuration for the valve using position 1 in the pipeline 


We reference the custom Tomcat authenticator (valve class and realm class) in the context.xml for our Digital Factory installation.  Keep in mind this is specific to our custom Authenticator and is here for an example.  



    <Loader delegate="true" />
    <Valve className="custom.saml.sp.SPAuthenticator" oiosamlHome="${catalina.base}/conf/saml" />
    <Realm className="custom.saml.sp.SAMLRealm" rolesAttribute="roles" usernameAttribute="uid" />


We then add the needed security constraints to the web.xml of our Digital Factory installation.  Keep in mind this is specific to our custom Authenticator and is here for an example.  
  
    <security-constraint>
        <web-resource-collection>
            <web-resource-name>Jahia protected resources requiring SAML authentication</web-resource-name>
                <url-pattern>/start</url-pattern>
                <url-pattern>/cms/*</url-pattern>
                <http-method>GET</http-method>
                <http-method>POST</http-method>
        </web-resource-collection>
        <auth-constraint>
            <role-name>*</role-name>
        </auth-constraint>
        <user-data-constraint>
            <transport-guarantee>CONFIDENTIAL</transport-guarantee>
        </user-data-constraint>
    </security-constraint>
 
    <security-constraint>
        <web-resource-collection>
        <web-resource-name>Jahia resources NOT requiring SAML authentication</web-resource-name>
        <url-pattern>/administration</url-pattern>
        <url-pattern>/tools</url-pattern>
        </web-resource-collection>
    </security-constraint>
 
    <security-role>
        <role-name>*</role-name>
    </security-role>

    
Note:  Any libraries required for our Custom Tomcat Authenticator were added to tomcat/libs directory and redundant libraries inside DigitalFactory were removed.   Also, be aware of different versions of the same libraries used by Jahia and the Tomcat Authenticator.
