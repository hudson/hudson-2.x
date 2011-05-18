/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Nikita Levyankov
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.security;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import groovy.lang.Binding;
import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.cli.CLICommand;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.security.FederatedLoginService.FederatedIdentity;
import hudson.util.DescriptorList;
import hudson.util.PluginServletFilter;
import hudson.util.spring.BeanBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.ui.rememberme.RememberMeServices;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.web.context.WebApplicationContext;

import static org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY;

/**
 * Pluggable security realm that connects external user database to Hudson.
 * <p/>
 * <p/>
 * If additional views/URLs need to be exposed,
 * an active {@link SecurityRealm} is bound to <tt>CONTEXT_ROOT/securityRealm/</tt>
 * through {@link Hudson#getSecurityRealm()}, so you can define additional pages and
 * operations on your {@link SecurityRealm}.
 * <p/>
 * <h2>How do I implement this class?</h2>
 * <p/>
 * For compatibility reasons, there are two somewhat different ways to implement a custom SecurityRealm.
 * <p/>
 * <p/>
 * One is to override the {@link #createSecurityComponents()} and create key Acegi components
 * that control the authentication process.
 * The default {@link SecurityRealm#createFilter(FilterConfig)} implementation then assembles them
 * into a chain of {@link Filter}s. All the incoming requests to Hudson go through this filter chain,
 * and when the filter chain is done, {@link SecurityContext#getAuthentication()} would tell us
 * who the current user is.
 * <p/>
 * <p/>
 * If your {@link SecurityRealm} needs to touch the default {@link Filter} chain configuration
 * (e.g., adding new ones), then you can also override {@link #createFilter(FilterConfig)} to do so.
 * <p/>
 * <p/>
 * This model is expected to fit most {@link SecurityRealm} implementations.
 * <p/>
 * <p/>
 * <p/>
 * The other way of doing this is to ignore {@link #createSecurityComponents()} completely (by returning
 * {@link SecurityComponents} created by the default constructor) and just concentrate on {@link #createFilter(FilterConfig)}.
 * As long as the resulting filter chain properly sets up {@link Authentication} object at the end of the processing,
 * Hudson doesn't really need you to fit the standard Acegi models like {@link AuthenticationManager} and
 * {@link UserDetailsService}.
 * <p/>
 * <p/>
 * This model is for those "weird" implementations.
 * <p/>
 * <p/>
 * <h2>Views</h2>
 * <dl>
 * <dt>loginLink.jelly</dt>
 * <dd>
 * This view renders the login link on the top right corner of every page, when the user
 * is anonymous. For {@link SecurityRealm}s that support user sign-up, this is a good place
 * to show a "sign up" link. See {@link HudsonPrivateSecurityRealm} implementation
 * for an example of this.
 * <p/>
 * <dt>config.jelly</dt>
 * <dd>
 * This view is used to render the configuration page in the system config screen.
 * </dl>
 *
 * @author Kohsuke Kawaguchi
 * @author Nikita Levyankov
 * @see PluginServletFilter
 * @since 1.160
 */
public abstract class SecurityRealm extends AbstractDescribableImpl<SecurityRealm> implements ExtensionPoint {

    /**
     * Creates fully-configured {@link AuthenticationManager} that performs authentication
     * against the user realm. The implementation hides how such authentication manager
     * is configured.
     * <p/>
     * <p/>
     * {@link AuthenticationManager} instantiation often depends on the user-specified parameters
     * (for example, if the authentication is based on LDAP, the user needs to specify
     * the host name of the LDAP server.) Such configuration is expected to be
     * presented to the user via <tt>config.jelly</tt> and then
     * captured as instance variables inside the {@link SecurityRealm} implementation.
     * <p/>
     * <p/>
     * Your {@link SecurityRealm} may also wants to alter {@link Filter} set up by
     * overriding {@link #createFilter(FilterConfig)}.
     */
    public abstract SecurityComponents createSecurityComponents();

