package eon.trocha.dto;

import lombok.Data;

@Data
public class RegistroRequest {
    private String nombre;
    private String username;
    private String email;
    private String password;
}