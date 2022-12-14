package com.kairos.persistence.model.auth;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.user.country.system_setting.SystemLanguageDTO;
import com.kairos.dto.user_context.CurrentUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails, Authentication {
    private static final long serialVersionUID = 1L;
    private final List<GrantedAuthority> authorities ;
    private final User user;
    private boolean authenticated = true;

    //

    public UserPrincipal(User user, List<GrantedAuthority> authorities) {
        this.authorities = authorities;
        this.user = user;
    }

    //

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Object getCredentials() {
        return user.getPassword();
    }

    @Override
    public Object getDetails() {

        CurrentUserDetails details = new CurrentUserDetails(this.user.getId(), this.user.getUserName(), this.user.nickName,
                this.user.firstName, this.user.getLastName(), this.user.getEmail(), this.user.isPasswordUpdated());
        details.setAge(this.user.getAge());
        details.setLastSelectedOrganizationId(this.getUser().getLastSelectedOrganizationId());
        details.setCountryId(this.getUser().getCountryId());
        details.setUnitWiseAccessRole(this.getUser().getUnitWiseAccessRole());
        details.setHubMember(this.user.getHubMember());
        details.setSystemAdmin(this.user.getSystemAdmin());
        details.setLastSelectedOrganizationCategory(this.user.getLastSelectedOrganizationCategory());
        details.setProfilePic(this.user.getProfilePic());
        details.setUserLanguage(ObjectMapperUtils.copyPropertiesByMapper(this.user.getUserLanguage(), SystemLanguageDTO.class));
        return details;
    }

    @Override
    public Object getPrincipal() {
        return user.getUserName();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public String getName() {
        return user.getUserName();
    }

    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;

    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "authorities=" + authorities +
                ", user=" + user +
                ", authenticated=" + authenticated +
                '}';
    }
}