    /**
     * Creates a {@link CliAuthenticator} object that authenticates an invocation of a CLI command.
     * See {@link CliAuthenticator} for more details.
     *
     * @param command The command about to be executed.
     * @return never null. By default, this method returns a no-op authenticator that always authenticates
     *         the session as authenticated by the transport (which is often just {@link Hudson#ANONYMOUS}.)
     */
    public CliAuthenticator createCliAuthenticator(final CLICommand command) {
        return new CliAuthenticator() {
            public Authentication authenticate() {
                return command.getTransportAuthentication();
            }
        };
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p/>
     * {@link SecurityRealm} is a singleton resource in Hudson, and therefore
     * it's always configured through <tt>config.jelly</tt> and never with
     * <tt>global.jelly</tt>.
     */
    public Descriptor<SecurityRealm> getDescriptor() {
        return super.getDescriptor();
    }

    /**
     * Returns the URL to submit a form for the authentication.
     * There's no need to override this, except for {@link LegacySecurityRealm}.
     */
    public String getAuthenticationGatewayUrl() {
        return "j_acegi_security_check";
    }

    /**
     * Gets the target URL of the "login" link.
     * There's no need to override this, except for {@link LegacySecurityRealm}.
     * On legacy implementation this should point to {@code loginEntry}, which
     * is protected by <tt>web.xml</tt>, so that the user can be eventually authenticated
     * by the container.
     * <p/>
     * <p/>
     * Path is relative from the context root of the Hudson application.
     * The URL returned by this method will get the "from" query parameter indicating
     * the page that the user was at.
     */
    public String getLoginUrl() {
        return "login";
    }

    /**
     * Returns true if this {@link SecurityRealm} supports explicit logout operation.
     * <p/>
     * <p/>
     * If the method returns false, "logout" link will not be displayed. This is useful
     * when authentication doesn't require an explicit login activity (such as NTLM authentication
     * or Kerberos authentication, where Hudson has no ability to log off the current user.)
     * <p/>
     * <p/>
     * By default, this method returns true.
     *
     * @since 1.307
     */
    public boolean canLogOut() {
        return true;
    }

    /**
     * Controls where the user is sent to after a logout. By default, it's the top page
     * of Hudson, but you can return arbitrary URL.
     *
     * @param req {@link StaplerRequest} that represents the current request. Primarily so that
     * you can get the context path. By the time this method is called, the session
     * is already invalidated. Never null.
     * @param auth The {@link Authentication} object that represents the user that was logging in.
     * This parameter allows you to redirect people to different pages depending on who they are.
     * @return never null.
     * @see #doLogout(StaplerRequest, StaplerResponse)
     * @since 1.314
     */
    protected String getPostLogOutUrl(StaplerRequest req, Authentication auth) {
        return req.getContextPath() + "/";
    }

    /**
     * Handles the logout processing.
     * <p/>
     * <p/>
     * The default implementation erases the session and do a few other clean up, then
     * redirect the user to the URL specified by {@link #getPostLogOutUrl(StaplerRequest, Authentication)}.
     *
     * @since 1.314
     */
    public void doLogout(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.clearContext();

        //Clear env property.
        EnvVars.clearHudsonUserEnvVar();

        // reset remember-me cookie
        Cookie cookie = new Cookie(ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY, "");
        cookie.setPath(req.getContextPath().length() > 0 ? req.getContextPath() : "/");
        rsp.addCookie(cookie);

        rsp.sendRedirect2(getPostLogOutUrl(req, auth));
    }

    /**
     * Returns true if this {@link SecurityRealm} allows online sign-up.
     * This creates a hyperlink that redirects users to <tt>CONTEXT_ROOT/signUp</tt>,
     * which will be served by the <tt>signup.jelly</tt> view of this class.
     * <p/>
     * <p/>
     * If the implementation needs to redirect the user to a different URL
     * for signing up, use the following jelly script as <tt>signup.jelly</tt>
     * <p/>
     * <pre><xmp>
     * <st:redirect url="http://www.sun.com/" xmlns:st="jelly:stapler"/>
     * </xmp></pre>
     */
    public boolean allowsSignup() {
        Class clz = getClass();
        return clz.getClassLoader().getResource(clz.getName().replace('.', '/') + "/signup.jelly") != null;
    }

    /**
     * Shortcut for {@link UserDetailsService#loadUserByUsername(String)}.
     *
     * @return never null.
     * @throws UserMayOrMayNotExistException If the security realm cannot even tell if the user exists or not.
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        return getSecurityComponents().userDetails.loadUserByUsername(username);
    }

    /**
     * If this {@link SecurityRealm} supports a look up of {@link GroupDetails} by their names, override this method
     * to provide the look up.
     * <p/>
     * <p/>
     * This information, when available, can be used by {@link AuthorizationStrategy}s to improve the UI and
     * error diagnostics for the user.
     */
    public GroupDetails loadGroupByGroupname(String groupname) throws UsernameNotFoundException, DataAccessException {
        throw new UserMayOrMayNotExistException(groupname);
    }

    /**
     * Starts the user registration process for a new user that has the given verified identity.
     * <p/>
     * <p/>
     * If the user logs in through a {@link FederatedLoginService}, verified that the current user
     * owns an {@linkplain FederatedIdentity identity}, but no existing user account has claimed that identity,
     * then this method is invoked.
     * <p/>
     * <p/>
     * The expected behaviour is to confirm that the user would like to create a new account, and
     * associate this federated identity to the newly created account (via {@link FederatedIdentity#addToCurrentUser()}.
     *
     * @throws UnsupportedOperationException If this implementation doesn't support the signup through this mechanism.
     *                                       This is the default implementation.
     * @since 1.394
     */
    public HttpResponse commenceSignup(FederatedIdentity identity) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@link DefaultManageableImageCaptchaService} holder to defer initialization.
     */
    private static final class CaptchaService {
        private static final DefaultManageableImageCaptchaService INSTANCE = new DefaultManageableImageCaptchaService();
    }

