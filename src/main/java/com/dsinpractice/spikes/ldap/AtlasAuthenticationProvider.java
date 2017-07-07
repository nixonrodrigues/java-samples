package com.dsinpractice.spikes.ldap;




import javax.annotation.PostConstruct;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.apache.commons.configuration.Configuration;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

import java.io.File;

@Component
public class AtlasAuthenticationProvider extends
        AtlasAbstractAuthenticationProvider {
    private static final Logger LOG = LoggerFactory
            .getLogger(AtlasAuthenticationProvider.class);

    private boolean fileAuthenticationMethodEnabled = true;
    private String ldapType = "NONE";
    public static final String FILE_AUTH_METHOD = "atlas.authentication.method.file";
    public static final String LDAP_AUTH_METHOD = "atlas.authentication.method.ldap";
    public static final String LDAP_TYPE = "atlas.authentication.method.ldap.type";

    public AtlasAuthenticationProvider(){
        setAuthenticationMethod();
    }

    private boolean ssoEnabled = false;

//    @Autowired

//    @Autowired
//    AtlasFileAuthenticationProvider fileAuthenticationProvider;


    void setAuthenticationMethod() {
        try {
            PropertiesConfiguration configuration = new PropertiesConfiguration(new File("src/main/resources/atlas-application.properties"));
           // Configuration configuration = ApplicationProperties.get();
            System.out.print("configuration == >>" + configuration );
            this.fileAuthenticationMethodEnabled = configuration.getBoolean(
                    FILE_AUTH_METHOD, true);

            boolean ldapAuthenticationEnabled = configuration.getBoolean(LDAP_AUTH_METHOD, false);
            System.out.print("configuration == >>" + ldapAuthenticationEnabled );
            if (ldapAuthenticationEnabled) {
                this.ldapType = configuration.getString(LDAP_TYPE, "NONE");
            } else {
                this.ldapType = "NONE";
            }
        } catch (Exception e) {
            LOG.error("Error while getting atlas.login.method application properties", e);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        System.out.println("Inside authenticate " + ldapType);
        if(ssoEnabled){
            if (authentication != null){
                authentication = getSSOAuthentication(authentication);
                if(authentication!=null && authentication.isAuthenticated()){
                    return authentication;
                }
            }
        } else {

            if (ldapType.equalsIgnoreCase("LDAP")) {
                try {
                    AtlasLdapAuthenticationProvider ldapAuthenticationProvider = new AtlasLdapAuthenticationProvider();

                    authentication = ldapAuthenticationProvider.authenticate(authentication);
                } catch (Exception ex) {
                    LOG.error("Error while LDAP authentication", ex);
                }
            } else if (ldapType.equalsIgnoreCase("AD")) {
                try {
                    AtlasADAuthenticationProvider adAuthenticationProvider =new AtlasADAuthenticationProvider();

                    authentication = adAuthenticationProvider.authenticate(authentication);
                } catch (Exception ex) {
                    LOG.error("Error while AD authentication", ex);
                }
            }
        }

        if (authentication != null) {
            if (authentication.isAuthenticated()) {
                System.out.print("===>>>>" +authentication.isAuthenticated());
                return authentication;
            } else if (fileAuthenticationMethodEnabled) {  // If the LDAP/AD authentication fails try the local filebased login method
              //  authentication = fileAuthenticationProvider.authenticate(authentication);

                if (authentication != null && authentication.isAuthenticated()) {
                    return authentication;
                }
            }
        }

        LOG.error("Authentication failed.");
        throw new AtlasAuthenticationException("Authentication failed.");
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    public void setSsoEnabled(boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    private Authentication getSSOAuthentication(Authentication authentication) throws AuthenticationException{
        return authentication;
    }


    private static ApplicationContext applicationContext = null;
    private static AtlasAuthenticationProvider authProvider = null;
    Authentication authentication;


    public  void setup(String username,String password){
        authentication = new UsernamePasswordAuthenticationToken(
                username, password, null);
        authentication.setAuthenticated(false);
        authProvider.authenticate(authentication);
    }

    public static void main(String[] args){
        applicationContext = new ClassPathXmlApplicationContext("file:src/main/resources/spring-security.xml");
        authProvider = applicationContext.getBean(com.dsinpractice.spikes.ldap.AtlasAuthenticationProvider.class);

        authProvider.setup(args[0],args[1]);

    }
}
