/**
 * Copyright (c) 2013-2024 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.tomcat;

import java.io.IOException;
import java.security.Principal;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.authenticator.SingleSignOnEntry;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.res.StringManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

/**
 * Extended implementation of Tomcat SSO valve to use Redis or Valkey as a storage.
 * This allows to cluster Tomcat without sticky sessions.
 */
public class RedissonSingleSignOn extends SingleSignOn {

  private static final StringManager sm = StringManager.getManager(RedissonSingleSignOn.class);
  private static final String SSO_SESSION_ENTRIES = "redisson:tomcat_sso";

  private RedissonSessionManager manager;

  void setSessionManager(RedissonSessionManager manager) {
    if (containerLog != null && containerLog.isTraceEnabled()) {
        containerLog.trace(sm.getString("redissonSingleSignOn.trace.setSessionManager", manager));
    }
    this.manager = manager;
  }

  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.invoke"));
      }
      String ssoSessionId = getSsoSessionId(request);
      syncAndGetSsoEntry(ssoSessionId);
      super.invoke(request, response);
  }

  @Override
  public void sessionDestroyed(String ssoId, Session session) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.sessionDestroyed"));
      }
      super.sessionDestroyed(ssoId, session);
      manager.getMap(SSO_SESSION_ENTRIES).fastRemove(ssoId);
  }

  @Override
  protected boolean associate(String ssoId, Session session) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.associate", ssoId, session));
      }
      syncAndGetSsoEntry(ssoId);
      boolean associated = super.associate(ssoId, session);
      if (associated) {
          manager.getMap(SSO_SESSION_ENTRIES).fastPut(ssoId, cache.get(ssoId));
      }
      return associated;
  }

  @Override
  protected boolean reauthenticate(String ssoId, Realm realm, Request request) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.reauthenticate"));
      }
      syncAndGetSsoEntry(ssoId);
      return super.reauthenticate(ssoId, realm, request);
  }

  @Override
  protected void register(String ssoId, Principal principal, String authType, String username, String password) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.register"));
      }  
      super.register(ssoId, principal, authType, username, password);
      manager.getMap(SSO_SESSION_ENTRIES).fastPut(ssoId, cache.get(ssoId));
  }

  @Override
  protected void deregister(String ssoId) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.deregister"));
      }
      super.deregister(ssoId);
      manager.getMap(SSO_SESSION_ENTRIES).fastRemove(ssoId);
  }

  @Override
  protected boolean update(String ssoId, Principal principal, String authType, String username, String password) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.update"));
      }
      syncAndGetSsoEntry(ssoId);
      boolean updated = super.update(ssoId, principal, authType, username, password);
      if (updated) {
          manager.getMap(SSO_SESSION_ENTRIES).fastPut(ssoId, cache.get(ssoId));
      }
      return updated;
  }

  @Override
  protected void removeSession(String ssoId, Session session) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.removeSession", session, ssoId));
      }
      SingleSignOnEntry sso = syncAndGetSsoEntry(ssoId);
      super.removeSession(ssoId, session);
      if (sso != null && sso.findSessions().isEmpty()) {
        deregister(ssoId);
      }
  }

  /**
   * Lookup {@code SingleSignOnEntry} for the given SSO ID and make sure local cache has the same value.
   * That applies also to non existence.
   *
   * @param ssoSessionId SSO session id we are looking for
   * @return matching {@code SingleSignOnEntry} instance or null when not found
   */
  private SingleSignOnEntry syncAndGetSsoEntry(String ssoSessionId) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.getSsoEntry", ssoSessionId));
      }
      if (ssoSessionId == null) {
          return null;
      }
      SingleSignOnEntry entry = (SingleSignOnEntry) manager.getMap(SSO_SESSION_ENTRIES).get(ssoSessionId);
      if (entry == null) {
          this.cache.remove(ssoSessionId);
      } else {
          this.cache.put(ssoSessionId, entry);
      }
      return entry;
  }

  /**
   * Retrieve SSO session ID from provided cookies in the request.
   *
   * @param request The request that has been sent to the server.
   * @return SSO session ID provided with the request or null when none provided
   */
  private String getSsoSessionId(Request request) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.getSsoSessionId", request.getRequestURI()));
      }
      Cookie cookie = null;
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
          for (Cookie value : cookies) {
              if (getCookieName().equals(value.getName())) {
                  cookie = value;
                  break;
              }
          }
      }
      if (cookie != null) {
          return cookie.getValue();
      }
      return null;
  }

}
