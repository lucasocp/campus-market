package br.edu.campusmarket.usuarios.domain;

/** O código comprova acesso ao convite associado ao R.A. na fonte consultada. */
public interface AcademicVerifier {
    void verify(String ra, String invitation);
}
