/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2010 The eXist Project
 *  http://exist-db.org
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.security.realm.activedirectory;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.config.annotation.*;
import org.exist.security.AuthenticationException;
import org.exist.security.Subject;
import org.exist.security.internal.AbstractAccount;
import org.exist.security.internal.SecurityManagerImpl;
import org.exist.security.internal.SubjectAccreditedImpl;
import org.exist.security.internal.AccountImpl;
import org.exist.security.realm.ldap.LDAPRealm;
import org.exist.security.realm.ldap.LdapContextFactory;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
@ConfigurationClass("ActiveDirectory")
public class ActiveDirectoryRealm extends LDAPRealm {

	private final static Logger LOG = Logger.getLogger(LDAPRealm.class);

	public ActiveDirectoryRealm(SecurityManagerImpl sm, Configuration config) {
		super(sm, config);
	}

	protected LdapContextFactory ensureContextFactory() {
		if (this.ldapContextFactory == null) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("No LdapContextFactory specified - creating a default instance.");
			}

			LdapContextFactory factory = new ContextFactory(configuration);

			this.ldapContextFactory = factory;
		}
		return this.ldapContextFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.security.Realm#getId()
	 */
	@Override
	public String getId() {
		String domain = ((ContextFactory) ensureContextFactory()).getDomain();
		return "ActiveDirectory@" + domain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.security.Realm#authenticate(java.lang.String,
	 * java.lang.Object)
	 */
	public Subject authenticate(String username, Object credentials) throws AuthenticationException {

//		String returnedAtts[] = { "sn", "givenName", "mail" };
//		String searchFilter = "(&(objectClass=user)(sAMAccountName=" + username + "))";
//
//		// Create the search controls
//		SearchControls searchCtls = new SearchControls();
//		searchCtls.setReturningAttributes(returnedAtts);
//
//		// Specify the search scope
//		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//
		LdapContext ctxGC = null;
//		boolean ldapUser = false;

		try {
			ctxGC = ensureContextFactory().getLdapContext(username, String.valueOf(credentials));

			// Search objects in GC using filters
//			NamingEnumeration<SearchResult> answer = ctxGC.search(((ContextFactory) ensureContextFactory()).getSearchBase(), searchFilter, searchCtls);
//
//			while (answer.hasMoreElements()) {
//				SearchResult sr = answer.next();
//				Attributes attrs = sr.getAttributes();
//				Map<String, Object> amap = null;
//				if (attrs != null) {
//					amap = new HashMap<String, Object>();
//					NamingEnumeration<? extends Attribute> ne = attrs.getAll();
//					while (ne.hasMore()) {
//						Attribute attr = ne.next();
//						amap.put(attr.getID(), attr.get());
//						ldapUser = true;
//					}
//					ne.close();
//				}
//			}
		} catch (NamingException e) {
			e.printStackTrace();
			throw new AuthenticationException(
					AuthenticationException.UNNOWN_EXCEPTION, 
					e.getMessage());
		}

		try {
			AbstractAccount account = (AbstractAccount) getAccount(username);
			if (account == null) {
				account = new AccountImpl(this, username);
				//TODO: addAccount(account);
			}

			return new SubjectAccreditedImpl(account, ctxGC);
		} catch (ConfigurationException e) {
			throw new AuthenticationException(
					AuthenticationException.UNNOWN_EXCEPTION,
					e.getMessage(), e);
		}
	}
}