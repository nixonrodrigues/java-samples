package com.dsinpractice.spikes.ldap;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class AtlasAbstractAuthenticationProvider implements
        AuthenticationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasAbstractAuthenticationProvider.class);

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }

    /**
     *
     * @param authentication
     * @return
     */
    public Authentication getAuthenticationWithGrantedAuthority(
            Authentication authentication) {
        UsernamePasswordAuthenticationToken result = null;
        if (authentication != null && authentication.isAuthenticated()) {
            final List<GrantedAuthority> grantedAuths = getAuthorities(authentication
                    .getName());
            final UserDetails userDetails = new User(authentication.getName(), authentication.getCredentials().toString(),
                    grantedAuths);
            result = new UsernamePasswordAuthenticationToken(userDetails,
                    authentication.getCredentials(), grantedAuths);
            result.setDetails(authentication.getDetails());
            return result;
        }
        return authentication;
    }

    /**
     * This method will be modified when actual roles are introduced.
     *
     */
    protected List<GrantedAuthority> getAuthorities(String username) {
        final List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("DATA_SCIENTIST"));
        return grantedAuths;
    }


    public Authentication getAuthenticationWithGrantedAuthorityFromUGI(
            Authentication authentication) {
        UsernamePasswordAuthenticationToken result = null;
        if (authentication != null && authentication.isAuthenticated()) {

            List<GrantedAuthority> grantedAuthsUGI = getAuthoritiesFromUGI(authentication
                    .getName());

            final UserDetails userDetails = new User(authentication.getName(), authentication.getCredentials().toString(),
                    grantedAuthsUGI);
            result = new UsernamePasswordAuthenticationToken(userDetails,
                    authentication.getCredentials(), grantedAuthsUGI);
            result.setDetails(authentication.getDetails());
            return result;
        }
        return authentication;
    }

    public static List<GrantedAuthority> getAuthoritiesFromUGI(String userName) {
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        Configuration config = new Configuration();

        try {
            Groups gp = new Groups(config);
            List<String> userGroups = gp.getGroups(userName);
            if (userGroups != null) {
                for (String group : userGroups) {
                    grantedAuths.add(new SimpleGrantedAuthority(group));
                }
            }
        } catch (java.io.IOException e) {
            LOG.error("Exception while fetching groups ", e);
        }
        return grantedAuths;
    }

}
