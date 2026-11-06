package br.edu.campusmarket.common;
import java.util.UUID;
/** Porta para divulgar mudanças públicas do catálogo, após commit. */
public interface CatalogEvents { void changed(UUID listingId); }