    /**
     * Generates a captcha image.
     */
    public final void doCaptcha(StaplerRequest req, StaplerResponse rsp) throws IOException {
        String id = req.getSession().getId();
        rsp.setContentType("image/png");
        rsp.addHeader("Cache-Control", "no-cache");
        ImageIO.write(CaptchaService.INSTANCE.getImageChallengeForID(id), "PNG", rsp.getOutputStream());
    }

    /**
     * Validates the captcha.
     */
    protected final boolean validateCaptcha(String text) {
        try {
            String id = Stapler.getCurrentRequest().getSession().getId();
            Boolean b = CaptchaService.INSTANCE.validateResponseForID(id, text);
            return b != null && b;
        } catch (CaptchaServiceException e) {
            LOGGER.log(Level.INFO, "Captcha validation had a problem", e);
            return false;
        }
    }

    /**
     * Picks up the instance of the given type from the spring context.
     * If there are multiple beans of the same type or if there are none,
     * this method treats that as an {@link IllegalArgumentException}.
     * <p/>
     * This method is intended to be used to pick up a Acegi object from
     * spring once the bean definition file is parsed.
     */
    protected static <T> T findBean(Class<T> type, ApplicationContext context) {
        Map m = context.getBeansOfType(type);
        switch (m.size()) {
            case 0:
                throw new IllegalArgumentException("No beans of " + type + " are defined");
            case 1:
                return type.cast(m.values().iterator().next());
            default:
                throw new IllegalArgumentException("Multiple beans of " + type + " are defined: " + m);
        }
    }

    /**
     * Holder for the SecurityComponents.
     */
    private transient SecurityComponents securityComponents;

    /**
     * Use this function to get the security components, without necessarily
     * recreating them.
     */
    public synchronized SecurityComponents getSecurityComponents() {
        if (this.securityComponents == null) {
            this.securityComponents = this.createSecurityComponents();
        }
        return this.securityComponents;
    }

    /**
     * Creates {@link Filter} that all the incoming HTTP requests will go through
     * for authentication.
     * <p/>
     * <p/>
     * The default implementation uses {@link #getSecurityComponents()} and builds
     * a standard filter chain from /WEB-INF/security/SecurityFilters.groovy.
     * But subclasses can override this to completely change the filter sequence.
     * <p/>
     * <p/>
     * For other plugins that want to contribute {@link Filter}, see
     * {@link PluginServletFilter}.
     *
     * @since 1.271
     */
    public Filter createFilter(FilterConfig filterConfig) {
        LOGGER.entering(SecurityRealm.class.getName(), "createFilter");

        Binding binding = new Binding();
        SecurityComponents sc = getSecurityComponents();
        binding.setVariable("securityComponents", sc);
        binding.setVariable("securityRealm", this);
        BeanBuilder builder = new BeanBuilder();
        builder.parse(filterConfig.getServletContext().getResourceAsStream("/WEB-INF/security/SecurityFilters.groovy"),
            binding);
        WebApplicationContext context = builder.createApplicationContext();
        return (Filter) context.getBean("filter");
    }

