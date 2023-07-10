package zendeskconnectordemo.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
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
	
	public List<TicketDTOCustomFields> integrateCustomFields() {
		List<TicketDTOCustomFields> fields = new ArrayList<>();
		TicketDTOCustomFields dtoCustomFields = new TicketDTOCustomFields();
//		String test = (String) FacesContext.getCurrentInstance().getViewRoot().findComponent("form:testne").getAttributes().get("value");
//		Ivy.log().info("test ne: " + test);
		
		String computerModelId = (String) FacesContext.getCurrentInstance().getViewRoot().findComponent("form:_" + Ivy.var().get("com.axonivy.connector.zendesk.custom.fields.computerModel")).getAttributes().get("zendeskFieldId");
		String computerModelValue = (String) FacesContext.getCurrentInstance().getViewRoot().findComponent("form:_" + Ivy.var().get("com.axonivy.connector.zendesk.custom.fields.computerModel")).getAttributes().get("value");
		dtoCustomFields.setId(new BigDecimal(computerModelId));
		dtoCustomFields.setValue(computerModelValue);
		fields.add(dtoCustomFields);
		
		String testId = (String) FacesContext.getCurrentInstance().getViewRoot().findComponent("form:_" + Ivy.var().get("com.axonivy.connector.zendesk.custom.fields.test")).getAttributes().get("zendeskFieldId");
		String testValue = (String) FacesContext.getCurrentInstance().getViewRoot().findComponent("form:_" + Ivy.var().get("com.axonivy.connector.zendesk.custom.fields.test")).getAttributes().get("value");
		dtoCustomFields = new TicketDTOCustomFields();
		dtoCustomFields.setId(new BigDecimal(testId));
		dtoCustomFields.setValue(testValue);
		fields.add(dtoCustomFields);
		
		return fields;
	}
}
