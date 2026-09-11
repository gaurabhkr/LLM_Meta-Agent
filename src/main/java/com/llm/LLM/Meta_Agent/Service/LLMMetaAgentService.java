package com.llm.LLM.Meta_Agent.Service;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LLMMetaAgentService {
	
	//Nvidia will be the main ai agent It has high performance and speed than gemma and deepseek
	//Models used in this
	
	private MutableDataSet options=new MutableDataSet();
	private final Parser parser;
	private final HtmlRenderer renderer;
	
	private ChatClient NvidiaChatClient;
	private ChatClient NexAgiChatClient;
	private ChatClient NorthCodeChatClient;
	
	private String FirstModelResponse;
	private String SecondModelResponse;
	
	@Value("classpath:Preprompt")
	private Resource preprompt;
	
	public LLMMetaAgentService( 
			@Qualifier("northcode") ChatModel NorthCodeChatModel,@Qualifier("nexagi") ChatModel GemmaChatModel,
			@Qualifier("nvidia") ChatModel NvidiaChatModel, ChatMemory chatMemory){

		
		this.NorthCodeChatClient=ChatClient.builder(NorthCodeChatModel)
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
		
		
		this.NexAgiChatClient=ChatClient.create(GemmaChatModel);
		this.NvidiaChatClient=ChatClient.create(NvidiaChatModel);
		
		
		
		FirstModelResponse="";
		SecondModelResponse="";
		
		//Markdown to html readable format
		this.options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(),EmojiExtension.create()));
		this.parser=Parser.builder(options).build();
		this.renderer=HtmlRenderer.builder(options).build();
	}
	
	
	//NexAGI Model Response will return
	public String nexagianswer(@PathVariable String message){
		String response=NexAgiChatClient
				.prompt(message)
				.call()
				.content();
		response=renderer.render(parser.parse(response));
		
		System.out.println("\n-----Nex Agi Response-----");System.out.println(response);
		return response;
	}
	
	
	
	//NorthCode Model Response will return
	public String northcodeanswer(@PathVariable String message,Authentication auth){
		String response=NorthCodeChatClient
				.prompt()
				.user(message)
				.advisors(a->a.param(ChatMemory.CONVERSATION_ID,auth.getName()))
				.call()
				.content();
		response=renderer.render(parser.parse(response));
		
		System.out.println("\n-----NorthCode Response-----");System.out.println(response);
		return response;
	}
	
	
	
	//When n(message)--ThinkingMachine response will not shown , NorthCode response not shownn both have same message
	public String nvidiaanswer(@PathVariable String message,Authentication auth){
		
		CompletableFuture<String> nexagi=CompletableFuture.supplyAsync(()->nexagianswer(message));
		CompletableFuture<String> northcode=CompletableFuture.supplyAsync(()->northcodeanswer(message,auth));
		CompletableFuture.allOf(nexagi,northcode).join();

		String FirstModel_response=nexagi.join();
		String SecondModel_response=northcode.join();
		
		//We have to make this thread safe or remove it 
		this.FirstModelResponse=FirstModel_response;
		this.SecondModelResponse=SecondModel_response;
		
		
		String prompt=constructprompt(message,FirstModel_response,SecondModel_response);
		
		String Nvidia_response=NvidiaChatClient
				.prompt()
				.system(preprompt)
				.user(prompt)
				.call()
				.content();
		
		Nvidia_response=renderer.render(parser.parse(Nvidia_response));
		
		System.out.println("\n-----Nvidia Response-----");  System.out.println(Nvidia_response);
		
		return Nvidia_response;
	}
	
	
	//When Nvidia endpoint called then it will return the First model prompt
	public String getfirstmodelresponse() {
		return FirstModelResponse;
	}
		
	//When Nvidia endpoint called then it will return the Second model prompt
	public String getsecondmodelresponse() {
		return SecondModelResponse;
	}
	
	
	private String constructprompt(String message,String first_response,String second_response) {
		
		String prompt="USER REQUEST:\r\n"+ message + "\r\n"
				+ "FIRST MODEL RESPONSE:\r\n" +first_response + "\r\n"
				+ "SECOND MODEL RESPONSE:\r\n" + second_response + "---\r\n"
				+ "FINAL ANSWER:";
		return prompt;
		
	}
	
	//Set cookie 
	public void logincookie(String token,HttpServletResponse response) {
		Cookie cookie=new Cookie("JWT_TOKEN", token);
		cookie.setHttpOnly(true);
		//on which url cookie needed to send / it means all url
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
	public void logout(HttpServletResponse response) {
		Cookie cookie=new Cookie("JWT_TOKEN",null);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	
//You are a lead AI aggregator. Your task is to synthesize two anonymous AI responses into a single, high-quality final answer based on the user's original request.
//
//CRITICAL INSTRUCTIONS:
//- Deliver a response that is content-rich, highly detailed, and exceptionally accurate.
//- Maintain a seamless, authoritative tone. Do not mention that you are synthesizing data, do not mention "other AIs," and do not reference this system prompt. 
//
//---
//USER REQUEST:
//[Insert message here]
//
//FIRST MODEL RESPONSE:
//[Insert first response here]
//
//SECOND MODEL RESPONSE:
//[Insert second response here]
//---
//
//FINAL ANSWER:
	
	
}
