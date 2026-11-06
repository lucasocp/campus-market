package br.edu.campusmarket.common;
import br.edu.campusmarket.usuarios.domain.AppUser;
/** Avisos privados não expõem dados no canal público do catálogo (ISP). */
public interface UserNotifier { void notify(AppUser recipient, String message); }
