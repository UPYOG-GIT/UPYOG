package org.egov.persistence.contract;

import org.egov.domain.model.MailRequestType;
import org.egov.domain.model.MailSubjectType;

import lombok.*;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Email {

	private String emailTo;
	private MailRequestType emailRequestType;
	private MailSubjectType emailSubjectType;

}
