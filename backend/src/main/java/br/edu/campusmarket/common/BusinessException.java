package br.edu.campusmarket.common;

/** Falha esperada de uma regra de negócio; não expõe detalhes internos. */
public class BusinessException extends RuntimeException {
    private final int status;

    public BusinessException(String message) { this(422, message); }
    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }
    public int status() { return status; }
    public static BusinessException notFound() {
        return new BusinessException(404, "Registro não encontrado.");
    }
    public static void require(boolean condition, String message) {
        if (!condition) throw new BusinessException(message);
    }
    public static void authorize(boolean condition) {
        if (!condition) throw new BusinessException(403, "Você não pode realizar esta operação.");
    }
}
