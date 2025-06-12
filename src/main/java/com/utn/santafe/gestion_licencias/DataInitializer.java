package com.utn.santafe.gestion_licencias;

import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void run(String... args) throws Exception {
        // Crear superusuario por defecto si no existe
        if (!usuarioService.existeDni("12345678")) {
            Usuario superuser = new Usuario(
                    "Super",
                    "Usuario",
                    "12345678",
                    LocalDate.of(1980, 1, 1),
                    Sexo.MASCULINO,
                    "Calle Falsa 123, Santa Fe",
                    "superuser@sistema.com",
                    "3426789456",
                    "admin123",
                    Rol.SUPERUSER
            );

            usuarioService.guardarUsuario(superuser);
            System.out.println("Superusuario creado - DNI: 12345678, Password: admin123");
        }
    }
}
