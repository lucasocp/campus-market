package br.edu.campusmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do backend CampusMarket.
 *
 * <p>Esta classe inicia a aplicação. Regras de cadastro, anúncios e negociações
 * serão implementadas nos respectivos módulos, preservando a separação de
 * responsabilidades.</p>
 *
 * <p>A anotação habilita a configuração automática do Spring Boot e a busca
 * de componentes neste pacote e em seus subpacotes.</p>
 */
@SpringBootApplication
public class CampusMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMarketApplication.class, args);
    }
}
