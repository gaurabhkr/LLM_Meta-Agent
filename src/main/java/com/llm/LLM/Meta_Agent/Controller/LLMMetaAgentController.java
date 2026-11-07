package com.llm.LLM.Meta_Agent.Controller;

import org.springframework.ai.retry.NonTransientAiException; 
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.llm.LLM.Meta_Agent.Service.LLMMetaAgentJwtService;
import com.llm.LLM.Meta_Agent.Service.LLMMetaAgentService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api")
public class LLMMetaAgentController {

	@Autowired
	private LLMMetaAgentService llmMetaAgentService;
	private final AuthenticationManager authenticationManager;
	private final LLMMetaAgentJwtService jwtservice;
	
	
	public LLMMetaAgentController(AuthenticationManager authenicationManager,LLMMetaAgentJwtService jwtservice){
		this.authenticationManager=authenicationManager;
		this.jwtservice=jwtservice;
	}
	
	
	//Gemma Model Response will return
	@ResponseBody
	@GetMapping("/recursiveflint/{message}")
	public ResponseEntity<String> nexagianswer(@PathVariable String message){
		String response=llmMetaAgentService.recursiveflintanswer(message);
		return ResponseEntity.ok(response);
	}
	
	
	
	//Deepseek Model Response will return
	@ResponseBody
	@GetMapping("/northcode/{message}")
	public ResponseEntity<String> northcodeanswer(@PathVariable String message,Authentication auth){
		String response=llmMetaAgentService.northcodeanswer(message,auth);
		return ResponseEntity.ok(response);
	}
	
	
	
	//When n(message)--gemini response will not shown , deepseek response not shownn both have same message
	@ResponseBody
	@GetMapping("/nvidia/{message}")
	public ResponseEntity<String> nvidiaanswer(@PathVariable String message,Authentication auth){
		String Nvidia_response=llmMetaAgentService.nvidiaanswer(message,auth);
		return ResponseEntity.ok(Nvidia_response);
	}
	
	
	
	//When Nvidia endpoint called then it will return the First model prompt
	@ResponseBody
	@GetMapping("/nvidia/firstmodelresponse")
	public String getfirstmodelresponse() {
		return llmMetaAgentService.getfirstmodelresponse();
	}
	
	//When Nvidia endpoint called then it will return the Second model prompt
	@ResponseBody
	@GetMapping("/nvidia/secondmodelresponse")
	public String getsecondmodelresponse() {
		return llmMetaAgentService.getsecondmodelresponse();
	}
	
	
	//Used when api call to give token
	@PostMapping("/login")
	public String login(@RequestParam String username,@RequestParam String password,HttpServletResponse response) {
		
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
		String token=jwtservice.genrateToken(username);
		llmMetaAgentService.logincookie(token, response);
		
		return "redirect:/index.html";
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletResponse response) {
		llmMetaAgentService.logout(response);
		return "forward:/login.html";
	}
	
	//It will not use /api
	@GetMapping("/")
	public String hom() {
		return "forward:/index.html";
	}
	
	@ExceptionHandler
	public ResponseEntity<String> handleTransient(TransientAiException ex){
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Model Service is temporarily unavailable. Please try again.");
	}
	@ExceptionHandler
	public ResponseEntity<String> handleNonTransient(NonTransientAiException ex){
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body("Model request failed. Contact admin @gaurabhkr. ");
	}
}
