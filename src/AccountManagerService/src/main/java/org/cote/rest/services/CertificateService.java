/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.rest.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.KeyStoreUtil;

@DeclareRoles({"admin","user"})
@Path("/certificate")
public class CertificateService {

	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(CertificateService.class);
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	/*
	 *         X509Certificate[] certChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        X509Certificate certificate = certChain[0];
	 */
	@RolesAllowed({"user"})
	@GET
	@Path("/public/{path:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}")
	@Produces({ "application/x-x509-user-cert" })
	public Response getOrganizationPublicCertificate(@PathParam("path") String path, @Context HttpServletRequest request){
		OrganizationType org = null;
		CredentialType cred = null;
		Certificate cert = null;
		logger.info("Request to find organization certificate from: " + path);
		AuditEnumType auditType = AuditEnumType.ORGANIZATION;
		if(path.startsWith("B64-")) path = BinaryUtil.fromBase64Str(path.substring(4,path.length())).replaceAll("%3D", "=");
		else if(path.startsWith("~") == false && path.startsWith(".") == false){
			path = "/" + path;
			/// Doubled up to allow for actual punctuation use
			/// Clearly this is a bandaid
			///	
			if(path.contains("..")) path = path.replaceAll("\\.\\.", "/");
			else path = path.replace('.', '/');
			logger.info("Alt path: " + path);
		}
		UserType user = ServiceUtil.getUserFromSession(request);
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "CertificateService", AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, auditType, path);
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(path);
	
			if(org != null){
				cred = CredentialService.getPrimaryCredential(org, CredentialEnumType.CERTIFICATE, true);
				if(cred != null){
					AuditService.permitResult(audit, "Global permit to find organization certificate");
					//logger.info("Pre Decode");
					cert = KeyStoreUtil.decodeCertificate(cred.getCredential());
					//logger.info("Post Decode");
				}
				else{
					AuditService.denyResult(audit, "Organization certificate not found for '" + path + "'");
				}
			}
			else{
				AuditService.denyResult(audit, "Organization not found from path '" + path + "'");
			}
		} catch (FactoryException | ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		byte[] out_bytes = (cred != null && cert != null ? cred.getCredential() : new byte[0]);

		//return new ByteArrayInputStream(new byte[0]);
		ResponseBuilder rb = Response.status((out_bytes.length > 0 ? 200 : 404));
		if(out_bytes.length > 0){
			InputStream is = new ByteArrayInputStream(out_bytes);
			rb.entity(is).header("Content-Disposition", "attachment; filename=\"" + org.getName() + ".cer\"");
		}
		return rb.build();
		/*
		return Response
				.status(200)
				.entity(is)
				.header("Content-Disposition", "attachment; filename=\"" + org.getName() + ".cer\"")
				.build()
		;
		*/
		//return Response.status(200).entity(cert).build();
	}

}