    /**
     * Singleton constant that represents "no authentication."
     */
    public static final SecurityRealm NO_AUTHENTICATION = new None();

    private static class None extends SecurityRealm {
        public SecurityComponents createSecurityComponents() {
            return new SecurityComponents(new AuthenticationManager() {
                public Authentication authenticate(Authentication authentication) {
                    return authentication;
                }
            }, new UserDetailsService() {
                public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException, DataAccessException {
                    throw new UsernameNotFoundException(username);
                }
            });
        }

        /**
         * This special instance is not configurable explicitly,
         * so it doesn't have a descriptor.
         */
        @Override
        public Descriptor<SecurityRealm> getDescriptor() {
            return null;
        }

        /**
         * There's no group.
         */
        @Override
        public GroupDetails loadGroupByGroupname(String groupname)
            throws UsernameNotFoundException, DataAccessException {
            throw new UsernameNotFoundException(groupname);
        }

        /**
         * We don't need any filter for this {@link SecurityRealm}.
         */
        @Override
        public Filter createFilter(FilterConfig filterConfig) {
            return new ChainedServletFilter();
        }

        /**
         * Maintain singleton semantics.
         */
        private Object readResolve() {
            return NO_AUTHENTICATION;
        }
    }

    /**
     * Just a tuple so that we can create various inter-related security related objects and
     * return them all at once.
     * <p/>
     * <p/>
     * None of the fields are ever null.
     *
     * @see SecurityRealm#createSecurityComponents()
     */
    public static final class SecurityComponents {
        public final AuthenticationManager manager;
        public final UserDetailsService userDetails;
        public final RememberMeServices rememberMe;

        public SecurityComponents() {
            // we use AuthenticationManagerProxy here just as an implementation that fails all the time,
            // not as a proxy. No one is supposed to use this as a proxy.
            this(new AuthenticationManagerProxy());
        }

        public SecurityComponents(AuthenticationManager manager) {
            // we use UserDetailsServiceProxy here just as an implementation that fails all the time,
            // not as a proxy. No one is supposed to use this as a proxy.
            this(manager, new UserDetailsServiceProxy());
        }

        public SecurityComponents(AuthenticationManager manager, UserDetailsService userDetails) {
            this(manager, userDetails, createRememberMeService(userDetails));
        }

        public SecurityComponents(AuthenticationManager manager, UserDetailsService userDetails,
                                  RememberMeServices rememberMe) {
            assert manager != null && userDetails != null && rememberMe != null;
            this.manager = manager;
            this.userDetails = userDetails;
            this.rememberMe = rememberMe;
        }

        private static RememberMeServices createRememberMeService(UserDetailsService uds) {
            // create our default TokenBasedRememberMeServices, which depends on the availability of the secret key
            TokenBasedRememberMeServices2 rms = new TokenBasedRememberMeServices2();
            rms.setUserDetailsService(uds);
            rms.setKey(Hudson.getInstance().getSecretKey());
            rms.setParameter("remember_me"); // this is the form field name in login.jelly
            return rms;
        }
    }

    /**
     * All registered {@link SecurityRealm} implementations.
     *
     * @deprecated as of 1.286
     *             Use {@link #all()} for read access, and use {@link Extension} for registration.
     */
    public static final DescriptorList<SecurityRealm> LIST = new DescriptorList<SecurityRealm>(SecurityRealm.class);

    /**
     * Returns all the registered {@link SecurityRealm} descriptors.
     */
    public static DescriptorExtensionList<SecurityRealm, Descriptor<SecurityRealm>> all() {
        return Hudson.getInstance().<SecurityRealm, Descriptor<SecurityRealm>>getDescriptorList(SecurityRealm.class);
    }


    private static final Logger LOGGER = Logger.getLogger(SecurityRealm.class.getName());

    /**
     * {@link GrantedAuthority} that represents the built-in "authenticated" role, which is granted to
     * anyone non-anonymous.
     */
    public static final GrantedAuthority AUTHENTICATED_AUTHORITY = new GrantedAuthorityImpl("authenticated");
}
