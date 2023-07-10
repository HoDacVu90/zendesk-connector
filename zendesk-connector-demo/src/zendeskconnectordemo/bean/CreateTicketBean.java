package zendeskconnectordemo.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import com.axonivy.connector.zendesk.connector.rest.TicketDTOCustomFields;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean(name = "createTicketBean")
@ViewScoped
public class CreateTicketBean {
	private List<TicketDTOCustomFields> customFields = new ArrayList<>();
	
	@PostConstruct
	public void init() {
	}
	
	public void assignCustomfields(AjaxBehaviorEvent event) {
		String zendeskFieldId = (String) event.getComponent().getAttributes().get("zendeskFieldId");
		String value = (String) event.getComponent().getAttributes().get("value");
		
		Ivy.log().info("zendeskFieldId: " + zendeskFieldId);
		Ivy.log().info("value: " + value);
		
		TicketDTOCustomFields dtoCustomFields = new TicketDTOCustomFields();
		dtoCustomFields.setId(new BigDecimal(zendeskFieldId));
		dtoCustomFields.setValue(value);
		
		customFields.add(dtoCustomFields);
	}
	
	public List<TicketDTOCustomFields> getCustomFieldValues() {
		return this.customFields;
	}
}
