package dev.askov.vipet.serialization;

public interface Importer<ImportedDataType> {

  ImportedDataType importData();
}
