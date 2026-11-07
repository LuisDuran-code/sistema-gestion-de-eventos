package com.eventos.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;

@Service
public class ImagenService {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = {'W', 'E', 'B', 'P'};

    private final AlmacenImagenes almacen;

    public ImagenService(AlmacenImagenes almacen) {
        this.almacen = almacen;
    }

    public String subir(MultipartFile archivo) {
        byte[] contenido = leer(archivo);
        if (contenido.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige una imagen");
        }
        if (!esImagenAdmitida(contenido)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Solo se admiten imágenes JPG, PNG o WebP");
        }
        return almacen.subir(contenido);
    }

    private byte[] leer(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen");
        }
    }

    /** Mira los primeros bytes: el nombre y el tipo declarados los decide quien envía el archivo. */
    private boolean esImagenAdmitida(byte[] c) {
        return empiezaCon(c, JPEG, 0) || empiezaCon(c, PNG, 0) || (empiezaCon(c, RIFF, 0) && empiezaCon(c, WEBP, 8));
    }

    private boolean empiezaCon(byte[] contenido, byte[] firma, int desde) {
        int hasta = desde + firma.length;
        return contenido.length >= hasta && Arrays.equals(Arrays.copyOfRange(contenido, desde, hasta), firma);
    }
}
