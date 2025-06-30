package com.lausn.onlyoffice.ui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/doc-editor")
public class OnlyOfficeEditorView extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private static final String ONLYOFFICE_SERVER_URL = "http://localhost:8080"; // OnlyOffice Document Server
	private static final String DOC_DOWNLOAD_URL = "https://static.onlyoffice.com/assets/docs/samples/demo.docx";
	private static final String SAVE_CALLBACK_URL = "http://localhost:8081/api/save/demo.docx";

	private final String secret;
	private Logger log = LoggerFactory.getLogger(OnlyOfficeEditorView.class.getName());

	
	public OnlyOfficeEditorView(@Value("${onlyoffice.secret.key}")  String secret) {
		this.secret=secret;
	}
	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		setSizeFull();
		// Container where OnlyOffice will render the editor
		Div editorDiv = new Div();
		editorDiv.setId("docEditorContainer");
		editorDiv.setWidthFull();
		editorDiv.setHeight("700px");

		add(editorDiv);

		String config;
		try {
			config = getEditorPage();
			log.info("Config:{}", config);
			Algorithm algorithm = Algorithm.HMAC256(secret);
			String token = JWT.create()
					.withPayload(config)
					.sign(algorithm);

			getElement().executeJs("""
					    const script = document.createElement('script');
					    script.src = $0 + '/web-apps/apps/api/documents/api.js';
					    console.log($1);
					    console.log($2);
					    script.onload = function () {
							 var config = %s;
							  config.token='%s';
							 console.log(config);
					        new DocsAPI.DocEditor('docEditorContainer', config);
					    };
					    document.head.appendChild(script);
					""".formatted(config, token), ONLYOFFICE_SERVER_URL, DOC_DOWNLOAD_URL, SAVE_CALLBACK_URL);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getEditorPage() throws Exception {

		String fileKey = UUID.randomUUID().toString(); // Should be unique per document version
		String fileName = "demo.docx";

		Map<String, Object> document = new HashMap<>();
		document.put("fileType", "docx");
		document.put("key", fileKey); // ✅ Required
		document.put("title", fileName);
		document.put("url", DOC_DOWNLOAD_URL);

		Map<String, Object> permissions = new HashMap<>();
		permissions.put("edit", true);
		permissions.put("download", true);
		permissions.put("print", true);
		permissions.put("save", true); // ✅ required to show Save button
		document.put("permissions", permissions);

		Map<String, Object> user = new HashMap<>();
		user.put("id", UUID.randomUUID().toString());
		user.put("name", "Joy Barua");

		Map<String, Object> editorConfig = new HashMap<>();
		editorConfig.put("mode", "edit");
		editorConfig.put("lang", "en");
		editorConfig.put("user", user);
		editorConfig.put("callbackUrl", SAVE_CALLBACK_URL);
		// Enable autosave and show Save button
		Map<String, Object> customization = new HashMap<>();
		customization.put("forcesave", true); // manually triggered save
		editorConfig.put("customization", customization);

		Map<String, Object> config = new HashMap<>();
		config.put("document", document);
		config.put("documentType", "word");
		config.put("editorConfig", editorConfig);
		// Build separate JS object string
		String jsonConfig = new ObjectMapper().writeValueAsString(config);
		return jsonConfig;
	}
}
