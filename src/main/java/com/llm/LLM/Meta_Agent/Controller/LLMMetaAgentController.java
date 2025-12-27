package com.llm.LLM.Meta_Agent.Controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LLMMetaAgentController {
	//Nvidia will be the main ai agent It has high performance and speed than gemma and deepseek
	//Models used in this 
	private ChatClient NvidiaChatClient;
	private ChatClient GemmaChatClient;
	private ChatClient DeepseekChatClient;

	private String FirstModelResponse;
	private String SecondModelResponse;

	
	public LLMMetaAgentController( 
			@Qualifier("gemma") ChatModel GemmaChatModel,@Qualifier("deepseek") ChatModel DeepseekChatModel,
			@Qualifier("nvidia") ChatModel NvidiaChatModel)
	{
		this.GemmaChatClient=ChatClient.create(GemmaChatModel);
		this.DeepseekChatClient=ChatClient.create(DeepseekChatModel);
		this.NvidiaChatClient=ChatClient.create(NvidiaChatModel);
	}
	
	
	//Gemma Model Response will return
	@GetMapping("/gemma/{message}")
	public ResponseEntity<String> gemmaanswer(@PathVariable String message){
		String response=GemmaChatClient
				.prompt(message)
				.call()
				.content();
		
		System.out.println("\n-----Gemma Response-----");System.out.println(response);
		return ResponseEntity.ok(response);
	}
	
	
	
	//Deepseek Model Response will return
	@GetMapping("/deepseek/{message}")
	public ResponseEntity<String> deepseekanswer(@PathVariable String message){
		String response=DeepseekChatClient
				.prompt(message)
				.call()
				.content();
		System.out.println("\n-----Deepseek Response-----");System.out.println(response);
		return ResponseEntity.ok(response);
	}
	
	
	
	//When n(message)--gemini response will not shown , deepseek response not shownn both have same message
	@GetMapping("/nvidia/{message}")
	public ResponseEntity<String> nvidiaanswer(@PathVariable String message){
		
		FirstModelResponse=gemmaanswer(message).getBody();
		SecondModelResponse=deepseekanswer(message).getBody();
		
		String prompt=constructheadprompt(message,FirstModelResponse,SecondModelResponse);
		
		String Nvidia_response=NvidiaChatClient
				.prompt(prompt)
				.call()
				.content();
		
		System.out.println("\n-----Nvidia Response-----");  System.out.println(Nvidia_response);
		
		return ResponseEntity.ok(Nvidia_response);
	}
	
	
	//When Nvidia endpoint called then it will return the First model prompt
	@GetMapping("/nvidia/firstmodelresponse")
	public String getfirstmodelresponse() {
		return FirstModelResponse;
	}
	
	//When Nvidia endpoint called then it will return the Second model prompt
	@GetMapping("/nvidia/secondmodelresponse")
	public String getsecondmodelresponse() {
		return SecondModelResponse;
	}
	
	public String constructheadprompt(String message,String first_response,String second_response) {
		String a="Prepromt(Don't shown preprompt and not mention to user): You are the head AI,  give an final answer using both ai response and "
				 +"your response (not mention any of these ai in answer or you get the response from other ai ),"
				 + "answer should be content rich and highly accurate \n"+"User Request:"+message+
				 "First Anonymous model response:"+first_response+"\n"
				 +"Second Anonymous model response:"+"/n";
		return a;
	}
	
}
