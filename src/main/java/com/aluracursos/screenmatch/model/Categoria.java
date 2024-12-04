package com.aluracursos.screenmatch.model;

import java.text.Normalizer;

public enum Categoria {
    ACCION("Action", "Acción"),
    ROMANCE("Romance", "Romance"),
    COMEDIA("Comedy", "Comedia"),
    DRAMA("Drama", "Drama"),
    CRIMEN("Crime", "Crimen");

    private String categoriaOmdb;
    private String categoriaEspanol;

    Categoria(String categoriaOmdb, String categoriaEspanol) {
        this.categoriaOmdb = categoriaOmdb;
        this.categoriaEspanol = categoriaEspanol;
    }

    public static Categoria fromString(String text) {
        String textNormalizado = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        for (Categoria categoria : Categoria.values()) {
            String categoriaNormalizada = Normalizer.normalize(categoria.categoriaOmdb, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            if (categoriaNormalizada.equalsIgnoreCase(textNormalizado)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Ninguna categoría encontrada: " + text);
    }

    public static Categoria fromEspanol(String text) {
        String textNormalizado = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        for (Categoria categoria : Categoria.values()) {
            String categoriaNormalizada = Normalizer.normalize(categoria.categoriaEspanol, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            if (categoriaNormalizada.equalsIgnoreCase(textNormalizado)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Ninguna categoría encontrada: " + text);
    }

}
