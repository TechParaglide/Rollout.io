package com.rollout.io.server.controlplaneservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ControlPlaneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlPlaneServiceApplication.class, args);
        System.out.println("Control Plane Service is running..");
    }

}
