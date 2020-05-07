package com.example.ndvi.libaums.partition.fs;


import android.support.annotation.Nullable;

import com.example.ndvi.libaums.driver.BlockDeviceDriver;
import com.example.ndvi.libaums.driver.ByteBlockDevice;
import com.example.ndvi.libaums.fs.FileSystemFactory;
import com.example.ndvi.libaums.partition.PartitionTable;
import com.example.ndvi.libaums.partition.PartitionTableFactory;

import java.io.IOException;

/**
 * Created by magnusja on 30/07/17.
 */

public class FileSystemPartitionTableCreator implements PartitionTableFactory.PartitionTableCreator {
    @Nullable
    @Override
    public PartitionTable read(BlockDeviceDriver blockDevice) throws IOException {
        try {
            return new FileSystemPartitionTable(blockDevice,
                    FileSystemFactory.createFileSystem(null, new ByteBlockDevice(blockDevice)));
        } catch(FileSystemFactory.UnsupportedFileSystemException e) {
            return null;
        }
    }
}
