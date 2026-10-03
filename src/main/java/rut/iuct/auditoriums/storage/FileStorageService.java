package rut.iuct.auditoriums.storage;

public interface FileStorageService {
    String newKey(String contentType);
    String canonicalKey(String key);
    void write(String key, byte[] content);
    byte[] read(String key);
    void delete(String key);
}
