package com.llm.LLM.Meta_Agent.configuration;

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
		this.BASE_URL=con.getBaseUrl();System.out.println(BASE_URL+API_KEY);
	}
	
	@Bean
	@Qualifier("gemma")
	public OpenAiChatModel gemmamodel() {
		
		//Making option for custom model and openaiapi they will make an openaichatmodel which is load into chatclient
		var options=OpenAiChatOptions.builder().model("google/gemma-3-12b-it:free").build();
		var openaiapi=OpenAiApi.builder().apiKey(API_KEY).baseUrl(BASE_URL).build();
		
		return OpenAiChatModel
				.builder()
				.openAiApi(openaiapi)
				.defaultOptions(options)
				.build();

	}
	
	@Bean
	@Qualifier("deepseek")
	public OpenAiChatModel deepseekmodel() {
		
		var options=OpenAiChatOptions.builder().model("tngtech/deepseek-r1t2-chimera:free").build();
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
		
		var options=OpenAiChatOptions.builder().model("nvidia/nemotron-3-nano-30b-a3b:free").build();
		var openaiapi=OpenAiApi.builder().apiKey(API_KEY).baseUrl(BASE_URL).build();
		
		return OpenAiChatModel
				.builder()
				.openAiApi(openaiapi)
				.defaultOptions(options)
				.build();

	}
// Not using 
//	@Bean
//	@Qualifier("gpt")
//	private OpenAiChatModel gptmodel() {
//		
//		var options=OpenAiChatOptions.builder().model("").build();
//		var openaiapi=OpenAiApi.builder().apiKey(API_KEY).baseUrl(API_KEY).build();
//		
//		return OpenAiChatModel
//				.builder()
//				.openAiApi(openaiapi)
//				.defaultOptions(options)
//				.build();
//
//	}
}
