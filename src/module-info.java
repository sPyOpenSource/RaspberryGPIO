module com.llama4j {
    requires jdk.incubator.vector;
    requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires jdk.unsupported;
    requires PrimeFactors;
    requires opencv;
    requires com.fazecast.jSerialComm;
    requires myopenlab;
    requires snakeyaml;
    
    exports com.llama4j;
}