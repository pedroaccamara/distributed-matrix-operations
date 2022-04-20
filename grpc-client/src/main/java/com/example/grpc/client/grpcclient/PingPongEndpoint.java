package com.example.grpc.client.grpcclient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.scheduling.annotation.EnableAsync;


@RestController
@EnableAsync
public class PingPongEndpoint {    

	GRPCClientService grpcClientService;

	@Autowired
	public PingPongEndpoint(GRPCClientService grpcClientService) {
		this.grpcClientService = grpcClientService;
	}
    @GetMapping("/add")
	public String add(RedirectAttributes redirectAttributes) {
		if (!TempStorage.getInitialised()) {
			redirectAttributes.addFlashAttribute("message", "Matrices have to be uploaded beforehand!");
			return "redirect:/";
		}
		if (TempStorage.getMatrix2().length > 2) return grpcClientService.biggerAdd();
		return grpcClientService.add();
	}
	@PostMapping("/mult")
	public String mult(@RequestParam("deadline") String deadline, RedirectAttributes redirectAttributes) {
		if (!TempStorage.getInitialised()) {
			redirectAttributes.addFlashAttribute("message", "Matrices have to be uploaded beforehand!");
			return "redirect:/";
		}
		if (TempStorage.getMatrix2().length > 2) return grpcClientService.biggerMult(deadline);
		return grpcClientService.mult();
	}
	@GetMapping("/asyncMult")
	public String asyncMult(RedirectAttributes redirectAttributes) throws InterruptedException {
		if (!TempStorage.getInitialised()) {
			redirectAttributes.addFlashAttribute("message", "Matrices have to be uploaded beforehand!");
			return "redirect:/";
		}
		if (TempStorage.getMatrix2().length > 2) return grpcClientService.biggerAsyncMult();
		return grpcClientService.mult();
	}
}
