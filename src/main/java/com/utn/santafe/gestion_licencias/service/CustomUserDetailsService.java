package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con DNI: " + dni));

        // Verificar que el usuario esté activo
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + dni);
        }

        return new User(
                usuario.getDni(),
                usuario.getPassword(),
                usuario.isActivo(),
                true,
                true,
                true,
                mapearRoles(usuario)
        );
    }

    private Collection<? extends GrantedAuthority> mapearRoles(Usuario usuario) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }
}
