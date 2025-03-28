/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2017  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 *
 */
package org.egov.infra.microservice.models;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable {

    private static final long serialVersionUID = -6099520777478122089L;

    private String id;

    @NotNull
    private String userName;

    private String name;

    private String type;

    private String mobile;

    private String email;
    
    private String uuid;
	
    private String password;

    private String idToken;
    
    @NotNull
    private List<Role> primaryrole;

    private List<TenantRole> additionalroles;	
    
    @NotNull
    private String tenantId;

    public UserInfo(final List<Role> primaryrole, final String id, final String userName, final String name, final String email,
            final String mobile, final String type, final String tenantId) {
        super();
        this.primaryrole = primaryrole;
        this.id = id;
        this.userName = userName;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.type = type;
        this.tenantId = tenantId;
    }

    public UserInfo() {
    }

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTenantId() {
        return tenantId;
    }

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public List<Role> getPrimaryrole() {
		return primaryrole;
	}

	public void setPrimaryrole(List<Role> primaryrole) {
		this.primaryrole = primaryrole;
	}

	public List<TenantRole> getAdditionalroles() {
		return additionalroles;
	}

	public void setAdditionalroles(List<TenantRole> additionalroles) {
		this.additionalroles = additionalroles;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    @Override
    public String toString() {
        return "UserInfo [id=" + id + ", userName=" + userName + ", name=" + name + ", type=" + type + ", mobile=" + mobile
                + ", email=" + email + ", uuid=" + uuid + ", password=" + password + ", idToken=" + idToken + ", primaryrole="
                + primaryrole + ", additionalroles=" + additionalroles + ", tenantId=" + tenantId + "]";
    }

}