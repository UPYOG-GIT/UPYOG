package org.egov.user.web.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.egov.user.domain.model.Role;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = { "code", "tenantId" })
public class RoleRequest {

	private String code;
	private String name;
	private String tenantId;

	public RoleRequest(Role domainRole) {
		this.code = domainRole.getCode();
		this.name = domainRole.getName();
		/*
		 * this.tenantId = !(domainRole.getCode().equals("CITIZEN") ||
		 * domainRole.getCode().equals("BPA_ARCHITECT") ||
		 * domainRole.getCode().equals("BPA_BUILDER") ||
		 * domainRole.getCode().equals("BPA_ENGINEER") ||
		 * domainRole.getCode().equals("BPA_STRUCTURALENGINEER") ||
		 * domainRole.getCode().equals("BPA_SUPERVISOR") ||
		 * domainRole.getCode().equals("BPA_TOWNPLANNER")) ? domainRole.getTenantId() :
		 * domainRole.getTenantId().split("\\.")[0];
		 */
		this.tenantId = domainRole.getTenantId();

	}

	public Role toDomain() {
		String processedTenantId;

		if (code.equals("CITIZEN") || code.equals("BPA_ARCHITECT") || code.equals("BPA_BUILDER")
				|| code.equals("BPA_ENGINEER") || code.equals("BPA_STRUCTURALENGINEER") || code.equals("BPA_SUPERVISOR")
				|| code.equals("BPA_TOWNPLANNER")) {
			processedTenantId = tenantId.split("\\.")[0];
		} else {
			processedTenantId = tenantId;
		}
//		return Role.builder().code(code).name(name).tenantId(tenantId).build();
		return Role.builder().code(code).name(name).tenantId(processedTenantId).build();
	}
}
