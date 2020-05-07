package com.example.ndvi.libaums.fs.fat32;


import com.example.ndvi.libaums.driver.BlockDeviceDriver;
import com.example.ndvi.libaums.fs.FileSystem;
import com.example.ndvi.libaums.fs.FileSystemCreator;
import com.example.ndvi.libaums.partition.PartitionTableEntry;

import java.io.IOException;

/**
 * Created by magnusja on 28/02/17.
 */

public class Fat32FileSystemCreator implements FileSystemCreator {

    @Override
    public FileSystem read(PartitionTableEntry entry, BlockDeviceDriver blockDevice) throws IOException {
        return Fat32FileSystem.read(blockDevice);
    }
}
