package org.jahia.params.valves;

import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Properties;





public class JahiaSAMLValve extends AutoRegisteredBaseAuthValve implements LogoutUrlProvider {


    private static final Logger logger = LoggerFactory.getLogger(JahiaSAMLValve.class);


    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }

        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();


        Principal principal = request.getUserPrincipal();



        String newUserName = null;


        //NOTE:  SAMLPrincipal is part the custom Tomcat Authenticator valve used for our example.
        //       For other Custom Container Authentication you would cast the principal to your own Class

        if (principal != null) {
            if (principal instanceof SAMLPrincipal) {
                SAMLPrincipal sp = (SAMLPrincipal) principal;
                if (logger.isDebugEnabled()) {
                    logger.info("Found user " + principal.getName() +
                            "  already in HttpServletRequest, using it to try to login...(Principal.toString=" + principal);
                }
                try {
                    JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(sp.getName());


                    if (jahiaUser != null) {
                        if (jahiaUser.isAccountLocked()) {
                            logger.info("Login failed. Account is locked for user " + sp.getName());
                            return;
                        }

                        authContext.getSessionFactory().setCurrentUser(jahiaUser);
                    } else {
                        //create the user
                        newUserName = sp.getName();
                        createUser(newUserName, authContext, sp);
                    }
                } catch (Exception e){

                }
            }

        }
        valveContext.invokeNext(context);
    }


    public String getLogoutUrl(HttpServletRequest request) {


        Principal principal = request.getUserPrincipal();

        //get the logout url and replace servlet Path for SAML if necessary
        String logoutURLbase =  Url.getServer(request)
                + request.getContextPath();

        String logoutURLfull = null;

        if (principal != null) {

            if (principal instanceof SAMLPrincipal) {
                //return SAML Logout
                logoutURLfull = logoutURLbase
                        + "/saml/Logout";

                return logoutURLfull;

            } else {
                logoutURLfull = logoutURLbase
                        + "/cms/logout";
                return logoutURLfull;
            }

        }

        return logoutURLfull;
    }

    public boolean hasCustomLogoutUrl() {
        return true;
    }

    public static void createUser(String userName, AuthValveContext context, SAMLPrincipal samlPrincipal) throws Exception {

        final JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();

        String completeName = null;
        String email = null;
        String fname = null;
        String lname = null;

        Properties props = new Properties();

        completeName = samlPrincipal.getNomComplet();
        email = samlPrincipal.getMail();

        //get first name and last name from complete Name which is "lastname firstname"
        //eg: "Carrié Darcy"
        int spaceBetweenFirstAndLastName = completeName.indexOf(" ");
        lname = completeName.substring(0, spaceBetweenFirstAndLastName);
        fname = completeName.substring(spaceBetweenFirstAndLastName+1);

        //create the user
        JahiaUser user1 = userManager.createUser(userName, "dummy1234", props);
        user1.setProperty("j:email", email);
        user1.setProperty("j:firstName", fname);
        user1.setProperty("j:lastName", lname);

        //set current user to the newly created user
        context.getSessionFactory().setCurrentUser(user1);


    }

}