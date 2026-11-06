package br.edu.campusmarket.anuncios.domain;
/** Contrato para imagens já normalizadas; permite trocar disco por objeto remoto. */
public interface ImageStorage {
    String save(byte[] jpeg);
    byte[] load(String key);
}
