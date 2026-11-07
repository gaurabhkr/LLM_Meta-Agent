package com.llm.LLM.Meta_Agent.Configuration;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMMetaAgentConfiguration {

	private final String API_KEY;
	private final String BASE_URL;
	
	public LLMMetaAgentConfiguration(OpenAiConnectionProperties con) {
		this.API_KEY=con.getApiKey();
		this.BASE_URL=con.getBaseUrl();System.out.println(BASE_URL+"     "+API_KEY);
	}
	
	@Bean
	@Qualifier("recursiveflint")
	public OpenAiChatModel nexagimodel() {
		
		//Making option for custom model and openaiapi they will make an openaichatmodel which is load into chatclient
		var options=OpenAiChatOptions.builder().model("recursive/flint-v1.0:free").build();
		var openaiapi=OpenAiApi.builder().apiKey(API_KEY).baseUrl(BASE_URL).build();
		
		return OpenAiChatModel
				.builder()
				.openAiApi(openaiapi)
				.defaultOptions(options)
				.build();

	}
	
	@Bean
	@Qualifier("northcode")
	public OpenAiChatModel northcodemodel() {
		
		var options=OpenAiChatOptions.builder().model("cohere/north-mini-code:free").build();
		var openaiapi=OpenAiApi.builder().apiKey(API_KEY).baseUrl(BASE_URL).build();
		
		return OpenAiChatModel
				.builder()
				.openAiApi(openaiapi)
				.defaultOptions(options)
				.build();

	}
	
	@Bean
	@Qualifier("nvidia")
	public OpenAiChatModel nvidiamodel() {
		
		var options=OpenAiChatOptions.builder().model("nvidia/nemotron-3.5-super-vl:free").build();
		var openaiapi=OpenAiApi.builder().apiKey(API_KEY).baseUrl(BASE_URL).build();
		
		return OpenAiChatModel
				.builder()
				.openAiApi(openaiapi)
				.defaultOptions(options)
				.build();

	}
	
	@Bean
	public ChatMemory chatMemory() {
		return MessageWindowChatMemory
				.builder()
				.maxMessages(10)
				.build();
	}
}
