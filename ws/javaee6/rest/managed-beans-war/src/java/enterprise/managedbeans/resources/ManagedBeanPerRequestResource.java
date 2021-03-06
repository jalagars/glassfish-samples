/*
 	Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
	
	This program and the accompanying materials are made available under the
	terms of the Eclipse Public License v. 2.0, which is available at
	http://www.eclipse.org/legal/epl-2.0.
	
	This Source Code may also be made available under the following Secondary
	Licenses when the conditions for such availability set forth in the
	Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
	version 2 with the GNU Classpath Exception, which is available at
	https://www.gnu.org/software/classpath/license.html.
	
	SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
*/

package enterprise.managedbeans.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/managedbean/per-request")
@ManagedBean
public class ManagedBeanPerRequestResource {

    @Resource(name="injectedResource") int injectedResource = 0;

    @Context UriInfo ui;
    
    @EJB StatelessSessionBean ssb;
    
    public ManagedBeanPerRequestResource() {
        Logger.getLogger(ManagedBeanPerRequestResource.class.getName()).log(Level.INFO, "In constructor " + this);
    }

    @PostConstruct
    public void postConstruct() {
        Logger.getLogger(ManagedBeanPerRequestResource.class.getName()).log(Level.INFO, "In post construct " + this + "; UriInfo: " + ui);
    }

    @PreDestroy
    public void preDestroy() {
        Logger.getLogger(ManagedBeanPerRequestResource.class.getName()).log(Level.INFO, "In pre destroy");
    }

    @GET 
    @Produces("text/plain")
    public String getMessage() {
        Logger.getLogger(ManagedBeanPerRequestResource.class.getName()).log(Level.INFO, "In getMessage " + this + "; UriInfo: " + ui);

        /*
         * The incrementation is here only for testing purposes - you shouldn't
         * be able to GET anything else than "1" from this resource.
         */

        return ssb.getMessage() + " " + Integer.toString(injectedResource++);
    }
}
