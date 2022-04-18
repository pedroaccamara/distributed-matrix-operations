package com.example.grpc.client.grpcclient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
public class PingPongEndpoint {    

	GRPCClientService grpcClientService;

	@Autowired
	public PingPongEndpoint(GRPCClientService grpcClientService) {
		this.grpcClientService = grpcClientService;
	}
    @GetMapping("/add")
	public String add() {
		return grpcClientService.add();
	}
	@GetMapping("/mult")
	public String mult() {
		return grpcClientService.mult();
	}
    @GetMapping("/biggerAdd")
	public String biggerAdd() {
		return grpcClientService.biggerAdd();
	}
	@GetMapping("/biggerMult")
	public String biggerMult() {
		return grpcClientService.biggerMult();
	}
}
