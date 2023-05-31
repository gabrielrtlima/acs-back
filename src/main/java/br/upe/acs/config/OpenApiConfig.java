package br.upe.acs.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(contact = @Contact(name = "Amadeu Santos", email = "amadeu.santos@upe.br", url = "https://github.com/amadeusantos"), description = "Sistema de envio de AC's da UPE Campus Garanhuns", title = "AC's - UPE", version = "1.0", license = @License(name = "Licença da API", url = "#"), termsOfService = "Termos de serviço"), servers = {
		@Server(description = "Local ENV", url = "http://localhost:8080") })
public class OpenApiConfig {
}
