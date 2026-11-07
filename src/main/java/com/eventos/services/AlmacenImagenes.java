package com.eventos.services;

public interface AlmacenImagenes {

    /** Guarda la imagen y devuelve su URL pública https. */
    String subir(byte[] contenido);
}
