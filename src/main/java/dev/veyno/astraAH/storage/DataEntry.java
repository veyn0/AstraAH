package dev.veyno.astraAH.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface DataEntry {

    void write(DataOutputStream dos) throws IOException;

    void read(DataInputStream dis) throws IOException;

}
